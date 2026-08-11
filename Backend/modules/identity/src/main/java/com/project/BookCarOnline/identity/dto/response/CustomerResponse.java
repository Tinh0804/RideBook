package com.project.BookCarOnline.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

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
