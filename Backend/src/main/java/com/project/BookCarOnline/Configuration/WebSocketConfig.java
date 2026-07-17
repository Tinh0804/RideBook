package com.project.BookCarOnline.Configuration;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kích hoạt một "broker" đơn giản để đẩy tin từ Server xuống Client
        // Client sẽ subscribe các đường dẫn bắt đầu bằng /topic
        config.enableSimpleBroker("/topic");

        // Các tin nhắn từ Client gửi lên Server sẽ bắt đầu bằng /app
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. ENDPOINT CHO MOBILE APP
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        // 2. ENDPOINT CHO WEB FRONTEND CŨ (Có SockJS fallback)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}