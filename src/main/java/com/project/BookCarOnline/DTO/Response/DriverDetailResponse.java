package com.project.BookCarOnline.DTO.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DriverDetailResponse {

    String driverId;
    String driverName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    Date birthDate;

    String citizenId;
    String drivingLicense;
    String criminalRecord;
    String phone;
    String email;
    String licensePlate;
    String vehicleName;
    String avatar;
    Boolean activityStatus;
    String gender;
    String address;
    String area;
    
    // Location
    Double currentLat;
    Double currentLng;

    // Vehicle Type Info
    String vehicleTypeId;
    String vehicleTypeName;
    Double pricePerKm;

    // Account Status
    AccountResponse account;
}
