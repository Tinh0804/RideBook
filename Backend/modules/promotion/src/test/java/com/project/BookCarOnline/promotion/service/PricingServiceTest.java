package com.project.BookCarOnline.promotion.service;

import com.project.BookCarOnline.promotion.dto.PromotionQuote;
import com.project.BookCarOnline.promotion.entity.CustomerPromotion;
import com.project.BookCarOnline.promotion.entity.Promotion;
import com.project.BookCarOnline.promotion.entity.enums.CustomerPromotionStatus;
import com.project.BookCarOnline.promotion.entity.enums.DiscountType;
import com.project.BookCarOnline.promotion.repository.CustomerPromotionRepository;
import com.project.BookCarOnline.promotion.repository.PromotionRepository;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    PromotionRepository promotionRepository;

    @Mock
    CustomerPromotionRepository customerPromotionRepository;

    @Mock
    RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    PricingService pricingService;

    PromotionQuote fixedDiscountQuote;
    PromotionQuote percentageDiscountQuote;

    @BeforeEach
    void setUp() {
        Timestamp future = Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS));

        fixedDiscountQuote = new PromotionQuote(
                "promo-1",
                "FIXED20K",
                "Giảm 20k",
                null,
                future,
                10,
                true,
                DiscountType.FIXED_AMOUNT,
                20000.0,
                50000.0
        );

        percentageDiscountQuote = new PromotionQuote(
                "promo-2",
                "PERCENT20",
                "Giảm 20% tối đa 30k",
                30000.0,
                future,
                10,
                true,
                DiscountType.PERCENTAGE,
                20.0,
                50000.0
        );
    }

    @Test
    void calculateDiscount_FixedAmount_ReturnsFixedDiscountValue() {
        double discount = pricingService.calculateDiscount(fixedDiscountQuote, 100000.0);
        assertEquals(20000.0, discount);
    }

    @Test
    void calculateDiscount_Percentage_WithinLimit_ReturnsCalculatedDiscount() {
        // 100,000 * 20% = 20,000 <= 30,000 limit
        double discount = pricingService.calculateDiscount(percentageDiscountQuote, 100000.0);
        assertEquals(20000.0, discount);
    }

    @Test
    void calculateDiscount_Percentage_ExceedsLimit_CappedAtLimit() {
        // 200,000 * 20% = 40,000 > 30,000 limit -> capped to 30,000
        double discount = pricingService.calculateDiscount(percentageDiscountQuote, 200000.0);
        assertEquals(30000.0, discount);
    }

    @Test
    void calculateDiscount_BelowMinTripValue_ReturnsZero() {
        // minTripValue is 50,000, rawPrice is 40,000
        double discount = pricingService.calculateDiscount(fixedDiscountQuote, 40000.0);
        assertEquals(0.0, discount);
    }

    @Test
    void calculateDiscount_ExpiredPromotion_ReturnsZero() {
        Timestamp past = Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS));
        PromotionQuote expiredQuote = new PromotionQuote(
                "promo-3",
                "EXPIRED",
                "Hết hạn",
                null,
                past,
                10,
                true,
                DiscountType.FIXED_AMOUNT,
                20000.0,
                50000.0
        );

        double discount = pricingService.calculateDiscount(expiredQuote, 100000.0);
        assertEquals(0.0, discount);
    }

    @Test
    void calculateTotalDiscount_CascadingDiscounts_CalculatesOnRemainingAmount() {
        // Raw price: 100,000
        // First discount (FIXED20K): 20,000 -> remaining 80,000
        // Second discount (PERCENT20): 80,000 * 20% = 16,000
        // Total discount: 36,000
        double totalDiscount = pricingService.calculateTotalDiscount(
                List.of(fixedDiscountQuote, percentageDiscountQuote), 100000.0);

        assertEquals(36000.0, totalDiscount);
    }

    @Test
    void validateAndConsumePromotion_ValidPromotion_DecrementsQuantityAndSaves() {
        Timestamp future = Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS));
        Promotion promotion = Promotion.builder()
                .promotionId("promo-1")
                .promotionCode("VALID20")
                .quantity(5)
                .isActive(true)
                .endTime(future)
                .minTripValue(50000.0)
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(20000.0)
                .build();

        when(promotionRepository.findByPromotionCode("VALID20")).thenReturn(Optional.of(promotion));
        when(customerPromotionRepository.findByCustomerIdAndPromotion_PromotionId("cust-1", "promo-1"))
                .thenReturn(Optional.empty());

        PromotionQuote result = pricingService.validateAndConsumePromotion("VALID20", "cust-1", 100000.0);

        assertNotNull(result);
        assertEquals(4, promotion.getQuantity());
        verify(promotionRepository).save(promotion);
        verify(redisTemplate).delete("promotion:VALID20");
    }

    @Test
    void validateAndConsumePromotion_NotFound_ReturnsNull() {
        when(promotionRepository.findByPromotionCode("UNKNOWN")).thenReturn(Optional.empty());

        PromotionQuote result = pricingService.validateAndConsumePromotion("UNKNOWN", "cust-1", 100000.0);

        assertNull(result);
    }

    @Test
    void markCustomerPromotionUsed_ValidPromotion_DecreasesQuantityAndSetsStatus() {
        CustomerPromotion cp = new CustomerPromotion();
        cp.setQuantity(2);
        cp.setStatus(CustomerPromotionStatus.SAVED);

        when(customerPromotionRepository.findByCustomerIdAndPromotion_PromotionId("cust-1", "promo-1"))
                .thenReturn(Optional.of(cp));

        pricingService.markCustomerPromotionUsed("cust-1", "promo-1");

        assertEquals(1, cp.getQuantity());
        assertEquals(CustomerPromotionStatus.SAVED, cp.getStatus());
        assertNotNull(cp.getUsedAt());
        verify(customerPromotionRepository).save(cp);
    }

    @Test
    void markCustomerPromotionUsed_LastQuantity_SetsStatusToUsed() {
        CustomerPromotion cp = new CustomerPromotion();
        cp.setQuantity(1);
        cp.setStatus(CustomerPromotionStatus.SAVED);

        when(customerPromotionRepository.findByCustomerIdAndPromotion_PromotionId("cust-1", "promo-1"))
                .thenReturn(Optional.of(cp));

        pricingService.markCustomerPromotionUsed("cust-1", "promo-1");

        assertEquals(0, cp.getQuantity());
        assertEquals(CustomerPromotionStatus.USED, cp.getStatus());
        verify(customerPromotionRepository).save(cp);
    }

    @Test
    void markCustomerPromotionUsed_NotFound_ThrowsAppException() {
        when(customerPromotionRepository.findByCustomerIdAndPromotion_PromotionId("cust-1", "promo-1"))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> pricingService.markCustomerPromotionUsed("cust-1", "promo-1"));

        assertEquals(ErrorCode.CUSTOMER_PROMOTION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void markCustomerPromotionUsed_ZeroQuantity_ThrowsOutOfStock() {
        CustomerPromotion cp = new CustomerPromotion();
        cp.setQuantity(0);

        when(customerPromotionRepository.findByCustomerIdAndPromotion_PromotionId("cust-1", "promo-1"))
                .thenReturn(Optional.of(cp));

        AppException exception = assertThrows(
                AppException.class,
                () -> pricingService.markCustomerPromotionUsed("cust-1", "promo-1"));

        assertEquals(ErrorCode.PROMOTION_OUT_OF_STOCK, exception.getErrorCode());
    }
}
