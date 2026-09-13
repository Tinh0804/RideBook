package com.project.BookCarOnline.promotion.repository;

import com.project.BookCarOnline.promotion.entity.PointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, String> {
    Page<PointTransaction> findByLoyaltyAccountIdOrderByCreatedAtDesc(String loyaltyAccountId, Pageable pageable);
}
