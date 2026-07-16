package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.DTO.Request.RegisterCustomerRequest;
import com.project.BookCarOnline.DTO.Request.UpdateCustomerRequest;
import com.project.BookCarOnline.DTO.Response.AccountResponse;
import com.project.BookCarOnline.DTO.Response.CustomerResponse;
import com.project.BookCarOnline.Entity.Account;
import com.project.BookCarOnline.Entity.Customer;
import com.project.BookCarOnline.Entity.Enum.PredefinedRole;
import com.project.BookCarOnline.Entity.Enum.Provider;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerService {

    AccountRepository accountRepository;
    CustomerRepository customerRepository;
    RoleRepository roleRepository;
    CustomerMapper mapper;
    FirebaseService firebaseService;

    PasswordEncoder passwordEncoder;

    @Transactional
    public CustomerResponse createCustomer(RegisterCustomerRequest request) {
        try {
            if (accountRepository.existsByUserName(request.getUserName())) {
                throw new IllegalArgumentException("Số điện thoại đã được đăng ký.");
            }
            log.info(PredefinedRole.CUSTOMER.getRoleName());
            Role role = roleRepository.findByRoleName(PredefinedRole.CUSTOMER)
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTS));
            Account account = Account.builder()
                    .userName(request.getUserName())
                    .passWord(passwordEncoder.encode(request.getPassWord()))
                    .roleNo(role)
                    .provider(Provider.LOCAL)
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

    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public Page<CustomerResponse> getAllCustomers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("customerName").ascending());
        Page<Customer> customers;
        if (search == null || search.trim().isEmpty()) {
            customers = customerRepository.findAll(pageable);
        } else {
            String searchTerm = "%" + search.trim().toLowerCase() + "%";
            customers = customerRepository.searchCustomers(searchTerm, pageable);
        }
        return customers.map(mapper::toCustomerResponse);
    }

    @Transactional
    public Boolean toggleCustomerAccountStatus(String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        Account account = customer.getAccount();
        account.setAccountStatus(!account.getAccountStatus());
        accountRepository.save(account);
        return account.getAccountStatus();
    }

    public CustomerResponse getMyInfo() {
        String profileId = SecurityUtils.getCurrentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        return this.getCustomerResponseById(profileId);

    }

    public CustomerResponse updateMyInfo(UpdateCustomerRequest request) throws IOException {
        Customer customer = getCurrentCustomer();

        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getCustomerName() != null) {
            customer.setCustomerName(request.getCustomerName());
        }
        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail());
        }
        if (request.getBirthDate() != null) {
            customer.setBirthDate(request.getBirthDate());
        }
        if (request.getGender() != null) {
            customer.setGender(request.getGender());
        }
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String oldFilePath = firebaseService.getFilePathFromUrl(customer.getAvatar());
            if (oldFilePath != null) {
                firebaseService.deleteFile(oldFilePath);
                log.info("Đã xóa ảnh cũ thành công: {}", oldFilePath);
            } else {
                String accountID = SecurityUtils.getCurrentAccountId()
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
                String folderPath = "users" + "/" + accountID;
                String fileURL = firebaseService.uploadFile(request.getAvatar(), folderPath, null);
                customer.setAvatar(fileURL);
            }

        }

        customerRepository.save(customer);

        return mapper.toCustomerResponse(customer);
    }
    private Customer getCurrentCustomer(){
        String profileId = SecurityUtils.getCurrentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.EXCHANGE_TOKEN_FAIL));
        Customer customer = customerRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        return customer;
    }
    public Boolean deleteMyAvatar() throws IOException {
        String profileId = SecurityUtils.getCurrentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        Customer customer = customerRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        if (customer.getAvatar() != null) {
            firebaseService.deleteFile(customer.getAvatar());
        } else {
            throw new AppException(ErrorCode.AVATAR_NOT_FOUND);
        }
        customer.setAvatar(null);
        customerRepository.save(customer);
        return true;
    }


    public CustomerResponse updateCustomerByAdmin(String customerId, UpdateCustomerRequest request) throws IOException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        if (request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()) {
            customer.setCustomerName(request.getCustomerName());
        }
        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            customer.setAddress(request.getAddress());
        }
        if (request.getBirthDate() != null) {
            customer.setBirthDate(request.getBirthDate());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            customer.setEmail(request.getEmail());
        }
        if (request.getGender() != null) {
            customer.setGender(request.getGender());
        }
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String oldFilePath = customer.getAvatar() != null ? firebaseService.getFilePathFromUrl(customer.getAvatar()) : null;
            if (oldFilePath != null) {
                firebaseService.deleteFile(oldFilePath);
                log.info("Đã xóa ảnh cũ thành công: {}", oldFilePath);
            }
            String accountID = customer.getAccount().getUserName();
            String folderPath = "users" + "/" + accountID;
            String fileURL = firebaseService.uploadFile(request.getAvatar(), folderPath, null);
            customer.setAvatar(fileURL);
        }

        customerRepository.save(customer);
        return mapper.toCustomerResponse(customer);
    }

    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public void changePasswordByAdmin(String customerId, String newPassword) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        Account account = customer.getAccount();
        account.setPassWord(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }
}
