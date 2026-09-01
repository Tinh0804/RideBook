package com.project.BookCarOnline.identity.service;

import com.project.BookCarOnline.identity.dto.request.AuthenticationRequest;
import com.project.BookCarOnline.identity.dto.response.AccountResponse;
import com.project.BookCarOnline.identity.dto.response.AuthenticationResponse;
import com.project.BookCarOnline.identity.entity.Account;
import com.project.BookCarOnline.identity.entity.Customer;
import com.project.BookCarOnline.identity.entity.Role;
import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;
import com.project.BookCarOnline.identity.mapper.AccountMapper;
import com.project.BookCarOnline.identity.repository.AccountRepository;
import com.project.BookCarOnline.identity.repository.CustomerRepository;
import com.project.BookCarOnline.identity.repository.DriverRepository;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock
    AccountRepository accountRepository;
    @Mock
    RedisTemplate<String, Object> redisTemplate;
    @Mock
    ValueOperations<String, Object> valueOperations;
    @Mock
    CustomerRepository customerRepository;
    @Mock
    DriverRepository driverRepository;
    @Mock
    AccountMapper accountMapper;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    EmailVerificationService emailVerificationService;
    @Mock
    LoginAttemptService loginAttemptService;
    @Mock
    RefreshTokenService refreshTokenService;

    AuthenticationService service;
    Account account;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().roleName(PredefinedRole.CUSTOMER).build();
        account = Account.builder()
                .accountId("account-id")
                .userName("customer@example.com")
                .passWord("encoded-password")
                .roleNo(role)
                .accountStatus(true)
                .emailVerified(true)
                .build();

        service = new AuthenticationService(
                accountRepository,
                redisTemplate,
                customerRepository,
                driverRepository,
                accountMapper,
                emailVerificationService,
                loginAttemptService,
                refreshTokenService,
                passwordEncoder
        );
        service.SIGNER_KEY = "0123456789012345678901234567890123456789012345678901234567890123";
        service.VALID_DURATION = 3600;
        service.REFRESHABLE_DURATION = 36000;

    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticateRejectsLockedPrincipalBeforeAccountLookup() {
        when(loginAttemptService.isLocked("customer@example.com")).thenReturn(true);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.authenticate(AuthenticationRequest.builder()
                        .userName("customer@example.com")
                        .passWord("password")
                        .roleName("CUSTOMER")
                        .build()));

        assertEquals(ErrorCode.ACCOUNT_TEMPORARILY_LOCKED, exception.getErrorCode());
        verifyNoInteractions(accountRepository);
    }

    @Test
    void authenticateCountsUnknownPrincipalWithoutRevealingAccountExistence() {
        when(accountRepository.findByUserName("unknown@example.com")).thenReturn(Optional.empty());
        when(loginAttemptService.recordFailure("unknown@example.com")).thenReturn(false);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.authenticate(AuthenticationRequest.builder()
                        .userName("unknown@example.com")
                        .passWord("password")
                        .roleName("CUSTOMER")
                        .build()));

        assertEquals(ErrorCode.USERNAME_OR_PASSWORD_INVALID, exception.getErrorCode());
        verify(loginAttemptService).recordFailure("unknown@example.com");
    }

    @Test
    void successfulAuthenticationClearsPreviousFailures() {
        authenticateAccount();

        verify(loginAttemptService).recordSuccess("customer@example.com");
    }

    @Test
    void fifthInvalidPasswordLocksAccount() {
        when(accountRepository.findByUserName("customer@example.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);
        when(loginAttemptService.recordFailure("customer@example.com")).thenReturn(true);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.authenticate(AuthenticationRequest.builder()
                        .userName("customer@example.com")
                        .passWord("wrong-password")
                        .roleName("CUSTOMER")
                        .build()));

        assertEquals(ErrorCode.ACCOUNT_TEMPORARILY_LOCKED, exception.getErrorCode());
    }

    @Test
    void authenticateRejectsUnverifiedLocalAccount() {
        account.setEmailVerified(false);
        when(accountRepository.findByUserName("customer@example.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.authenticate(AuthenticationRequest.builder()
                        .userName("customer@example.com")
                        .passWord("password")
                        .roleName("CUSTOMER")
                        .build()));

        assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, exception.getErrorCode());
    }

    @Test
    void refreshUsesRefreshTokenExpirationAndReturnsUsableRotation() throws Exception {
        Customer customer = Customer.builder().customerId("customer-id").build();
        when(customerRepository.findByAccountId("account-id")).thenReturn(Optional.of(customer));
        when(accountRepository.findById("account-id")).thenReturn(Optional.of(account));
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(refreshTokenService.consume(anyString(), eq("account-id")))
                .thenReturn(true, true);

        String refreshToken = service.generateRefreshToken(account);
        service.VALID_DURATION = -1;
        SecurityContextHolder.clearContext();

        AuthenticationResponse response = service.refreshToken(refreshToken);
        AuthenticationResponse rotatedResponse = service.refreshToken(response.getRefreshToken());

        assertTrue(response.isSuccess());
        assertTrue(rotatedResponse.isSuccess());
    }

    @Test
    void issuedRefreshTokenIsRegisteredWithItsJwtExpiration() throws Exception {
        AuthenticationResponse authentication = authenticateAccount();

        verify(refreshTokenService).store(
                eq(authentication.getRefreshToken()),
                eq("account-id"),
                any(Instant.class));
    }

    @Test
    void refreshTokenCannotBeReusedAfterSuccessfulRotation() throws Exception {
        Customer customer = Customer.builder().customerId("customer-id").build();
        when(customerRepository.findByAccountId("account-id")).thenReturn(Optional.of(customer));
        when(accountRepository.findById("account-id")).thenReturn(Optional.of(account));
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String originalRefreshToken = service.generateRefreshToken(account);
        when(refreshTokenService.consume(originalRefreshToken, "account-id"))
                .thenReturn(true, false);

        AuthenticationResponse rotated = service.refreshToken(originalRefreshToken);
        AppException replay = assertThrows(
                AppException.class,
                () -> service.refreshToken(originalRefreshToken));

        assertTrue(rotated.isSuccess());
        assertEquals(ErrorCode.TOKEN_BLACKLISTED, replay.getErrorCode());
    }

    @Test
    void logoutConsumesRefreshTokenAndBlacklistsBothTokens() throws Exception {
        Customer customer = Customer.builder().customerId("customer-id").build();
        when(customerRepository.findByAccountId("account-id")).thenReturn(Optional.of(customer));
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(refreshTokenService.consume(anyString(), eq("account-id"))).thenReturn(true);

        String accessToken = service.generateAccessToken(account);
        String refreshToken = service.generateRefreshToken(account);
        Jwt jwt = Jwt.withTokenValue(accessToken)
                .header("alg", "HS512")
                .subject("account-id")
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        service.logout(refreshToken);

        verify(refreshTokenService).consume(refreshToken, "account-id");
        verify(valueOperations, times(2)).set(
                anyString(), anyString(), anyLong(), eq(java.util.concurrent.TimeUnit.MILLISECONDS));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "null", "not-a-jwt"})
    void refreshRejectsMalformedTokenAsInvalidToken(String token) {
        AppException exception = assertThrows(
                AppException.class,
                () -> service.refreshToken(token)
        );

        assertEquals(ErrorCode.INVALID_TOKEN, exception.getErrorCode());
    }

    @Test
    void refreshRejectsAccessToken() throws Exception {
        AuthenticationResponse authentication = authenticateAccount();

        AppException exception = assertThrows(
                AppException.class,
                () -> service.refreshToken(authentication.getToken())
        );

        assertEquals(ErrorCode.INVALID_TOKEN, exception.getErrorCode());
    }

    @Test
    void introspectRejectsRefreshToken() throws Exception {
        AuthenticationResponse authentication = authenticateAccount();

        assertFalse(service.introspect(authentication.getRefreshToken()));
    }

    @Test
    void refreshRejectsAccountWhoseEmailIsNotVerified() throws Exception {
        Customer customer = Customer.builder().customerId("customer-id").build();
        when(customerRepository.findByAccountId("account-id")).thenReturn(Optional.of(customer));
        when(accountRepository.findById("account-id")).thenReturn(Optional.of(account));
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        String refreshToken = service.generateRefreshToken(account);
        account.setEmailVerified(false);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.refreshToken(refreshToken));

        assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, exception.getErrorCode());
    }

    private AuthenticationResponse authenticateAccount() {
        Customer customer = Customer.builder().customerId("customer-id").build();
        when(accountRepository.findByUserName("customer@example.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(customerRepository.findByAccountId("account-id")).thenReturn(Optional.of(customer));
        when(accountMapper.toAccountResponse(account)).thenReturn(AccountResponse.builder().build());

        return service.authenticate(AuthenticationRequest.builder()
                .userName("customer@example.com")
                .passWord("password")
                .roleName("CUSTOMER")
                .build());
    }
}
