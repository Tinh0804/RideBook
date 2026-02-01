package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.ExchangeTokenRequest;
import com.project.BookCarOnline.DTO.Response.AuthenticationResponse;
import com.project.BookCarOnline.Service.OAuth2ExchangeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RestController
@RequestMapping("/auth/oauth2")
@Slf4j
public class OAuth2AuthController {
    OAuth2ExchangeService oAuth2ExchangeService;

//    @PostMapping("/{provider}/token")
    @PostMapping("/google")
    public APIResponse<AuthenticationResponse> exchangeTokenGoogle(@RequestBody ExchangeTokenRequest request) {
        log.info(request.getCode());
        AuthenticationResponse response = oAuth2ExchangeService.exchange(request);
        return APIResponse.<AuthenticationResponse>builder()
                .result(response)
                .status(response.isSuccess() ? 200 : 401)
                .message(response.isSuccess() ? "Token exchange successful" : "Token exchange failed")
                .build();
    }

    @PostMapping("/facebook")
    public APIResponse<AuthenticationResponse> exchangeTokenFacebook(@RequestBody ExchangeTokenRequest request){
        log.info(request.getCode());
        AuthenticationResponse response = oAuth2ExchangeService.exchange(request);
        return APIResponse.<AuthenticationResponse>builder()
                .result(response)
                .status(response.isSuccess() ? 200 : 401)
                .message(response.isSuccess() ? "Token exchange successful" : "Token exchange failed")
                .build();
    }


}
