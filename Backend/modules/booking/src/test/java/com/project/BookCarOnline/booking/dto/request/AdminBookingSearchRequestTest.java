package com.project.BookCarOnline.booking.dto.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class AdminBookingSearchRequestTest {

    @Test
    void createsStableMultiFieldPageable() {
        AdminBookingSearchRequest request = new AdminBookingSearchRequest();
        request.setPage(2);
        request.setSize(25);
        request.setSort("totalPrice:desc,bookingTime:asc");

        var pageable = request.toPageable();

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(25);
        assertThat(pageable.getSort().stream().map(order -> order.getProperty()))
                .containsExactly("totalPrice", "bookingTime", "bookingId");
    }

    @Test
    void rejectsInvertedRanges() {
        AdminBookingSearchRequest request = new AdminBookingSearchRequest();
        request.setBookingFrom(LocalDateTime.parse("2026-09-02T10:00:00"));
        request.setBookingTo(LocalDateTime.parse("2026-09-01T10:00:00"));

        assertThatThrownBy(request::validateRanges)
                .isInstanceOf(IllegalArgumentException.class);

        request.setBookingFrom(null);
        request.setBookingTo(null);
        request.setMinPrice(200_000D);
        request.setMaxPrice(100_000D);
        assertThatThrownBy(request::validateRanges)
                .isInstanceOf(IllegalArgumentException.class);
    }
}
