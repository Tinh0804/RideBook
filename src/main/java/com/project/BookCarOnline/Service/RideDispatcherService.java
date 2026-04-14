package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.Entity.Driver;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Repository.DriverRepository;
import com.project.BookCarOnline.Repository.RideBookRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideDispatcherService {

    private final RideBookRepository bookingRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final DriverRepository driverRepository;

    @Async
    public void startDispatching(String bookingId, List<Driver> candidateDrivers) {
        if (candidateDrivers == null || candidateDrivers.isEmpty()) {
            log.warn("Không tìm thấy tài xế gần khách, fallback gửi tất cả tài xế đang hoạt động.");
            candidateDrivers = driverRepository.findByActivityStatusTrue();
        }

        log.info("Bắt đầu điều phối chuyến xe {} cho {} tài xế", bookingId, candidateDrivers.size());

        for (Driver driver : candidateDrivers) {
            // Kiểm tra xem chuyến xe còn ở trạng thái PENDING không trước khi gửi cho tài xế tiếp theo
            if (isBookingTakenOrCancelled(bookingId)) {
                return;
            }

            // 1. Gửi thông báo WebSocket
            String destination = "/topic/driver/" + driver.getDriverId();
            String payload = "NEW_RIDE:" + bookingId;
            messagingTemplate.convertAndSend(destination, payload);
            log.info("Đã gửi yêu cầu tới tài xế: {} - {}", driver.getDriverId(), destination);

            // 2. Đợi phản hồi (Dùng polling tối ưu)
            if (waitForAcceptance(bookingId, 20)) {
                log.info("Chuyến xe {} đã được tiếp nhận thành công.", bookingId);
                return;
            }
        }

        cancelBookingAutomatically(bookingId);
    }

    private boolean isBookingTakenOrCancelled(String bookingId) {
        // Tối ưu: Chỉ lấy trường Status thay vì toàn bộ Entity
        BookingStatus currentStatus = bookingRepository.findBookingStatusByBookingId(bookingId);
        if (currentStatus == null) {
            return false;
        }
        return currentStatus != BookingStatus.PENDING;
    }

    private boolean waitForAcceptance(String bookingId, int seconds) {
        for (int i = 0; i < seconds; i++) {
            try {
                // Polling interval: 1s
                Thread.sleep(1000);

                // Lấy status gọn nhẹ nhất có thể
                BookingStatus status = bookingRepository.findBookingStatusByBookingId(bookingId);

                if (BookingStatus.ACCEPTED.equals(status)) {
                    bookingRepository.findById(bookingId).ifPresent(booking -> {
                        if (booking.getCustomerNo() != null && booking.getDriverNo() != null) {
                            String payload = "DRIVER_ASSIGNED:" + bookingId + ":" +
                                    booking.getDriverNo().getDriverName() + ":" +
                                    booking.getDriverNo().getPhone();
                            messagingTemplate.convertAndSend(
                                    "/topic/customer/" + booking.getCustomerNo().getCustomerId(),
                                    payload);
                        }
                    });
                    return true;
                }

                // Nếu khách hàng chủ động hủy chuyến trong lúc đang tìm tài xế
                if (BookingStatus.CANCELLED.equals(status)) {
                    bookingRepository.deleteById(bookingId);
                    return false;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    @Transactional
    protected void cancelBookingAutomatically(String bookingId) {
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            if (BookingStatus.PENDING.equals(booking.getBookingStatus())) {
                log.warn("Không có tài xế nhận chuyến {}. Hệ thống tự động hủy.", bookingId);
                bookingRepository.deleteById(bookingId);
                // Thông báo cho khách hàng qua WebSocket
                messagingTemplate.convertAndSend("/topic/customer/" + booking.getCustomerNo().getCustomerId(), "NO_DRIVER_FOUND");
            }
        });
    }
}