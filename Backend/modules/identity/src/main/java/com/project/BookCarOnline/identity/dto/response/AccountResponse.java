package com.project.BookCarOnline.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Date;

@Data //có cả @Getter,@Setter,@NoArgsConstructor

@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {
     String accountId;
     String userName;
     RoleResponse role;
     @Builder.Default
     Boolean accountStatus = true; // Default value for account status
     Date createdAt ; // Default value for created date

}
