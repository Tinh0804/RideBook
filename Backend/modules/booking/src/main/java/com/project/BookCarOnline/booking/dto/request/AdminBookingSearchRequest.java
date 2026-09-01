package com.project.BookCarOnline.booking.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AdminBookingSearchRequest", description = "Bộ lọc tìm kiếm booking dành cho admin")
public class AdminBookingSearchRequest extends AdminBookingFilter {

    @Min(0)
    @Schema(description = "Trang hiện tại, bắt đầu từ 0", defaultValue = "0", minimum = "0")
    private int page = 0;

    @Min(1)
    @Max(100)
    @Schema(description = "Số phần tử mỗi trang", defaultValue = "20", minimum = "1", maximum = "100")
    private int size = 20;

    public Pageable toPageable() {
        validateRanges();
        return PageRequest.of(page, size, parseSort());
    }
}
