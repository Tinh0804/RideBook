package com.project.BookCarOnline.DTO.Redis;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FareQuote {
    String quoteId;
    String vehicleTypeId;
    Double distance;
    Double basePrice;
    Double surcharge;
    Double surgeMultiplier;
    Double totalPrice;
    Double discount;
    String promotionId;
    String pickupLocation;
    String dropoffLocation;
}
