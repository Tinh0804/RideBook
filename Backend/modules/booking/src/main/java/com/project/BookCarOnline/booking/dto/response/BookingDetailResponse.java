package com.project.BookCarOnline.booking.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.BookCarOnline.booking.entity.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "BookingDetailResponse", description = "Chi tiết booking dành cho admin")
public class BookingDetailResponse {

    @Schema(description = "Booking ID")
    String bookingId;
    @Schema(description = "Customer ID")
    String customerId;
    @Schema(description = "Tên khách hàng")
    String customerName;
    @Schema(description = "Số điện thoại khách hàng")
    String customerPhone;
    @Schema(description = "Driver ID", nullable = true)
    String driverId;
    @Schema(description = "Tên tài xế", nullable = true)
    String driverName;
    @Schema(description = "Số điện thoại tài xế", nullable = true)
    String driverPhone;
    @Schema(description = "Tên loại xe")
    String vehicleTypeName;
    @Schema(description = "URL icon loại xe")
    String vehicleTypeIcon;
    @Schema(description = "Biển số xe", nullable = true)
    String licensePlate;
    @Schema(description = "Điểm đón")
    String pickupLocation;
    @Schema(description = "Điểm đến")
    String dropoffLocation;
    @Schema(description = "Vĩ độ điểm đón")
    Double pickupLat;
    @Schema(description = "Kinh độ điểm đón")
    Double pickupLng;
    @Schema(description = "Vĩ độ điểm đến")
    Double dropoffLat;
    @Schema(description = "Kinh độ điểm đến")
    Double dropoffLng;
    @Schema(description = "Giá gốc", format = "double")
    Double originalPrice;
    @Schema(description = "Giá cuối cùng", format = "double")
    Double totalPrice;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian tạo booking", type = "string", example = "2026-09-01 10:30:00")
    Timestamp bookingTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian đón", type = "string", nullable = true)
    Timestamp pickupTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian đến", type = "string", nullable = true)
    Timestamp arrivalTime;
    
    @Schema(description = "Trạng thái booking")
    BookingStatus bookingStatus;
    @Schema(description = "Khoảng cách chuyến đi", format = "double")
    Double distance;
    @Schema(description = "Thời lượng chuyến đi", format = "double", nullable = true)
    Double duration;
    @Schema(description = "Phương thức thanh toán", nullable = true)
    String paymentMethod;
    @Schema(description = "Trạng thái đã thanh toán", nullable = true)
    Boolean paymentStatus;
    @Schema(description = "Danh sách khuyến mãi đã áp dụng")
    List<BookingPromotionDTO> appliedPromotions;
    @Schema(description = "Điểm đánh giá chuyến đi", minimum = "1", maximum = "5", nullable = true)
    Double rating;
    @Schema(description = "Nội dung đánh giá", nullable = true)
    String review;
}
