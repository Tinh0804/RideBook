package com.project.BookCarOnline.identity.service;

import com.project.BookCarOnline.identity.dto.response.AuthenticationResponse;
import com.project.BookCarOnline.identity.entity.Account;
import com.project.BookCarOnline.identity.entity.Customer;
import com.project.BookCarOnline.identity.entity.Role;
import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;
import com.project.BookCarOnline.identity.mapper.AccountMapper;
import com.project.BookCarOnline.identity.repository.AccountRepository;
import com.project.BookCarOnline.identity.repository.CustomerRepository;
import com.project.BookCarOnline.identity.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

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
        account = Account.builder().accountId("account-id").roleNo(role).build();
        Customer customer = Customer.builder().customerId("customer-id").build();

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

        when(customerRepository.findByAccountId("account-id")).thenReturn(Optional.of(customer));
        when(accountRepository.findById("account-id")).thenReturn(Optional.of(account));
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void refreshUsesRefreshTokenExpirationWithoutRequiringAccessToken() throws Exception {
        String refreshToken = service.generateToken(account, service.REFRESHABLE_DURATION);
        service.VALID_DURATION = -1;
        SecurityContextHolder.clearContext();

        AuthenticationResponse response = service.refreshToken(refreshToken);

        assertTrue(response.isSuccess());
    }
}
