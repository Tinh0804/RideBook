package com.project.BookCarOnline.promotion.dto;

import com.project.BookCarOnline.promotion.entity.enums.DiscountType;

import java.sql.Timestamp;

public record PromotionQuote(
        String promotionId,
        String promotionCode,
        String promotionName,
        Double discountLimit,
        Timestamp endTime,
        Integer quantity,
        Boolean active,
        DiscountType discountType,
        Double discountValue,
        Double minTripValue) {
}
