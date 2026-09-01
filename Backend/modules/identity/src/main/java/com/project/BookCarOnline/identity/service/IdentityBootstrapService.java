package com.project.BookCarOnline.identity.service;

import com.project.BookCarOnline.identity.entity.Account;
import com.project.BookCarOnline.identity.entity.Customer;
import com.project.BookCarOnline.identity.entity.Role;
import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;
import com.project.BookCarOnline.identity.entity.enums.Provider;
import com.project.BookCarOnline.identity.repository.AccountRepository;
import com.project.BookCarOnline.identity.repository.CustomerRepository;
import com.project.BookCarOnline.identity.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityBootstrapService {

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public void initialize(String adminUsername, String adminPassword) {
        for (PredefinedRole predefinedRole : PredefinedRole.values()) {
            if (!roleRepository.existsByRoleName(predefinedRole)) {
                roleRepository.save(Role.builder()
                        .roleName(predefinedRole)
                        .description("Role " + predefinedRole.name())
                        .build());
            }
        }

        if (accountRepository.existsByUserName(adminUsername)) {
            return;
        }

        Role adminRole = roleRepository.findByRoleName(PredefinedRole.ADMIN).orElseThrow();
        Account account = accountRepository.save(Account.builder()
                .userName(adminUsername)
                .passWord(passwordEncoder.encode(adminPassword))
                .roleNo(adminRole)
                .accountStatus(true)
                .emailVerified(true)
                .provider(Provider.LOCAL)
                .build());
        customerRepository.save(Customer.builder()
                .customerName("System Admin")
                .phone("0366900821")
                .email("lhqtinh2005@gmail.com")
                .account(account)
                .build());
        log.warn("Default admin account created; change its password immediately");
    }
}
