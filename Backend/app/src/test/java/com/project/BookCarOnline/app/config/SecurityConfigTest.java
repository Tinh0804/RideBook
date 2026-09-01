package com.project.BookCarOnline.app.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

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
