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
import com.project.BookCarOnline.Utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    FirebaseStorageService firebaseStorageService;

    public DriverDetailResponse getMyInfo() {
        String profileId = SecurityUtils.getCurrentProfileId().orElseThrow(()->new AppException(ErrorCode.PROFILE_NOT_FOUND));
        Driver driver = driverRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        return mapper.toDriverDetailResponse(driver);
    }

    @PreAuthorize("hasRole('"+PredefinedRole.RoleName.ADMIN+"'))")
    public List<DriverDetailResponse> getAllDrivers() {
        log.info("Fetching all drivers");
        List<Driver> drivers = driverRepository.findAll();
        return drivers.stream()
                .map(mapper::toDriverDetailResponse)
                .collect(Collectors.toList());
    }


    public List<DriverDetailResponse> getAllActiveDrivers() {
        log.info("Fetching all active drivers");
        List<Driver> drivers = driverRepository.findByActivityStatusTrue();
        return drivers.stream()
                .map(mapper::toDriverDetailResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DriverDetailResponse createDriver(CreateDriverRequest request, VehicleType vehicleType) {
        log.info("Creating new driver: {}", request.getEmail());

        // Validate unique constraints
        validateDriverUniqueness(request);

        // Create Account for Driver
        Role driverRole = roleRepository.findByRoleId(PredefinedRole.DRIVER.getRoleName())
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
    public DriverDetailResponse updateDriver(String driverId, UpdateDriverRequest request, VehicleType vehicleType) throws IOException {
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
        if(request.getAvatar()!=null){
            String oldFilePath = firebaseStorageService.getFilePathFromUrl(driver.getAvatar());
            if (oldFilePath != null) {
                firebaseStorageService.deleteFile(oldFilePath);
                log.info("Đã xóa ảnh cũ thành công: {}", oldFilePath);
            }
            else {
                String accountID = SecurityUtils.getCurrentAccountId().orElseThrow(()->new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
                String folderPath = "drivers"+ "/" + accountID + "/avatar";
                String fileURL = firebaseStorageService.uploadFile(request.getAvatar(), folderPath, null);
                driver.setAvatar(fileURL);
            }
        }
        mapper.updateDriver(driver, request);

        if(request.getCitizenIdImage()!=null){
            String oldFilePath = firebaseStorageService.getFilePathFromUrl(driver.getAvatar());
            if (oldFilePath != null) {
                firebaseStorageService.deleteFile(oldFilePath);
                log.info("Đã xóa ảnh cũ thành công: {}", oldFilePath);
            }
            else {
                String accountID = SecurityUtils.getCurrentAccountId().orElseThrow(()->new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
                String folderPath = "drivers"+ "/" + accountID + "/citizenId";
                String fileURL = firebaseStorageService.uploadFile(request.getAvatar(), folderPath, null);
                driver.setCitizenId(fileURL);
            }
        }
        if(request.getDrivingLicenseImage()!=null){
            String oldFilePath = firebaseStorageService.getFilePathFromUrl(driver.getDrivingLicense());
            if (oldFilePath != null) {
                firebaseStorageService.deleteFile(oldFilePath);
                log.info("Đã xóa ảnh cũ thành công: {}", oldFilePath);
            }
            else {
                String accountID = SecurityUtils.getCurrentAccountId().orElseThrow(()->new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
                String folderPath = "drivers"+ "/" + accountID + "/drivingLicense";
                String fileURL = firebaseStorageService.uploadFile(request.getAvatar(), folderPath, null);
                driver.setDrivingLicense(fileURL);
            }
        }

        // Update driver fields

        
        if (vehicleType != null) {
            driver.setVehicleType(vehicleType);
        }

        Driver updatedDriver = driverRepository.save(driver);
        log.info("Driver updated successfully: {}", driverId);

        return mapper.toDriverDetailResponse(updatedDriver);
    }

    @PreAuthorize("hasRole('"+PredefinedRole.RoleName.ADMIN+"'))")
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

    @PreAuthorize("hasRole('"+PredefinedRole.RoleName.ADMIN+"'))")
    public DriverDetailResponse getDriverById(String driverId) {
        log.info("Fetching driver by ID: {}", driverId);
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        return mapper.toDriverDetailResponse(driver);
    }


    public List<DriverDetailResponse> getDriversByArea(String area) {
        log.info("Fetching drivers by area: {}", area);
        List<Driver> drivers = driverRepository.findByAreaAndActivityStatusTrue(area);
        return drivers.stream()
                .map(mapper::toDriverDetailResponse)
                .collect(Collectors.toList());
    }


    public List<DriverDetailResponse> getDriversByVehicleType(String vehicleTypeId) {
        log.info("Fetching drivers by vehicle type: {}", vehicleTypeId);
        List<Driver> drivers = driverRepository.findByVehicleType_VehicleTypeIdAndActivityStatusTrue(vehicleTypeId);
        return drivers.stream()
                .map(mapper::toDriverDetailResponse)
                .collect(Collectors.toList());
    }


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


    public static String extractCityFromAddress(String address) {
        if (address == null || !address.contains(",")) {
            return "";
        }
        String[] parts = address.split(",");
        return parts[parts.length - 1].trim();
    }
}
