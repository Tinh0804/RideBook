package com.project.BookCarOnline.DTO.Request;

import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthenticationRequest {
    String userName;
//        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
//             message = "Mật khẩu phải chứa ít nhất một chữ số, một chữ cái in thường, một chữ cái in hoa, một ký tự đặc biệt và không chứa khoảng trắng")
    String passWord;
    String roleName;
}
