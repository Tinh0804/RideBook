package com.project.BookCarOnline.booking.dto.response;

import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
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
    Double pickupLat;
    Double pickupLng;
    Double dropoffLat;
    Double dropoffLng;
    Double distance;
    Double price;
    BookingStatus bookingStatus;
}
