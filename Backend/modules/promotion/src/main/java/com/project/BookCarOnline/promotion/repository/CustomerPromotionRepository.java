package com.project.BookCarOnline.promotion.repository;

import com.project.BookCarOnline.promotion.entity.CustomerPromotion;
import com.project.BookCarOnline.promotion.entity.enums.CustomerPromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerPromotionRepository extends JpaRepository<CustomerPromotion, String> {
    List<CustomerPromotion> findByCustomerId(String customerId);
    List<CustomerPromotion> findByCustomerIdAndStatus(String customerId, CustomerPromotionStatus status);
    Optional<CustomerPromotion> findByCustomerIdAndPromotion_PromotionId(String customerId, String promotionId);
    boolean existsByCustomerIdAndPromotion_PromotionId(String customerId, String promotionId);
    int countByCustomerIdAndPromotion_PromotionIdAndStatus(String customerId, String promotionId, CustomerPromotionStatus status);

    // Thống kê theo promotion
    int countByPromotion_PromotionId(String promotionId);
    int countByPromotion_PromotionIdAndStatus(String promotionId, CustomerPromotionStatus status);
}
