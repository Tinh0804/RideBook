package com.project.BookCarOnline.booking.dto.request;

import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
import com.project.BookCarOnline.shared.util.AdminSortParser;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Schema(name = "AdminBookingFilter", description = "Bộ lọc và sắp xếp booking dành cho admin")
public class AdminBookingFilter {

    private static final Map<String, String> SORT_FIELDS = Map.of(
            "bookingTime", "bookingTime",
            "totalPrice", "totalPrice",
            "bookingStatus", "bookingStatus",
            "distance", "distance");

    @Size(max = 500)
    @Schema(
            description = "Danh sách field:direction, phân cách bằng dấu phẩy. Field: bookingTime, totalPrice, bookingStatus, distance",
            defaultValue = "bookingTime:desc",
            example = "bookingTime:desc,totalPrice:asc")
    private String sort = "bookingTime:desc";

    @Size(max = 200)
    @Schema(description = "Tìm không phân biệt hoa thường theo mã booking, địa chỉ, khách hàng hoặc tài xế")
    private String search;

    @Size(max = 20)
    @Schema(description = "Các trạng thái booking cần lọc")
    private Set<BookingStatus> statuses = new LinkedHashSet<>();

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Cận dưới thời gian tạo booking, inclusive", type = "string", format = "date-time")
    private LocalDateTime bookingFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Cận trên thời gian tạo booking, inclusive", type = "string", format = "date-time")
    private LocalDateTime bookingTo;

    @DecimalMin("0")
    @Schema(description = "Giá tối thiểu", minimum = "0")
    private Double minPrice;

    @DecimalMin("0")
    @Schema(description = "Giá tối đa", minimum = "0")
    private Double maxPrice;

    @DecimalMin("1")
    @DecimalMax("5")
    @Schema(description = "Điểm đánh giá tối thiểu", minimum = "1", maximum = "5")
    private Double minRating;

    @DecimalMin("1")
    @DecimalMax("5")
    @Schema(description = "Điểm đánh giá tối đa", minimum = "1", maximum = "5")
    private Double maxRating;

    @Parameter(hidden = true)
    @Schema(hidden = true)
    private String status;

    @Parameter(hidden = true)
    @Schema(hidden = true)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @Parameter(hidden = true)
    @Schema(hidden = true)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    public Pageable toExportPageable(int exportPage, int exportSize) {
        validateRanges();
        return PageRequest.of(exportPage, exportSize, parseSort());
    }

    public void validateRanges() {
        applyLegacyAliases();
        if (bookingFrom != null && bookingTo != null && bookingFrom.isAfter(bookingTo)) {
            throw new IllegalArgumentException("bookingFrom không được sau bookingTo");
        }
        validateRange(minPrice, maxPrice, "minPrice không được lớn hơn maxPrice");
        validateRange(minRating, maxRating, "minRating không được lớn hơn maxRating");
    }

    public String normalizedSearch() {
        return search == null || search.isBlank() ? null : search.trim().toLowerCase(Locale.ROOT);
    }

    protected org.springframework.data.domain.Sort parseSort() {
        return AdminSortParser.parse(sort, SORT_FIELDS, "bookingTime:desc", "bookingId");
    }

    private void applyLegacyAliases() {
        if ((statuses == null || statuses.isEmpty()) && status != null && !status.isBlank()
                && !"ALL".equalsIgnoreCase(status)) {
            statuses = new LinkedHashSet<>(Set.of(BookingStatus.valueOf(status.toUpperCase(Locale.ROOT))));
        }
        if (bookingFrom == null && fromDate != null) {
            bookingFrom = fromDate.atStartOfDay();
        }
        if (bookingTo == null && toDate != null) {
            bookingTo = toDate.plusDays(1).atStartOfDay().minusNanos(1);
        }
    }

    private static void validateRange(Double minimum, Double maximum, String message) {
        if (minimum != null && maximum != null && minimum > maximum) {
            throw new IllegalArgumentException(message);
        }
    }
}
