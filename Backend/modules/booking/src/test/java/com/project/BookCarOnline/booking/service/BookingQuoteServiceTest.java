package com.project.BookCarOnline.booking.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingQuoteServiceTest {

    @Test
    void calculatesHaversineDistance() {
        assertEquals(0, BookingQuoteService.calculateDistanceKm(21.0278, 105.8342, 21.0278, 105.8342));
        assertEquals(1138, BookingQuoteService.calculateDistanceKm(21.0278, 105.8342, 10.8231, 106.6297), 10);
    }
}
