package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.UpdateDriverRequest;
import com.project.BookCarOnline.DTO.Response.DriverDetailResponse;
import com.project.BookCarOnline.Service.DriverService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/admin/drivers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDriverController {

    DriverService driverService;

    @GetMapping
    public APIResponse<Page<DriverDetailResponse>> getAllDrivers(
            @RequestParam(value = "page",defaultValue = "0") int page,
            @RequestParam(value = "size",defaultValue = "20") int size,
            @RequestParam(value = "search", required = false) String search
    ) {
        log.info("REST API: GET /admin/drivers - Fetching all drivers");
        Page<DriverDetailResponse> drivers = driverService.searchDrivers(page,size,search);
        return APIResponse.<Page<DriverDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách tài xế")
                .result(drivers)
                .build();
    }

    @GetMapping("/{driverId}")
    public APIResponse<DriverDetailResponse> getDriverById(@PathVariable String driverId) {
        log.info("REST API: GET /admin/drivers/{} - Fetching driver by ID", driverId);
        DriverDetailResponse driver = driverService.getDriverById(driverId);
        return APIResponse.<DriverDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thông tin tài xế")
                .result(driver)
                .build();
    }

    @PutMapping(value = "/{driverId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public APIResponse<DriverDetailResponse> updateDriver(
            @PathVariable String driverId,
            @Valid @ModelAttribute UpdateDriverRequest request) throws IOException {
        log.info("REST API: PUT /admin/drivers/{} - Updating driver", driverId);

        DriverDetailResponse driver = driverService.updateDriver(driverId, request, null);
        
        return APIResponse.<DriverDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật tài xế thành công")
                .result(driver)
                .build();
    }

    @DeleteMapping("/{driverId}")
    public APIResponse<Void> deleteDriver(@PathVariable String driverId) {
        log.info("REST API: DELETE /admin/drivers/{} - Deleting driver", driverId);
        driverService.deleteDriver(driverId);
        return APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Xóa tài xế thành công")
                .build();
    }

    @PutMapping("/{driverId}/account-status")
    public APIResponse<Boolean> toggleDriverAccountStatus(@PathVariable String driverId) {
        log.info("REST API: PUT /admin/drivers/{}/account-status - Toggling account status", driverId);
        Boolean status = driverService.toggleDriverAccountStatus(driverId);
        return APIResponse.<Boolean>builder()
                .status(HttpStatus.OK.value())
                .message(status ? "Mở khóa tài khoản tài xế thành công" : "Khóa tài khoản tài xế thành công")
                .result(status)
                .build();
    }
}
