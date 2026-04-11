package com.project.BookCarOnline.Controller;

import com.nimbusds.jose.JOSEException;
import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.AuthenticationRequest;
import com.project.BookCarOnline.DTO.Request.ExchangeTokenRequest;
import com.project.BookCarOnline.DTO.Response.AuthenticationResponse;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Service.AuthenticationService;
import com.project.BookCarOnline.Service.OAuth2ExchangeService;
import com.project.BookCarOnline.Utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class AuthenticationController {
    AuthenticationService service;
    OAuth2ExchangeService oAuth2ExchangeService;


    @PostMapping("/login")
    APIResponse<AuthenticationResponse> authenticateLogin(@RequestBody AuthenticationRequest request){
       AuthenticationResponse response =  service.authenticate(request);

       return APIResponse.<AuthenticationResponse>builder()
               .result(response)
               .status(response.isSuccess() ? 200 : 401)
                .message(response.isSuccess() ? "Login successful" : "Login failed")
               .build();
    }

    @PostMapping("/introspect")
    @SecurityRequirement(name = "bearerAuth")
    APIResponse<Boolean> authenticateIntrospect() throws ParseException, JOSEException {
        String token = SecurityUtils.getCurrentToken().orElseThrow(()->new AppException(ErrorCode.TOKEN_NOT_FOUND));
        boolean isValid = service.introspect(token);
        return APIResponse.<Boolean>builder()
                .result(isValid)
                .status(isValid ? 200 : 401)
                .message(isValid ? "Token is valid" : "Token is invalid")
                .build();
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    APIResponse<Boolean> logout(@RequestParam("refreshToken") String refreshToken) throws ParseException, JOSEException {
        service.logout(refreshToken);
        return APIResponse.<Boolean>builder()
                .result(true)
                .status(200)
                .message("Logout successful")
                .build();
    }
    @PostMapping("/refresh-token")
    @SecurityRequirement(name = "bearerAuth")
    APIResponse<AuthenticationResponse> refreshToken(@RequestParam("refreshToken") String refreshToken) throws ParseException, JOSEException {
        AuthenticationResponse response = service.refreshToken(refreshToken);
        return APIResponse.<AuthenticationResponse>builder()
                .result(response)
                .status(200)
                .message("Token refreshed successfully")
                .build();
    }

    @PostMapping("oauth2/external-login")
    public APIResponse<AuthenticationResponse> exchangeToken(@RequestBody ExchangeTokenRequest request) {
        log.info(request.getCode());
        AuthenticationResponse response = oAuth2ExchangeService.exchange(request);
        return APIResponse.<AuthenticationResponse>builder()
                .result(response)
                .status(response.isSuccess() ? 200 : 401)
                .message(response.isSuccess() ? "Token exchange successful" : "Token exchange failed")
                .build();
    }



}
