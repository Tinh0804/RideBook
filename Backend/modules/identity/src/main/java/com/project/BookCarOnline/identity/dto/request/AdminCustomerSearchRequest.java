package com.project.BookCarOnline.identity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name = "AdminCustomerSearchRequest", description = "Paginated admin customer search request")
public class AdminCustomerSearchRequest extends AdminCustomerFilter {

    @Min(0)
    @Schema(description = "Zero-based page number", minimum = "0", defaultValue = "0", example = "0")
    private int page = 0;

    @Min(1)
    @Max(100)
    @Schema(description = "Page size", minimum = "1", maximum = "100", defaultValue = "20", example = "20")
    private int size = 20;
}
