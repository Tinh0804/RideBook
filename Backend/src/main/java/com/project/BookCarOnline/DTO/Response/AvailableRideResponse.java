package com.project.BookCarOnline.DTO.Response;

import com.project.BookCarOnline.Entity.Enum.BookingStatus;
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
