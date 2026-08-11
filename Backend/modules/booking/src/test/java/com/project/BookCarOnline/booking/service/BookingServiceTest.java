package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.entity.Booking;
import com.project.BookCarOnline.booking.repository.BookingRepository;
import com.project.BookCarOnline.finance.dto.PaymentSummary;
import com.project.BookCarOnline.finance.entity.enums.PaymentMethod;
import com.project.BookCarOnline.finance.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    BookingRepository bookingRepository;

    @Mock
    PaymentService paymentService;

    @InjectMocks
    BookingService bookingService;

    @Test
    void duplicateSuccessfulCallbackDoesNotDispatchAgain() {
        Booking booking = Booking.builder()
                .bookingId("booking-1")
                .paymentId("payment-1")
                .build();
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(paymentService.get("payment-1"))
                .thenReturn(new PaymentSummary("payment-1", PaymentMethod.ONLINE, 100_000D, true));

        bookingService.confirmOnlinePayment("booking-1");

        verify(paymentService, never()).markPaid("payment-1");
        verify(bookingRepository, never()).save(booking);
    }
}
