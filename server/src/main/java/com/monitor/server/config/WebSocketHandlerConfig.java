package com.monitor.server.config;

import com.monitor.server.websocket.ClientWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Cấu hình WebSocket Handler (không dùng STOMP)
 * Sử dụng TextWebSocketHandler trực tiếp
 */
@Configuration
@EnableWebSocket
public class WebSocketHandlerConfig implements WebSocketConfigurer {
    
    @Autowired
    private ClientWebSocketHandler clientWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Đăng ký handler cho client WebSocket
        registry.addHandler(clientWebSocketHandler, "/ws/client")
            .setAllowedOrigins("*");
    }
}

