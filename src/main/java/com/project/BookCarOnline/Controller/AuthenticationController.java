package com.project.BookCarOnline.Controller;

import com.nimbusds.jose.JOSEException;
import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.AuthenticationRequest;
import com.project.BookCarOnline.DTO.Response.AuthenticationResponse;
import com.project.BookCarOnline.Service.AuthenticationService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class AuthenticationController {
    AuthenticationService service;

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
    APIResponse<Boolean> authenticateIntrospect(@RequestHeader("Authorization") String authorizationHeader) throws ParseException, JOSEException {
        String token = authorizationHeader.replace("Bearer ", "");
        boolean isValid = service.introspect(token);
        return APIResponse.<Boolean>builder()
                .result(isValid)
                .status(isValid ? 200 : 401)
                .message(isValid ? "Token is valid" : "Token is invalid")
                .build();
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    APIResponse<Boolean> logout(@RequestHeader("Authorization") String authorizationHeader) throws ParseException, JOSEException {
        String token = authorizationHeader.substring(7);
        service.logout(token);
        return APIResponse.<Boolean>builder()
                .result(true)
                .status(200)
                .message("Logout successful")
                .build();
    }
    @PostMapping("/refresh")
    @SecurityRequirement(name = "bearerAuth")
    APIResponse<String> refreshToken(@RequestParam("refreshToken") String refreshToken) throws ParseException, JOSEException {
        String response = service.refresToken(refreshToken);
        return APIResponse.<String>builder()
                .result(response)
                .status(200)
                .message("Token refreshed successfully")
                .build();
    }

}
