package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.CustomerPromotion;
import com.project.BookCarOnline.Entity.Enum.CustomerPromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerPromotionRepository extends JpaRepository<CustomerPromotion, String> {
    List<CustomerPromotion> findByCustomer_CustomerId(String customerId);
    List<CustomerPromotion> findByCustomer_CustomerIdAndStatus(String customerId, CustomerPromotionStatus status);
    Optional<CustomerPromotion> findByCustomer_CustomerIdAndPromotion_PromotionId(String customerId, String promotionId);
    boolean existsByCustomer_CustomerIdAndPromotion_PromotionId(String customerId, String promotionId);
    int countByCustomer_CustomerIdAndPromotion_PromotionIdAndStatus(String customerId, String promotionId, CustomerPromotionStatus status);
}
