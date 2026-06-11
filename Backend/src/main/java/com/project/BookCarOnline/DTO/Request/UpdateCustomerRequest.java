package com.project.BookCarOnline.DTO.Request;

import com.google.api.client.util.DateTime;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;

@Data
public class UpdateCustomerRequest {
    String address;
    String phone;
    String customerName;
    String email;
    String gender;
    MultipartFile avatar;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date birthDate;

}
