package com.project.BookCarOnline.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduledBookingQueuePromoterTest {

    @Mock
    ScheduledBookingQueue scheduledBookingQueue;

    @Test
    void scheduledTickPromotesDueRedisMembers() {
        ScheduledBookingQueuePromoter promoter = new ScheduledBookingQueuePromoter(scheduledBookingQueue);

        promoter.promoteDueBookings();

        verify(scheduledBookingQueue).promoteDue(any(Instant.class));
    }
}
