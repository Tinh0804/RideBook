package com.project.BookCarOnline.booking.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDetailResponse {

    String bookingId;
    String customerId;
    String customerName;
    String customerPhone;
    String driverId;
    String driverName;
    String driverPhone;
    String vehicleTypeName;
    String vehicleTypeIcon;
    String licensePlate;
    String pickupLocation;
    String dropoffLocation;
    Double pickupLat;
    Double pickupLng;
    Double dropoffLat;
    Double dropoffLng;
    Double originalPrice;
    Double totalPrice;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Timestamp bookingTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(
            description = "Giờ đón dự kiến của chuyến hẹn giờ; null với chuyến đặt ngay",
            type = "string",
            format = "date-time",
            example = "2026-09-02T08:30:00",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    LocalDateTime scheduledAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Timestamp pickupTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Timestamp arrivalTime;
    
    BookingStatus bookingStatus;
    Double distance;
    Double duration;
    String paymentMethod;
    Boolean paymentStatus;
    List<BookingPromotionDTO> appliedPromotions;
    Integer rating;
    String review;
}
