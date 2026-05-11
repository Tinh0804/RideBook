package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, String> {
    Optional<Promotion> findByPromotionCode(String promotionCode);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.quantity > 0 AND p.startTime <= :now AND p.endTime >= :now")
    List<Promotion> findActivePromotions(@Param("now") Timestamp now);
}
