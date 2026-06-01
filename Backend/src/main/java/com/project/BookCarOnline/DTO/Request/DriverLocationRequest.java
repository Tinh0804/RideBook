package com.project.BookCarOnline.DTO.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DriverLocationRequest {
    String bookingId;
    Double lat;
    Double lng;
}
