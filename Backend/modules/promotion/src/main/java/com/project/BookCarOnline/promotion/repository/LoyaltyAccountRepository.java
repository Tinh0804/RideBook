package com.project.BookCarOnline.promotion.repository;

import com.project.BookCarOnline.promotion.entity.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, String> {
    Optional<LoyaltyAccount> findByAccountId(String accountId);
}
