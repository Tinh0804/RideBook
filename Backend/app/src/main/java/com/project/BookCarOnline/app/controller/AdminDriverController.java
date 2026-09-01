package com.project.BookCarOnline.app.controller;

import com.project.BookCarOnline.shared.dto.APIResponse;
import com.project.BookCarOnline.identity.dto.request.AdminChangePasswordRequest;
import com.project.BookCarOnline.identity.dto.request.AdminDriverFilter;
import com.project.BookCarOnline.identity.dto.request.AdminDriverSearchRequest;
import com.project.BookCarOnline.identity.dto.request.UpdateDriverRequest;
import com.project.BookCarOnline.identity.dto.response.DriverDetailResponse;
import com.project.BookCarOnline.identity.service.DriverManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springdoc.core.annotations.ParameterObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/admin/drivers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Admin Drivers", description = "Tìm kiếm, lọc và export tài xế dành cho admin")
public class AdminDriverController {

    DriverManagementService driverManagementService;

    @GetMapping
    @Operation(
            operationId = "searchAdminDrivers",
            summary = "Search and filter drivers",
            description = "Phân trang, multi-field sort và kết hợp các bộ lọc tài xế dành cho admin.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching drivers"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or sort"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Admin role required")
    })
    public APIResponse<Page<DriverDetailResponse>> getAllDrivers(
            @Valid @ParameterObject AdminDriverSearchRequest request) {
        log.info("REST API: GET /admin/drivers - Fetching all drivers");
        Page<DriverDetailResponse> drivers = driverManagementService.search(request);
        return APIResponse.<Page<DriverDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách tài xế")
                .result(drivers)
                .build();
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(
            operationId = "exportAdminDrivers",
            summary = "Export filtered drivers as UTF-8 CSV",
            description = "Dùng cùng filter và sort với search; không phân trang.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UTF-8 CSV attachment"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or sort"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Admin role required")
    })
    public ResponseEntity<StreamingResponseBody> exportDrivers(
            @Valid @ParameterObject AdminDriverFilter filter) {
        StreamingResponseBody body = outputStream -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                driverManagementService.export(filter, writer);
            }
        };
        String filename = "drivers-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(body);
    }

    @GetMapping("/{driverId}")
    public APIResponse<DriverDetailResponse> getDriverById(@PathVariable String driverId) {
        log.info("REST API: GET /admin/drivers/{} - Fetching driver by ID", driverId);
        DriverDetailResponse driver = driverManagementService.getById(driverId);
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

        DriverDetailResponse driver = driverManagementService.update(driverId, request);
        
        return APIResponse.<DriverDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật tài xế thành công")
                .result(driver)
                .build();
    }

    @DeleteMapping("/{driverId}")
    public APIResponse<Void> deleteDriver(@PathVariable String driverId) {
        log.info("REST API: DELETE /admin/drivers/{} - Deleting driver", driverId);
        driverManagementService.delete(driverId);
        return APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Xóa tài xế thành công")
                .build();
    }

    @PutMapping("/{driverId}/account-status")
    public APIResponse<Boolean> toggleDriverAccountStatus(@PathVariable String driverId) {
        log.info("REST API: PUT /admin/drivers/{}/account-status - Toggling account status", driverId);
        Boolean status = driverManagementService.toggleAccountStatus(driverId);
        return APIResponse.<Boolean>builder()
                .status(HttpStatus.OK.value())
                .message(status ? "Mở khóa tài khoản tài xế thành công" : "Khóa tài khoản tài xế thành công")
                .result(status)
                .build();
    }

    @PutMapping("/{driverId}/password")
    public APIResponse<Boolean> changeDriverPassword(
            @PathVariable String driverId,
            @Valid @RequestBody AdminChangePasswordRequest request) {
        log.info("REST API: PUT /admin/drivers/{}/password - Changing driver password", driverId);
        driverManagementService.changePassword(driverId, request.getNewPassword());
        return APIResponse.<Boolean>builder()
                .status(HttpStatus.OK.value())
                .message("Đổi mật khẩu tài xế thành công")
                .result(true)
                .build();
    }

}
