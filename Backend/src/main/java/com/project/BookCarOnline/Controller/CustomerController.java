package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.RegisterCustomerRequest;
import com.project.BookCarOnline.DTO.Request.UpdateCustomerRequest;
import com.project.BookCarOnline.DTO.Response.CustomerResponse;
import com.project.BookCarOnline.Entity.Customer;
import com.project.BookCarOnline.Service.CustomerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class CustomerController {
    CustomerService service;

    @PostMapping("/register")
    APIResponse<CustomerResponse> createCustomer(@RequestBody RegisterCustomerRequest request) {
        CustomerResponse customerResponse = service.createCustomer(request);
        return APIResponse.<CustomerResponse>builder()
                .result(customerResponse)
                .message("Customer created successfully")
                .build();
    }

    @GetMapping("/my-info")
    APIResponse<CustomerResponse> getMyInfo() {
        CustomerResponse customerResponse = service.getMyInfo();
        return APIResponse.<CustomerResponse>builder()
                .result(customerResponse)
                .message("Information of you")
                .build();
    }

    @GetMapping("")
    @SecurityRequirement(name = "bearerAuth")
    APIResponse<List<Customer>> getAllCustomer() {
        List<Customer> customerResponse = service.getAllCustomers();
        return APIResponse.<List<Customer>>builder()
                .result(customerResponse)
                .message("All customers retrieved successfully")
                .build();
    }

    @PutMapping(value = "/my-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    APIResponse<CustomerResponse> updateMyInfo(@ModelAttribute UpdateCustomerRequest request) throws IOException {
        CustomerResponse customerResponse = service.updateMyInfo(request);
        return APIResponse.<CustomerResponse>builder()
                .result(customerResponse)
                .message("Your information has been updated successfully")
                .build();
    }

    @DeleteMapping("/my-avatar")
    APIResponse<Boolean> deleteMyAvatar() throws IOException {
        Boolean result = service.deleteMyAvatar();
        return APIResponse.<Boolean>builder()
                .result(result)
                .message("Your avatar has been deleted successfully")
                .build();
    }
}
