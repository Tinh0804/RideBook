package com.project.BookCarOnline.Controller;
import com.project.BookCarOnline.DTO.Redis.DriverLocation;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.CreateDriverRequest;
import com.project.BookCarOnline.DTO.Request.DriverLocationRequest;
import com.project.BookCarOnline.DTO.Request.UpdateDriverRequest;
import com.project.BookCarOnline.DTO.Response.DailyRevenueDTO;
import com.project.BookCarOnline.DTO.Response.DriverDashboardResponse;
import com.project.BookCarOnline.DTO.Response.DriverDetailResponse;
import com.project.BookCarOnline.DTO.Response.DriverResponse;
import com.project.BookCarOnline.Entity.Enum.PredefinedRole;
import com.project.BookCarOnline.Entity.VehicleType;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Repository.RideBookRepository;
import com.project.BookCarOnline.Service.DriverCacheService;
import com.project.BookCarOnline.Service.DriverService;
import com.project.BookCarOnline.Utils.SecurityUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DriverController {
    
    DriverService driverService;
    SimpMessagingTemplate messagingTemplate;
    DriverCacheService driverCacheService;



    @GetMapping("/my-info")
    public APIResponse<DriverDetailResponse> getMyInfo(){
        DriverDetailResponse response = driverService.getMyInfo();
        return APIResponse.<DriverDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thông tin tài xế")
                .result(response)
                .build();
    }

    @GetMapping("/my-dashboard")
    public APIResponse<DriverDashboardResponse> getMyDashboard(){
        DriverDashboardResponse response = driverService.getDriverDashboard();
        return APIResponse.<com.project.BookCarOnline.DTO.Response.DriverDashboardResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê tài xế")
                .result(response)
                .build();
    }

    @GetMapping("/my-revenue")
    public APIResponse<com.project.BookCarOnline.DTO.Response.DriverRevenueResponse> getMyRevenue(
            @RequestParam(name = "period", defaultValue = "week") String period){
        com.project.BookCarOnline.DTO.Response.DriverRevenueResponse response = driverService.getDriverRevenue(period);
        return APIResponse.<com.project.BookCarOnline.DTO.Response.DriverRevenueResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy dữ liệu thống kê thành công")
                .result(response)
                .build();
    }

    @GetMapping("/my-revenue/daily")
    public APIResponse<DailyRevenueDTO> getDailyRevenue(
            @RequestParam(name = "date", required = false) String dateStr){
        com.project.BookCarOnline.DTO.Response.DailyRevenueDTO response = driverService.getDailyRevenue(dateStr);
        return APIResponse.<com.project.BookCarOnline.DTO.Response.DailyRevenueDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy dữ liệu thống kê ngày thành công")
                .result(response)
                .build();
    }
    @PutMapping(value = "/status-activity")
    public APIResponse<Boolean> updateStatusActive(){
        String driverId = SecurityUtils.getCurrentProfileId().orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        boolean isActive = driverService.toggleDriverActivityStatus(driverId);
        return APIResponse.<Boolean>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật trạng thái hoạt động thành công")
                .result(isActive)
                .build();
    }

    @PutMapping(value = "/my-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public APIResponse<DriverDetailResponse> updateMyInfo(@Valid @ModelAttribute UpdateDriverRequest request) throws IOException {
        String driverId = SecurityUtils.getCurrentProfileId().orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        log.info("REST API: PUT /drivers/my-info - Updating driver {}", driverId);
        DriverDetailResponse driver = driverService.updateDriver(driverId, request, null);
        return APIResponse.<DriverDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật thông tin thành công")
                .result(driver)
                .build();
    }

    @PutMapping("/location/free")
    public APIResponse<Void> updateFreeLocation(@RequestBody DriverLocationRequest request) {
        String driverId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (request.getLat() != null && request.getLng() != null) {
            driverService.updateFreeLocation(driverId, request.getLat(), request.getLng());
        }
        return APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Location updated")
                .build();
    }




    @GetMapping("/active")
    public APIResponse<List<DriverDetailResponse>> getActiveDrivers() {
        log.info("REST API: GET /drivers/active - Fetching active drivers");
        List<DriverDetailResponse> drivers = driverService.getAllActiveDrivers();
        return APIResponse.<List<DriverDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách tài xế đang hoạt động")
                .result(drivers)
                .build();
    }


    @GetMapping("/area/{area}")
    public APIResponse<List<DriverDetailResponse>> getDriversByArea(@PathVariable String area) {
        log.info("REST API: GET /drivers/area/{} - Fetching drivers by area", area);
        List<DriverDetailResponse> drivers = driverService.getDriversByArea(area);
        return APIResponse.<List<DriverDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách tài xế theo khu vực: " + area)
                .result(drivers)
                .build();
    }


    @GetMapping("/vehicle-type/{vehicleTypeId}")
    public APIResponse<List<DriverDetailResponse>> getDriversByVehicleType(@PathVariable String vehicleTypeId) {
        log.info("REST API: GET /drivers/vehicle-type/{} - Fetching drivers by vehicle type", vehicleTypeId);
        List<DriverDetailResponse> drivers = driverService.getDriversByVehicleType(vehicleTypeId);
        return APIResponse.<List<DriverDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách tài xế theo loại xe")
                .result(drivers)
                .build();
    }


    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse<DriverDetailResponse> createDriver(@Valid @ModelAttribute CreateDriverRequest request) throws IOException {
        log.info("REST API: POST /drivers - Creating new driver: {}", request.getEmail());

        DriverDetailResponse driver = driverService.createDriver(request, null);
        
        return APIResponse.<DriverDetailResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tạo tài xế thành công")
                .result(driver)
                .build();
    }


    @MessageMapping("/driver/location")
    public void updateDriverLocation(@Payload DriverLocationRequest request) {
        if (request.getBookingId() == null || request.getLat() == null || request.getLng() == null) {
            log.warn("Invalid driver location payload received: {}", request);
            return;
        }

        log.debug("Driver location update for booking {}: lat={}, lng={}",
                request.getBookingId(), request.getLat(), request.getLng());


        driverCacheService.saveLocation(request.getBookingId(), request.getLat(), request.getLng());

        messagingTemplate.convertAndSend(
                "/topic/booking/" + request.getBookingId() + "/driver-location",
                request
        );
    }

    @GetMapping("/location/{bookingId}")
    public APIResponse<DriverLocation> getDriverLocation(@PathVariable String bookingId) {
        DriverLocation location = driverCacheService.getLocation(bookingId);
        return APIResponse.<DriverLocation>builder()
                .status(HttpStatus.OK.value())
                .message("Driver location")
                .result(location) 
                .build();
    }
}
