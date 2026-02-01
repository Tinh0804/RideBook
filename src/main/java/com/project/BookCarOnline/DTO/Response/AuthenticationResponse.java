package com.project.BookCarOnline.DTO.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project.BookCarOnline.Entity.Account;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data //có cả @Getter,@Setter,@NoArgsConstructor
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthenticationResponse {
    boolean success;
    String token;
    String refreshToken;
    AccountResponse account;
}
