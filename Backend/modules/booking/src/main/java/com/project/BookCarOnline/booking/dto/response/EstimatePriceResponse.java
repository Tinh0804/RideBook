package com.project.BookCarOnline.booking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EstimatePriceResponse {
    String vehicleTypeId;
    Double distance;
    Double basePrice;
    Double surcharge;
    Double surgeMultiplier;
    Double originalPrice;
    Double totalPrice;
    Double discount;
    String quoteId;
    Long expiryTime;
}
