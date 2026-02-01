package com.project.BookCarOnline.DTO.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateDriverRequest {

    @NotBlank(message = "Tên tài xế không được để trống")
    String driverName;

    @NotNull(message = "Ngày sinh không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    Date birthDate;

    @NotBlank(message = "CCCD không được để trống")
    @Pattern(regexp = "^\\d{12}$", message = "CCCD phải có 12 chữ số")
    String citizenId;

    @NotBlank(message = "GPLX không được để trống")
    String drivingLicense;

    String criminalRecord; // Lý lịch tư pháp (LLTP)

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại không hợp lệ")
    String phone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    String email;

    @NotBlank(message = "Biển số xe không được để trống")
    String licensePlate;

    @NotBlank(message = "Tên xe không được để trống")
    String vehicleName;

    @NotBlank(message = "Giới tính không được để trống")
    String gender;

    @NotBlank(message = "Địa chỉ không được để trống")
    String address;

    @NotBlank(message = "Khu vực không được để trống")
    String area;

    @NotBlank(message = "Loại xe không được để trống")
    String vehicleTypeId;

    // For account creation
    @NotBlank(message = "Mật khẩu không được để trống")
    String password;

    // For file uploads (will be handled separately via multipart)
    String avatar;
    String citizenIdImage;
    String drivingLicenseImage;
}
