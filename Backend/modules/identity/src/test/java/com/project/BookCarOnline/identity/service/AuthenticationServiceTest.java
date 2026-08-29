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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
                .build();

        service = new AuthenticationService(
                accountRepository,
                redisTemplate,
                customerRepository,
                driverRepository,
                accountMapper,
                passwordEncoder
        );
        service.SIGNER_KEY = "0123456789012345678901234567890123456789012345678901234567890123";
        service.VALID_DURATION = 3600;
        service.REFRESHABLE_DURATION = 36000;

    }

    @Test
    void refreshUsesRefreshTokenExpirationAndReturnsUsableRotation() throws Exception {
        Customer customer = Customer.builder().customerId("customer-id").build();
        when(customerRepository.findByAccountId("account-id")).thenReturn(Optional.of(customer));
        when(accountRepository.findById("account-id")).thenReturn(Optional.of(account));
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String refreshToken = service.generateRefreshToken(account);
        service.VALID_DURATION = -1;
        SecurityContextHolder.clearContext();

        AuthenticationResponse response = service.refreshToken(refreshToken);
        AuthenticationResponse rotatedResponse = service.refreshToken(response.getRefreshToken());

        assertTrue(response.isSuccess());
        assertTrue(rotatedResponse.isSuccess());
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
