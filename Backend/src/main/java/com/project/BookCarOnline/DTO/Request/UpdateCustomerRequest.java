package com.project.BookCarOnline.DTO.Request;

import com.google.api.client.util.DateTime;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;

@Data
public class UpdateCustomerRequest {
    String diaChi;
    String SDT;
    String tenKH;
    String email;
    String gioiTinh;
    MultipartFile avatar;
    Date ngaySinh;

}
