package com.project.BookCarOnline.identity.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    
    @NotBlank(message = "Firebase Token không được để trống")
    String firebaseToken;
    
    @NotBlank(message = "Mật khẩu mới không được để trống")
    String newPassword;
}
