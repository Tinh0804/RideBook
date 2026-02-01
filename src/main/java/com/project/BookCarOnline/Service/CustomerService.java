package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.DTO.Request.RegisterCustomerRequest;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    PasswordEncoder passwordEncoder;

    @Transactional
    public CustomerResponse createCustomer(RegisterCustomerRequest request) {
        try {
            if (accountRepository.existsByUserName(request.getUserName())) {
                throw new IllegalArgumentException("Số điện thoại đã được đăng ký.");
            }
            log.info(PredefinedRole.CUSTOMER.getDescription());
            Role role = roleRepository.findByRoleId(PredefinedRole.CUSTOMER.getDescription())
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

    public CustomerResponse getCustomerById(String customerID) {
        Customer customer = customerRepository.findById(customerID)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        return mapper.toCustomerResponse(customer);
    }
    public List<Customer> getAllCustomers() {

        return  customerRepository.findAll();
    }
    public CustomerResponse getMyInfo(){
        String profileId = SecurityUtils.getCurrentProfileId().orElseThrow(()-> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        Customer customer = customerRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        Role role = roleRepository.findByRoleId(PredefinedRole.CUSTOMER.getDescription())
                .orElseThrow(()->new AppException(ErrorCode.ROLE_NOT_EXISTS));


        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .customerName(customer.getCustomerName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .build();
    }


}
