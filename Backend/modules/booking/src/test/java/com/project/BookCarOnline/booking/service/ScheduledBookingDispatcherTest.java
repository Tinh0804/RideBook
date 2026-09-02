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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
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

    @Mock
    ScheduledBookingQueue scheduledBookingQueue;

    BookingSchedulingProperties properties;
    ScheduledBookingDispatcher scheduledBookingDispatcher;

    @BeforeEach
    void setUp() {
        properties = new BookingSchedulingProperties();
        properties.setDispatchBefore(Duration.ofMinutes(15));
        scheduledBookingDispatcher = new ScheduledBookingDispatcher(
                bookingRepository,
                paymentService,
                rideDispatcherService,
                scheduledBookingQueue,
                properties);
    }

    @Test
    void dispatchesAndAcknowledgesPaidBookingFromStream() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 1, 8, 0);
        Booking queued = queuedBooking(now.plusMinutes(10));
        Booking claimed = queuedBooking(now.plusMinutes(10));
        claimed.setBookingStatus(BookingStatus.PENDING);
        MapRecord<String, String, String> message = message("booking-1");

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(queued), Optional.of(claimed));
        when(paymentService.get("payment-1"))
                .thenReturn(new PaymentSummary("payment-1", PaymentMethod.ONLINE, 100_000D, true));
        when(bookingRepository.claimScheduledBooking(
                "booking-1", BookingStatus.QUEUED, BookingStatus.PENDING, now.plusMinutes(15)))
                .thenReturn(1);

        scheduledBookingDispatcher.handle(message, now);

        verify(rideDispatcherService).dispatchScheduledBooking(claimed);
        verify(scheduledBookingQueue).acknowledge(message);
    }

    @Test
    void leavesMessagePendingUntilOnlinePaymentSucceeds() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 1, 8, 0);
        Booking queued = queuedBooking(now.plusMinutes(5));
        MapRecord<String, String, String> message = message("booking-1");
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(queued));
        when(paymentService.get("payment-1"))
                .thenReturn(new PaymentSummary("payment-1", PaymentMethod.ONLINE, 100_000D, false));

        scheduledBookingDispatcher.handle(message, now);

        verify(bookingRepository, never()).claimScheduledBooking(any(), any(), any(), any());
        verify(rideDispatcherService, never()).dispatchScheduledBooking(any());
        verify(scheduledBookingQueue, never()).acknowledge(any());
    }

    @Test
    void retriesDispatchForClaimedScheduledBookingAfterConsumerCrash() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 1, 8, 0);
        Booking claimed = queuedBooking(now.plusMinutes(5));
        claimed.setBookingStatus(BookingStatus.PENDING);
        MapRecord<String, String, String> message = message("booking-1");
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(claimed));

        scheduledBookingDispatcher.handle(message, now);

        verify(rideDispatcherService).dispatchScheduledBooking(claimed);
        verify(scheduledBookingQueue).acknowledge(message);
        verify(bookingRepository, never()).claimScheduledBooking(any(), any(), any(), any());
    }

    @Test
    void acknowledgesStaleMessageForCancelledBooking() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 1, 8, 0);
        Booking cancelled = queuedBooking(now.plusMinutes(5));
        cancelled.setBookingStatus(BookingStatus.CANCELLED);
        MapRecord<String, String, String> message = message("booking-1");
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(cancelled));

        scheduledBookingDispatcher.handle(message, now);

        verify(scheduledBookingQueue).acknowledge(message);
        verify(rideDispatcherService, never()).dispatchScheduledBooking(any());
    }

    @Test
    void leavesMessagePendingWhenDispatcherFailsBeforeHandoffCompletes() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 1, 8, 0);
        Booking claimed = queuedBooking(now.plusMinutes(5));
        claimed.setBookingStatus(BookingStatus.PENDING);
        MapRecord<String, String, String> message = message("booking-1");
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(claimed));
        doThrow(new IllegalStateException("dispatch unavailable"))
                .when(rideDispatcherService).dispatchScheduledBooking(claimed);

        scheduledBookingDispatcher.handle(message, now);

        verify(scheduledBookingQueue, never()).acknowledge(message);
    }

    @Test
    void acknowledgesMessageForMissingBookingAfterCommitGracePeriod() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 1, 8, 0);
        MapRecord<String, String, String> message = StreamRecords.newRecord()
                .ofMap(Map.of(ScheduledBookingQueue.BOOKING_ID_FIELD, "missing-booking"))
                .withStreamKey(properties.getStreamKey())
                .withId(RecordId.of(now.minusMinutes(1)
                        .atZone(java.time.ZoneId.of(properties.getZone()))
                        .toInstant()
                        .toEpochMilli(), 0));
        when(bookingRepository.findById("missing-booking")).thenReturn(Optional.empty());

        scheduledBookingDispatcher.handle(message, now);

        verify(scheduledBookingQueue).acknowledge(message);
    }

    private MapRecord<String, String, String> message(String bookingId) {
        return StreamRecords.newRecord()
                .ofMap(Map.of(ScheduledBookingQueue.BOOKING_ID_FIELD, bookingId))
                .withStreamKey(properties.getStreamKey())
                .withId(RecordId.of("1725152400000-0"));
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
