package com.project.BookCarOnline.DTO.Response;

import com.project.BookCarOnline.Entity.VehicleType;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.UUID;

@Data
@Builder
public class DriverResponse {
    private UUID id;
    private String name;
    private String phoneNumber;
    private String address;
    private String role; // Assuming role is a string, you can change it to an enum if needed
    private String licenseNumber; // Assuming license number is a string, adjust as necessary
    private VehicleType vehicleType; // Assuming vehicle type is a string, adjust as necessary
    private String vehicleNumber; // Assuming vehicle number is a string, adjust as necessary
    private Double currentLat;
    private Double currentLng;

}
