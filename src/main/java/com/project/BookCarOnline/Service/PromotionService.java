package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.DTO.Request.CreatePromotionRequest;
import com.project.BookCarOnline.DTO.Response.PromotionResponse;
import com.project.BookCarOnline.Entity.Promotion;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Mapper.PromotionMapper;
import com.project.BookCarOnline.Repository.PromotionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionService {
    PromotionRepository promotionRepository;
    PromotionMapper promotionMapper;

    public PromotionResponse createPromotion(CreatePromotionRequest request) {
        if (promotionRepository.findByPromotionCode(request.getPromotionCode()).isPresent()) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // Code existed
        }
        Promotion promotion = promotionMapper.toPromotion(request);
        return promotionMapper.toPromotionResponse(promotionRepository.save(promotion));
    }

    public List<PromotionResponse> getActivePromotions() {
        List<Promotion> promotions = promotionRepository.findActivePromotions(Timestamp.from(Instant.now()));
        return promotions.stream().map(promotionMapper::toPromotionResponse).collect(Collectors.toList());
    }

    public PromotionResponse getPromotionByCode(String code) {
        Promotion promotion = promotionRepository.findByPromotionCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION)); // Not found
        
        if (!promotion.getIsActive() || promotion.getQuantity() <= 0 || 
            promotion.getEndTime().before(Timestamp.from(Instant.now()))) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // Invalid/expired
        }
        
        return promotionMapper.toPromotionResponse(promotion);
    }
}
