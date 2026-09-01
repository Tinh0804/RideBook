package com.project.BookCarOnline.identity.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    @Mock
    RedisTemplate<String, Object> redisTemplate;
    @Mock
    ValueOperations<String, Object> valueOperations;

    RefreshTokenService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new RefreshTokenService(redisTemplate);
    }

    @Test
    void storesOnlyTokenDigestWithRemainingLifetime() {
        service.store("raw-refresh-token", "account-id", Instant.now().plusSeconds(60));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
        verify(valueOperations).set(
                keyCaptor.capture(),
                eq("account-id"),
                ttlCaptor.capture(),
                eq(TimeUnit.MILLISECONDS));

        assertTrue(keyCaptor.getValue().startsWith("auth:refresh:v1:"));
        assertFalse(keyCaptor.getValue().contains("raw-refresh-token"));
        assertTrue(ttlCaptor.getValue() > 0 && ttlCaptor.getValue() <= 60_000);
    }

    @Test
    void consumesRefreshTokenExactlyOnceAtomically() {
        when(valueOperations.getAndDelete(anyString()))
                .thenReturn("account-id")
                .thenReturn(null);

        assertTrue(service.consume("raw-refresh-token", "account-id"));
        assertFalse(service.consume("raw-refresh-token", "account-id"));
    }

    @Test
    void rejectsTokenStoredForAnotherAccount() {
        when(valueOperations.getAndDelete(anyString())).thenReturn("another-account");

        assertFalse(service.consume("raw-refresh-token", "account-id"));
    }
}
