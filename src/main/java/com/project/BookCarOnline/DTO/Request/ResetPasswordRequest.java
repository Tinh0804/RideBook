package com.project.BookCarOnline.DTO.Request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    
    @NotBlank(message = "Số điện thoại không được để trống")
    String phone;
    
    @NotBlank(message = "Mật khẩu mới không được để trống")
    String newPassword;
}
