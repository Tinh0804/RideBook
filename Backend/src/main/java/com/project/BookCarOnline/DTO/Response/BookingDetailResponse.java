package com.project.BookCarOnline.DTO.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

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
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Timestamp pickupTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Timestamp arrivalTime;
    
    BookingStatus bookingStatus;
    Double distance;
    Double duration;
    String paymentMethod;
    Boolean paymentStatus;
    String promotionCode;
    Integer rating;
    String review;
}
