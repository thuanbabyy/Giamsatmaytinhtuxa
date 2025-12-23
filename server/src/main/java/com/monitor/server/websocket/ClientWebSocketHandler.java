package com.monitor.server.websocket;

import com.google.gson.Gson;
import com.monitor.server.service.CommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket Handler cho client connections
 * Xử lý kết nối WebSocket từ client để nhận lệnh điều khiển
 */
@Component
public class ClientWebSocketHandler extends TextWebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientWebSocketHandler.class);
    
    @Autowired
    private CommandService commandService;
    
    private Gson gson = new Gson();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("Client WebSocket đã kết nối: {}", session.getId());
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            logger.debug("Nhận message từ client: {}", payload);
            
            // Parse message
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) gson.fromJson(payload, Map.class);
            String machineId = (String) data.get("machineId");
            String type = (String) data.get("type");
            
            if ("client".equals(type) && machineId != null) {
                // Đăng ký client
                commandService.registerClient(machineId, session);
                
                // Gửi xác nhận
                Map<String, Object> response = new HashMap<>();
                response.put("status", "connected");
                response.put("message", "Đã kết nối thành công");
                session.sendMessage(new TextMessage(gson.toJson(response)));
            }
            
            // Xử lý kết quả từ client (sau khi thực thi lệnh)
            if (data.containsKey("result")) {
                logger.info("Nhận kết quả từ client {}: {}", machineId, data.get("result"));
            }
            
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý WebSocket message: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Client WebSocket đã đóng: {} - {}", session.getId(), status);
        
        // Hủy đăng ký client
        commandService.unregisterClientBySession(session);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Lỗi WebSocket transport: {}", exception.getMessage(), exception);
    }
}

