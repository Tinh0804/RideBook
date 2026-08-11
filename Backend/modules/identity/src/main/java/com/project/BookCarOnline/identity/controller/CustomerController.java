package com.project.BookCarOnline.identity.controller;

import com.project.BookCarOnline.shared.dto.APIResponse;
import com.project.BookCarOnline.identity.dto.request.RegisterCustomerRequest;
import com.project.BookCarOnline.identity.dto.request.UpdateCustomerRequest;
import com.project.BookCarOnline.identity.dto.response.CustomerResponse;
import com.project.BookCarOnline.identity.entity.Customer;
import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;
import com.project.BookCarOnline.identity.service.CustomerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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
