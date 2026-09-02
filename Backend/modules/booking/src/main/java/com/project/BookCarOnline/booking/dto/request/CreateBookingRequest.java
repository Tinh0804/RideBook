package com.project.BookCarOnline.booking.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Dữ liệu tạo chuyến tức thời hoặc chuyến hẹn giờ")
public class CreateBookingRequest {

    @NotBlank(message = "ID khách hàng không được để trống")
    String customerId;

    String paymentId;

    List<String> promotionCodes; // Hỗ trợ nhiều mã giảm giá

    String quoteId;

    @NotBlank(message = "Điểm đón không được để trống")
    String pickupLocation;

    @NotBlank(message = "Điểm trả không được để trống")
    String dropoffLocation;

    Double pickupLat;
    Double pickupLng;
    Double dropoffLat;
    Double dropoffLng;

    @NotNull(message = "Khoảng cách không được để trống")
    @Positive(message = "Khoảng cách phải lớn hơn 0")
    Double distance;

    @NotBlank(message = "Loại xe không được để trống")
    String vehicleTypeId;

    // ONLINE | CASH
    String paymentMethod;

    String returnUrl;

    @Schema(
            description = "Giờ đón dự kiến theo múi giờ Asia/Ho_Chi_Minh. Bỏ trống để đặt chuyến ngay; phải nằm trong tương lai khi được cung cấp.",
            type = "string",
            format = "date-time",
            example = "2026-09-02T08:30:00",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    LocalDateTime scheduledAt;

}
