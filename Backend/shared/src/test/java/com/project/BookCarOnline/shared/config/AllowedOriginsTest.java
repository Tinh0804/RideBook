package com.project.BookCarOnline.shared.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllowedOriginsTest {

    @Test
    void parsesAndDeduplicatesHttpOrigins() {
        assertThat(AllowedOrigins.parse(
                " https://ridebook.example.com, http://localhost:5173,https://ridebook.example.com "))
                .containsExactly("https://ridebook.example.com", "http://localhost:5173");
    }

    @Test
    void rejectsWildcardOrigins() {
        assertThatThrownBy(() -> AllowedOrigins.parse("https://ridebook.example.com,*"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wildcard");
    }

    @Test
    void rejectsMissingOrigins() {
        assertThatThrownBy(() -> AllowedOrigins.parse("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one");
    }

    @Test
    void rejectsNonHttpOrigins() {
        assertThatThrownBy(() -> AllowedOrigins.parse("file:///tmp/index.html"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTP or HTTPS");
    }

    @Test
    void rejectsUrlsThatAreNotOrigins() {
        assertThatThrownBy(() -> AllowedOrigins.parse("https://ridebook.example.com/app?mode=prod"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("origin only");
    }
}
