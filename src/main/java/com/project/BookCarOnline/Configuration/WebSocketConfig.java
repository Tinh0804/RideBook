package com.project.BookCarOnline.Configuration;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Đây là lệnh kích hoạt SimpMessagingTemplate
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
        // Đây là điểm kết nối (URL) để Frontend (Mobile/Web) kết nối vào
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép mọi nguồn (CORS)
                .withSockJS(); // Hỗ trợ fallback nếu trình duyệt không hỗ trợ WS thuần
    }
}