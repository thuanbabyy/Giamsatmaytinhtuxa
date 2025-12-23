package com.monitor.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * Cấu hình WebSocket cho hệ thống
 * Hỗ trợ STOMP protocol để giao tiếp real-time
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint WebSocket với CORS
        registry.addEndpoint("/ws-stomp")
            .setAllowedOriginPatterns("http://localhost:8080", "http://127.0.0.1:8080")
            .withSockJS()
            ;
            
        // Thêm endpoint không dùng SockJS
        registry.addEndpoint("/ws-stomp")
            .setAllowedOriginPatterns("http://localhost:8080", "http://127.0.0.1:8080")
            .setHandshakeHandler(new DefaultHandshakeHandler())
            .setAllowedOrigins("http://localhost:8080", "http://127.0.0.1:8080");
    }
}

