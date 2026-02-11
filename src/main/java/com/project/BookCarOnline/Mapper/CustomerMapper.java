package com.project.BookCarOnline.Mapper;


import com.project.BookCarOnline.DTO.Response.CustomerResponse;
import com.project.BookCarOnline.Entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Mapping(source = "customerId", target = "customerId")
    CustomerResponse toCustomerResponse(Customer customer);
}
