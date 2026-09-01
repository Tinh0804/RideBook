package com.project.BookCarOnline.identity.controller;

import com.project.BookCarOnline.shared.dto.APIResponse;
import com.project.BookCarOnline.identity.dto.request.AdminChangePasswordRequest;
import com.project.BookCarOnline.identity.dto.request.AdminCustomerFilter;
import com.project.BookCarOnline.identity.dto.request.AdminCustomerSearchRequest;
import com.project.BookCarOnline.identity.dto.request.UpdateCustomerRequest;
import com.project.BookCarOnline.identity.dto.response.CustomerResponse;
import com.project.BookCarOnline.identity.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springdoc.core.annotations.ParameterObject;

@RestController
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Admin Customers", description = "Tìm kiếm, lọc và export khách hàng dành cho admin")
public class AdminCustomerController {

    CustomerService service;

    @GetMapping
    @Operation(
            operationId = "searchAdminCustomers",
            summary = "Search and filter customers",
            description = "Phân trang, multi-field sort và kết hợp các bộ lọc khách hàng dành cho admin.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching customers"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or sort"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Admin role required")
    })
    public APIResponse<Page<CustomerResponse>> getAllCustomer(
            @Valid @ParameterObject AdminCustomerSearchRequest request) {
        Page<CustomerResponse> result = service.search(request);
        return APIResponse.<Page<CustomerResponse>>builder()
                .result(result)
                .message("All customers retrieved successfully")
                .build();
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(
            operationId = "exportAdminCustomers",
            summary = "Export filtered customers as UTF-8 CSV",
            description = "Dùng cùng filter và sort với search; không phân trang.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UTF-8 CSV attachment"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or sort"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Admin role required")
    })
    public ResponseEntity<StreamingResponseBody> exportCustomers(
            @Valid @ParameterObject AdminCustomerFilter filter) {
        StreamingResponseBody body = outputStream -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                service.export(filter, writer);
            }
        };
        String filename = "customers-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(body);
    }

    @GetMapping("/{customerId}")
    public APIResponse<CustomerResponse> getCustomerById(@PathVariable String customerId) {
        CustomerResponse result = service.getCustomerResponseById(customerId);
        return APIResponse.<CustomerResponse>builder()
                .result(result)
                .message("Customer retrieved successfully")
                .build();
    }

    @PutMapping("/{customerId}/account-status")
    public APIResponse<Boolean> toggleAccountStatus(@PathVariable String customerId) {
        Boolean status = service.toggleCustomerAccountStatus(customerId);
        return APIResponse.<Boolean>builder()
                .result(status)
                .message(status ? "Mở khóa tài khoản khách hàng thành công" : "Khóa tài khoản khách hàng thành công")
                .build();
    }

    @PutMapping(value = "/{customerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public APIResponse<CustomerResponse> updateCustomerInfo(
            @PathVariable String customerId,
            @ModelAttribute UpdateCustomerRequest request) throws IOException {
        CustomerResponse result = service.updateCustomerByAdmin(customerId, request);
        return APIResponse.<CustomerResponse>builder()
                .result(result)
                .message("Cập nhật thông tin khách hàng thành công")
                .build();
    }

    @PutMapping("/{customerId}/password")
    public APIResponse<Boolean> changeCustomerPassword(
            @PathVariable String customerId,
            @Valid @RequestBody AdminChangePasswordRequest request) {
        service.changePasswordByAdmin(customerId, request.getNewPassword());
        return APIResponse.<Boolean>builder()
                .result(true)
                .message("Đổi mật khẩu khách hàng thành công")
                .build();
    }

}
