package com.project.BookCarOnline.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledBookingQueuePromoter {

    private final ScheduledBookingQueue scheduledBookingQueue;

    @Scheduled(fixedDelayString = "${app.booking.scheduling.promote-interval:1s}")
    public void promoteDueBookings() {
        try {
            long promoted = scheduledBookingQueue.promoteDue(Instant.now());
            if (promoted > 0) {
                log.debug("[ScheduledBooking] Đã chuyển {} booking từ ZSET sang Stream", promoted);
            }
        } catch (RuntimeException exception) {
            log.error("[ScheduledBooking] Không thể chuyển booking đến hạn sang Stream", exception);
        }
    }
}
