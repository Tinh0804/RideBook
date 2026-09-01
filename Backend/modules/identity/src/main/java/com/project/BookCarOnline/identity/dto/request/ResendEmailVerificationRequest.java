package com.project.BookCarOnline.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResendEmailVerificationRequest {

    @NotBlank(message = "Username không được để trống")
    private String userName;
}
