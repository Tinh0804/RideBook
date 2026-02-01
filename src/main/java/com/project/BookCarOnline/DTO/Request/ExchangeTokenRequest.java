package com.project.BookCarOnline.DTO.Request;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExchangeTokenRequest {
    private final String code;
    private final String clientId;
//    private final String clientSecret;
    private final String registrationId;
    private final String redirectUri;
    private final String grantType;
}
