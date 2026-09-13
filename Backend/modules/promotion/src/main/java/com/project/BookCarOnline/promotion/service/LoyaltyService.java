package com.project.BookCarOnline.promotion.service;

import com.project.BookCarOnline.promotion.config.LoyaltyProperties;
import com.project.BookCarOnline.promotion.entity.LoyaltyAccount;
import com.project.BookCarOnline.promotion.entity.PointTransaction;
import com.project.BookCarOnline.promotion.entity.enums.MembershipTier;
import com.project.BookCarOnline.promotion.entity.enums.PointTransactionType;
import com.project.BookCarOnline.promotion.repository.LoyaltyAccountRepository;
import com.project.BookCarOnline.promotion.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final LoyaltyProperties loyaltyProperties;

    public LoyaltyAccount getOrCreateAccount(String accountId) {
        return loyaltyAccountRepository.findByAccountId(accountId).orElseGet(() -> {
            LoyaltyAccount newAccount = LoyaltyAccount.builder()
                    .accountId(accountId)
                    .currentPoints(0)
                    .lifetimePoints(0)
                    .tier(MembershipTier.BRONZE)
                    .totalSpent(0.0)
                    .totalRides(0)
                    .build();
            return loyaltyAccountRepository.save(newAccount);
        });
    }

    @Transactional
    public void earnPointsFromTrip(String accountId, double amountSpent, String bookingId) {
        LoyaltyAccount account = getOrCreateAccount(accountId);
        
        double multiplier = getCustomerPointMultiplier(account.getTier());
        // Calculate points based on application properties and tier multiplier
        int pointsEarned = (int) ((amountSpent / loyaltyProperties.getEarnRate()) * multiplier);
        if (pointsEarned <= 0) return;

        account.setCurrentPoints(account.getCurrentPoints() + pointsEarned);
        account.setLifetimePoints(account.getLifetimePoints() + pointsEarned);
        account.setTotalSpent(account.getTotalSpent() + amountSpent);
        account.setTotalRides(account.getTotalRides() + 1);

        checkAndUpgradeTier(account);

        loyaltyAccountRepository.save(account);

        PointTransaction transaction = PointTransaction.builder()
                .loyaltyAccountId(account.getLoyaltyAccountId())
                .amount(pointsEarned)
                .type(PointTransactionType.EARNED)
                .description("Earned points from trip")
                .referenceId(bookingId)
                .build();
        pointTransactionRepository.save(transaction);
    }

    @Transactional
    public void redeemCoins(String accountId, int coinsToRedeem, String bookingId) {
        LoyaltyAccount account = getOrCreateAccount(accountId);
        
        if (account.getCurrentPoints() < coinsToRedeem) {
            throw new IllegalArgumentException("Not enough coins");
        }

        account.setCurrentPoints(account.getCurrentPoints() - coinsToRedeem);
        loyaltyAccountRepository.save(account);

        PointTransaction transaction = PointTransaction.builder()
                .loyaltyAccountId(account.getLoyaltyAccountId())
                .amount(-coinsToRedeem)
                .type(PointTransactionType.REDEEMED)
                .description("Redeemed coins for trip discount")
                .referenceId(bookingId)
                .build();
        pointTransactionRepository.save(transaction);
    }

    public double calculateCoinDiscount(int coins) {
        return coins * loyaltyProperties.getCoinValue();
    }
    
    public double getCustomerPointMultiplier(MembershipTier tier) {
        if (tier == null) return 1.0;
        LoyaltyProperties.TierProperties config = loyaltyProperties.getTiers().get(tier.name().toLowerCase());
        return config != null ? config.getCustomerMultiplier() : 1.0;
    }
    
    public double getDriverCommissionRate(MembershipTier tier) {
        if (tier == null) return 0.20; // Default fallback
        LoyaltyProperties.TierProperties config = loyaltyProperties.getTiers().get(tier.name().toLowerCase());
        return config != null ? config.getDriverCommission() : 0.20;
    }

    private void checkAndUpgradeTier(LoyaltyAccount account) {
        Map<String, LoyaltyProperties.TierProperties> tiers = loyaltyProperties.getTiers();
        
        int currentLifetimePoints = account.getLifetimePoints();
        
        // Find the highest tier they qualify for
        MembershipTier newTier = MembershipTier.BRONZE;
        
        if (tiers.containsKey("diamond") && currentLifetimePoints >= tiers.get("diamond").getThreshold()) {
            newTier = MembershipTier.DIAMOND;
        } else if (tiers.containsKey("gold") && currentLifetimePoints >= tiers.get("gold").getThreshold()) {
            newTier = MembershipTier.GOLD;
        } else if (tiers.containsKey("silver") && currentLifetimePoints >= tiers.get("silver").getThreshold()) {
            newTier = MembershipTier.SILVER;
        }
        
        if (account.getTier() != newTier) {
            log.info("Account {} upgraded from {} to {}", account.getAccountId(), account.getTier(), newTier);
            account.setTier(newTier);
        }
    }

    public Page<PointTransaction> getTransactionHistory(String accountId, Pageable pageable) {
        LoyaltyAccount account = getOrCreateAccount(accountId);
        return pointTransactionRepository.findByLoyaltyAccountIdOrderByCreatedAtDesc(account.getLoyaltyAccountId(), pageable);
    }
    
    public LoyaltyProperties getLoyaltyProperties() {
        return loyaltyProperties;
    }
}
