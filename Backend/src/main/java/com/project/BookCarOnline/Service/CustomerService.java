package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.DTO.Request.RegisterCustomerRequest;
import com.project.BookCarOnline.DTO.Request.UpdateCustomerRequest;
import com.project.BookCarOnline.DTO.Response.AccountResponse;
import com.project.BookCarOnline.DTO.Response.CustomerResponse;
import com.project.BookCarOnline.Entity.Account;
import com.project.BookCarOnline.Entity.Customer;
import com.project.BookCarOnline.Entity.Enum.PredefinedRole;
import com.project.BookCarOnline.Entity.Role;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Mapper.CustomerMapper;
import com.project.BookCarOnline.Repository.AccountRepository;
import com.project.BookCarOnline.Repository.CustomerRepository;
import com.project.BookCarOnline.Repository.RoleRepository;
import com.project.BookCarOnline.Utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class CustomerService {

    AccountRepository accountRepository;
    CustomerRepository customerRepository;
    RoleRepository roleRepository;
    CustomerMapper mapper;
    FirebaseStorageService firebaseStorageService;

    PasswordEncoder passwordEncoder;

    @Transactional
    public CustomerResponse createCustomer(RegisterCustomerRequest request) {
        try {
            if (accountRepository.existsByUserName(request.getUserName())) {
                throw new IllegalArgumentException("Số điện thoại đã được đăng ký.");
            }
            log.info(PredefinedRole.CUSTOMER.getRoleName());
            Role role = roleRepository.findByRoleId(PredefinedRole.CUSTOMER.getRoleName())
                    .orElseThrow(()->new AppException(ErrorCode.ROLE_NOT_EXISTS));
            Account account = Account.builder()
                    .userName(request.getUserName())
                    .passWord(passwordEncoder.encode(request.getPassWord()))
                    .roleNo(role)
                    .accountStatus(true)
                    .createdAt(new Date())
                    .build();
            accountRepository.save(account); // Optional: nếu cascade không tự cập nhật

            Customer khachHang = Customer.builder()
                    .phone(request.getPhoneNumber())
                    .address(request.getAddress())
                    .customerName(request.getName())
                    .account(account)
                    .build();

            customerRepository.save(khachHang);


            return mapper.toCustomerResponse(khachHang);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vai trò không tồn tại.");
        }
    }

    public CustomerResponse getCustomerResponseById(String customerID) {
        Customer customer = customerRepository.findById(customerID)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        return mapper.toCustomerResponse(customer);
    }
    @PreAuthorize("hasRole('"+PredefinedRole.RoleName.ADMIN+"')")
    public List<Customer> getAllCustomers() {

        return  customerRepository.findAll();
    }
    public CustomerResponse getMyInfo(){
        String profileId = SecurityUtils.getCurrentProfileId().orElseThrow(()-> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        return this.getCustomerResponseById(profileId);

    }
    public CustomerResponse updateMyInfo(UpdateCustomerRequest request) throws IOException {
        String profileId = SecurityUtils.getCurrentProfileId().orElseThrow(()-> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        Customer customer = customerRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        if(request.getPhone() != null){
            customer.setPhone(request.getPhone());
        }
        if (request.getAddress() != null){
            customer.setAddress(request.getAddress());
        }
        if (request.getCustomerName() != null){
            customer.setCustomerName(request.getCustomerName());
        }
        if (request.getEmail() != null){
            customer.setEmail(request.getEmail());
        }
        if(request.getBirthDate() != null){
            customer.setBirthDate(request.getBirthDate());
        }
        if(request.getGender() != null){
            customer.setGender(request.getGender());
        }
        if(request.getAvatar() != null && !request.getAvatar().isEmpty()){
            String oldFilePath = firebaseStorageService.getFilePathFromUrl(customer.getAvatar());
            if (oldFilePath != null) {
                firebaseStorageService.deleteFile(oldFilePath);
                log.info("Đã xóa ảnh cũ thành công: {}", oldFilePath);
            }
            else {
                String accountID = SecurityUtils.getCurrentAccountId().orElseThrow(()->new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
                String folderPath = "users"+ "/" + accountID;
                String fileURL = firebaseStorageService.uploadFile(request.getAvatar(), folderPath, null);
                customer.setAvatar(fileURL);
            }

        }

        customerRepository.save(customer);

        return mapper.toCustomerResponse(customer);
    }
    public Boolean deleteMyAvatar() throws IOException {
        String profileId = SecurityUtils.getCurrentProfileId().orElseThrow(()-> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        Customer customer = customerRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        if(customer.getAvatar() != null){
            firebaseStorageService.deleteFile(customer.getAvatar());
        }else{
            throw new AppException(ErrorCode.AVATAR_NOT_FOUND);
        }
        customer.setAvatar(null);
        customerRepository.save(customer);
        return true;
    }


}
