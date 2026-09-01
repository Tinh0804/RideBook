package com.project.BookCarOnline.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "DriverDetailResponse", description = "Thông tin tài xế dành cho admin")
public class DriverDetailResponse {

    @Schema(description = "Driver ID")
    String driverId;
    @Schema(description = "Tên tài xế")
    String driverName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày sinh", type = "string", format = "date", nullable = true)
    Date birthDate;

    @Schema(description = "Số căn cước", nullable = true)
    String citizenId;
    @Schema(description = "Giấy phép lái xe", nullable = true)
    String drivingLicense;
    @Schema(description = "Hồ sơ tư pháp", nullable = true)
    String criminalRecord;
    @Schema(description = "Số điện thoại")
    String phone;
    @Schema(description = "Email", format = "email", nullable = true)
    String email;
    @Schema(description = "Biển số xe", nullable = true)
    String licensePlate;
    @Schema(description = "Tên xe", nullable = true)
    String vehicleName;
    @Schema(description = "URL avatar", nullable = true)
    String avatar;
    @Schema(description = "Trạng thái hoạt động", nullable = true)
    Boolean activityStatus;
    @Schema(description = "Giới tính", nullable = true)
    String gender;
    @Schema(description = "Địa chỉ", nullable = true)
    String address;
    @Schema(description = "Khu vực", nullable = true)
    String area;
    @Schema(description = "Điểm đánh giá", minimum = "0", maximum = "5", nullable = true)
    Double score;
    
    // Location
    @Schema(description = "Vĩ độ hiện tại", nullable = true)
    Double currentLat;
    @Schema(description = "Kinh độ hiện tại", nullable = true)
    Double currentLng;

    // Vehicle Type Info
    @Schema(description = "Vehicle type ID", nullable = true)
    String vehicleTypeId;
    @Schema(description = "Tên loại xe", nullable = true)
    String vehicleTypeName;
    @Schema(description = "URL icon loại xe", nullable = true)
    String vehicleTypeIcon;
    @Schema(description = "Đơn giá mỗi km", nullable = true)
    Double pricePerKm;


    // Account Status
    @Schema(description = "Thông tin tài khoản")
    AccountResponse account;
}
