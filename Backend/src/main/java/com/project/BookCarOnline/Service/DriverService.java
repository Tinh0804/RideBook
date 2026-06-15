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

    public DriverRevenueResponse getDriverRevenue(String period) {
        String driverId = SecurityUtils.getCurrentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        // 🔹 summary
        Object[] summaryResult = rideBookRepository.getRevenueSummary(driverId);

        long totalTrips = 0;
        double totalRevenue = 0.0;

        if (summaryResult != null && summaryResult.length > 0) {
            Object tripsObj = summaryResult[0];
            if (tripsObj instanceof Number) {
                totalTrips = ((Number) tripsObj).longValue();
            } else if (tripsObj instanceof Object[]) {
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
        Map<LocalDate, Double> revenueMap = new HashMap<>();
        
        for (Object[] r : results) {
            LocalDate date = null;
            double revenue = 0.0;
            if (r != null && r.length > 0) {
                if (r[0] instanceof java.sql.Date) {
                    date = ((java.sql.Date) r[0]).toLocalDate();
                } else if (r[0] instanceof LocalDate) {
                    date = (LocalDate) r[0];
                }
                if (r.length > 2 && r[2] instanceof Number) {
                    revenue = ((Number) r[2]).doubleValue();
                }
            }
            if (date != null) {
                revenueMap.put(date, revenue * 0.8); // 20% platform fee
            }
        }

        List<RevenueDetailDTO> details = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM");

        if ("week".equalsIgnoreCase(period)) {
            for (int i = 6; i >= 0; i--) {
                LocalDate d = today.minusDays(i);
                details.add(RevenueDetailDTO.builder()
                        .timeLabel(d.format(dayFormatter))
                        .tripCount(0)
                        .revenue(revenueMap.getOrDefault(d, 0.0))
                        .build());
            }
        } else if ("month".equalsIgnoreCase(period)) {
            LocalDate startOfMonth = today.withDayOfMonth(1);
            int lengthOfMonth = today.lengthOfMonth();
            for (int i = 0; i < lengthOfMonth; i++) {
                LocalDate d = startOfMonth.plusDays(i);
                details.add(RevenueDetailDTO.builder()
                        .timeLabel(d.format(dayFormatter))
                        .tripCount(0)
                        .revenue(revenueMap.getOrDefault(d, 0.0))
                        .build());
            }
        } else {
            // year or all
            double[] monthRevenues = new double[12];
            for (Map.Entry<LocalDate, Double> entry : revenueMap.entrySet()) {
                if (entry.getKey().getYear() == today.getYear()) {
                    int month = entry.getKey().getMonthValue();
                    monthRevenues[month - 1] += entry.getValue();
                }
            }
            for (int i = 0; i < 12; i++) {
                details.add(RevenueDetailDTO.builder()
                        .timeLabel("T" + (i + 1))
                        .tripCount(0)
                        .revenue(monthRevenues[i])
                        .build());
            }
        }

        return DriverRevenueResponse.builder()
                .summary(summary)
                .details(details)
                .build();
    }

    public com.project.BookCarOnline.DTO.Response.DailyRevenueDTO getDailyRevenue(String dateStr) {
        String driverId = SecurityUtils.getCurrentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        LocalDate targetDate;
        try {
            targetDate = LocalDate.parse(dateStr);
        } catch (Exception e) {
            targetDate = LocalDate.now();
        }

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.plusDays(1).atStartOfDay();

        List<Booking> trips = rideBookRepository.findByDriverAndStatusAndDateRange(
                driverId, BookingStatus.COMPLETED, startOfDay, endOfDay);

        double PLATFORM_FEE_RATE = 0.20;
        int QUEST_GOAL = 10;
        double QUEST_REWARD = 50000.0;

        double grossRevenue = 0.0;
        double cashIncome = 0.0;
        double onlineIncome = 0.0;

        for (Booking b : trips) {
            double price = b.getTotalPrice() != null ? b.getTotalPrice() : 0.0;
            grossRevenue += price;
            if (b.getPaymentNo() != null && com.project.BookCarOnline.Entity.Enum.PaymentMethod.CASH == b.getPaymentNo().getPaymentType()) {
                cashIncome += price;
            } else {
                onlineIncome += price;
            }
        }

        double platformFee = grossRevenue * PLATFORM_FEE_RATE;
        double netIncome = grossRevenue - platformFee;

        int totalTrips = trips.size();
        boolean isQuestCompleted = totalTrips >= QUEST_GOAL;
        double questEarned = isQuestCompleted ? QUEST_REWARD : 0.0;
        double finalIncome = netIncome + questEarned;

        return com.project.BookCarOnline.DTO.Response.DailyRevenueDTO.builder()
                .date(targetDate.toString())
                .grossRevenue(grossRevenue)
                .netIncome(netIncome)
                .platformFee(platformFee)
                .cashIncome(cashIncome)
                .onlineIncome(onlineIncome)
                .totalTrips(totalTrips)
                .questGoal(QUEST_GOAL)
                .questReward(QUEST_REWARD)
                .isQuestCompleted(isQuestCompleted)
                .questEarned(questEarned)
                .finalIncome(finalIncome)
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

        validateDriverUniqueness(request);

        Role driverRole = roleRepository.findByRoleId(PredefinedRole.DRIVER.getRoleName())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTS));



        // Create Driver entity
        Driver driver = mapper.toDriverFromCreateRequest(request);
        if (vehicleType != null) {
            driver.setVehicleType(vehicleType);
        }
        driver.setActivityStatus(true);
        Account account = Account.builder()
                .userName(request.getPhone())
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

    @Transactional
    public Boolean toggleDriverAccountStatus(String driverId) {
        log.info("Toggling account status for driver: {}", driverId);
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        Account account = driver.getAccount();
        account.setAccountStatus(!account.getAccountStatus());
        accountRepository.save(account);
        log.info("Driver account status toggled: {} is now {}", driverId, account.getAccountStatus());
        return account.getAccountStatus();
    }


    public List<DriverDetailResponse> getDriversByVehicleType(String vehicleTypeId) {
        log.info("Fetching drivers by vehicle type: {}", vehicleTypeId);
        List<Driver> drivers = driverRepository.findByVehicleType_VehicleTypeIdAndActivityStatusTrue(vehicleTypeId);
        return drivers.stream()
                .map(mapper::toDriverDetailResponse)
                .collect(Collectors.toList());
    }
    public void updateDriverLocation(String driverId, Double lat, Double lng) {
        log.info("Updating location for driver: {}", driverId);
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        driver.setCurrentLat(lat);
        driver.setCurrentLng(lng);
        driverRepository.save(driver);
        log.info("Driver location updated successfully: {} is now at ({}, {})", driverId, lat, lng);
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
