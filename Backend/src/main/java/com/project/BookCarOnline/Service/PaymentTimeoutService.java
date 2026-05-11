package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.Entity.Booking;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import com.project.BookCarOnline.Repository.RideBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTimeoutService {
    private final RideBookRepository bookingRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    public void schedulePaymentTimeout(String bookingId, long timeoutMillis) {
        try {
            Thread.sleep(timeoutMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            boolean paid = booking.getPaymentNo() != null && Boolean.TRUE.equals(booking.getPaymentNo().getPaymentStatus());
            if (!paid && BookingStatus.PENDING.equals(booking.getBookingStatus())) {
                booking.setBookingStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                if (booking.getCustomerNo() != null) {
                    messagingTemplate.convertAndSend(
                            "/topic/customer/" + booking.getCustomerNo().getCustomerId(),
                            "PAYMENT_TIMEOUT:" + bookingId);
                }
            }
        });
    }
}
