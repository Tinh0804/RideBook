package com.project.BookCarOnline.promotion.mapper;

import com.project.BookCarOnline.promotion.dto.request.CreatePromotionRequest;
import com.project.BookCarOnline.promotion.dto.response.PromotionResponse;
import com.project.BookCarOnline.promotion.entity.Promotion;
import com.project.BookCarOnline.promotion.entity.enums.DiscountType;
import org.springframework.stereotype.Component;

@Component
public class PromotionMapper {

    /** Maps the create contract while leaving the generated identifier to persistence. */
    public Promotion toPromotion(CreatePromotionRequest request) {
        if (request == null) {
            return null;
        }
        return Promotion.builder()
                .promotionCode(request.getPromotionCode())
                .promotionName(request.getPromotionName())
                .discountLimit(request.getDiscountLimit())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .applicationCondition(request.getApplicationCondition())
                .quantity(request.getQuantity())
                .isActive(request.getIsActive())
                .isPublic(request.getIsPublic())
                .discountType(request.getDiscountType() == null
                        ? null
                        : DiscountType.valueOf(request.getDiscountType()))
                .discountValue(request.getDiscountValue())
                .minTripValue(request.getMinTripValue())
                .usageLimitPerUser(request.getUsageLimitPerUser())
                .promotionImage(request.getPromotionImage())
                .build();
    }

    /** Maps promotion state; usage counters are populated by the application query. */
    public PromotionResponse toPromotionResponse(Promotion promotion) {
        if (promotion == null) {
            return null;
        }
        return PromotionResponse.builder()
                .promotionId(promotion.getPromotionId())
                .promotionCode(promotion.getPromotionCode())
                .promotionName(promotion.getPromotionName())
                .discountLimit(promotion.getDiscountLimit())
                .startTime(promotion.getStartTime())
                .endTime(promotion.getEndTime())
                .applicationCondition(promotion.getApplicationCondition())
                .quantity(promotion.getQuantity())
                .isActive(promotion.getIsActive())
                .isPublic(promotion.getIsPublic())
                .promotionImage(promotion.getPromotionImage())
                .discountType(promotion.getDiscountType() == null
                        ? null
                        : promotion.getDiscountType().name())
                .discountValue(promotion.getDiscountValue())
                .minTripValue(promotion.getMinTripValue())
                .usageLimitPerUser(promotion.getUsageLimitPerUser())
                .build();
    }
}
