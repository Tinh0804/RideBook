package com.project.BookCarOnline.identity.service;

import com.project.BookCarOnline.catalog.dto.VehicleTypeSummary;
import com.project.BookCarOnline.catalog.service.VehicleTypeService;
import com.project.BookCarOnline.identity.dto.request.CreateDriverRequest;
import com.project.BookCarOnline.identity.dto.request.UpdateDriverRequest;
import com.project.BookCarOnline.identity.dto.response.DriverDetailResponse;
import com.project.BookCarOnline.identity.entity.Account;
import com.project.BookCarOnline.identity.entity.Driver;
import com.project.BookCarOnline.identity.entity.Role;
import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;
import com.project.BookCarOnline.identity.entity.enums.Provider;
import com.project.BookCarOnline.identity.mapper.DriverMapper;
import com.project.BookCarOnline.identity.repository.AccountRepository;
import com.project.BookCarOnline.identity.repository.DriverRepository;
import com.project.BookCarOnline.identity.repository.RoleRepository;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverManagementService {

    private final DriverRepository driverRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final DriverMapper driverMapper;
    private final PasswordEncoder passwordEncoder;
    private final FirebaseService firebaseService;
    private final VehicleTypeService vehicleTypeService;
    private final EmailVerificationService emailVerificationService;

    public DriverDetailResponse getMyInfo() {
        String driverId = SecurityUtils.getCurrentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        return getById(driverId);
    }

    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public Page<DriverDetailResponse> search(int page, int size, String search) {
        var pageable = PageRequest.of(page, size, Sort.by("driverName").ascending());
        Page<Driver> drivers = search == null || search.isBlank()
                ? driverRepository.findAll(pageable)
                : driverRepository.searchDrivers("%" + search.trim().toLowerCase() + "%", pageable);
        return drivers.map(this::toResponse);
    }

    public List<DriverDetailResponse> getActive() {
        return driverRepository.findByActivityStatusTrue().stream().map(this::toResponse).toList();
    }

    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public List<DriverDetailResponse> getActiveByArea(String area) {
        return driverRepository.findByAreaAndActivityStatusTrue(area).stream().map(this::toResponse).toList();
    }

    public List<DriverDetailResponse> getActiveByVehicleType(String vehicleTypeId) {
        return driverRepository.findByVehicleTypeIdAndActivityStatusTrue(vehicleTypeId).stream()
                .map(this::toResponse)
                .toList();
    }

    public DriverDetailResponse getById(String driverId) {
        return toResponse(getEntity(driverId));
    }

    @Transactional
    public DriverDetailResponse create(CreateDriverRequest request) throws IOException {
        validateUnique(request);
        vehicleTypeService.getVehicleTypeSummary(request.getVehicleTypeId());

        Role driverRole = roleRepository.findByRoleName(PredefinedRole.DRIVER)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTS));
        Account account = accountRepository.save(Account.builder()
                .userName(request.getPhone())
                .passWord(passwordEncoder.encode(request.getPassword()))
                .roleNo(driverRole)
                .provider(Provider.LOCAL)
                .accountStatus(true)
                .emailVerified(false)
                .createdAt(new Date())
                .build());

        Driver driver = driverMapper.toDriverFromCreateRequest(request);
        driver.setAccount(account);
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            driver.setAvatar(firebaseService.uploadFile(
                    request.getAvatar(), "drivers/" + account.getAccountId() + "/avatar", null));
        }
        Driver savedDriver = driverRepository.save(driver);
        emailVerificationService.sendVerificationEmail(account, request.getEmail());
        return toResponse(savedDriver);
    }

    @Transactional
    public DriverDetailResponse update(String driverId, UpdateDriverRequest request) throws IOException {
        Driver driver = getEntity(driverId);
        validateUnique(driver, request);
        if (request.getVehicleTypeId() != null) {
            vehicleTypeService.getVehicleTypeSummary(request.getVehicleTypeId());
        }

        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            deleteAvatar(driver.getAvatar());
            driver.setAvatar(firebaseService.uploadFile(
                    request.getAvatar(), "drivers/" + driver.getAccount().getAccountId() + "/avatar", null));
        }
        driverMapper.updateDriver(driver, request);
        return toResponse(driverRepository.save(driver));
    }

    @Transactional
    public void delete(String driverId) {
        Driver driver = getEntity(driverId);
        String accountId = driver.getAccount().getAccountId();
        driverRepository.delete(driver);
        accountRepository.deleteById(accountId);
    }

    @Transactional
    public DriverDetailResponse toggleActivity(String driverId, Double lat, Double lng) {
        Driver driver = getEntity(driverId);
        driver.setActivityStatus(!Boolean.TRUE.equals(driver.getActivityStatus()));
        if (lat != null && lng != null) {
            driver.setCurrentLat(lat);
            driver.setCurrentLng(lng);
        }
        return toResponse(driverRepository.save(driver));
    }

    @Transactional
    public boolean toggleAccountStatus(String driverId) {
        Account account = getEntity(driverId).getAccount();
        account.setAccountStatus(!Boolean.TRUE.equals(account.getAccountStatus()));
        accountRepository.save(account);
        return account.getAccountStatus();
    }

    @Transactional
    public void changePassword(String driverId, String newPassword) {
        Account account = getEntity(driverId).getAccount();
        account.setPassWord(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    private Driver getEntity(String driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
    }

    private DriverDetailResponse toResponse(Driver driver) {
        DriverDetailResponse response = driverMapper.toDriverDetailResponse(driver);
        if (driver.getVehicleTypeId() != null) {
            VehicleTypeSummary vehicleType = vehicleTypeService.getVehicleTypeSummary(driver.getVehicleTypeId());
            response.setVehicleTypeName(vehicleType.vehicleTypeName());
            response.setVehicleTypeIcon(vehicleType.icon());
            response.setPricePerKm(vehicleType.pricePerKm());
        }
        return response;
    }

    private void validateUnique(CreateDriverRequest request) {
        if (driverRepository.existsByEmail(request.getEmail())
                || driverRepository.existsByPhone(request.getPhone())
                || driverRepository.existsByCitizenId(request.getCitizenId())
                || driverRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
    }

    private void validateUnique(Driver driver, UpdateDriverRequest request) {
        if (changed(request.getEmail(), driver.getEmail()) && driverRepository.existsByEmail(request.getEmail())
                || changed(request.getPhone(), driver.getPhone()) && driverRepository.existsByPhone(request.getPhone())
                || changed(request.getCitizenId(), driver.getCitizenId()) && driverRepository.existsByCitizenId(request.getCitizenId())
                || changed(request.getLicensePlate(), driver.getLicensePlate()) && driverRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
    }

    private boolean changed(String requested, String current) {
        return requested != null && !requested.equals(current);
    }

    private void deleteAvatar(String avatar) {
        String oldPath = firebaseService.getFilePathFromUrl(avatar);
        if (oldPath != null) {
            firebaseService.deleteFile(oldPath);
        }
    }
}
