package com.project.BookCarOnline.identity.service;

import com.project.BookCarOnline.catalog.dto.VehicleTypeSummary;
import com.project.BookCarOnline.catalog.service.VehicleTypeService;
import com.project.BookCarOnline.identity.dto.request.CreateDriverRequest;
import com.project.BookCarOnline.identity.dto.response.DriverDetailResponse;
import com.project.BookCarOnline.identity.entity.Account;
import com.project.BookCarOnline.identity.entity.Driver;
import com.project.BookCarOnline.identity.entity.Role;
import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;
import com.project.BookCarOnline.identity.mapper.DriverMapper;
import com.project.BookCarOnline.identity.repository.AccountRepository;
import com.project.BookCarOnline.identity.repository.DriverRepository;
import com.project.BookCarOnline.identity.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverManagementServiceTest {

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
    @Mock
    EmailVerificationService emailVerificationService;

    @InjectMocks
    DriverManagementService service;

    @Test
    void createDriverRequiresEmailVerificationAndSendsEmail() throws Exception {
        CreateDriverRequest request = CreateDriverRequest.builder()
                .phone("0912345678")
                .email("driver@example.com")
                .password("password")
                .citizenId("012345678901")
                .licensePlate("51A-12345")
                .vehicleTypeId("vehicle-type")
                .build();
        Role driverRole = Role.builder().roleName(PredefinedRole.DRIVER).build();
        Driver driver = Driver.builder().vehicleTypeId("vehicle-type").build();
        VehicleTypeSummary vehicleType = new VehicleTypeSummary(
                "vehicle-type", "Car", 10_000D, 4, "icon");

        when(roleRepository.findByRoleName(PredefinedRole.DRIVER)).thenReturn(Optional.of(driverRole));
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setAccountId("account-id");
            return account;
        });
        when(driverMapper.toDriverFromCreateRequest(request)).thenReturn(driver);
        when(driverRepository.save(driver)).thenReturn(driver);
        when(driverMapper.toDriverDetailResponse(driver)).thenReturn(new DriverDetailResponse());
        when(vehicleTypeService.getVehicleTypeSummary("vehicle-type")).thenReturn(vehicleType);

        service.create(request);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertFalse(accountCaptor.getValue().isEmailVerified());
        verify(emailVerificationService)
                .sendVerificationEmail(accountCaptor.getValue(), "driver@example.com");
    }
}
