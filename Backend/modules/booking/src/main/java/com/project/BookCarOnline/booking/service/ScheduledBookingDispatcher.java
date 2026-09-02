package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.config.BookingSchedulingProperties;
import com.project.BookCarOnline.booking.entity.Booking;
import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
import com.project.BookCarOnline.booking.repository.BookingRepository;
import com.project.BookCarOnline.finance.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledBookingDispatcher {

    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;
    private final RideDispatcherService rideDispatcherService;
    private final ScheduledBookingQueue scheduledBookingQueue;
    private final BookingSchedulingProperties properties;

    public void onMessage(MapRecord<String, String, String> message) {
        handle(message, LocalDateTime.now(ZoneId.of(properties.getZone())));
    }

    void handle(MapRecord<String, String, String> message, LocalDateTime now) {
        String bookingId = message.getValue().get(ScheduledBookingQueue.BOOKING_ID_FIELD);
        if (bookingId == null || bookingId.isBlank()) {
            log.warn("[ScheduledBooking] Bỏ qua stream message={} thiếu bookingId", message.getId());
            scheduledBookingQueue.acknowledge(message);
            return;
        }

        try {
            Optional<Booking> existing = bookingRepository.findById(bookingId);
            if (existing.isEmpty()) {
                if (message.getId().getTimestamp() != null
                        && message.getId().getTimestamp() + properties.getPendingMinIdle().toMillis()
                        <= now.atZone(ZoneId.of(properties.getZone())).toInstant().toEpochMilli()) {
                    log.warn("[ScheduledBooking] Xóa stale message={} cho booking={} không tồn tại",
                            message.getId(),
                            bookingId);
                    scheduledBookingQueue.acknowledge(message);
                } else {
                    log.warn("[ScheduledBooking] Booking={} chưa khả dụng; giữ message để retry", bookingId);
                }
                return;
            }

            Booking booking = existing.get();
            if (isRetryableClaimedBooking(booking)) {
                dispatch(booking);
                scheduledBookingQueue.acknowledge(message);
                return;
            }
            if (!BookingStatus.QUEUED.equals(booking.getBookingStatus())) {
                scheduledBookingQueue.acknowledge(message);
                return;
            }
            if (!hasSuccessfulPayment(booking)) {
                log.debug("[ScheduledBooking] Booking={} đang chờ thanh toán", bookingId);
                return;
            }

            int claimed = bookingRepository.claimScheduledBooking(
                    bookingId,
                    BookingStatus.QUEUED,
                    BookingStatus.PENDING,
                    now.plus(properties.getDispatchBefore()));
            if (claimed == 0) {
                return;
            }

            Booking claimedBooking = bookingRepository.findById(bookingId).orElseThrow();
            dispatch(claimedBooking);
            scheduledBookingQueue.acknowledge(message);
        } catch (RuntimeException exception) {
            log.error("[ScheduledBooking] Dispatch thất bại cho booking={}; giữ message để retry",
                    bookingId,
                    exception);
        }
    }

    private boolean isRetryableClaimedBooking(Booking booking) {
        return BookingStatus.PENDING.equals(booking.getBookingStatus())
                && booking.getScheduledAt() != null
                && booking.getDriverId() == null;
    }

    private void dispatch(Booking booking) {
        rideDispatcherService.dispatchScheduledBooking(booking);
    }

    private boolean hasSuccessfulPayment(Booking booking) {
        return booking.getPaymentId() != null
                && Boolean.TRUE.equals(paymentService.get(booking.getPaymentId()).paid());
    }
}
