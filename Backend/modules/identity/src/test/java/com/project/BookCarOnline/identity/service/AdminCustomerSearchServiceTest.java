package com.project.BookCarOnline.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.BookCarOnline.identity.dto.request.AdminCustomerFilter;
import com.project.BookCarOnline.identity.dto.request.AdminCustomerSearchRequest;
import com.project.BookCarOnline.identity.dto.response.CustomerResponse;
import com.project.BookCarOnline.identity.entity.Account;
import com.project.BookCarOnline.identity.entity.Customer;
import com.project.BookCarOnline.identity.mapper.CustomerMapper;
import com.project.BookCarOnline.identity.repository.AccountRepository;
import com.project.BookCarOnline.identity.repository.CustomerRepository;
import com.project.BookCarOnline.identity.repository.RoleRepository;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminCustomerSearchServiceTest {

    @Mock
    AccountRepository accountRepository;
    @Mock
    CustomerRepository customerRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    CustomerMapper customerMapper;
    @Mock
    FirebaseService firebaseService;
    @Mock
    PasswordEncoder passwordEncoder;

    CustomerService service;

    @BeforeEach
    void setUp() {
        service = new CustomerService(
                accountRepository,
                customerRepository,
                roleRepository,
                customerMapper,
                firebaseService,
                passwordEncoder);
    }

    @Test
    void searchUsesContractPaginationAndMultiFieldSort() {
        AdminCustomerSearchRequest request = new AdminCustomerSearchRequest();
        request.setPage(2);
        request.setSize(25);
        request.setSort("createdAt:desc,customerName:asc");
        Customer customer = Customer.builder().customerId("customer-1").build();
        CustomerResponse response = CustomerResponse.builder().customerId("customer-1").build();
        when(customerRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(customer)));
        when(customerMapper.toCustomerResponse(customer)).thenReturn(response);

        Page<CustomerResponse> result = service.search(request);

        assertThat(result.getContent()).containsExactly(response);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(customerRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(25);
        assertThat(pageable.getSort().stream().map(order -> order.getProperty()))
                .containsExactly("account.createdAt", "customerName", "customerId");
    }

    @Test
    void searchRejectsInvertedCreatedRangeBeforeQuerying() {
        AdminCustomerSearchRequest request = new AdminCustomerSearchRequest();
        request.setCreatedFrom(LocalDateTime.parse("2026-09-02T00:00:00"));
        request.setCreatedTo(LocalDateTime.parse("2026-09-01T00:00:00"));

        assertThatThrownBy(() -> service.search(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("createdFrom");
        verify(customerRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void exportWritesUtf8CsvWithIsoLocalDateTimeAndSafeCells() {
        Account account = Account.builder()
                .accountStatus(true)
                .createdAt(Timestamp.valueOf("2026-09-01 12:30:45"))
                .build();
        Customer customer = Customer.builder()
                .customerId("customer-1")
                .customerName("=HYPERLINK(\"bad\")")
                .phone("0900000000")
                .email("customer@example.com")
                .gender("FEMALE")
                .address("Da Nang, Viet Nam")
                .account(account)
                .build();
        when(customerRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(customer)));
        StringWriter output = new StringWriter();

        service.export(new AdminCustomerFilter(), output);

        assertThat(output.toString())
                .startsWith("\uFEFFcustomerId,customerName,phone,email,gender,birthDate,address,accountStatus,createdAt\r\n")
                .contains("\"'=HYPERLINK(\"\"bad\"\")\"")
                .contains("\"Da Nang, Viet Nam\"")
                .contains("2026-09-01T12:30:45");
    }
}
