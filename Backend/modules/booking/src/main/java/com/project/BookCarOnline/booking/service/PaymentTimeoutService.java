package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.entity.Booking;
import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
import com.project.BookCarOnline.booking.repository.BookingRepository;
import com.project.BookCarOnline.finance.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTimeoutService {
    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;
    private final SimpMessagingTemplate messagingTemplate;

    public void schedulePaymentTimeout(String bookingId, long timeoutMillis) {
        CompletableFuture.runAsync(() -> {
            bookingRepository.findById(bookingId).ifPresent(booking -> {
                boolean paid = booking.getPaymentId() != null
                        && Boolean.TRUE.equals(paymentService.get(booking.getPaymentId()).paid());
                if (!paid && BookingStatus.PENDING.equals(booking.getBookingStatus())) {
                    booking.setBookingStatus(BookingStatus.CANCELLED);
                    bookingRepository.save(booking);
                    if (booking.getCustomerId() != null) {
                        messagingTemplate.convertAndSend(
                                "/topic/customer/" + booking.getCustomerId(),
                                "PAYMENT_TIMEOUT:" + bookingId);
                    }
                }
            });
        }, CompletableFuture.delayedExecutor(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS));
    }
}
