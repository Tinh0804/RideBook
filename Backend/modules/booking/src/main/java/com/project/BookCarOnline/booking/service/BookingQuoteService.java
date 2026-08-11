package com.project.BookCarOnline.booking.service;

import com.project.BookCarOnline.booking.dto.redis.FareQuote;
import com.project.BookCarOnline.booking.dto.request.EstimatePriceRequest;
import com.project.BookCarOnline.booking.dto.response.EstimatePriceResponse;
import com.project.BookCarOnline.catalog.dto.VehicleTypeSummary;
import com.project.BookCarOnline.catalog.service.VehicleTypeService;
import com.project.BookCarOnline.promotion.dto.PromotionQuote;
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
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.time-to-live.quote}")
    private long quoteTtlSeconds;

    public List<EstimatePriceResponse> estimate(EstimatePriceRequest request) {
        double distance = calculateDistanceKm(
                request.getPickupLat(), request.getPickupLng(),
                request.getDropoffLat(), request.getDropoffLng());
        List<PromotionQuote> promotions = pricingService.resolvePromotions(request.getPromotionCodes());
        long expiresAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(quoteTtlSeconds);
        return vehicleTypeService.getVehicleTypeSummaries().stream()
                .map(vehicleType -> createQuote(vehicleType, distance, promotions, expiresAt))
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
            long expiresAt) {
        double pricePerKm = vehicleType.pricePerKm() != null ? vehicleType.pricePerKm() : 0;
        double basePrice = pricePerKm * distance;
        double surcharge = vehicleType.vehicleTypeId() != null
                ? vehicleTypeService.getCurrentSurcharge(vehicleType.vehicleTypeId())
                : 1;
        double surgeMultiplier = 1;
        double rawPrice = basePrice * surcharge * surgeMultiplier;
        double discount = pricingService.calculateTotalDiscount(promotions, rawPrice);
        double originalPrice = roundToThousand(rawPrice);
        double totalPrice = Math.max(0, roundToThousand(originalPrice - discount));
        String quoteId = UUID.randomUUID().toString();

        FareQuote quote = FareQuote.builder()
                .quoteId(quoteId)
                .vehicleTypeId(vehicleType.vehicleTypeId())
                .distance(distance)
                .basePrice(basePrice)
                .surcharge(surcharge)
                .surgeMultiplier(surgeMultiplier)
                .originalPrice(originalPrice)
                .totalPrice(totalPrice)
                .discount(discount)
                .promotionIds(promotions != null
                        ? promotions.stream().map(PromotionQuote::promotionCode).toList()
                        : List.of())
                .build();
        redisTemplate.opsForValue().set(key(quoteId), quote, quoteTtlSeconds, TimeUnit.SECONDS);

        return EstimatePriceResponse.builder()
                .vehicleTypeId(vehicleType.vehicleTypeId())
                .distance(distance)
                .basePrice(basePrice)
                .surcharge(surcharge)
                .surgeMultiplier(surgeMultiplier)
                .originalPrice(originalPrice)
                .discount(discount)
                .totalPrice(totalPrice)
                .quoteId(quoteId)
                .expiryTime(expiresAt)
                .build();
    }

    static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int earthRadiusKm = 6371;
        double latitudeDistance = Math.toRadians(lat2 - lat1);
        double longitudeDistance = Math.toRadians(lon2 - lon1);
        double haversine = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(longitudeDistance / 2) * Math.sin(longitudeDistance / 2);
        return earthRadiusKm * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private double roundToThousand(double value) {
        return Math.round(value / 1000) * 1000;
    }

    private String key(String quoteId) {
        return "quote:" + quoteId;
    }
}
