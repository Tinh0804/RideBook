package com.project.BookCarOnline.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

@Data //có cả @Getter,@Setter,@NoArgsConstructor

@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "AccountResponse", description = "Thông tin tài khoản công khai")
public class AccountResponse {
     @Schema(description = "Account ID")
     String accountId;
     @Schema(description = "Tên đăng nhập")
     String userName;
     @Schema(description = "Vai trò tài khoản")
     RoleResponse role;
     @Schema(description = "Tài khoản đang hoạt động")
     @Builder.Default
     Boolean accountStatus = true; // Default value for account status
     @Schema(description = "Thời gian tạo tài khoản", type = "string", format = "date-time")
     Date createdAt ; // Default value for created date

}
