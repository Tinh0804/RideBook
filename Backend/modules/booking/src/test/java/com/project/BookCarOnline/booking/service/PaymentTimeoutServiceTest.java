package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.entity.Booking;
import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
import com.project.BookCarOnline.booking.repository.BookingRepository;
import com.project.BookCarOnline.finance.dto.PaymentSummary;
import com.project.BookCarOnline.finance.entity.enums.PaymentMethod;
import com.project.BookCarOnline.finance.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentTimeoutServiceTest {

    @Mock
    BookingRepository bookingRepository;

    @Mock
    PaymentService paymentService;

    @Mock
    SimpMessagingTemplate messagingTemplate;

    @Mock
    ScheduledBookingQueue scheduledBookingQueue;

    @InjectMocks
    PaymentTimeoutService paymentTimeoutService;

    @Test
    void cancelsUnpaidQueuedBookingAfterTimeout() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .customerId("customer-1")
                .paymentId("payment-1")
                .bookingStatus(BookingStatus.QUEUED)
                .scheduledAt(LocalDateTime.of(2026, 9, 2, 8, 30))
                .build();
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(paymentService.get("payment-1"))
                .thenReturn(new PaymentSummary("payment-1", PaymentMethod.ONLINE, 100_000D, false));

        paymentTimeoutService.expireIfUnpaid("booking-1");

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(booking);
        verify(scheduledBookingQueue).remove("booking-1");
        verify(messagingTemplate).convertAndSend(
                "/topic/customer/customer-1", "PAYMENT_TIMEOUT:booking-1");
    }
}
