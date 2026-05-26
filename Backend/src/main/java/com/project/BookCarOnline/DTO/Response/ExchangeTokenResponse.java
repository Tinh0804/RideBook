package com.project.BookCarOnline.DTO.Response;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data //có cả @Getter,@Setter,@NoArgsConstructor
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class) // Chuyển đổi tên thuộc tính sang snake_case
public class ExchangeTokenResponse {
    String accessToken;
    Long expiresIn;
    String refreshToken;
    String scope;
    String tokenType;
}
