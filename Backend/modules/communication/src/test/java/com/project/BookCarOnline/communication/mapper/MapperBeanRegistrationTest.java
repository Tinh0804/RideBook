package com.project.BookCarOnline.communication.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MapperBeanRegistrationTest {

    @Test
    void mappersAreRegisteredAsSpringBeans() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.scan("com.project.BookCarOnline.communication.mapper");
            context.refresh();

            assertNotNull(context.getBean(ChatMapper.class));
            assertNotNull(context.getBean(NotificationMapper.class));
        }
    }
}
