package com.project.BookCarOnline.app.config;

import com.project.BookCarOnline.identity.service.IdentityBootstrapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {

    private final IdentityBootstrapService identityBootstrapService;

    private static final String ADMIN_USER_NAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    @Bean
    ApplicationRunner applicationRunner() {
        return args -> {
            log.info("Bắt đầu khởi tạo dữ liệu hệ thống...");
            identityBootstrapService.initialize(ADMIN_USER_NAME, ADMIN_PASSWORD);
        };
    }
}
