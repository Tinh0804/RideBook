package com.project.BookCarOnline.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    StringRedisTemplate redisTemplate;
    @Mock
    FilterChain filterChain;

    RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter(
                redisTemplate,
                new ObjectMapper(),
                10, Duration.ofMinutes(1),
                3, Duration.ofMinutes(10),
                5, Duration.ofMinutes(5));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void loginOverLimitReturns429WithoutCallingDownstream() throws Exception {
        MockHttpServletRequest request = request("POST", "/RideBook/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any())).thenReturn(11L);

        filter.doFilter(request, response, filterChain);

        assertEquals(429, response.getStatus());
        assertEquals("60", response.getHeader("Retry-After"));
        assertFalse(response.getContentAsString().contains("203.0.113.10"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void requestWithinLimitContinues() throws Exception {
        MockHttpServletRequest request = request("PUT", "/RideBook/auth/reset-password");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any())).thenReturn(1L);

        filter.doFilter(request, response, filterChain);

        assertEquals("5", response.getHeader("X-RateLimit-Limit"));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void downstreamExceptionIsNotConvertedIntoRateLimitFailure() throws Exception {
        MockHttpServletRequest request = request("POST", "/RideBook/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any())).thenReturn(1L);
        doThrow(new IllegalStateException("downstream failure"))
                .when(filterChain).doFilter(request, response);

        assertThrows(
                IllegalStateException.class,
                () -> filter.doFilter(request, response, filterChain));

        assertEquals(200, response.getStatus());
    }

    private MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setContextPath("/RideBook");
        request.setRemoteAddr("203.0.113.10");
        return request;
    }
}
