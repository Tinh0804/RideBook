package com.project.BookCarOnline.identity.repository.specification;

import com.project.BookCarOnline.identity.dto.request.AdminCustomerFilter;
import com.project.BookCarOnline.identity.entity.Account;
import com.project.BookCarOnline.identity.entity.Customer;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class CustomerSpecifications {

    private CustomerSpecifications() {
    }

    public static Specification<Customer> from(AdminCustomerFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String pattern = containsPattern(filter.getSearch());
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("customerName")), pattern, '\\'),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), pattern, '\\'),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern, '\\'),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), pattern, '\\')));
            }

            List<String> genders = normalizedValues(filter.getGenders());
            if (!genders.isEmpty()) {
                predicates.add(criteriaBuilder.lower(root.get("gender")).in(genders));
            }

            if (filter.getAccountStatus() != null
                    || filter.getCreatedFrom() != null
                    || filter.getCreatedTo() != null) {
                Join<Customer, Account> account = root.join("account", JoinType.LEFT);
                if (filter.getAccountStatus() != null) {
                    predicates.add(criteriaBuilder.equal(account.get("accountStatus"), filter.getAccountStatus()));
                }
                if (filter.getCreatedFrom() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                            account.get("createdAt"), Timestamp.valueOf(filter.getCreatedFrom())));
                }
                if (filter.getCreatedTo() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                            account.get("createdAt"), Timestamp.valueOf(filter.getCreatedTo())));
                }
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static List<String> normalizedValues(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
    }

    private static String containsPattern(String value) {
        String escaped = value.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
