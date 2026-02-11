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
    // Chỉ chạy logic này nếu cấu hình spring.sql.init.enabled=true (tùy chọn)
    @ConditionalOnProperty(prefix = "spring.sql", name = "init.enabled", havingValue = "true", matchIfMissing = true)
    ApplicationRunner applicationRunner() {
        return args -> {
            // 1. Khởi tạo Role ADMIN nếu chưa có
            Role adminRole = roleRepository.findByRoleName(PredefinedRole.RoleName.ADMIN)
                    .orElseGet(() -> roleRepository.save(
                            Role.builder()
                                    .roleId(PredefinedRole.RoleName.ADMIN)
                                    .roleName("Quản trị viên")
                                    .build()
                    ));

            // 2. Khởi tạo Role CUSTOMER nếu chưa có (Để sẵn cho logic OAuth2)
            if (!roleRepository.existsById(PredefinedRole.RoleName.ADMIN)) {
                roleRepository.save(Role.builder().roleName(PredefinedRole.RoleName.CUSTOMER).build());
            }

            // 3. Khởi tạo tài khoản Admin mặc định
            if (!accountRepository.existsByUserName(ADMIN_USER_NAME)) {
                Account account = Account.builder()
                        .userName(ADMIN_USER_NAME)
                        .passWord(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roleNo(adminRole)
                        .accountStatus(true)
                        .provider(Provider.LOCAL.name().toLowerCase(Locale.ROOT))
                        .build();
                accountRepository.save(account);

                Customer adminProfile = Customer.builder()
                        .customerName("System Admin")
                        .phone("0366900821")
                        .email("lhqtinh2005@gmail.com")
                        .account(account)
                        .build();
                customerRepository.save(adminProfile);

                log.warn("Account Admin khởi tạo thành công với mật khẩu mặc định: {}", ADMIN_PASSWORD);
            } else {
                log.info("Tài khoản Admin đã tồn tại, bỏ qua bước khởi tạo.");
            }
        };
    }
}