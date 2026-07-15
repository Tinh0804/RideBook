package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.DTO.Redis.DriverGeoResult;
import com.project.BookCarOnline.DTO.Redis.DriverLocation;
import com.project.BookCarOnline.DTO.Redis.DriverStats;
import com.project.BookCarOnline.Entity.Enum.RejectionType;
import com.project.BookCarOnline.Repository.BookingRejectionRepository;
import com.project.BookCarOnline.Repository.RatingRepository;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service tổng hợp các thao tác liên quan đến Redis/Cache của Tài xế:
 * 1. Tracking vị trí chi tiết cho cuốc xe hiện tại (Location)
 * 2. Tìm kiếm tài xế quanh đây siêu tốc (GEO)
 * 3. Đệm điểm số ưu tiên để tránh N+1 Query (Caffeine Cache)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DriverCacheService {

    RatingRepository ratingRepository;
    BookingRejectionRepository rejectionRepository;

    RedisTemplate<String, Object> redisTemplate;

    static final String LOCATION_PREFIX = "driver:location:";
    static final long   LOCATION_TTL_HOURS  = 2L;
    static final String GEO_KEY = "geo:drivers:active";



    public void saveLocation(String bookingId, double lat, double lng) {
        String key = LOCATION_PREFIX + bookingId;
        DriverLocation location = new DriverLocation(lat, lng, Instant.now().toEpochMilli());
        redisTemplate.opsForValue().set(key, location, LOCATION_TTL_HOURS, TimeUnit.HOURS);
        log.debug("Saved driver location for booking {}: lat={}, lng={}", bookingId, lat, lng);
    }

    public DriverLocation getLocation(String bookingId) {
        Object raw = redisTemplate.opsForValue().get(LOCATION_PREFIX + bookingId);
        if (raw instanceof DriverLocation loc) return loc;
        return null;
    }

    public void clearLocation(String bookingId) {
        redisTemplate.delete(LOCATION_PREFIX + bookingId);
        log.debug("Cleared driver location for booking {}", bookingId);
    }



    private String getGeoKey(String vehicleTypeId) {
        return GEO_KEY + ":" + vehicleTypeId;
    }

    public void addDriverLocationGeo(String driverId, String vehicleTypeId, double lat, double lng) {
        if (vehicleTypeId == null) return;
        redisTemplate.opsForGeo().add(getGeoKey(vehicleTypeId), new Point(lng, lat), driverId);
        log.debug("[GEO] Cập nhật vị trí tài xế {} ({}) → ({}, {})", driverId, vehicleTypeId, lat, lng);
    }

    public void removeDriverLocationGeo(String driverId, String vehicleTypeId) {
        if (vehicleTypeId == null) return;
        redisTemplate.opsForGeo().remove(getGeoKey(vehicleTypeId), driverId);
        log.debug("[GEO] Xóa vị trí tài xế {} khỏi Redis GEO ({})", driverId, vehicleTypeId);
    }

    public List<DriverGeoResult> findNearbyDrivers(String vehicleTypeId, double lat, double lng, double radiusKm) {
        if (vehicleTypeId == null) return Collections.emptyList();
        
        Circle searchArea = new Circle(
                new Point(lng, lat),
                new Distance(radiusKm, Metrics.KILOMETERS)
        );

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .sortAscending()
                .limit(50);

        GeoResults<RedisGeoCommands.GeoLocation<Object>> results =
                redisTemplate.opsForGeo().radius(getGeoKey(vehicleTypeId), searchArea, args);

        if (results == null) {
            log.info("[GEO] Không tìm thấy tài xế nào ({}) trong bán kính {}km tại ({}, {})", vehicleTypeId, radiusKm, lat, lng);
            return Collections.emptyList();
        }

        List<DriverGeoResult> drivers = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<Object>> result : results) {
            String driverId = result.getContent().getName().toString();
            if (Boolean.TRUE.equals(redisTemplate.hasKey("driver:on_hold:" + driverId))) {
                continue; // Skip drivers that are on hold
            }
            double distanceKm = result.getDistance().getValue();
            drivers.add(new DriverGeoResult(driverId, distanceKm));
        }

        log.info("[GEO] Tìm thấy {} tài xế ({}) trong bán kính {}km tại ({}, {})", drivers.size(), vehicleTypeId, radiusKm, lat, lng);
        return drivers;
    }


    public void holdDriver(String driverId, String bookingId) {
        redisTemplate.opsForValue().set("driver:on_hold:" + driverId, bookingId, 16, TimeUnit.SECONDS);
    }

    public void releaseDriver(String driverId) {
        redisTemplate.delete("driver:on_hold:" + driverId);
    }

    @Cacheable(value = "driverStats", key = "#driverId")
    public DriverStats getDriverStats(String driverId) {
        log.debug("[DriverScore] Tính điểm từ DB cho tài xế: {}", driverId);

        Double avgRating = ratingRepository.getAverageRatingByDriver(driverId);
        int rejectCount = rejectionRepository.countByDriverIdAndType(driverId, RejectionType.REJECTED);
        int ignoreCount = rejectionRepository.countByDriverIdAndType(driverId, RejectionType.IGNORED);

        return new DriverStats(
                avgRating != null ? avgRating : 3.0,
                rejectCount,
                ignoreCount
        );
    }

    @CacheEvict(value = "driverStats", key = "#driverId")
    public void evictDriverStats(String driverId) {
        log.debug("[DriverScore] Xóa cache cho tài xế: {}", driverId);
    }

}
