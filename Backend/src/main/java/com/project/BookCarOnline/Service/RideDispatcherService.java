package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.Entity.Booking;
import com.project.BookCarOnline.Entity.BookingRejection;
import com.project.BookCarOnline.Entity.Driver;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import com.project.BookCarOnline.Entity.Enum.RejectionType;
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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
  Luồng:
   1. Nhận danh sách tài xế đã được sắp xếp theo điểm ưu tiên.
   2. Lần lượt gửi cuốc đến từng tài xế:
      - Loại trừ tài xế đã từ chối / bỏ qua booking này trước đó.
      - Chờ DISPATCH_TIMEOUT_SECONDS giây.
          + Nếu tài xế nhận → broadcast cho khách, kết thúc.
          + Nếu timeout → ghi IGNORED, chuyển sang tài xế tiếp theo.
   3. Nếu tất cả tài xế trong danh sách ưu tiên ko nhân, thì gán lại lần nữa cho những tài xế đã từ chối/ignore trước đó (bỏ qua blacklist)
  4. Nếu vẫn không có ai → tự động hủy booking, thông báo khách.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RideDispatcherService {

    private final RideBookRepository          bookingRepository;
    private final DriverRepository            driverRepository;
    private final BookingRejectionRepository  rejectionRepository;
    private final SimpMessagingTemplate       messagingTemplate;
    private final Constant                    constant;


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

        if(!freshDrivers.isEmpty()){
            boolean accepted = dispatchToList(bookingId,freshDrivers,blacklist);
            if(accepted)
                return ;
        }
        if(!ignoredDrivers.isEmpty()){
             boolean accepted = dispatchToList(bookingId, ignoredDrivers, Collections.emptySet());
             if (accepted)
                 return;
        }


        log.info("[Dispatch] Không có tài xế nào nhận chuyến {}. Tiến hành hủy tự động.", bookingId);
        cancelBookingAutomatically(bookingId);
    }


    @Transactional
    public void recordRejection(String bookingId, String driverId) {
        saveRejection(bookingId, driverId, RejectionType.REJECTED);
        log.info("[Dispatch] Tài xế {} từ chối booking {}", driverId, bookingId);
    }

    private boolean dispatchToList(String bookingId, List<Driver> drivers, Set<String> blacklist) {
        for (Driver driver : drivers) {
            // 1. Bỏ qua nếu tài xế trong blacklist
            if (!blacklist.isEmpty() && blacklist.contains(driver.getDriverId())) {
                log.info("[Dispatch] Bỏ qua tài xế {} (đã từ chối/ignore trước đó)", driver.getDriverId());
                continue;
            }

            // 2. Kiểm tra booking còn PENDING không
            if (isBookingTakenOrCancelled(bookingId)) {
                log.info("[Dispatch] Booking {} không còn PENDING, dừng dispatch.", bookingId);
                return true; // Đã được xử lý (nhận hoặc khách hủy)
            }

            // 3. Gửi cuốc xe đến tài xế qua WebSocket
            sendRideRequestToDriver(bookingId, driver.getDriverId());

            // 4. Chờ phản hồi
            WaitResult result = waitForResponse(bookingId, driver.getDriverId(),
                                                constant.getDISPATCH_TIMEOUT_SECONDS());
            switch (result) {
                case ACCEPTED -> {
                    notifyCustomerDriverAssigned(bookingId);
                    return true;
                }
                case CUSTOMER_CANCELLED -> {
                    log.info("[Dispatch] Khách hủy booking {} trong lúc tìm tài xế.", bookingId);
                    messagingTemplate.convertAndSend("/topic/driver/" + driver.getDriverId(), "CUSTOMER_CANCELLED:" + bookingId);
                    return true;
                }
                case DRIVER_REJECTED -> {
                    // Đã được ghi bởi recordRejection(), cập nhật blacklist local
                    blacklist.add(driver.getDriverId());
                    log.info("[Dispatch] Tài xế {} từ chối. Chuyển sang tài xế tiếp theo.", driver.getDriverId());
                }
                case TIMEOUT -> {
                    // Tài xế bỏ qua (không phản hồi) → ghi IGNORED
                    saveRejection(bookingId, driver.getDriverId(), RejectionType.IGNORED);
                    blacklist.add(driver.getDriverId());
                    log.info("[Dispatch] Tài xế {} hết thời gian phản hồi (IGNORED).", driver.getDriverId());
                }
            }
        }
        return false;
    }

    /**
     * Polling trạng thái booking cho đến khi hết timeout.
     * Trả về kết quả tương ứng để dispatcher quyết định hành động tiếp theo.
     */
    private WaitResult waitForResponse(String bookingId, String driverId, int timeoutSeconds) {
        for (int i = 0; i < timeoutSeconds; i++) {
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return WaitResult.TIMEOUT;
            }

            BookingStatus status = bookingRepository.findBookingStatusByBookingId(bookingId);

            if (BookingStatus.ACCEPTED.equals(status)) {
                return WaitResult.ACCEPTED;
            }

            if (BookingStatus.CANCELLED.equals(status)) {
                return WaitResult.CUSTOMER_CANCELLED;
            }

            // Tài xế chủ động từ chối sẽ gọi recordRejection() → ghi vào DB
            // Ta kiểm tra xem rejection đã được ghi chưa
            if (rejectionRepository.existsByBooking_BookingIdAndDriver_DriverId(bookingId, driverId)) {
                return WaitResult.DRIVER_REJECTED;
            }
        }

        return WaitResult.TIMEOUT;
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


    private enum WaitResult {
        ACCEPTED,
        DRIVER_REJECTED,
        CUSTOMER_CANCELLED,
        TIMEOUT
    }
}