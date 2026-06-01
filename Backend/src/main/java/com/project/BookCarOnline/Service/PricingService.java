package com.project.BookCarOnline.Service;


import com.project.BookCarOnline.Entity.CustomerPromotion;
import com.project.BookCarOnline.Entity.Enum.CustomerPromotionStatus;
import com.project.BookCarOnline.Entity.Enum.DiscountType;
import com.project.BookCarOnline.Entity.Promotion;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Repository.CustomerPromotionRepository;
import com.project.BookCarOnline.Repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    private final PromotionRepository           promotionRepository;
    private final CustomerPromotionRepository   customerPromotionRepository;

    public double calculateDiscount(Promotion promotion, double rawPrice) {
        if (promotion == null) return 0.0;
        if (!isPromotionApplicable(promotion, rawPrice)) return 0.0;

        return switch (promotion.getDiscountType()) {
            case FIXED_AMOUNT -> promotion.getDiscountValue() != null ? promotion.getDiscountValue() : 0.0;
            case PERCENTAGE   -> calculatePercentageDiscount(promotion, rawPrice);
            default -> {
                log.warn("[Pricing] DiscountType không hỗ trợ: {}", promotion.getDiscountType());
                yield 0.0;
            }
        };
    }

    private double calculatePercentageDiscount(Promotion promotion, double rawPrice) {
        double percent  = promotion.getDiscountValue() != null ? promotion.getDiscountValue() : 0.0;
        double discount = rawPrice * percent / 100.0;
        // Cap theo discountLimit nếu có
        if (promotion.getDiscountLimit() != null && discount > promotion.getDiscountLimit()) {
            discount = promotion.getDiscountLimit();
        }
        return discount;
    }

    private boolean isPromotionApplicable(Promotion promotion, double rawPrice) {
        if (!promotion.getIsActive()) return false;
        if (promotion.getQuantity() <= 0) return false;
        if (promotion.getEndTime().before(Timestamp.from(Instant.now()))) return false;
        if (promotion.getMinTripValue() != null && rawPrice < promotion.getMinTripValue()) return false;
        return true;
    }

    @Transactional
    public Promotion validateAndConsumePromotion(String promotionCode,
                                                 String customerId,
                                                 double tripPrice) {
        Promotion promotion = promotionRepository.findByPromotionCode(promotionCode)
                .orElse(null);

        if (promotion == null) return null; // Code không tồn tại → bỏ qua

        // Kiểm tra còn hiệu lực
        if (!promotion.getIsActive() || promotion.getQuantity() <= 0
                || promotion.getEndTime().before(Timestamp.from(Instant.now()))) {
            throw new AppException(ErrorCode.PROMOTION_NOT_ACTIVE);
        }

        // Kiểm tra trip value
        if (promotion.getMinTripValue() != null && tripPrice < promotion.getMinTripValue()) {
            throw new AppException(ErrorCode.PROMOTION_NOT_ACTIVE); // Hoặc TRIP_VALUE_TOO_LOW
        }

        // Kiểm tra usage limit per user
        if (promotion.getUsageLimitPerUser() != null && promotion.getUsageLimitPerUser() > 0) {
            int used = customerPromotionRepository
                    .countByCustomer_CustomerIdAndPromotion_PromotionIdAndStatus(
                            customerId, promotion.getPromotionId(), CustomerPromotionStatus.USED);
            if (used >= promotion.getUsageLimitPerUser()) {
                throw new RuntimeException("Bạn đã hết lượt sử dụng mã khuyến mãi này");
            }
        }
        
        promotion.setQuantity(promotion.getQuantity() - 1);
        promotionRepository.save(promotion);

        log.info("[Pricing] Áp dụng promotion {} cho customer {}", promotionCode, customerId);
        return promotion;
    }
}