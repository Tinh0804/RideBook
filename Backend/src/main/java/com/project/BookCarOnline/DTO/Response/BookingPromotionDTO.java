package com.project.BookCarOnline.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingPromotionDTO {
    String promotionCode;
    String promotionName;
    Double discountAmount;
}
