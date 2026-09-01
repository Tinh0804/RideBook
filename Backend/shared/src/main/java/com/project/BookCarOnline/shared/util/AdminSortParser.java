package com.project.BookCarOnline.shared.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Sort;

public final class AdminSortParser {

    private static final int MAX_SORT_CLAUSES = 5;

    private AdminSortParser() {
    }

    public static Sort parse(
            String requestedSort,
            Map<String, String> allowedFields,
            String defaultSort,
            String stableProperty) {
        String effectiveSort = requestedSort == null || requestedSort.isBlank()
                ? defaultSort
                : requestedSort;
        String[] clauses = effectiveSort.split(",", -1);
        if (clauses.length > MAX_SORT_CLAUSES) {
            throw new IllegalArgumentException("Chỉ hỗ trợ tối đa 5 điều kiện sắp xếp");
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (String clause : clauses) {
            String[] parts = clause.trim().split(":", -1);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                throw new IllegalArgumentException("Sort phải có định dạng field:asc hoặc field:desc");
            }
            String property = allowedFields.get(parts[0].trim());
            if (property == null) {
                throw new IllegalArgumentException("Trường sort không được hỗ trợ: " + parts[0].trim());
            }
            Sort.Direction direction;
            try {
                direction = Sort.Direction.fromString(parts[1].trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Chiều sort chỉ nhận asc hoặc desc");
            }
            orders.add(new Sort.Order(direction, property));
        }
        if (orders.stream().noneMatch(order -> order.getProperty().equals(stableProperty))) {
            orders.add(Sort.Order.asc(stableProperty));
        }
        return Sort.by(orders);
    }
}
