package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.CreateDriverRequest;
import com.project.BookCarOnline.DTO.Request.UpdateDriverRequest;
import com.project.BookCarOnline.DTO.Response.DriverDetailResponse;
import com.project.BookCarOnline.DTO.Response.DriverResponse;
import com.project.BookCarOnline.Entity.Enum.PredefinedRole;
import com.project.BookCarOnline.Entity.VehicleType;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Repository.RideBookRepository;
import com.project.BookCarOnline.Service.DriverService;
import com.project.BookCarOnline.Utils.SecurityUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
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


    @GetMapping
    @PreAuthorize("hasRole("+ PredefinedRole.RoleName.ADMIN +")")
    public APIResponse<List<DriverDetailResponse>> getAllDrivers() {
        log.info("REST API: GET /drivers - Fetching all drivers");
        List<DriverDetailResponse> drivers = driverService.getAllDrivers();
        return APIResponse.<List<DriverDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách tài xế")
                .result(drivers)
                .build();
    }
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
    public APIResponse<com.project.BookCarOnline.DTO.Response.DriverDashboardResponse> getMyDashboard(){
        com.project.BookCarOnline.DTO.Response.DriverDashboardResponse response = driverService.getDriverDashboard();
        return APIResponse.<com.project.BookCarOnline.DTO.Response.DriverDashboardResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê tài xế")
                .result(response)
                .build();
    }

    @GetMapping("/my-revenue")
    public APIResponse<com.project.BookCarOnline.DTO.Response.DriverRevenueResponse> getMyRevenue(){
        com.project.BookCarOnline.DTO.Response.DriverRevenueResponse response = driverService.getDriverRevenue();
        return APIResponse.<com.project.BookCarOnline.DTO.Response.DriverRevenueResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy dữ liệu thống kê thành công")
                .result(response)
                .build();
    }
    @PutMapping("/status-activity")
    public APIResponse<Boolean> updateStatusActive(){
        String driverId = SecurityUtils.getCurrentProfileId().orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        boolean isActive = driverService.toggleDriverActivityStatus(driverId);
        return APIResponse.<Boolean>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật trạng thái hoạt động thành công")
                .result(isActive)
                .build();
    }

    @GetMapping("/{driverId}")
    @PreAuthorize("hasRole("+ PredefinedRole.RoleName.ADMIN +")")
    public APIResponse<DriverDetailResponse> getDriverById(@PathVariable String driverId) {
        log.info("REST API: GET /drivers/{} - Fetching driver by ID", driverId);
        DriverDetailResponse driver = driverService.getDriverById(driverId);
        return APIResponse.<DriverDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thông tin tài xế")
                .result(driver)
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


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse<DriverDetailResponse> createDriver(@Valid @RequestBody CreateDriverRequest request) {
        log.info("REST API: POST /drivers - Creating new driver: {}", request.getEmail());

        DriverDetailResponse driver = driverService.createDriver(request, null);
        
        return APIResponse.<DriverDetailResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tạo tài xế thành công")
                .result(driver)
                .build();
    }


    @PutMapping("/{driverId}")
    @PreAuthorize("hasRole("+ PredefinedRole.RoleName.ADMIN +")")
    public APIResponse<DriverDetailResponse> updateDriver(
            @PathVariable String driverId,
            @Valid @RequestBody UpdateDriverRequest request) throws IOException {
        log.info("REST API: PUT /drivers/{} - Updating driver", driverId);

        // Ensure the ID in path matches the ID in request body
//        request.setDriverId(driverId);
        DriverDetailResponse driver = driverService.updateDriver(driverId, request, null);
        
        return APIResponse.<DriverDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật tài xế thành công")
                .result(driver)
                .build();
    }


    @DeleteMapping("/{driverId}")
    @PreAuthorize("hasRole("+ PredefinedRole.RoleName.ADMIN +")")
    public APIResponse<Void> deleteDriver(@PathVariable String driverId) {
        log.info("REST API: DELETE /drivers/{} - Deleting driver", driverId);
        driverService.deleteDriver(driverId);
        return APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Xóa tài xế thành công")
                .build();
    }
}
