package com.project.BookCarOnline.DTO.Redis;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

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
    Double originalPrice;
    Double totalPrice;
    Double discount;
    List<String> promotionIds;  // Hỗ trợ nhiều mã giảm giá
    String pickupLocation;
    String dropoffLocation;
}
