package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.config.BookingSchedulingProperties;
import com.project.BookCarOnline.booking.entity.Booking;
import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
import com.project.BookCarOnline.booking.repository.BookingRepository;
import com.project.BookCarOnline.finance.dto.PaymentSummary;
import com.project.BookCarOnline.finance.entity.enums.PaymentMethod;
import com.project.BookCarOnline.finance.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledBookingDispatcherTest {

    @Mock
    BookingRepository bookingRepository;

    @Mock
    PaymentService paymentService;

    @Mock
    RideDispatcherService rideDispatcherService;

    BookingSchedulingProperties properties;
    ScheduledBookingDispatcher scheduledBookingDispatcher;

    @BeforeEach
    void setUp() {
        properties = new BookingSchedulingProperties();
        properties.setDispatchBefore(Duration.ofMinutes(15));
        properties.setBatchSize(25);
        scheduledBookingDispatcher = new ScheduledBookingDispatcher(
                bookingRepository, paymentService, rideDispatcherService, properties);
    }

    @Test
    void dispatchesPaidBookingWhenItEntersDispatchWindow() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 1, 8, 0);
        LocalDateTime scheduledAt = now.plusMinutes(10);
        Booking queued = queuedBooking(scheduledAt);
        Booking claimed = queuedBooking(scheduledAt);
        claimed.setBookingStatus(BookingStatus.PENDING);

        when(bookingRepository.findDueScheduledBookings(
                eq(BookingStatus.QUEUED), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(queued));
        when(paymentService.get("payment-1"))
                .thenReturn(new PaymentSummary("payment-1", PaymentMethod.ONLINE, 100_000D, true));
        when(bookingRepository.claimScheduledBooking(
                "booking-1", BookingStatus.QUEUED, BookingStatus.PENDING, now.plusMinutes(15)))
                .thenReturn(1);
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(claimed));

        scheduledBookingDispatcher.dispatchDueBookings(now);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(bookingRepository).findDueScheduledBookings(
                eq(BookingStatus.QUEUED), eq(now.plusMinutes(15)), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(25);
        verify(rideDispatcherService).dispatchNearbyDrivers(claimed, 10.75D, 106.67D, Set.of());
    }

    @Test
    void skipsQueuedOnlineBookingUntilPaymentSucceeds() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 1, 8, 0);
        Booking queued = queuedBooking(now.plusMinutes(5));
        when(bookingRepository.findDueScheduledBookings(
                eq(BookingStatus.QUEUED), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(queued));
        when(paymentService.get("payment-1"))
                .thenReturn(new PaymentSummary("payment-1", PaymentMethod.ONLINE, 100_000D, false));

        scheduledBookingDispatcher.dispatchDueBookings(now);

        verify(bookingRepository, never()).claimScheduledBooking(any(), any(), any(), any());
        verify(rideDispatcherService, never()).dispatchNearbyDrivers(any(), any(Double.class), any(Double.class), any());
    }

    @Test
    void doesNotDispatchWhenAnotherSchedulerAlreadyClaimedBooking() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 1, 8, 0);
        Booking queued = queuedBooking(now.plusMinutes(5));
        when(bookingRepository.findDueScheduledBookings(
                eq(BookingStatus.QUEUED), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(queued));
        when(paymentService.get("payment-1"))
                .thenReturn(new PaymentSummary("payment-1", PaymentMethod.CASH, 100_000D, true));
        when(bookingRepository.claimScheduledBooking(
                eq("booking-1"), eq(BookingStatus.QUEUED), eq(BookingStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(0);

        scheduledBookingDispatcher.dispatchDueBookings(now);

        verify(rideDispatcherService, never()).dispatchNearbyDrivers(any(), any(Double.class), any(Double.class), any());
    }

    private Booking queuedBooking(LocalDateTime scheduledAt) {
        return Booking.builder()
                .bookingId("booking-1")
                .paymentId("payment-1")
                .bookingStatus(BookingStatus.QUEUED)
                .scheduledAt(scheduledAt)
                .pickupLat(10.75D)
                .pickupLng(106.67D)
                .build();
    }
}
