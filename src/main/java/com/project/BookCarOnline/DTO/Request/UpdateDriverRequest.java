package com.project.BookCarOnline.DTO.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.UUID;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateDriverRequest {


    String driverName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    Date birthDate;

    @Pattern(regexp = "^\\d{12}$", message = "CCCD phải có 12 chữ số")
    String citizenId;

    String drivingLicense;

    String criminalRecord;

    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại không hợp lệ")
    String phone;

    @Email(message = "Email không hợp lệ")
    String email;

    String licensePlate;

    String vehicleName;

    String gender;

    String address;

    String area;

    String vehicleTypeId;

    Boolean activityStatus;

    // For file uploads
    String avatar;
    String citizenIdImage;
    String drivingLicenseImage;

    // Location tracking
    Double currentLat;
    Double currentLng;
}
