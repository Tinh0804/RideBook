package com.project.BookCarOnline.DTO.Request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateRatingRequest {

    @NotBlank(message = "ID chuyến đi không được để trống")
    String bookingId;

    @Min(value = 1, message = "Điểm đánh giá phải từ 1 đến 5")
    @Max(value = 5, message = "Điểm đánh giá phải từ 1 đến 5")
    double rating;

    String feedback;
}
