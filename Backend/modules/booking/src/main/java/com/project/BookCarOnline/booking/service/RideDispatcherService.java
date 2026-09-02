package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.entity.Booking;
import com.project.BookCarOnline.booking.entity.BookingRejection;
import com.project.BookCarOnline.booking.dto.redis.DriverGeoResult;
import com.project.BookCarOnline.booking.dto.redis.DriverStats;
import com.project.BookCarOnline.identity.dto.summary.DriverSummary;
import com.project.BookCarOnline.identity.service.IdentityQueryService;
import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
import com.project.BookCarOnline.booking.entity.enums.RejectionType;
import com.project.BookCarOnline.booking.entity.enums.WaitResult;
import com.project.BookCarOnline.booking.repository.BookingRejectionRepository;
import com.project.BookCarOnline.booking.repository.BookingRepository;
import com.project.BookCarOnline.booking.config.DispatchPolicy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideDispatcherService {

    private final BookingRepository bookingRepository;
    private final IdentityQueryService        identityQueryService;
    private final BookingRejectionRepository  rejectionRepository;
    private final SimpMessagingTemplate       messagingTemplate;
    private final DispatchPolicy dispatchPolicy;
    private final DriverCacheService          driverCacheService;

    private final ConcurrentHashMap<String, CompletableFuture<WaitResult>> pendingDispatches = new ConcurrentHashMap<>();

    public void resolveDispatch(String bookingId, WaitResult result) {
        CompletableFuture<WaitResult> future = pendingDispatches.get(bookingId);
        if (future != null) {
            future.complete(result);
        }
    }


    @Async
    public void dispatchNearbyDrivers(Booking booking, double latitude, double longitude, Set<String> blacklist) {
        dispatchNearbyDriversNow(booking, latitude, longitude, blacklist);
    }

    public void dispatchScheduledBooking(Booking booking) {
        dispatchNearbyDriversNow(booking, booking.getPickupLat(), booking.getPickupLng(), Set.of());
    }

    private void dispatchNearbyDriversNow(
            Booking booking,
            double latitude,
            double longitude,
            Set<String> blacklist) {
        String vehicleTypeId = booking.getVehicleTypeId();
        List<DriverGeoResult> nearbyDrivers = driverCacheService.findNearbyDrivers(
                vehicleTypeId, latitude, longitude, dispatchPolicy.getSEARCH_RADIUS_KM());
        if (nearbyDrivers.isEmpty()) {
            log.info("[Dispatch] Không tìm thấy tài xế quanh booking={}", booking.getBookingId());
            startDispatching(booking.getBookingId(), List.of());
            return;
        }

        List<String> driverIds = nearbyDrivers.stream()
                .map(DriverGeoResult::getDriverId)
                .filter(driverId -> !blacklist.contains(driverId))
                .toList();
        if (driverIds.isEmpty()) {
            log.info("[Dispatch] Tất cả tài xế quanh booking={} đều bị loại", booking.getBookingId());
            startDispatching(booking.getBookingId(), List.of());
            return;
        }

        Map<String, Double> distances = nearbyDrivers.stream().collect(Collectors.toMap(
                DriverGeoResult::getDriverId,
                DriverGeoResult::getDistanceKm,
                (first, ignored) -> first));
        List<DriverSummary> candidates = new ArrayList<>(identityQueryService.getDrivers(driverIds).stream()
                .filter(driver -> Boolean.TRUE.equals(driver.activityStatus()))
                .filter(driver -> vehicleTypeId.equals(driver.vehicleTypeId()))
                .filter(driver -> !bookingRepository.existsByDriverIdAndBookingStatusIn(
                        driver.driverId(),
                        List.of(BookingStatus.ACCEPTED, BookingStatus.IN_PROGRESS, BookingStatus.ARRIVED)))
                .toList());
        candidates.sort(Comparator.comparingDouble((DriverSummary driver) -> calculateScore(
                driver,
                distances.getOrDefault(driver.driverId(), dispatchPolicy.getSEARCH_RADIUS_KM()),
                driverCacheService.getDriverStats(driver.driverId()))).reversed());

        log.info("[Dispatch] Booking={} có {} tài xế ưu tiên", booking.getBookingId(), candidates.size());
        startDispatching(booking.getBookingId(), candidates);
    }

    private void startDispatching(String bookingId, List<DriverSummary> prioritizedList) {
        log.info("[Dispatch] Bắt đầu điều phối booking={} với {} tài xế ưu tiên",
                 bookingId, prioritizedList.size());

        // Lấy danh sách tài xế đã từ chối / bỏ qua booking này (tránh gửi lại)
        Set<String> blacklist = rejectionRepository.findDriverIdsByBookingId(bookingId);
        var patitionedDrivers = prioritizedList.stream()
                .collect(Collectors.partitioningBy(d->blacklist.contains(d.driverId())));

        List<DriverSummary> ignoredDrivers = patitionedDrivers.get(true);
        List<DriverSummary> freshDrivers   = patitionedDrivers.get(false);

        List<DriverSummary> driversToDispatch = new ArrayList<>();
        driversToDispatch.addAll(freshDrivers);
        driversToDispatch.addAll(ignoredDrivers);

        dispatchToNextDriver(bookingId, driversToDispatch, 0);
    }

    private double calculateScore(DriverSummary driver, double distanceKm, DriverStats stats) {
        double distanceScore = Math.max(0, 1 - distanceKm / dispatchPolicy.getMAX_DISTANCE_KM());
        double ratingScore = stats.getAvgRating() / 5;
        double idleScore = Math.min(getIdleMinutes(driver) / dispatchPolicy.getMAX_IDLE_MIN(), 1);
        double rejectPenalty = Math.min(stats.getRejectCount() * 0.5, 1);
        double ignorePenalty = Math.min(stats.getIgnoreCount() * 0.2, 1);
        return dispatchPolicy.getW_DISTANCE() * distanceScore
                + dispatchPolicy.getW_RATING() * ratingScore
                + dispatchPolicy.getW_IDLE() * idleScore
                - dispatchPolicy.getW_REJECT() * rejectPenalty
                - dispatchPolicy.getW_IGNORE() * ignorePenalty;
    }

    private long getIdleMinutes(DriverSummary driver) {
        if (driver.lastTripTime() == null) {
            return 60;
        }
        return Duration.between(Instant.ofEpochMilli(driver.lastTripTime().getTime()), Instant.now()).toMinutes();
    }


    @Transactional
    public void recordRejection(String bookingId, String driverId) {
        saveRejection(bookingId, driverId, RejectionType.REJECTED);
        log.info("[Dispatch] Tài xế {} từ chối booking {}", driverId, bookingId);
        resolveDispatch(bookingId, WaitResult.DRIVER_REJECTED);
    }

    private void dispatchToNextDriver(String bookingId, List<DriverSummary> drivers, int index) {
        if (index >= drivers.size()) {
            log.info("[Dispatch] Không có tài xế nào nhận chuyến {}. Tiến hành hủy tự động.", bookingId);
            cancelBookingAutomatically(bookingId);
            return;
        }

        DriverSummary driver = drivers.get(index);

        if (isBookingTakenOrCancelled(bookingId)) {
            log.info("[Dispatch] Booking {} không còn PENDING, dừng dispatch.", bookingId);
            return;
        }

        sendRideRequestToDriver(bookingId, driver.driverId());
        driverCacheService.holdDriver(driver.driverId(), bookingId);

        CompletableFuture<WaitResult> future = new CompletableFuture<>();
        pendingDispatches.put(bookingId, future);

        future.completeOnTimeout(
                WaitResult.TIMEOUT, dispatchPolicy.getDISPATCH_TIMEOUT_SECONDS(), TimeUnit.SECONDS)
              .thenAcceptAsync(result -> {
                  pendingDispatches.remove(bookingId);
                  driverCacheService.releaseDriver(driver.driverId());
                  switch (result) {
                      case ACCEPTED -> {
                          notifyCustomerDriverAssigned(bookingId);
                      }
                      case CUSTOMER_CANCELLED -> {
                          log.info("[Dispatch] Khách hủy booking {} trong lúc tìm tài xế.", bookingId);
                          messagingTemplate.convertAndSend("/topic/driver/" + driver.driverId(), "CUSTOMER_CANCELLED:" + bookingId);
                      }
                      case DRIVER_REJECTED -> {
                          log.info("[Dispatch] Tài xế {} từ chối. Chuyển sang tài xế tiếp theo.", driver.driverId());
                          dispatchToNextDriver(bookingId, drivers, index + 1);
                      }
                      case TIMEOUT -> {
                          saveRejection(bookingId, driver.driverId(), RejectionType.IGNORED);
                          log.info("[Dispatch] Tài xế {} hết thời gian phản hồi (IGNORED).", driver.driverId());
                          dispatchToNextDriver(bookingId, drivers, index + 1);
                      }
                  }
              });
    }

    private void sendRideRequestToDriver(String bookingId, String driverId) {
        String destination = "/topic/driver/" + driverId;
        String payload     = "NEW_RIDE:" + bookingId;
        messagingTemplate.convertAndSend(destination, payload);
        log.info("[Dispatch] Đã gửi cuốc xe {} → tài xế {}", bookingId, driverId);
    }

    private void notifyCustomerDriverAssigned(String bookingId) {
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            if (booking.getCustomerId() != null && booking.getDriverId() != null) {
                DriverSummary driver = identityQueryService.getDriver(booking.getDriverId());
                String payload = "DRIVER_ASSIGNED:" + bookingId
                        + ":" + driver.driverName()
                        + ":" + driver.phone()
                        + ":" + driver.licensePlate();
                messagingTemplate.convertAndSend(
                        "/topic/customer/" + booking.getCustomerId(), payload);

                log.info("[Dispatch] Đã thông báo khách hàng tài xế {} nhận booking {}",
                         driver.driverId(), bookingId);
            }
        });
    }

    private void saveRejection(String bookingId, String driverId, RejectionType type) {

        if (rejectionRepository.existsByBooking_BookingIdAndDriverId(bookingId, driverId)) {
            return;
        }
        identityQueryService.getDriver(driverId);
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            BookingRejection rejection = BookingRejection.builder()
                    .booking(booking)
                    .driverId(driverId)
                    .rejectionType(type)
                    .build();
            rejectionRepository.save(rejection);
            driverCacheService.evictDriverStats(driverId);
        });
    }

    private boolean isBookingTakenOrCancelled(String bookingId) {
        BookingStatus status = bookingRepository.findBookingStatusByBookingId(bookingId);
        return status != null && status != BookingStatus.PENDING;
    }

    @Transactional
    protected void cancelBookingAutomatically(String bookingId) {
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            if (!BookingStatus.PENDING.equals(booking.getBookingStatus()))
                return;
            booking.setBookingStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            log.warn("[Dispatch] Không có tài xế nhận booking {}. Đã tự động hủy.", bookingId);
            if (booking.getCustomerId() != null) {
                messagingTemplate.convertAndSend(
                        "/topic/customer/" + booking.getCustomerId(),
                        "NO_DRIVER_FOUND:" + bookingId);
            }
        });
    }



}
