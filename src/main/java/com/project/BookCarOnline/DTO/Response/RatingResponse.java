package com.project.BookCarOnline.DTO.Response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingResponse {
    String ratingId;
    String bookingId;
    String customerId;
    String driverId;
    double score;
    String review;
    Date createdAt;
}
