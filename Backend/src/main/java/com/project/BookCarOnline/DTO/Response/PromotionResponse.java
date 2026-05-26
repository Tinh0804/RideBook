package com.project.BookCarOnline.DTO.Response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Data
@Builder
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
    
    // New fields
    String discountType;
    Double discountValue;
    Double minTripValue;
    Integer usageLimitPerUser;
}
