package com.project.BookCarOnline.identity.service;

import com.project.BookCarOnline.identity.dto.request.RegisterCustomerRequest;
import com.project.BookCarOnline.identity.dto.response.CustomerResponse;
import com.project.BookCarOnline.identity.entity.Account;
import com.project.BookCarOnline.identity.entity.Customer;
import com.project.BookCarOnline.identity.entity.Role;
import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;
import com.project.BookCarOnline.identity.entity.enums.Provider;
import com.project.BookCarOnline.identity.mapper.CustomerMapper;
import com.project.BookCarOnline.identity.repository.AccountRepository;
import com.project.BookCarOnline.identity.repository.CustomerRepository;
import com.project.BookCarOnline.identity.repository.RoleRepository;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    AccountRepository accountRepository;

    @Mock
    CustomerRepository customerRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    CustomerMapper mapper;

    @Mock
    FirebaseService firebaseService;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    EmailVerificationService emailVerificationService;

    @InjectMocks
    CustomerService customerService;

    Role customerRole;
    Account sampleAccount;
    Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        customerRole = Role.builder()
                .roleName(PredefinedRole.CUSTOMER)
                .description("Customer role")
                .build();

        sampleAccount = Account.builder()
                .accountId("acc-1")
                .userName("0912345678")
                .passWord("encodedPassword")
                .roleNo(customerRole)
                .provider(Provider.LOCAL)
                .accountStatus(true)
                .build();

        sampleCustomer = Customer.builder()
                .customerId("cust-1")
                .customerName("Nguyen Van A")
                .phone("0912345678")
                .address("123 Ha Noi")
                .account(sampleAccount)
                .build();
    }

    @Test
    void createCustomer_Success_CreatesAccountAndCustomer() {
        RegisterCustomerRequest request = RegisterCustomerRequest.builder()
                .userName("0912345678")
                .passWord("password123")
                .name("Nguyen Van A")
                .phoneNumber("0912345678")
                .email("customer@example.com")
                .address("123 Ha Noi")
                .confirm("password123")
                .build();

        when(accountRepository.existsByUserName("0912345678")).thenReturn(false);
        when(roleRepository.findByRoleName(PredefinedRole.CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        CustomerResponse expectedResponse = CustomerResponse.builder()
                .customerId("cust-1")
                .customerName("Nguyen Van A")
                .phone("0912345678")
                .build();
        when(mapper.toCustomerResponse(any(Customer.class))).thenReturn(expectedResponse);

        CustomerResponse response = customerService.createCustomer(request);

        assertNotNull(response);
        assertEquals("cust-1", response.getCustomerId());
        assertEquals("Nguyen Van A", response.getCustomerName());

        verify(accountRepository).save(any(Account.class));
        verify(customerRepository).save(any(Customer.class));

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertFalse(accountCaptor.getValue().isEmailVerified());

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertEquals("customer@example.com", customerCaptor.getValue().getEmail());
        verify(emailVerificationService)
                .sendVerificationEmail(accountCaptor.getValue(), "customer@example.com");
    }

    @Test
    void createCustomer_DuplicateUsername_ThrowsResponseStatusExceptionBadRequest() {
        RegisterCustomerRequest request = RegisterCustomerRequest.builder()
                .userName("0912345678")
                .passWord("password123")
                .name("Nguyen Van A")
                .phoneNumber("0912345678")
                .address("123 Ha Noi")
                .confirm("password123")
                .build();

        when(accountRepository.existsByUserName("0912345678")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> customerService.createCustomer(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Số điện thoại đã được đăng ký"));
    }

    @Test
    void getCustomerResponseById_Found_ReturnsResponse() {
        when(customerRepository.findById("cust-1")).thenReturn(Optional.of(sampleCustomer));

        CustomerResponse expectedResponse = CustomerResponse.builder()
                .customerId("cust-1")
                .customerName("Nguyen Van A")
                .phone("0912345678")
                .build();
        when(mapper.toCustomerResponse(sampleCustomer)).thenReturn(expectedResponse);

        CustomerResponse response = customerService.getCustomerResponseById("cust-1");

        assertNotNull(response);
        assertEquals("cust-1", response.getCustomerId());
        assertEquals("Nguyen Van A", response.getCustomerName());
    }

    @Test
    void getCustomerResponseById_NotFound_ThrowsAppException() {
        when(customerRepository.findById("unknown-id")).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> customerService.getCustomerResponseById("unknown-id"));

        assertEquals(ErrorCode.USER_NOT_EXITED, exception.getErrorCode());
    }

    @Test
    void toggleCustomerAccountStatus_ExistingCustomer_TogglesAndReturnsNewStatus() {
        sampleAccount.setAccountStatus(true);
        when(customerRepository.findById("cust-1")).thenReturn(Optional.of(sampleCustomer));

        Boolean newStatus = customerService.toggleCustomerAccountStatus("cust-1");

        assertFalse(newStatus);
        assertFalse(sampleAccount.getAccountStatus());
        verify(accountRepository).save(sampleAccount);
    }

    @Test
    void changePasswordByAdmin_Success_EncodesNewPasswordAndSaves() {
        when(customerRepository.findById("cust-1")).thenReturn(Optional.of(sampleCustomer));
        when(passwordEncoder.encode("newSecretPass")).thenReturn("encodedNewSecret");

        customerService.changePasswordByAdmin("cust-1", "newSecretPass");

        assertEquals("encodedNewSecret", sampleAccount.getPassWord());
        verify(accountRepository).save(sampleAccount);
    }
}
