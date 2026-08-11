package com.project.BookCarOnline.promotion.service;

import com.project.BookCarOnline.promotion.dto.request.CreatePromotionRequest;
import com.project.BookCarOnline.promotion.dto.response.PromotionResponse;
import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;
import com.project.BookCarOnline.promotion.entity.Promotion;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.promotion.mapper.PromotionMapper;
import com.project.BookCarOnline.promotion.repository.PromotionRepository;
import com.project.BookCarOnline.promotion.repository.CustomerPromotionRepository;
import com.project.BookCarOnline.identity.service.CustomerService;
import com.project.BookCarOnline.promotion.entity.CustomerPromotion;
import com.project.BookCarOnline.promotion.entity.enums.CustomerPromotionStatus;
import com.project.BookCarOnline.promotion.entity.enums.DiscountType;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionService {
    PromotionRepository promotionRepository;
    PromotionMapper promotionMapper;
    CustomerPromotionRepository customerPromotionRepository;
    CustomerService customerService;
    RedisTemplate<String, Object> redisTemplate;

    public PromotionResponse createPromotion(CreatePromotionRequest request) {
        if (promotionRepository.findByPromotionCode(request.getPromotionCode()).isPresent()) {
            throw new AppException(ErrorCode.PROMOTION_ALREADY_EXISTS);
        }
        Promotion promotion = promotionMapper.toPromotion(request);
        return promotionMapper.toPromotionResponse(promotionRepository.save(promotion));
    }

    public List<PromotionResponse> getActivePromotions() {
        return promotionRepository
                .findActivePromotions(Timestamp.from(Instant.now()))
                .stream().map(promotionMapper::toPromotionResponse)
                .collect(Collectors.toList());
    }

    public PromotionResponse getPromotionByCode(String code) {
        Promotion promotion = promotionRepository.findByPromotionCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        if (!promotion.getIsActive())
            throw new AppException(ErrorCode.PROMOTION_NOT_ACTIVE);
        if (promotion.getQuantity() <= 0)
            throw new AppException(ErrorCode.PROMOTION_OUT_OF_STOCK);
        if (promotion.getEndTime().before(Timestamp.from(Instant.now())))
            throw new AppException(ErrorCode.PROMOTION_EXPIRED);

        return promotionMapper.toPromotionResponse(promotion);
    }

    // ── Admin only ───────────────────────────────────────────────────

    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public List<PromotionResponse> getAllPromotions() {
        Timestamp now = Timestamp.from(Instant.now());
        return promotionRepository.findAll()
                .stream()
                .map(p -> {
                    PromotionResponse resp = promotionMapper.toPromotionResponse(p);
                    // Append computed statistics
                    int usedCount  = customerPromotionRepository.countByPromotion_PromotionIdAndStatus(p.getPromotionId(), CustomerPromotionStatus.USED);
                    int savedCount = customerPromotionRepository.countByPromotion_PromotionIdAndStatus(p.getPromotionId(), CustomerPromotionStatus.SAVED);
                    resp.setUsedCount(usedCount);
                    resp.setSavedCount(savedCount);
                    resp.setIsExpired(p.getEndTime() != null && p.getEndTime().before(now));
                    return resp;
                })
                .collect(Collectors.toList());
    }

    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    @Transactional
    public PromotionResponse updatePromotion(String promotionId, CreatePromotionRequest request) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        if (!promotion.getPromotionCode().equals(request.getPromotionCode()) &&
                promotionRepository.findByPromotionCode(request.getPromotionCode()).isPresent()) {
            throw new AppException(ErrorCode.PROMOTION_ALREADY_EXISTS);
        }

        promotion.setPromotionCode(request.getPromotionCode());
        promotion.setPromotionName(request.getPromotionName());
        promotion.setDiscountLimit(request.getDiscountLimit());
        promotion.setStartTime(request.getStartTime());
        promotion.setEndTime(request.getEndTime());
        promotion.setApplicationCondition(request.getApplicationCondition());
        promotion.setQuantity(request.getQuantity());

        if (request.getDiscountType() != null)
            promotion.setDiscountType(DiscountType.valueOf(request.getDiscountType()));
        if (request.getDiscountValue() != null)
            promotion.setDiscountValue(request.getDiscountValue());
        if (request.getMinTripValue() != null)
            promotion.setMinTripValue(request.getMinTripValue());
        if (request.getUsageLimitPerUser() != null)
            promotion.setUsageLimitPerUser(request.getUsageLimitPerUser());
        if (request.getPromotionImage() != null)
            promotion.setPromotionImage(request.getPromotionImage());
        if (request.getIsActive() != null)
            promotion.setIsActive(request.getIsActive());
        if (request.getIsPublic() != null)
            promotion.setIsPublic(request.getIsPublic());

        log.info("[Promotion] Admin cập nhật khuyến mãi id={}", promotionId);
        Promotion saved = promotionRepository.save(promotion);
        redisTemplate.delete("promotion:" + saved.getPromotionCode());
        return promotionMapper.toPromotionResponse(saved);
    }

    @Transactional
    public PromotionResponse toggleActive(String promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        promotion.setIsActive(!promotion.getIsActive());
        log.info("[Promotion] Toggle isActive={} cho id={}", promotion.getIsActive(), promotionId);
        Promotion saved = promotionRepository.save(promotion);
        redisTemplate.delete("promotion:" + saved.getPromotionCode());
        return promotionMapper.toPromotionResponse(saved);
    }

    @Transactional
    public PromotionResponse toggleVisibility(String promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        Boolean newStatus = promotion.getIsPublic() != null ? !promotion.getIsPublic() : false;
        promotion.setIsPublic(newStatus);
        log.info("[Promotion] Toggle isPublic={} cho id={}", newStatus, promotionId);
        Promotion saved = promotionRepository.save(promotion);
        redisTemplate.delete("promotion:" + saved.getPromotionCode());
        return promotionMapper.toPromotionResponse(saved);
    }

    @Transactional
    public void deletePromotion(String promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        promotionRepository.deleteById(promotionId);
        redisTemplate.delete("promotion:" + promotion.getPromotionCode());
        log.info("[Promotion] Admin xóa khuyến mãi id={}", promotionId);
    }

    // ── Customer Voucher Actions
    // ───────────────────────────────────────────────────

    @Transactional
    public void savePromotionForCustomer(String customerId, String promotionCode) {
        customerService.getCustomerResponseById(customerId);

        Promotion promotion = promotionRepository.findByPromotionCode(promotionCode)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        if (!promotion.getIsActive())
            throw new AppException(ErrorCode.PROMOTION_NOT_ACTIVE);
        if (promotion.getQuantity() <= 0)
            throw new AppException(ErrorCode.PROMOTION_OUT_OF_STOCK);
        if (promotion.getEndTime().before(Timestamp.from(Instant.now())))
            throw new AppException(ErrorCode.PROMOTION_EXPIRED);

        // Check if already saved
        if (customerPromotionRepository.existsByCustomerIdAndPromotion_PromotionId(customerId,
                promotion.getPromotionId())) {
            throw new RuntimeException("Bạn đã lưu mã khuyến mãi này rồi");
        }

        CustomerPromotion cp = CustomerPromotion.builder()
                .customerId(customerId)
                .promotion(promotion)
                .status(CustomerPromotionStatus.SAVED)
                .savedAt(Timestamp.from(Instant.now()))
                .build();
        customerPromotionRepository.save(cp);
    }

    public List<PromotionResponse> getMyPromotions(String customerId) {
        List<CustomerPromotion> list = customerPromotionRepository.findByCustomerId(customerId);
        return list.stream()
                .map(cp -> promotionMapper.toPromotionResponse(cp.getPromotion()))
                .collect(Collectors.toList());
    }
}
