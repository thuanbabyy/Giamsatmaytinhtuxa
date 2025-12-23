package com.monitor.client.websocket;

import com.google.gson.Gson;
import com.monitor.client.command.CommandHandler;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket Client để nhận lệnh từ server
 */
public class ClientWebSocket extends WebSocketClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientWebSocket.class);
    
    private String machineId;
    @SuppressWarnings("unused")
    private String secretKey; // Có thể dùng để xác thực WebSocket trong tương lai
    private CommandHandler commandHandler;
    private Gson gson;
    
    public ClientWebSocket(URI serverUri, String machineId, String secretKey, CommandHandler commandHandler) {
        super(serverUri);
        this.machineId = machineId;
        this.secretKey = secretKey;
        this.commandHandler = commandHandler;
        this.gson = new Gson();
    }
    
    @Override
    public void onOpen(ServerHandshake handshake) {
        logger.info("Đã kết nối WebSocket đến server");
        
        // Gửi thông tin xác thực
        Map<String, String> auth = new HashMap<>();
        auth.put("machineId", machineId);
        auth.put("type", "client");
        
        send(gson.toJson(auth));
    }
    
    @Override
    public void onMessage(String message) {
        logger.info("Nhận được lệnh từ server: {}", message);
        
        try {
            // Parse lệnh từ server
            @SuppressWarnings("unchecked")
            Map<String, Object> commandData = (Map<String, Object>) gson.fromJson(message, Map.class);
            String command = (String) commandData.get("command");
            String targetMachineId = (String) commandData.get("machineId");
            
            // Kiểm tra xem lệnh có dành cho máy này không
            if (targetMachineId != null && !targetMachineId.equals(machineId)) {
                logger.debug("Lệnh không dành cho máy này: {}", targetMachineId);
                return;
            }
            
            // Xử lý lệnh
            Map<String, Object> result = commandHandler.handleCommand(command);
            
            // Gửi kết quả về server
            Map<String, Object> response = new HashMap<>();
            response.put("machineId", machineId);
            response.put("result", result);
            
            send(gson.toJson(response));
            
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý lệnh từ server: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.warn("WebSocket đã đóng. Code: {}, Reason: {}", code, reason);
        
        // Tự động kết nối lại sau 5 giây (trong thread riêng để tránh lỗi)
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                if (!isOpen()) {
                    logger.info("Đang thử kết nối lại WebSocket...");
                    reconnect();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error("Lỗi khi reconnect: {}", e.getMessage(), e);
            }
        }).start();
    }
    
    @Override
    public void onError(Exception ex) {
        logger.error("Lỗi WebSocket: {}", ex.getMessage(), ex);
    }
}

