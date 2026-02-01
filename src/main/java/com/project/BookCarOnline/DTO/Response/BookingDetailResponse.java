package com.project.BookCarOnline.DTO.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    Double totalPrice;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Timestamp bookingTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Timestamp pickupTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Timestamp arrivalTime;
    
    String bookingStatus;
    Double distance;
    Double duration;
    String paymentMethod;
    String promotionCode;
}
