package com.project.BookCarOnline.Mapper;

import com.project.BookCarOnline.DTO.Request.CreatePromotionRequest;
import com.project.BookCarOnline.DTO.Response.PromotionResponse;
import com.project.BookCarOnline.Entity.Promotion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PromotionMapper {
    Promotion toPromotion(CreatePromotionRequest request);
    PromotionResponse toPromotionResponse(Promotion promotion);
}
