package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.DTO.Request.CreatePromotionRequest;
import com.project.BookCarOnline.DTO.Response.PromotionResponse;
import com.project.BookCarOnline.Entity.Promotion;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Mapper.PromotionMapper;
import com.project.BookCarOnline.Repository.PromotionRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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

    // ── Khách hàng & Admin ───────────────────────────────────────────

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

    /** Lấy toàn bộ khuyến mãi (kể cả đã tắt) để Admin quản lý */
    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll()
                .stream().map(promotionMapper::toPromotionResponse)
                .collect(Collectors.toList());
    }

    /** Cập nhật thông tin khuyến mãi */
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
        if (request.getIsActive() != null) promotion.setIsActive(request.getIsActive());

        log.info("[Promotion] Admin cập nhật khuyến mãi id={}", promotionId);
        return promotionMapper.toPromotionResponse(promotionRepository.save(promotion));
    }

    /** Bật/tắt trạng thái hoạt động */
    @Transactional
    public PromotionResponse toggleActive(String promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        promotion.setIsActive(!promotion.getIsActive());
        log.info("[Promotion] Toggle isActive={} cho id={}", promotion.getIsActive(), promotionId);
        return promotionMapper.toPromotionResponse(promotionRepository.save(promotion));
    }

    /** Xóa vĩnh viễn khuyến mãi */
    @Transactional
    public void deletePromotion(String promotionId) {
        if (!promotionRepository.existsById(promotionId)) {
            throw new AppException(ErrorCode.PROMOTION_NOT_FOUND);
        }
        promotionRepository.deleteById(promotionId);
        log.info("[Promotion] Admin xóa khuyến mãi id={}", promotionId);
    }
}
