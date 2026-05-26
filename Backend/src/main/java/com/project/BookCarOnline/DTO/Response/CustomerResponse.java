package com.project.BookCarOnline.DTO.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.BookCarOnline.Entity.Account;
import com.project.BookCarOnline.Entity.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerResponse {
    private String customerId;
    private String customerName;
    private String phone;
    private String address;
    private String email;
    private String gender;
    private String avatar;

    private String birthDate;

    private AccountResponse account;
}
