package com.project.BookCarOnline.identity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@Schema(name = "AdminDriverFilter", description = "Advanced filters and sorting for admin driver search/export")
public class AdminDriverFilter {

    @Size(max = 200)
    @Schema(description = "Case-insensitive search across name, phone, email, citizen ID, license plate, vehicle name, and area",
            maxLength = 200, example = "51A-12345")
    private String search;

    @Size(max = 500)
    @Schema(description = "Comma-separated field:direction terms. Allowed fields: driverName, score, lastTripTime, area, createdAt",
            defaultValue = "driverName:asc", example = "score:desc,driverName:asc")
    private String sort = "driverName:asc";

    @Schema(description = "Filter by account enabled status", example = "true")
    private Boolean accountStatus;

    @Schema(description = "Filter by driver online/activity status", example = "true")
    private Boolean activityStatus;

    @Size(max = 100)
    @Schema(description = "Comma-separated vehicle type IDs", example = "vehicle-type-1,vehicle-type-2")
    private List<String> vehicleTypeIds;

    @Size(max = 100)
    @Schema(description = "Comma-separated exact area values, case-insensitive", example = "Ho Chi Minh,Da Nang")
    private List<String> areas;

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    @Schema(description = "Inclusive minimum driver rating", minimum = "0", maximum = "5", example = "4.0")
    private Double minRating;

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    @Schema(description = "Inclusive maximum driver rating", minimum = "0", maximum = "5", example = "5.0")
    private Double maxRating;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Inclusive lower account creation-time bound", type = "string", format = "date-time",
            example = "2026-09-01T00:00:00")
    private LocalDateTime createdFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Inclusive upper account creation-time bound", type = "string", format = "date-time",
            example = "2026-09-30T23:59:59")
    private LocalDateTime createdTo;
}
