package com.project.BookCarOnline.booking.mapper;

import com.project.BookCarOnline.booking.dto.response.RatingResponse;
import com.project.BookCarOnline.booking.entity.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {

    public RatingResponse toRatingResponse(Rating rating) {
        if (rating == null) {
            return null;
        }
        return RatingResponse.builder()
                .ratingId(rating.getRatingId())
                .bookingId(rating.getBookingNo() != null ? rating.getBookingNo().getBookingId() : null)
                .score(rating.getScore())
                .review(rating.getReview())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}
