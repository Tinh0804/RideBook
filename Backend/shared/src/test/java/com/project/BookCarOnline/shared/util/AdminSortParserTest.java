package com.project.BookCarOnline.shared.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class AdminSortParserTest {

    private static final Map<String, String> ALLOWED = new LinkedHashMap<>(Map.of(
            "name", "customerName",
            "createdAt", "account.createdAt"));

    @Test
    void parseUsesDefaultAndAddsStableIdSort() {
        Sort sort = AdminSortParser.parse(null, ALLOWED, "name:asc", "customerId");

        assertThat(sort.stream().map(Sort.Order::getProperty))
                .containsExactly("customerName", "customerId");
        assertThat(sort.getOrderFor("customerName").isAscending()).isTrue();
    }

    @Test
    void parsePreservesMultipleClausesAndMapsApiNames() {
        Sort sort = AdminSortParser.parse(
                "createdAt:desc,name:asc", ALLOWED, "name:asc", "customerId");

        assertThat(sort.stream().map(Sort.Order::getProperty))
                .containsExactly("account.createdAt", "customerName", "customerId");
        assertThat(sort.getOrderFor("account.createdAt").isDescending()).isTrue();
    }

    @Test
    void parseRejectsUnknownFieldsAndDirections() {
        assertThatThrownBy(() -> AdminSortParser.parse(
                        "password:asc", ALLOWED, "name:asc", "customerId"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AdminSortParser.parse(
                        "name:sideways", ALLOWED, "name:asc", "customerId"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseRejectsMoreThanFiveClauses() {
        assertThatThrownBy(() -> AdminSortParser.parse(
                        "name:asc,name:desc,name:asc,name:desc,name:asc,name:desc",
                        ALLOWED,
                        "name:asc",
                        "customerId"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
