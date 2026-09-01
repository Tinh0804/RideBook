package com.project.BookCarOnline.identity.controller;

import com.project.BookCarOnline.identity.service.AuthenticationService;
import com.project.BookCarOnline.identity.service.OAuth2ExchangeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    AuthenticationService authenticationService;
    @Mock
    OAuth2ExchangeService oAuth2ExchangeService;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AuthenticationController(authenticationService, oAuth2ExchangeService))
                .build();
    }

    @Test
    void verifyEmailConsumesTokenThroughAuthenticationService() throws Exception {
        mockMvc.perform(post("/auth/email-verification/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"valid-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));

        verify(authenticationService).verifyEmail("valid-token");
    }

    @Test
    void resendEmailVerificationUsesGenericSuccessResponse() throws Exception {
        mockMvc.perform(post("/auth/email-verification/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"0912345678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));

        verify(authenticationService).resendEmailVerification("0912345678");
    }
}
