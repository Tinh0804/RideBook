package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.DTO.Request.CreateDriverRequest;
import com.project.BookCarOnline.DTO.Request.UpdateDriverRequest;
import com.project.BookCarOnline.DTO.Response.DriverDetailResponse;
import com.project.BookCarOnline.DTO.Response.DriverResponse;
import com.project.BookCarOnline.DTO.Response.DriverRevenueResponse;
import com.project.BookCarOnline.DTO.Response.RevenueDetailDTO;
import com.project.BookCarOnline.DTO.Response.RevenueSummaryDTO;
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
import com.project.BookCarOnline.DTO.Response.DriverDashboardResponse;
import com.project.BookCarOnline.Entity.Booking;
import com.project.BookCarOnline.Entity.Enum.BookingStatus;
import com.project.BookCarOnline.Entity.Rating;
import com.project.BookCarOnline.Repository.RideBookRepository;
import com.project.BookCarOnline.Repository.RatingRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    RideBookRepository rideBookRepository;
    RatingRepository ratingRepository;

    FirebaseStorageService firebaseStorageService;

    public DriverDetailResponse getMyInfo() {
        String profileId = SecurityUtils.getCurrentProfileId().orElseThrow(()->new AppException(ErrorCode.PROFILE_NOT_FOUND));
        Driver driver = driverRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        return mapper.toDriverDetailResponse(driver);
    }

    public DriverDashboardResponse getDriverDashboard() {
        String driverId = SecurityUtils.getCurrentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        long totalRides = rideBookRepository.countCompletedRides(driverId);
        double totalIncome = Optional.ofNullable(
                rideBookRepository.sumTotalIncome(driverId)
        ).orElse(0.0);

        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        double todayIncome = Optional.ofNullable(
                rideBookRepository.sumTodayIncome(driverId,startOfDay,endOfDay)
        ).orElse(0.0);

        double averageRating = Optional.ofNullable(
                ratingRepository.getAverageRatingByDriver(driverId)
        ).orElse(5.0);

        return DriverDashboardResponse.builder()
                .totalRides(totalRides)
                .totalIncome(totalIncome)
                .todayIncome(todayIncome)
                .averageRating(Math.round(averageRating))
                .build();
    }

    public DriverRevenueResponse getDriverRevenue() {
        String driverId = SecurityUtils.getCurrentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // 🔹 summary
        Object[] summaryResult = rideBookRepository.getRevenueSummary(driverId);

        // ✅ Fix: Xử lý an toàn, tránh ClassCastException
        long totalTrips = 0;
        double totalRevenue = 0.0;

        if (summaryResult != null && summaryResult.length > 0) {
            // Kiểm tra và xử lý từng phần tử
            Object tripsObj = summaryResult[0];
            if (tripsObj instanceof Number) {
                totalTrips = ((Number) tripsObj).longValue();
            } else if (tripsObj instanceof Object[]) {
                // Trường hợp query trả về mảng lồng nhau
                Object[] inner = (Object[]) tripsObj;
                if (inner.length > 0 && inner[0] instanceof Number) {
                    totalTrips = ((Number) inner[0]).longValue();
                }
            }

            Object revenueObj = summaryResult.length > 1 ? summaryResult[1] : null;
            if (revenueObj instanceof Number) {
                totalRevenue = ((Number) revenueObj).doubleValue();
            } else if (revenueObj instanceof Object[]) {
                Object[] inner = (Object[]) revenueObj;
                if (inner.length > 0 && inner[0] instanceof Number) {
                    totalRevenue = ((Number) inner[0]).doubleValue();
                }
            }
        }

        RevenueSummaryDTO summary = RevenueSummaryDTO.builder()
                .totalTrips(totalTrips)
                .totalRevenue(totalRevenue)
                .build();

        // 🔹 details (group by date)
        List<Object[]> results = rideBookRepository.getRevenueByDate(driverId);

        List<RevenueDetailDTO> details = results.stream()
                .map(r -> {
                    // ✅ Fix phần detail tương tự
                    LocalDate date = null;
                    int tripCount = 0;
                    double revenue = 0.0;

                    if (r != null && r.length > 0) {
                        // Xử lý date
                        if (r[0] instanceof java.sql.Date) {
                            date = ((java.sql.Date) r[0]).toLocalDate();
                        } else if (r[0] instanceof LocalDate) {
                            date = (LocalDate) r[0];
                        }

                        // Xử lý tripCount
                        if (r.length > 1 && r[1] instanceof Number) {
                            tripCount = ((Number) r[1]).intValue();
                        }

                        // Xử lý revenue
                        if (r.length > 2 && r[2] instanceof Number) {
                            revenue = ((Number) r[2]).doubleValue();
                        }
                    }

                    return RevenueDetailDTO.builder()
                            .timeLabel(date != null ? date.format(formatter) : "")
                            .tripCount(tripCount)
                            .revenue(revenue)
                            .build();
                })
                .toList();

        return DriverRevenueResponse.builder()
                .summary(summary)
                .details(details)
                .build();
    }


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
    public Boolean toggleDriverActivityStatus(String driverId) {
        log.info("Toggling activity status for driver: {}", driverId);
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        driver.setActivityStatus(!driver.getActivityStatus());
        driverRepository.save(driver);
        log.info("Driver activity status toggled successfully: {} is now {}", driverId, driver.getActivityStatus());
        return driver.getActivityStatus();
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
