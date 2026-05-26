package com.project.BookCarOnline.DTO.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.BookCarOnline.Entity.Account;
import com.project.BookCarOnline.Entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Data //có cả @Getter,@Setter,@NoArgsConstructor

@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {
     String accountId;
     String userName;
     Role role;
     Boolean accountStatus = true; // Default value for account status
     Date createdAt ; // Default value for created date

}
