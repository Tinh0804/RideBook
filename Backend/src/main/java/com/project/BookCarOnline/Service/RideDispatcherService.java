package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.Entity.Booking;
import com.project.BookCarOnline.Entity.BookingRejection;
import com.project.BookCarOnline.Entity.Driver;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import com.project.BookCarOnline.Entity.Enum.RejectionType;
import com.project.BookCarOnline.Entity.Enum.WaitResult;
import com.project.BookCarOnline.Repository.BookingRejectionRepository;
import com.project.BookCarOnline.Repository.DriverRepository;
import com.project.BookCarOnline.Repository.RideBookRepository;
import com.project.BookCarOnline.Utils.Constant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideDispatcherService {

    private final RideBookRepository          bookingRepository;
    private final DriverRepository            driverRepository;
    private final BookingRejectionRepository  rejectionRepository;
    private final SimpMessagingTemplate       messagingTemplate;
    private final Constant                    constant;
    private final DriverCacheService          driverCacheService;

    private final ConcurrentHashMap<String, CompletableFuture<WaitResult>> pendingDispatches = new ConcurrentHashMap<>();

    public void resolveDispatch(String bookingId, WaitResult result) {
        CompletableFuture<WaitResult> future = pendingDispatches.get(bookingId);
        if (future != null) {
            future.complete(result);
        }
    }


    @Async
    public void startDispatching(String bookingId, List<Driver> prioritizedList) {
        log.info("[Dispatch] Bắt đầu điều phối booking={} với {} tài xế ưu tiên",
                 bookingId, prioritizedList.size());

        // Lấy danh sách tài xế đã từ chối / bỏ qua booking này (tránh gửi lại)
        Set<String> blacklist = rejectionRepository.findDriverIdsByBookingId(bookingId);
        var patitionedDrivers = prioritizedList.stream()
                .collect(Collectors.partitioningBy(d->blacklist.contains(d.getDriverId())));

        List<Driver> ignoredDrivers = patitionedDrivers.get(true);
        List<Driver> freshDrivers   = patitionedDrivers.get(false);

        List<Driver> driversToDispatch = new ArrayList<>();
        driversToDispatch.addAll(freshDrivers);
        driversToDispatch.addAll(ignoredDrivers);

        dispatchToNextDriver(bookingId, driversToDispatch, 0);
    }


    @Transactional
    public void recordRejection(String bookingId, String driverId) {
        saveRejection(bookingId, driverId, RejectionType.REJECTED);
        log.info("[Dispatch] Tài xế {} từ chối booking {}", driverId, bookingId);
        resolveDispatch(bookingId, WaitResult.DRIVER_REJECTED);
    }

    private void dispatchToNextDriver(String bookingId, List<Driver> drivers, int index) {
        if (index >= drivers.size()) {
            log.info("[Dispatch] Không có tài xế nào nhận chuyến {}. Tiến hành hủy tự động.", bookingId);
            cancelBookingAutomatically(bookingId);
            return;
        }

        Driver driver = drivers.get(index);

        if (isBookingTakenOrCancelled(bookingId)) {
            log.info("[Dispatch] Booking {} không còn PENDING, dừng dispatch.", bookingId);
            return;
        }

        sendRideRequestToDriver(bookingId, driver.getDriverId());
        driverCacheService.holdDriver(driver.getDriverId(), bookingId);

        CompletableFuture<WaitResult> future = new CompletableFuture<>();
        pendingDispatches.put(bookingId, future);

        future.completeOnTimeout(WaitResult.TIMEOUT, constant.getDISPATCH_TIMEOUT_SECONDS(), TimeUnit.SECONDS)
              .thenAcceptAsync(result -> {
                  pendingDispatches.remove(bookingId);
                  driverCacheService.releaseDriver(driver.getDriverId());
                  switch (result) {
                      case ACCEPTED -> {
                          notifyCustomerDriverAssigned(bookingId);
                      }
                      case CUSTOMER_CANCELLED -> {
                          log.info("[Dispatch] Khách hủy booking {} trong lúc tìm tài xế.", bookingId);
                          messagingTemplate.convertAndSend("/topic/driver/" + driver.getDriverId(), "CUSTOMER_CANCELLED:" + bookingId);
                      }
                      case DRIVER_REJECTED -> {
                          log.info("[Dispatch] Tài xế {} từ chối. Chuyển sang tài xế tiếp theo.", driver.getDriverId());
                          dispatchToNextDriver(bookingId, drivers, index + 1);
                      }
                      case TIMEOUT -> {
                          saveRejection(bookingId, driver.getDriverId(), RejectionType.IGNORED);
                          log.info("[Dispatch] Tài xế {} hết thời gian phản hồi (IGNORED).", driver.getDriverId());
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
            if (booking.getCustomerNo() != null && booking.getDriverNo() != null) {
                Driver driver  = booking.getDriverNo();
                String payload = "DRIVER_ASSIGNED:" + bookingId
                        + ":" + driver.getDriverName()
                        + ":" + driver.getPhone()
                        + ":" + driver.getLicensePlate();
                messagingTemplate.convertAndSend(
                        "/topic/customer/" + booking.getCustomerNo().getCustomerId(), payload);

                log.info("[Dispatch] Đã thông báo khách hàng tài xế {} nhận booking {}",
                         driver.getDriverId(), bookingId);
            }
        });
    }

    private void saveRejection(String bookingId, String driverId, RejectionType type) {

        if (rejectionRepository.existsByBooking_BookingIdAndDriver_DriverId(bookingId, driverId)) {
            return;
        }
        bookingRepository.findById(bookingId).ifPresent(booking ->
            driverRepository.findById(driverId).ifPresent(driver -> {
                BookingRejection rejection = BookingRejection.builder()
                        .booking(booking)
                        .driver(driver)
                        .rejectionType(type)
                        .build();
                rejectionRepository.save(rejection);
                
                // === Xóa cache điểm ưu tiên của tài xế ===
                driverCacheService.evictDriverStats(driverId);
            })
        );
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
            if (booking.getCustomerNo() != null) {
                messagingTemplate.convertAndSend(
                        "/topic/customer/" + booking.getCustomerNo().getCustomerId(),
                        "NO_DRIVER_FOUND:" + bookingId);
            }
        });
    }



}