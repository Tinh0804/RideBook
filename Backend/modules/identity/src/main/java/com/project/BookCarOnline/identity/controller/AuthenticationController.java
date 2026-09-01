package com.project.BookCarOnline.identity.controller;

import com.nimbusds.jose.JOSEException;
import com.project.BookCarOnline.shared.dto.APIResponse;
import com.project.BookCarOnline.identity.dto.request.AuthenticationRequest;
import com.project.BookCarOnline.identity.dto.request.ExchangeTokenRequest;
import com.project.BookCarOnline.identity.dto.request.EmailVerificationRequest;
import com.project.BookCarOnline.identity.dto.request.ResendEmailVerificationRequest;
import com.project.BookCarOnline.identity.dto.request.ResetPasswordRequest;
import com.project.BookCarOnline.identity.dto.response.AuthenticationResponse;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.identity.service.AuthenticationService;
import com.project.BookCarOnline.identity.service.OAuth2ExchangeService;
import com.project.BookCarOnline.shared.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class AuthenticationController {
    AuthenticationService service;
    OAuth2ExchangeService oAuth2ExchangeService;

    @PostMapping("/email-verification/verify")
    APIResponse<Boolean> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        service.verifyEmail(request.getToken());
        return APIResponse.<Boolean>builder()
                .result(true)
                .status(200)
                .message("Email verified successfully")
                .build();
    }

    @PostMapping("/email-verification/resend")
    APIResponse<Boolean> resendEmailVerification(
            @Valid @RequestBody ResendEmailVerificationRequest request) {
        service.resendEmailVerification(request.getUserName());
        return APIResponse.<Boolean>builder()
                .result(true)
                .status(200)
                .message("If the account is eligible, a verification email has been sent")
                .build();
    }


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
    APIResponse<Boolean> authenticateIntrospect() throws JOSEException {
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
    APIResponse<Boolean> logout(@RequestParam("refreshToken") String refreshToken) throws JOSEException {
        service.logout(refreshToken);
        return APIResponse.<Boolean>builder()
                .result(true)
                .status(200)
                .message("Logout successful")
                .build();
    }
    @PostMapping("/refresh-token")
    APIResponse<AuthenticationResponse> refreshToken(@RequestParam("refreshToken") String refreshToken) throws JOSEException {
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


    @GetMapping("/check-phone")
    public APIResponse<Boolean> checkPhone(@RequestParam("phone") String phone) {
        boolean exists = service.checkPhoneExist(phone);
        return APIResponse.<Boolean>builder()
                .result(exists)
                .status(exists ? 200 : 404)
                .message(exists ? "Phone exists" : "Phone does not exist")
                .build();
    }

    @PutMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Boolean> changePassword(@Valid @RequestBody com.project.BookCarOnline.identity.dto.request.ChangePasswordRequest request) {
        service.changePassword(request.getOldPassword(), request.getNewPassword());
        return APIResponse.<Boolean>builder()
                .result(true)
                .status(200)
                .message("Mật khẩu đã được thay đổi thành công")
                .build();
    }

    @PutMapping("/reset-password")
    public APIResponse<Boolean> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        service.resetPassword(request.getFirebaseToken(), request.getNewPassword());
        return APIResponse.<Boolean>builder()
                .result(true)
                .status(200)
                .message("Password updated successfully")
                .build();
    }

}
