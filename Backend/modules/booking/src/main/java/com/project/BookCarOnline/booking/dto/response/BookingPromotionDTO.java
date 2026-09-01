package com.project.BookCarOnline.booking.dto.response;

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
