package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.dto.redis.FareQuote;
import com.project.BookCarOnline.booking.dto.request.EstimatePriceRequest;
import com.project.BookCarOnline.booking.dto.response.EstimatePriceResponse;
import com.project.BookCarOnline.catalog.dto.VehicleTypeSummary;
import com.project.BookCarOnline.catalog.service.VehicleTypeService;
import com.project.BookCarOnline.promotion.dto.PromotionQuote;
import com.project.BookCarOnline.promotion.entity.LoyaltyAccount;
import com.project.BookCarOnline.promotion.service.LoyaltyService;
import com.project.BookCarOnline.promotion.service.PricingService;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BookingQuoteService {

    private final VehicleTypeService vehicleTypeService;
    private final PricingService pricingService;
    private final LoyaltyService loyaltyService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.time-to-live.quote}")
    private long quoteTtlSeconds;

    public List<EstimatePriceResponse> estimate(EstimatePriceRequest request) {
        double distance = calculateDistanceKm(
                request.getPickupLat(), request.getPickupLng(),
                request.getDropoffLat(), request.getDropoffLng());
        List<PromotionQuote> promotions = pricingService.resolvePromotions(request.getPromotionCodes());
        long expiresAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(quoteTtlSeconds);
        
        Double coinsDiscount = 0.0;
        
        if (request.getCustomerId() != null) {
            LoyaltyAccount account = loyaltyService.getOrCreateAccount(request.getCustomerId());
            
            if (request.getUseCoins() != null && request.getUseCoins() > 0) {
                int coinsToUse = Math.min(request.getUseCoins(), account.getCurrentPoints());
                coinsDiscount = loyaltyService.calculateCoinDiscount(coinsToUse);
            }
        }
        
        final Double finalTierDiscountRate = 0.0;
        final Double finalCoinsDiscount = coinsDiscount;
        final Integer usedCoins = request.getUseCoins() != null ? request.getUseCoins() : 0;
        
        return vehicleTypeService.getVehicleTypeSummaries().stream()
                .map(vehicleType -> createQuote(vehicleType, distance, promotions, expiresAt, finalTierDiscountRate, finalCoinsDiscount, usedCoins))
                .toList();
    }

    public FareQuote getQuote(String quoteId) {
        if (quoteId == null || quoteId.isBlank()) {
            throw new AppException(ErrorCode.QUOTE_EXPIRED);
        }
        FareQuote quote = (FareQuote) redisTemplate.opsForValue().get(key(quoteId));
        if (quote == null) {
            throw new AppException(ErrorCode.QUOTE_EXPIRED);
        }
        return quote;
    }

    public void deleteQuote(String quoteId) {
        redisTemplate.delete(key(quoteId));
    }

    private EstimatePriceResponse createQuote(
            VehicleTypeSummary vehicleType,
            double distance,
            List<PromotionQuote> promotions,
            long expiresAt,
            Double tierDiscountRate,
            Double coinsDiscount,
            Integer usedCoins) {
        double pricePerKm = vehicleType.pricePerKm() != null ? vehicleType.pricePerKm() : 0;
        double basePrice = pricePerKm * distance;
        double surcharge = vehicleType.vehicleTypeId() != null
                ? vehicleTypeService.getCurrentSurcharge(vehicleType.vehicleTypeId())
                : 1;
        double surgeMultiplier = 1;
        double rawPrice = basePrice * surcharge * surgeMultiplier;
        
        double promoDiscount = pricingService.calculateTotalDiscount(promotions, rawPrice);
        double tierDiscountAmount = rawPrice * tierDiscountRate;
        
        double originalPrice = roundToThousand(rawPrice);
        double totalDiscount = promoDiscount + tierDiscountAmount + coinsDiscount;
        double totalPrice = Math.max(0, roundToThousand(originalPrice - totalDiscount));

        String quoteId = UUID.randomUUID().toString();
        List<String> promotionIds = promotions.stream().map(PromotionQuote::promotionId).toList();

        FareQuote quote = FareQuote.builder()
                .quoteId(quoteId)
                .vehicleTypeId(vehicleType.vehicleTypeId())
                .distance(distance)
                .basePrice(basePrice)
                .surcharge(surcharge)
                .surgeMultiplier(surgeMultiplier)
                .originalPrice(originalPrice)
                .totalPrice(totalPrice)
                .discount(promoDiscount)
                .tierDiscount(tierDiscountAmount)
                .coinsDiscount(coinsDiscount)
                .usedCoins(usedCoins)
                .promotionIds(promotionIds)
                .build();

        redisTemplate.opsForValue().set(key(quoteId), quote, quoteTtlSeconds, TimeUnit.SECONDS);

        return EstimatePriceResponse.builder()
                .vehicleTypeId(vehicleType.vehicleTypeId())
                .distance(distance)
                .basePrice(basePrice)
                .surcharge(surcharge)
                .surgeMultiplier(surgeMultiplier)
                .originalPrice(originalPrice)
                .totalPrice(totalPrice)
                .discount(promoDiscount)
                .tierDiscount(tierDiscountAmount)
                .coinsDiscount(coinsDiscount)
                .quoteId(quoteId)
                .expiryTime(expiresAt)
                .build();
    }

    static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        int R = 6371; // Bán kính trái đất km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.max(0.1, R * c);
    }

    private double roundToThousand(double amount) {
        return Math.round(amount / 1000.0) * 1000.0;
    }

    private String key(String quoteId) {
        return "ridebook:booking:quote:" + quoteId;
    }
}
