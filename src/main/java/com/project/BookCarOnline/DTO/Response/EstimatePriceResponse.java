package com.project.BookCarOnline.DTO.Response;

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
    Double totalPrice;
    Double discount;
}
