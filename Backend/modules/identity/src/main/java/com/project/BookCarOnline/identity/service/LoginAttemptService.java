package com.project.BookCarOnline.identity.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoginAttemptService {

    static final String FAILURE_PREFIX = "auth:login:fail:v1:";
    static final String LOCK_PREFIX = "auth:login:lock:v1:";
    static final DefaultRedisScript<Long> RECORD_FAILURE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[2]) == 1 then
                return 1
            end
            local attempts = redis.call('INCR', KEYS[1])
            if attempts == 1 then
                redis.call('PEXPIRE', KEYS[1], ARGV[2])
            end
            if attempts >= tonumber(ARGV[1]) then
                redis.call('SET', KEYS[2], '1', 'PX', ARGV[3])
                redis.call('DEL', KEYS[1])
                return 1
            end
            return 0
            """, Long.class);

    RedisTemplate<String, Object> redisTemplate;

    @NonFinal
    @Value("${app.security.login-lockout.max-attempts:5}")
    int maxAttempts;

    @NonFinal
    @Value("${app.security.login-lockout.failure-window:15m}")
    Duration failureWindow;

    @NonFinal
    @Value("${app.security.login-lockout.cooldown:15m}")
    Duration lockCooldown;

    public boolean isLocked(String principal) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey(principal)));
    }

    public boolean recordFailure(String principal) {
        Long result = redisTemplate.execute(
                RECORD_FAILURE_SCRIPT,
                List.of(failureKey(principal), lockKey(principal)),
                maxAttempts,
                failureWindow.toMillis(),
                lockCooldown.toMillis());
        return Long.valueOf(1L).equals(result);
    }

    public void recordSuccess(String principal) {
        redisTemplate.delete(List.of(failureKey(principal), lockKey(principal)));
    }

    private String failureKey(String principal) {
        return FAILURE_PREFIX + principalHash(principal);
    }

    private String lockKey(String principal) {
        return LOCK_PREFIX + principalHash(principal);
    }

    private String principalHash(String principal) {
        String normalized = principal == null ? "" : principal.trim().toLowerCase(Locale.ROOT);
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
