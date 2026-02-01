package com.project.BookCarOnline.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AvailableRideResponse {

    String bookingId;
    String customerId;
    String pickupLocation;
    String dropoffLocation;
    Double distance;
    Double price;
    String bookingStatus;
}
