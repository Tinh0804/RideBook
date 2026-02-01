package com.project.BookCarOnline.Mapper;


import com.project.BookCarOnline.DTO.Response.CustomerResponse;
import com.project.BookCarOnline.Entity.Customer;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponse toCustomerResponse(Customer customer);
}
