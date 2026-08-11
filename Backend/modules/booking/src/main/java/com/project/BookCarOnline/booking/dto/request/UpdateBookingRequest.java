package com.project.BookCarOnline.booking.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateBookingRequest {

    String bookingId;
    String driverId;
    String bookingStatus;
    String note;
}
