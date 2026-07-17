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

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    private final PromotionRepository           promotionRepository;
    private final CustomerPromotionRepository   customerPromotionRepository;
    private final RedisTemplate<String, Object> redisTemplate;

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

    public double calculateTotalDiscount(List<Promotion> promotions, double rawPrice) {
        if (promotions == null || promotions.isEmpty()) return 0.0;
        double remaining = rawPrice;
        double total = 0.0;
        for (Promotion p : promotions) {
            if (!isPromotionApplicable(p, remaining)) continue;
            double d = calculateDiscount(p, remaining);
            total += d;
            remaining = Math.max(0.0, remaining - d); // giảm giá lần lượt trên giá còn lại
        }
        return total;
    }

    /** Lấy danh sách Promotion hợp lệ từ các mã */
    public List<Promotion> resolvePromotions(List<String> promotionCodes) {
        if (promotionCodes == null || promotionCodes.isEmpty()) return List.of();
        List<Promotion> result = new ArrayList<>();
        for (String code : promotionCodes) {
            if (code == null || code.isBlank()) continue;
            
            String cacheKey = "promotion:" + code;
            Promotion promo = (Promotion) redisTemplate.opsForValue().get(cacheKey);
            if (promo == null) {
                promo = promotionRepository.findByPromotionCode(code).orElse(null);
                if (promo != null) {
                    redisTemplate.opsForValue().set(cacheKey, promo, 1, TimeUnit.HOURS);
                }
            }
            if (promo != null) {
                result.add(promo);
            }
        }
        return result;
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
        if (!Boolean.TRUE.equals(promotion.getIsActive())) return false;
        if (promotion.getQuantity() == null || promotion.getQuantity() <= 0) return false;
        
        Timestamp now = Timestamp.from(Instant.now());
        if (promotion.getEndTime() != null && promotion.getEndTime().before(now)) return false;
        
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
            return null;
        }

        // Kiểm tra trip value
        if (promotion.getMinTripValue() != null && tripPrice < promotion.getMinTripValue()) {
            return null; 
        }

        // Kiểm tra số lượng còn lại trong ví (nếu có lưu)
        CustomerPromotion cp = customerPromotionRepository
                .findByCustomer_CustomerIdAndPromotion_PromotionId(customerId, promotion.getPromotionId())
                .orElse(null);
        if (cp != null && cp.getQuantity() != null && cp.getQuantity() <= 0) {
            return null;
        }
        
        promotion.setQuantity(promotion.getQuantity() - 1);
        promotionRepository.save(promotion);
        redisTemplate.delete("promotion:" + promotionCode);

        log.info("[Pricing] Áp dụng promotion {} cho customer {}", promotionCode, customerId);
        return promotion;
    }
}