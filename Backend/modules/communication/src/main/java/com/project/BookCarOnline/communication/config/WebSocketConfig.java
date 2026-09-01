package com.project.BookCarOnline.communication.config;


import com.project.BookCarOnline.shared.config.AllowedOrigins;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final String[] allowedOrigins;

    public WebSocketConfig(@Value("${app.security.allowed-origins}") String allowedOrigins) {
        this.allowedOrigins = AllowedOrigins.parse(allowedOrigins);
    }

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
                .setAllowedOrigins(allowedOrigins);

        // 2. ENDPOINT CHO WEB FRONTEND CŨ (Có SockJS fallback)
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins)
                .withSockJS();
    }
}
