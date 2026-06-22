package com.project.BookCarOnline.DTO.Request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class RegisterCustomerRequest {
    String userName;
    String passWord;

    String name;
    String phoneNumber;
    String address;
    String confirm;

}
