package com.project.BookCarOnline.DTO.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayPaymentRequest {

    @NotBlank(message = "Booking ID không được để trống")
    String bookingId;

    @NotNull(message = "Số tiền không được để trống")
    @Min(value = 10000, message = "Số tiền tối thiểu 10,000 VND")
    Long amount;

    @NotBlank(message = "Mô tả không được để trống")
    String orderInfo;

    String returnUrl;

    String locale; // "vn" or "en"
}
