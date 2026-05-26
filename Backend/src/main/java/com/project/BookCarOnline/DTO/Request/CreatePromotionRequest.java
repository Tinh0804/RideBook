package com.project.BookCarOnline.DTO.Request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePromotionRequest {

    @NotBlank(message = "Mã khuyến mãi không được để trống")
    String promotionCode;

    @NotBlank(message = "Tên khuyến mãi không được để trống")
    String promotionName;

    @Min(value = 1, message = "Hạn mức tối thiểu là 1")
    Double discountLimit;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    Timestamp startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    Timestamp endTime;

    String applicationCondition;

    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    Integer quantity;

    String discountType;
    Double discountValue;
    Double minTripValue;
    Integer usageLimitPerUser;

    @Builder.Default
    Boolean isActive = true;
}
