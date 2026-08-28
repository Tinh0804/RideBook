package com.project.BookCarOnline;

import com.project.BookCarOnline.booking.mapper.RatingMapper;
import com.project.BookCarOnline.communication.mapper.ChatMapper;
import com.project.BookCarOnline.communication.mapper.NotificationMapper;
import com.project.BookCarOnline.finance.mapper.WalletMapper;
import com.project.BookCarOnline.identity.mapper.AccountMapper;
import com.project.BookCarOnline.identity.mapper.CustomerMapper;
import com.project.BookCarOnline.identity.mapper.DriverMapper;
import com.project.BookCarOnline.promotion.mapper.PromotionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MapperBeanRegistrationTest {

    @Test
    void allMappersAreRegisteredAsSpringBeans() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.scan(
                    "com.project.BookCarOnline.booking.mapper",
                    "com.project.BookCarOnline.communication.mapper",
                    "com.project.BookCarOnline.finance.mapper",
                    "com.project.BookCarOnline.identity.mapper",
                    "com.project.BookCarOnline.promotion.mapper");
            context.refresh();

            assertNotNull(context.getBean(AccountMapper.class));
            assertNotNull(context.getBean(CustomerMapper.class));
            assertNotNull(context.getBean(DriverMapper.class));
            assertNotNull(context.getBean(RatingMapper.class));
            assertNotNull(context.getBean(ChatMapper.class));
            assertNotNull(context.getBean(NotificationMapper.class));
            assertNotNull(context.getBean(WalletMapper.class));
            assertNotNull(context.getBean(PromotionMapper.class));
        }
    }
}
