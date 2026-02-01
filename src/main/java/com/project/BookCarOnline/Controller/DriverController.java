package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.CreateDriverRequest;
import com.project.BookCarOnline.DTO.Request.UpdateDriverRequest;
import com.project.BookCarOnline.DTO.Response.DriverDetailResponse;
import com.project.BookCarOnline.DTO.Response.DriverResponse;
import com.project.BookCarOnline.Entity.VehicleType;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Repository.RideBookRepository;
import com.project.BookCarOnline.Service.DriverService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Driver Controller
 * Converted from: TaiXeController.java (Servlet)
 * 
 * Original endpoints:
 * - GET  /api-driver          -> Get all drivers
 * - POST /api-driver?action=create -> Create driver
 * - POST /api-driver?action=update -> Update driver
 * - PUT  /api-driver          -> Update driver
 * - DELETE /api-driver?id=xxx -> Delete driver
 * 
 * New REST endpoints:
 * - GET    /drivers           -> Get all drivers
 * - GET    /drivers/{id}      -> Get driver by ID
 * - POST   /drivers           -> Create driver
 * - PUT    /drivers/{id}      -> Update driver
 * - DELETE /drivers/{id}      -> Delete driver
 * - GET    /drivers/active    -> Get active drivers
 * - GET    /drivers/area/{area} -> Get drivers by area
 * - GET    /drivers/vehicle-type/{vehicleTypeId} -> Get drivers by vehicle type
 */
@Slf4j
@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DriverController {
    
    DriverService driverService;

    /**
     * Get all drivers
     * Converted from: TaiXeController.doGet()
     * 
     * @return List of all drivers with account status
     */
    @GetMapping
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
    public APIResponse<DriverResponse> getMyInfo(){
        DriverResponse response = driverService.getMyInfo();
        return APIResponse.<DriverResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thông tin tài xế")
                .result(response)
                .build();
    }

    /**
     * Get driver by ID
     * 
     * @param driverId Driver ID
     * @return Driver details
     */
    @GetMapping("/{driverId}")
    public APIResponse<DriverDetailResponse> getDriverById(@PathVariable String driverId) {
        log.info("REST API: GET /drivers/{} - Fetching driver by ID", driverId);
        DriverDetailResponse driver = driverService.getDriverById(driverId);
        return APIResponse.<DriverDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thông tin tài xế")
                .result(driver)
                .build();
    }

    /**
     * Get all active drivers
     * 
     * @return List of active drivers
     */
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

    /**
     * Get drivers by area
     * 
     * @param area Area name
     * @return List of drivers in the area
     */
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

    /**
     * Get drivers by vehicle type
     * 
     * @param vehicleTypeId Vehicle type ID
     * @return List of drivers with the vehicle type
     */
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

    /**
     * Create new driver
     * Converted from: TaiXeController.doPost() with action=create
     * 
     * Original: POST /api-driver?action=create with multipart form data
     * New: POST /drivers with JSON body
     * 
     * Note: File upload handling (avatar, CCCD, GPLX) will need separate endpoint
     * or multipart handling. For now, we accept image URLs/paths in the request.
     * 
     * @param request Create driver request
     * @return Created driver details
     */
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

    /**
     * Update driver
     * Converted from: TaiXeController.doPost() with action=update and TaiXeController.doPut()
     * 
     * Original: POST /api-driver?action=update OR PUT /api-driver with multipart form data
     * New: PUT /drivers/{driverId} with JSON body
     * 
     * @param driverId Driver ID
     * @param request Update driver request
     * @return Updated driver details
     */
    @PutMapping("/{driverId}")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<DriverDetailResponse> updateDriver(
            @PathVariable String driverId,
            @Valid @RequestBody UpdateDriverRequest request) {
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

    /**
     * Delete driver (soft delete)
     * Converted from: TaiXeController.doDelete()
     * 
     * Original: DELETE /api-driver?id=xxx
     * New: DELETE /drivers/{driverId}
     * 
     * @param driverId Driver ID
     * @return Success message
     */
    @DeleteMapping("/{driverId}")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Void> deleteDriver(@PathVariable String driverId) {
        log.info("REST API: DELETE /drivers/{} - Deleting driver", driverId);
        driverService.deleteDriver(driverId);
        return APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Xóa tài xế thành công")
                .build();
    }
}
