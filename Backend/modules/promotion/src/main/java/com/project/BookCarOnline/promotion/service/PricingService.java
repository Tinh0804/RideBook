package com.project.BookCarOnline.promotion.service;


import com.project.BookCarOnline.promotion.entity.CustomerPromotion;
import com.project.BookCarOnline.promotion.entity.enums.CustomerPromotionStatus;
import com.project.BookCarOnline.promotion.entity.enums.DiscountType;
import com.project.BookCarOnline.promotion.entity.Promotion;
import com.project.BookCarOnline.promotion.dto.PromotionQuote;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.promotion.repository.CustomerPromotionRepository;
import com.project.BookCarOnline.promotion.repository.PromotionRepository;
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

    public double calculateDiscount(PromotionQuote promotion, double rawPrice) {
        if (promotion == null) return 0.0;
        if (!isPromotionApplicable(promotion, rawPrice)) return 0.0;

        return switch (promotion.discountType()) {
            case FIXED_AMOUNT -> promotion.discountValue() != null ? promotion.discountValue() : 0.0;
            case PERCENTAGE   -> calculatePercentageDiscount(promotion, rawPrice);
            default -> {
                log.warn("[Pricing] DiscountType không hỗ trợ: {}", promotion.discountType());
                yield 0.0;
            }
        };
    }

    public double calculateTotalDiscount(List<PromotionQuote> promotions, double rawPrice) {
        if (promotions == null || promotions.isEmpty()) return 0.0;
        double remaining = rawPrice;
        double total = 0.0;
        for (PromotionQuote p : promotions) {
            if (!isPromotionApplicable(p, remaining)) continue;
            double d = calculateDiscount(p, remaining);
            total += d;
            remaining = Math.max(0.0, remaining - d); // giảm giá lần lượt trên giá còn lại
        }
        return total;
    }

    /** Lấy danh sách Promotion hợp lệ từ các mã */
    public List<PromotionQuote> resolvePromotions(List<String> promotionCodes) {
        if (promotionCodes == null || promotionCodes.isEmpty()) return List.of();
        List<PromotionQuote> result = new ArrayList<>();
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
                result.add(toQuote(promo));
            }
        }
        return result;
    }

    private double calculatePercentageDiscount(PromotionQuote promotion, double rawPrice) {
        double percent  = promotion.discountValue() != null ? promotion.discountValue() : 0.0;
        double discount = rawPrice * percent / 100.0;
        // Cap theo discountLimit nếu có
        if (promotion.discountLimit() != null && discount > promotion.discountLimit()) {
            discount = promotion.discountLimit();
        }
        return discount;
    }

    private boolean isPromotionApplicable(PromotionQuote promotion, double rawPrice) {
        if (!Boolean.TRUE.equals(promotion.active())) return false;
        if (promotion.quantity() == null || promotion.quantity() <= 0) return false;
        
        Timestamp now = Timestamp.from(Instant.now());
        if (promotion.endTime() != null && promotion.endTime().before(now)) return false;
        
        if (promotion.minTripValue() != null && rawPrice < promotion.minTripValue()) return false;
        return true;
    }

    @Transactional
    public PromotionQuote validateAndConsumePromotion(String promotionCode,
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
                .findByCustomerIdAndPromotion_PromotionId(customerId, promotion.getPromotionId())
                .orElse(null);
        if (cp != null && cp.getQuantity() != null && cp.getQuantity() <= 0) {
            return null;
        }
        
        promotion.setQuantity(promotion.getQuantity() - 1);
        promotionRepository.save(promotion);
        redisTemplate.delete("promotion:" + promotionCode);

        log.info("[Pricing] Áp dụng promotion {} cho customer {}", promotionCode, customerId);
        return toQuote(promotion);
    }

    @Transactional
    public void markCustomerPromotionUsed(String customerId, String promotionId) {
        CustomerPromotion customerPromotion = customerPromotionRepository
                .findByCustomerIdAndPromotion_PromotionId(customerId, promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROMOTION_NOT_FOUND));
        int quantity = customerPromotion.getQuantity() != null ? customerPromotion.getQuantity() : 0;
        if (quantity <= 0) {
            throw new AppException(ErrorCode.PROMOTION_OUT_OF_STOCK);
        }
        customerPromotion.setQuantity(quantity - 1);
        customerPromotion.setUsedAt(Timestamp.from(Instant.now()));
        customerPromotion.setStatus(customerPromotion.getQuantity() <= 0
                ? CustomerPromotionStatus.USED
                : CustomerPromotionStatus.SAVED);
        customerPromotionRepository.save(customerPromotion);
    }

    public List<PromotionQuote> getPromotions(Iterable<String> promotionIds) {
        return promotionRepository.findAllById(promotionIds).stream().map(this::toQuote).toList();
    }

    private PromotionQuote toQuote(Promotion promotion) {
        return new PromotionQuote(
                promotion.getPromotionId(),
                promotion.getPromotionCode(),
                promotion.getPromotionName(),
                promotion.getDiscountLimit(),
                promotion.getEndTime(),
                promotion.getQuantity(),
                promotion.getIsActive(),
                promotion.getDiscountType(),
                promotion.getDiscountValue(),
                promotion.getMinTripValue());
    }
}
