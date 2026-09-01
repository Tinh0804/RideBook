package com.project.BookCarOnline.identity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "AdminCustomerFilter", description = "Advanced filters and sorting for admin customer search/export")
public class AdminCustomerFilter {

    @Size(max = 200)
    @Schema(description = "Case-insensitive search across name, phone, email, and address", maxLength = 200,
            example = "nguyen")
    private String search;

    @Size(max = 500)
    @Schema(description = "Comma-separated field:direction terms. Allowed fields: customerName, phone, email, birthDate, createdAt",
            defaultValue = "customerName:asc", example = "createdAt:desc,customerName:asc")
    private String sort = "customerName:asc";

    @Schema(description = "Filter by account enabled status", example = "true")
    private Boolean accountStatus;

    @Size(max = 100)
    @Schema(description = "Comma-separated exact gender values, case-insensitive", example = "MALE,FEMALE")
    private List<String> genders;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Inclusive lower account creation-time bound", type = "string", format = "date-time",
            example = "2026-09-01T00:00:00")
    private LocalDateTime createdFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Inclusive upper account creation-time bound", type = "string", format = "date-time",
            example = "2026-09-30T23:59:59")
    private LocalDateTime createdTo;
}
