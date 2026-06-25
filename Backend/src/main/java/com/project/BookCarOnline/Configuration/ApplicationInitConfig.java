package com.project.BookCarOnline.Configuration;

import com.project.BookCarOnline.DTO.Response.CustomerResponse;

import com.project.BookCarOnline.Entity.Account;
import com.project.BookCarOnline.Entity.Customer;
import com.project.BookCarOnline.Entity.Enum.PredefinedRole;
import com.project.BookCarOnline.Entity.Enum.Provider;
import com.project.BookCarOnline.Entity.Role;
import com.project.BookCarOnline.Repository.AccountRepository;
import com.project.BookCarOnline.Repository.CustomerRepository;
import com.project.BookCarOnline.Repository.RoleRepository;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

@Configuration
@Slf4j
public class ApplicationInitConfig {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private static final String ADMIN_USER_NAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    @Bean
    ApplicationRunner applicationRunner() {
        return args -> {
            log.info("Bắt đầu khởi tạo dữ liệu hệ thống...");

            // 1. Khởi tạo tất cả các Role
            for (PredefinedRole predefinedRole : PredefinedRole.values()) {
                if (!roleRepository.existsByRoleName(predefinedRole)) {
                    roleRepository.save(
                            Role.builder()
                                    .roleName(predefinedRole)
                                    .description("Role " + predefinedRole.name())
                                    .build()
                    );
                    log.info("Đã tạo Role {}", predefinedRole.name());
                }
            }


            // Lấy role Admin để gán cho tài khoản (chắc chắn đã tồn tại sau bước 1)
            Role adminRole = roleRepository.findByRoleName(PredefinedRole.ADMIN).orElseThrow();

            // 3. Khởi tạo tài khoản Admin mặc định
            if (!accountRepository.existsByUserName(ADMIN_USER_NAME)) {
                Account account = Account.builder()
                        .userName(ADMIN_USER_NAME)
                        .passWord(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roleNo(adminRole)
                        .accountStatus(true)
                        .provider(Provider.LOCAL)
                        .build();

                // Lưu Account trước
                account = accountRepository.save(account);

                // Sau đó mới tạo Profile Customer cho Admin
                Customer adminProfile = Customer.builder()
                        .customerName("System Admin")
                        .phone("0366900821")
                        .email("lhqtinh2005@gmail.com")
                        .account(account)
                        .build();
                customerRepository.save(adminProfile);

                log.warn(">>> Account Admin khởi tạo thành công với mật khẩu: {}", ADMIN_PASSWORD);
            } else {
                log.info("Tài khoản Admin đã tồn tại.");
            }
        };
    }
}