package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.config.BookingSchedulingProperties;
import com.project.BookCarOnline.booking.entity.Booking;
import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
import com.project.BookCarOnline.booking.repository.BookingRepository;
import com.project.BookCarOnline.finance.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledBookingDispatcher {

    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;
    private final RideDispatcherService rideDispatcherService;
    private final BookingSchedulingProperties properties;

    @Scheduled(
            cron = "${app.booking.scheduling.cron:0 * * * * *}",
            zone = "${app.booking.scheduling.zone:Asia/Ho_Chi_Minh}")
    public void dispatchDueBookings() {
        dispatchDueBookings(LocalDateTime.now(ZoneId.of(properties.getZone())));
    }

    void dispatchDueBookings(LocalDateTime now) {
        LocalDateTime cutoff = now.plus(properties.getDispatchBefore());
        List<Booking> dueBookings = bookingRepository.findDueScheduledBookings(
                BookingStatus.QUEUED,
                cutoff,
                PageRequest.of(0, properties.getBatchSize()));

        for (Booking candidate : dueBookings) {
            dispatchIfEligible(candidate, cutoff);
        }
    }

    private void dispatchIfEligible(Booking candidate, LocalDateTime cutoff) {
        try {
            if (!hasSuccessfulPayment(candidate)) {
                log.debug("[ScheduledBooking] Booking={} đang chờ thanh toán", candidate.getBookingId());
                return;
            }

            int claimed = bookingRepository.claimScheduledBooking(
                    candidate.getBookingId(),
                    BookingStatus.QUEUED,
                    BookingStatus.PENDING,
                    cutoff);
            if (claimed == 0) {
                return;
            }

            bookingRepository.findById(candidate.getBookingId()).ifPresentOrElse(
                    booking -> rideDispatcherService.dispatchNearbyDrivers(
                            booking,
                            booking.getPickupLat(),
                            booking.getPickupLng(),
                            Set.of()),
                    () -> log.warn("[ScheduledBooking] Không tìm thấy booking={} sau khi claim",
                            candidate.getBookingId()));
        } catch (RuntimeException exception) {
            log.error("[ScheduledBooking] Không thể dispatch booking={}", candidate.getBookingId(), exception);
        }
    }

    private boolean hasSuccessfulPayment(Booking booking) {
        return booking.getPaymentId() != null
                && Boolean.TRUE.equals(paymentService.get(booking.getPaymentId()).paid());
    }
}
