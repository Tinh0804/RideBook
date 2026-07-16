package com.project.BookCarOnline.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminChangePasswordRequest {
    @NotBlank(message = "Mật khẩu mới không được để trống")
    String newPassword;
}
