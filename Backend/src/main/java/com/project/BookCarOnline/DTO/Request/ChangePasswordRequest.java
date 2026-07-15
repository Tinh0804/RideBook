package com.project.BookCarOnline.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    @NotBlank(message = "Mật khẩu cũ không được để trống")
    String oldPassword;
    
    @NotBlank(message = "Mật khẩu mới không được để trống")
    String newPassword;
}
