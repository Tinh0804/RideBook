package com.project.BookCarOnline.identity.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    RedisTemplate<String, Object> redisTemplate;

    LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService(redisTemplate);
        ReflectionTestUtils.setField(service, "maxAttempts", 5);
        ReflectionTestUtils.setField(service, "failureWindow", Duration.ofMinutes(15));
        ReflectionTestUtils.setField(service, "lockCooldown", Duration.ofMinutes(15));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void recordFailureUsesAtomicScriptAndHashedPrincipalKeys() {
        when(redisTemplate.execute(
                any(RedisScript.class), anyList(), any(), any(), any()))
                .thenReturn(1L);

        boolean locked = service.recordFailure("Customer@Example.com");

        assertTrue(locked);
        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        verify(redisTemplate).execute(
                any(RedisScript.class), keysCaptor.capture(), any(), any(), any());
        List<String> keys = keysCaptor.getValue();
        assertTrue(keys.get(0).startsWith("auth:login:fail:v1:"));
        assertTrue(keys.get(1).startsWith("auth:login:lock:v1:"));
        assertFalse(String.join("", keys).contains("customer@example.com"));
    }

    @Test
    void isLockedUsesCooldownKey() {
        when(redisTemplate.hasKey(any())).thenReturn(true);

        assertTrue(service.isLocked("0912345678"));
    }

    @Test
    void recordSuccessClearsFailureAndLockState() {
        service.recordSuccess("0912345678");

        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        verify(redisTemplate).delete(keysCaptor.capture());
        assertTrue(keysCaptor.getValue().stream().allMatch(key -> !key.contains("0912345678")));
    }
}
