package com.project.BookCarOnline.DTO.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionResponse {
    String promotionId;
    String promotionCode;
    String promotionName;
    Double discountLimit;
    Timestamp startTime;
    Timestamp endTime;
    String applicationCondition;
    Integer quantity;
    Boolean isActive;
    String promotionImage;

    // Discount info
    String discountType;
    Double discountValue;
    Double minTripValue;
    Integer usageLimitPerUser;

    // Computed stats (admin only)
    Integer usedCount;
    Integer savedCount;
    Boolean isExpired;
}
