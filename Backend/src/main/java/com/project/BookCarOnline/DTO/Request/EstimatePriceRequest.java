package com.project.BookCarOnline.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EstimatePriceRequest {
    @NotNull(message = "Vĩ độ điểm đón không được để trống")
    Double pickupLat;

    @NotNull(message = "Kinh độ điểm đón không được để trống")
    Double pickupLng;

    @NotNull(message = "Vĩ độ điểm đến không được để trống")
    Double dropoffLat;

    @NotNull(message = "Kinh độ điểm đến không được để trống")
    Double dropoffLng;

    String promotionCode;
}
