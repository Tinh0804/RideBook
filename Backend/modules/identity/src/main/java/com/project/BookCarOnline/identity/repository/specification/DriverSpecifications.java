package com.project.BookCarOnline.identity.repository.specification;

import com.project.BookCarOnline.identity.dto.request.AdminDriverFilter;
import com.project.BookCarOnline.identity.entity.Account;
import com.project.BookCarOnline.identity.entity.Driver;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class DriverSpecifications {

    private DriverSpecifications() {
    }

    public static Specification<Driver> from(AdminDriverFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String pattern = containsPattern(filter.getSearch());
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("driverName")), pattern, '\\'),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), pattern, '\\'),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern, '\\'),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("citizenId")), pattern, '\\'),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("licensePlate")), pattern, '\\'),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("vehicleName")), pattern, '\\'),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("area")), pattern, '\\')));
            }
            if (filter.getActivityStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("activityStatus"), filter.getActivityStatus()));
            }

            List<String> vehicleTypeIds = normalizedValues(filter.getVehicleTypeIds(), false);
            if (!vehicleTypeIds.isEmpty()) {
                predicates.add(root.get("vehicleTypeId").in(vehicleTypeIds));
            }
            List<String> areas = normalizedValues(filter.getAreas(), true);
            if (!areas.isEmpty()) {
                predicates.add(criteriaBuilder.lower(root.get("area")).in(areas));
            }
            if (filter.getMinRating() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("score"), filter.getMinRating()));
            }
            if (filter.getMaxRating() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("score"), filter.getMaxRating()));
            }

            if (filter.getAccountStatus() != null
                    || filter.getCreatedFrom() != null
                    || filter.getCreatedTo() != null) {
                Join<Driver, Account> account = root.join("account", JoinType.LEFT);
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

    private static List<String> normalizedValues(List<String> values, boolean lowerCase) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .map(value -> lowerCase ? value.toLowerCase(Locale.ROOT) : value)
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
