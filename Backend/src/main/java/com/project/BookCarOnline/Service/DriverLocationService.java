package com.project.BookCarOnline.Service;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Service để lưu và đọc vị trí GPS của tài xế vào Redis.
 * Key pattern: driver:location:{bookingId}
 * TTL: 2 giờ (tự động xóa khi chuyến kết thúc)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DriverLocationService {

    static final String KEY_PREFIX = "driver:location:";
    static final long   TTL_HOURS  = 2L;

    RedisTemplate<String, Object> redisTemplate;

    /**
     * Lưu/cập nhật vị trí tài xế cho một booking cụ thể.
     */
    public void saveLocation(String bookingId, double lat, double lng) {
        String key = KEY_PREFIX + bookingId;
        DriverLocation location = new DriverLocation(lat, lng, Instant.now().toEpochMilli());
        redisTemplate.opsForValue().set(key, location, TTL_HOURS, TimeUnit.HOURS);
        log.debug("Saved driver location for booking {}: lat={}, lng={}", bookingId, lat, lng);
    }

    /**
     * Đọc vị trí tài xế hiện tại từ Redis theo bookingId.
     * Trả về null nếu không có dữ liệu (chưa cập nhật hoặc đã TTL).
     */
    public DriverLocation getLocation(String bookingId) {
        Object raw = redisTemplate.opsForValue().get(KEY_PREFIX + bookingId);
        if (raw instanceof DriverLocation loc) return loc;
        return null;
    }

    /**
     * Xóa vị trí tài xế khi chuyến đi kết thúc / bị hủy.
     */
    public void clearLocation(String bookingId) {
        redisTemplate.delete(KEY_PREFIX + bookingId);
        log.debug("Cleared driver location for booking {}", bookingId);
    }

    // ── Inner DTO ─────────────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverLocation implements Serializable {
        double lat;
        double lng;
        long   timestamp; // epoch millis
    }
}
