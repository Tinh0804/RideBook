package com.project.BookCarOnline.promotion.mapper;

import com.project.BookCarOnline.promotion.dto.request.CreatePromotionRequest;
import com.project.BookCarOnline.promotion.dto.response.PromotionResponse;
import com.project.BookCarOnline.promotion.entity.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PromotionMapper {
    @Mapping(target = "promotionId", ignore = true)
    Promotion toPromotion(CreatePromotionRequest request);

    @Mapping(target = "usedCount", ignore = true)
    @Mapping(target = "savedCount", ignore = true)
    @Mapping(target = "isExpired", ignore = true)
    PromotionResponse toPromotionResponse(Promotion promotion);
}
