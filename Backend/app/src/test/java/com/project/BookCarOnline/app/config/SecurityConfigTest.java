package com.project.BookCarOnline.app.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void publicAuthRoutesDoNotIncludeAuthenticatedOperations() {
        SecurityConfig securityConfig = new SecurityConfig(null, "http://localhost:5173");

        String[] publicEndpoints = (String[]) ReflectionTestUtils.getField(securityConfig, "AUTH_ENDPOINTS");

        assertThat(publicEndpoints)
                .doesNotContain("/auth/**", "/auth/logout", "/auth/introspect", "/auth/change-password")
                .contains("/auth/login", "/auth/refresh-token", "/auth/email-verification/**");
    }

    @Test
    void usesOnlyConfiguredCorsOrigins() {
        SecurityConfig securityConfig = new SecurityConfig(
                null,
                "https://ridebook.example.com,http://localhost:5173");

        UrlBasedCorsConfigurationSource source =
                (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();
        CorsConfiguration configuration = source.getCorsConfigurations().get("/**");

        assertThat(configuration.getAllowedOrigins())
                .containsExactly("https://ridebook.example.com", "http://localhost:5173");
        assertThat(configuration.getAllowedOriginPatterns()).isNullOrEmpty();
    }
}
