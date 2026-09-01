package com.project.BookCarOnline.identity.service;

import com.project.BookCarOnline.identity.dto.request.RegisterCustomerRequest;
import com.project.BookCarOnline.identity.dto.request.UpdateCustomerRequest;
import com.project.BookCarOnline.identity.dto.request.AdminCustomerFilter;
import com.project.BookCarOnline.identity.dto.request.AdminCustomerSearchRequest;
import com.project.BookCarOnline.identity.dto.response.AccountResponse;
import com.project.BookCarOnline.identity.dto.response.CustomerResponse;
import com.project.BookCarOnline.identity.entity.Account;
import com.project.BookCarOnline.identity.entity.Customer;
import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;
import com.project.BookCarOnline.identity.entity.enums.Provider;
import com.project.BookCarOnline.identity.entity.Role;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.identity.mapper.CustomerMapper;
import com.project.BookCarOnline.identity.repository.AccountRepository;
import com.project.BookCarOnline.identity.repository.CustomerRepository;
import com.project.BookCarOnline.identity.repository.RoleRepository;
import com.project.BookCarOnline.identity.repository.specification.CustomerSpecifications;
import com.project.BookCarOnline.identity.service.FirebaseService;
import com.project.BookCarOnline.shared.security.SecurityUtils;
import com.project.BookCarOnline.shared.util.AdminSortParser;
import com.project.BookCarOnline.shared.util.CsvUtils;
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
import java.io.Writer;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerService {

    private static final int EXPORT_PAGE_SIZE = 500;
    private static final Map<String, String> ADMIN_SORT_FIELDS = Map.of(
            "customerName", "customerName",
            "phone", "phone",
            "email", "email",
            "birthDate", "birthDate",
            "createdAt", "account.createdAt");

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
        AdminCustomerSearchRequest request = new AdminCustomerSearchRequest();
        request.setPage(page);
        request.setSize(size);
        request.setSearch(search);
        return search(request);
    }

    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public Page<CustomerResponse> search(AdminCustomerSearchRequest request) {
        validate(request);
        Pageable pageable = PageRequest.of(
                request.getPage(), request.getSize(), parseSort(request.getSort()));
        return customerRepository.findAll(CustomerSpecifications.from(request), pageable)
                .map(mapper::toCustomerResponse);
    }

    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public void export(AdminCustomerFilter filter, Writer writer) {
        validate(filter);
        CsvUtils.writeBom(writer);
        CsvUtils.writeRow(
                writer,
                "customerId",
                "customerName",
                "phone",
                "email",
                "gender",
                "birthDate",
                "address",
                "accountStatus",
                "createdAt");

        int pageNumber = 0;
        Page<Customer> customers;
        do {
            Pageable pageable = PageRequest.of(pageNumber++, EXPORT_PAGE_SIZE, parseSort(filter.getSort()));
            customers = customerRepository.findAll(CustomerSpecifications.from(filter), pageable);
            customers.forEach(customer -> CsvUtils.writeRow(
                    writer,
                    customer.getCustomerId(),
                    customer.getCustomerName(),
                    customer.getPhone(),
                    customer.getEmail(),
                    customer.getGender(),
                    formatDate(customer.getBirthDate()),
                    customer.getAddress(),
                    customer.getAccount() != null ? customer.getAccount().getAccountStatus() : null,
                    customer.getAccount() != null ? formatDateTime(customer.getAccount().getCreatedAt()) : null));
        } while (customers.hasNext());
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

    private Sort parseSort(String sort) {
        return AdminSortParser.parse(sort, ADMIN_SORT_FIELDS, "customerName:asc", "customerId");
    }

    private void validate(AdminCustomerFilter filter) {
        if (filter.getCreatedFrom() != null
                && filter.getCreatedTo() != null
                && filter.getCreatedFrom().isAfter(filter.getCreatedTo())) {
            throw new IllegalArgumentException("createdFrom phải nhỏ hơn hoặc bằng createdTo");
        }
        if (filter instanceof AdminCustomerSearchRequest request
                && (request.getPage() < 0 || request.getSize() < 1 || request.getSize() > 100)) {
            throw new IllegalArgumentException("page phải >= 0 và size phải trong khoảng 1..100");
        }
    }

    private String formatDate(Date value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate().toString();
        }
        return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
    }

    private String formatDateTime(Date value) {
        if (value == null) {
            return null;
        }
        LocalDateTime localDateTime = value instanceof Timestamp timestamp
                ? timestamp.toLocalDateTime()
                : LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
        return localDateTime.toString();
    }
}
