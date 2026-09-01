package com.project.BookCarOnline.identity.mapper;


import com.project.BookCarOnline.identity.dto.response.AccountResponse;
import com.project.BookCarOnline.identity.dto.response.RoleResponse;
import com.project.BookCarOnline.identity.entity.Account;
import com.project.BookCarOnline.identity.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(source = "roleNo", target = "role")
    AccountResponse toAccountResponse(Account account);

    default RoleResponse toRoleResponse(Role role) {
        return role == null ? null : new RoleResponse(role.getRoleName(), role.getDescription());
    }
}
