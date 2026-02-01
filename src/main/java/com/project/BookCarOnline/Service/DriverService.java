package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.DTO.Request.CreateDriverRequest;
import com.project.BookCarOnline.DTO.Request.UpdateDriverRequest;
import com.project.BookCarOnline.DTO.Response.DriverDetailResponse;
import com.project.BookCarOnline.DTO.Response.DriverResponse;
import com.project.BookCarOnline.Entity.Account;
import com.project.BookCarOnline.Entity.Driver;
import com.project.BookCarOnline.Entity.Enum.PredefinedRole;
import com.project.BookCarOnline.Entity.Role;
import com.project.BookCarOnline.Entity.VehicleType;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Mapper.DriverMapper;
import com.project.BookCarOnline.Repository.AccountRepository;
import com.project.BookCarOnline.Repository.DriverRepository;
import com.project.BookCarOnline.Repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DriverService {
    DriverRepository driverRepository;
    AccountRepository accountRepository;
    RoleRepository roleRepository;
    DriverMapper mapper;
    PasswordEncoder passwordEncoder;

    /**
     * Get driver info by authenticated account
     */
    public DriverResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String accountId = context.getAuthentication().getName();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        Driver driver = driverRepository.findByAccountId(account.getAccountId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        return mapper.toDriverResponse(driver);
    }

    /**
     * Get all drivers with account status
     * Converted from: TaiXeController.doGet()
     */
    public List<DriverDetailResponse> getAllDrivers() {
        log.info("Fetching all drivers");
        List<Driver> drivers = driverRepository.findAll();
        return drivers.stream()
                .map(mapper::toDriverDetailResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all active drivers
     */
    public List<DriverDetailResponse> getAllActiveDrivers() {
        log.info("Fetching all active drivers");
        List<Driver> drivers = driverRepository.findByActivityStatusTrue();
        return drivers.stream()
                .map(mapper::toDriverDetailResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create new driver with account
     * Converted from: TaiXeService.themTaiXe()
     */
    @Transactional
    public DriverDetailResponse createDriver(CreateDriverRequest request, VehicleType vehicleType) {
        log.info("Creating new driver: {}", request.getEmail());

        // Validate unique constraints
        validateDriverUniqueness(request);

        // Create Account for Driver
        Role driverRole = roleRepository.findByRoleId(PredefinedRole.DRIVER.getDescription())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTS));



        // Create Driver entity
        Driver driver = mapper.toDriverFromCreateRequest(request);
        // vehicleType will be set later or managed separately
        if (vehicleType != null) {
            driver.setVehicleType(vehicleType);
        }
        driver.setActivityStatus(true);
        Account account = Account.builder()
                .userName(request.getPhone()) // Using phone as username
                .passWord(passwordEncoder.encode(request.getPassword()))
                .roleNo(driverRole)
                .accountStatus(true)
                .createdAt(new Date())
                .build();
        accountRepository.save(account);

        driver.setAccount(account);
        Driver savedDriver = driverRepository.save(driver);


        log.info("Driver created successfully with ID: {}", savedDriver.getDriverId());

        return mapper.toDriverDetailResponse(savedDriver);
    }


    @Transactional
    public DriverDetailResponse updateDriver(String driverId, UpdateDriverRequest request, VehicleType vehicleType) {
        log.info("Updating driver: {}", driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        // Validate unique constraints if fields are being updated
        if (request.getEmail() != null && !request.getEmail().equals(driver.getEmail())) {
            if (driverRepository.existsByEmail(request.getEmail())) {
                throw new AppException(ErrorCode.USER_EXISTED);
            }
        }
        if (request.getPhone() != null && !request.getPhone().equals(driver.getPhone())) {
            if (driverRepository.existsByPhone(request.getPhone())) {
                throw new AppException(ErrorCode.USER_EXISTED);
            }
        }
        if (request.getCitizenId() != null && !request.getCitizenId().equals(driver.getCitizenId())) {
            if (driverRepository.existsByCitizenId(request.getCitizenId())) {
                throw new AppException(ErrorCode.USER_EXISTED);
            }
        }
        if (request.getLicensePlate() != null && !request.getLicensePlate().equals(driver.getLicensePlate())) {
            if (driverRepository.existsByLicensePlate(request.getLicensePlate())) {
                throw new AppException(ErrorCode.USER_EXISTED);
            }
        }

        // Update driver fields
        mapper.updateDriver(driver, request);
        
        if (vehicleType != null) {
            driver.setVehicleType(vehicleType);
        }

        Driver updatedDriver = driverRepository.save(driver);
        log.info("Driver updated successfully: {}", driverId);

        return mapper.toDriverDetailResponse(updatedDriver);
    }

    /**
     * Delete driver
     * Converted from: TaiXeController.doDelete()
     */
    @Transactional
    public void deleteDriver(String driverId) {
        log.info("Deleting driver: {}", driverId);
        
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        // Soft delete by setting activityStatus to false
        driverRepository.deleteById(driver.getDriverId());
        accountRepository.deleteById(driver.getAccount().getAccountId());

        log.info("Driver deleted successfully: {}", driverId);
    }

    /**
     * Get driver by ID
     */
    public DriverDetailResponse getDriverById(String driverId) {
        log.info("Fetching driver by ID: {}", driverId);
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        return mapper.toDriverDetailResponse(driver);
    }

    /**
     * Get drivers by area
     */
    public List<DriverDetailResponse> getDriversByArea(String area) {
        log.info("Fetching drivers by area: {}", area);
        List<Driver> drivers = driverRepository.findByAreaAndActivityStatusTrue(area);
        return drivers.stream()
                .map(mapper::toDriverDetailResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get drivers by vehicle type
     */
    public List<DriverDetailResponse> getDriversByVehicleType(String vehicleTypeId) {
        log.info("Fetching drivers by vehicle type: {}", vehicleTypeId);
        List<Driver> drivers = driverRepository.findByVehicleType_VehicleTypeIdAndActivityStatusTrue(vehicleTypeId);
        return drivers.stream()
                .map(mapper::toDriverDetailResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validate driver uniqueness
     */
    private void validateDriverUniqueness(CreateDriverRequest request) {
        if (driverRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (driverRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (driverRepository.existsByCitizenId(request.getCitizenId())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (driverRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
    }

    /**
     * Extract city from address
     * Converted from: TaiXeService.layThanhPho()
     */
    public static String extractCityFromAddress(String address) {
        if (address == null || !address.contains(",")) {
            return "";
        }
        String[] parts = address.split(",");
        return parts[parts.length - 1].trim();
    }
}
