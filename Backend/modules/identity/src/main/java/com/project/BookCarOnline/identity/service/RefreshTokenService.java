package com.project.BookCarOnline.identity.service;

import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenService {
    static final String KEY_PREFIX = "auth:refresh:v1:";

    RedisTemplate<String, Object> redisTemplate;

    void store(String refreshToken, String accountId, Instant expirationTime) {
        long ttlMillis = expirationTime.toEpochMilli() - Instant.now().toEpochMilli();
        if (!StringUtils.hasText(refreshToken) || !StringUtils.hasText(accountId) || ttlMillis <= 0) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        redisTemplate.opsForValue().set(
                key(refreshToken),
                accountId,
                ttlMillis,
                TimeUnit.MILLISECONDS);
    }

    boolean consume(String refreshToken, String accountId) {
        if (!StringUtils.hasText(refreshToken) || !StringUtils.hasText(accountId)) {
            return false;
        }

        Object storedAccountId = redisTemplate.opsForValue().getAndDelete(key(refreshToken));
        return accountId.equals(storedAccountId);
    }

    private String key(String refreshToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
