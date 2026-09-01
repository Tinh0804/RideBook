package com.project.BookCarOnline.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.BookCarOnline.catalog.service.VehicleTypeService;
import com.project.BookCarOnline.identity.dto.request.AdminDriverFilter;
import com.project.BookCarOnline.identity.dto.request.AdminDriverSearchRequest;
import com.project.BookCarOnline.identity.dto.response.DriverDetailResponse;
import com.project.BookCarOnline.identity.entity.Account;
import com.project.BookCarOnline.identity.entity.Driver;
import com.project.BookCarOnline.identity.mapper.DriverMapper;
import com.project.BookCarOnline.identity.repository.AccountRepository;
import com.project.BookCarOnline.identity.repository.DriverRepository;
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
class AdminDriverSearchServiceTest {

    @Mock
    DriverRepository driverRepository;
    @Mock
    AccountRepository accountRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    DriverMapper driverMapper;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    FirebaseService firebaseService;
    @Mock
    VehicleTypeService vehicleTypeService;

    DriverManagementService service;

    @BeforeEach
    void setUp() {
        service = new DriverManagementService(
                driverRepository,
                accountRepository,
                roleRepository,
                driverMapper,
                passwordEncoder,
                firebaseService,
                vehicleTypeService);
    }

    @Test
    void searchUsesContractPaginationAndMultiFieldSort() {
        AdminDriverSearchRequest request = new AdminDriverSearchRequest();
        request.setPage(1);
        request.setSize(40);
        request.setSort("score:desc,driverName:asc");
        Driver driver = Driver.builder().driverId("driver-1").build();
        DriverDetailResponse response = DriverDetailResponse.builder().driverId("driver-1").build();
        when(driverRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(driver)));
        when(driverMapper.toDriverDetailResponse(driver)).thenReturn(response);

        Page<DriverDetailResponse> result = service.search(request);

        assertThat(result.getContent()).containsExactly(response);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(driverRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(40);
        assertThat(pageable.getSort().stream().map(order -> order.getProperty()))
                .containsExactly("score", "driverName", "driverId");
    }

    @Test
    void searchRejectsInvertedRatingRangeBeforeQuerying() {
        AdminDriverSearchRequest request = new AdminDriverSearchRequest();
        request.setMinRating(4.5);
        request.setMaxRating(4.0);

        assertThatThrownBy(() -> service.search(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minRating");
        verify(driverRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void exportWritesUtf8CsvWithIsoLocalDateTimeAndSafeCells() {
        Account account = Account.builder()
                .accountStatus(false)
                .createdAt(Timestamp.valueOf("2026-09-01 08:15:30"))
                .build();
        Driver driver = Driver.builder()
                .driverId("driver-1")
                .driverName("Nguyen Van A")
                .phone("0900000001")
                .email("driver@example.com")
                .licensePlate("=1+1")
                .vehicleName("Sedan")
                .vehicleTypeId("vehicle-type-1")
                .area("Ho Chi Minh")
                .score(4.8)
                .activityStatus(true)
                .account(account)
                .build();
        when(driverRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(driver)));
        StringWriter output = new StringWriter();

        service.export(new AdminDriverFilter(), output);

        assertThat(output.toString())
                .startsWith("\uFEFFdriverId,driverName,phone,email,citizenId,licensePlate,vehicleName,vehicleTypeId,area,score,activityStatus,accountStatus,createdAt\r\n")
                .contains("\"'=1+1\"")
                .contains("2026-09-01T08:15:30");
    }
}
