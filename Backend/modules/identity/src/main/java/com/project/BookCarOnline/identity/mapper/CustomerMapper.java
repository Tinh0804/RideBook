package com.project.BookCarOnline.identity.mapper;


import com.project.BookCarOnline.identity.dto.response.CustomerResponse;
import com.project.BookCarOnline.identity.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = AccountMapper.class)
public interface CustomerMapper {
    @Mapping(source = "customerId", target = "customerId")
    CustomerResponse toCustomerResponse(Customer customer);
}
