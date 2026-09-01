package com.project.BookCarOnline.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.BookCarOnline.shared.dto.APIResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final DefaultRedisScript<Long> INCREMENT_SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
                redis.call('PEXPIRE', KEYS[1], ARGV[1])
            end
            return current
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Map<Route, Policy> policies;

    public RateLimitFilter(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${app.security.rate-limit.login.limit:10}") int loginLimit,
            @Value("${app.security.rate-limit.login.window:1m}") Duration loginWindow,
            @Value("${app.security.rate-limit.forgot-password.limit:3}") int forgotPasswordLimit,
            @Value("${app.security.rate-limit.forgot-password.window:10m}") Duration forgotPasswordWindow,
            @Value("${app.security.rate-limit.verification.limit:5}") int verificationLimit,
            @Value("${app.security.rate-limit.verification.window:5m}") Duration verificationWindow) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        Policy login = new Policy("login", loginLimit, loginWindow);
        Policy forgotPassword = new Policy("forgot-password", forgotPasswordLimit, forgotPasswordWindow);
        Policy verification = new Policy("verification", verificationLimit, verificationWindow);
        this.policies = Map.of(
                new Route("POST", "/auth/login"), login,
                new Route("GET", "/auth/check-phone"), forgotPassword,
                new Route("POST", "/auth/forgot-password"), forgotPassword,
                new Route("POST", "/auth/email-verification/resend"), forgotPassword,
                new Route("PUT", "/auth/reset-password"), verification,
                new Route("POST", "/auth/email-verification/verify"), verification);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Policy policy = policies.get(new Route(request.getMethod(), applicationPath(request)));
        if (policy == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Long count;
        try {
            String key = "auth:rate:v1:" + policy.name() + ":" + sha256(request.getRemoteAddr());
            count = redisTemplate.execute(
                    INCREMENT_SCRIPT,
                    List.of(key),
                    Long.toString(policy.window().toMillis()));
        } catch (RuntimeException exception) {
            writeError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Authentication rate limiting is temporarily unavailable");
            return;
        }

        if (count == null) {
            writeError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Authentication rate limiting is temporarily unavailable");
            return;
        }

        response.setHeader("X-RateLimit-Limit", Integer.toString(policy.limit()));
        response.setHeader("X-RateLimit-Remaining",
                Long.toString(Math.max(0, policy.limit() - count)));
        if (count > policy.limit()) {
            response.setHeader("Retry-After",
                    Long.toString(Math.max(1, policy.window().toSeconds())));
            writeError(response, 429, "Too many requests");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String applicationPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        return contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)
                ? uri.substring(contextPath.length())
                : uri;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), APIResponse.builder()
                .status(status)
                .message(message)
                .build());
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private record Route(String method, String path) {
    }

    private record Policy(String name, int limit, Duration window) {
    }
}
