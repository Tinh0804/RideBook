package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Response.CustomerResponse;
import com.project.BookCarOnline.Service.CustomerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminCustomerController {

    CustomerService service;

    @GetMapping
    public APIResponse<Page<CustomerResponse>> getAllCustomer(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        Page<CustomerResponse> result = service.getAllCustomers(page, size, search);
        return APIResponse.<Page<CustomerResponse>>builder()
                .result(result)
                .message("All customers retrieved successfully")
                .build();
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
}
