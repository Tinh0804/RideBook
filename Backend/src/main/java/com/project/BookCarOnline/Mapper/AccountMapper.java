package com.project.BookCarOnline.Mapper;


import com.project.BookCarOnline.DTO.Request.AccountRequest;
import com.project.BookCarOnline.DTO.Response.AccountResponse;
import com.project.BookCarOnline.Entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    Account toAccount(AccountRequest request);

    @Mapping(source = "roleNo", target = "role")
    AccountResponse toAccountResponse(Account account);
}
