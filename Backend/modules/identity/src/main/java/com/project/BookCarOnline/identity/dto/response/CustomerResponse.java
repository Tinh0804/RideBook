package com.project.BookCarOnline.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "CustomerResponse", description = "Thông tin khách hàng dành cho admin")
public class CustomerResponse {
    @Schema(description = "Customer ID")
    private String customerId;
    @Schema(description = "Tên khách hàng")
    private String customerName;
    @Schema(description = "Số điện thoại")
    private String phone;
    @Schema(description = "Địa chỉ", nullable = true)
    private String address;
    @Schema(description = "Email", format = "email", nullable = true)
    private String email;
    @Schema(description = "Giới tính", nullable = true)
    private String gender;
    @Schema(description = "URL avatar", nullable = true)
    private String avatar;

    @Schema(description = "Ngày sinh", type = "string", format = "date", nullable = true)
    private String birthDate;

    @Schema(description = "Thông tin tài khoản")
    private AccountResponse account;
}
