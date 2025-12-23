package com.monitor.server.service;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service quản lý gửi lệnh đến client qua WebSocket
 * - Lưu trữ WebSocket sessions của các client
 * - Gửi lệnh điều khiển đến client
 */
@Service
public class CommandService {
    
    private static final Logger logger = LoggerFactory.getLogger(CommandService.class);
    
    // Lưu trữ WebSocket sessions theo machineId
    private final Map<String, WebSocketSession> clientSessions = new ConcurrentHashMap<>();
    // Lưu trữ mapping session -> machineId
    private final Map<WebSocketSession, String> sessionToMachineId = new ConcurrentHashMap<>();
    
    private Gson gson = new Gson();
    
    /**
     * Đăng ký WebSocket session của client
     */
    public void registerClient(String machineId, WebSocketSession session) {
        clientSessions.put(machineId, session);
        sessionToMachineId.put(session, machineId);
        logger.info("Đã đăng ký client: {}", machineId);
    }
    
    /**
     * Hủy đăng ký WebSocket session
     */
    public void unregisterClient(String machineId) {
        WebSocketSession session = clientSessions.remove(machineId);
        if (session != null) {
            sessionToMachineId.remove(session);
        }
        logger.info("Đã hủy đăng ký client: {}", machineId);
    }
    
    /**
     * Hủy đăng ký WebSocket session theo session
     */
    public void unregisterClientBySession(WebSocketSession session) {
        String machineId = sessionToMachineId.remove(session);
        if (machineId != null) {
            clientSessions.remove(machineId);
            logger.info("Đã hủy đăng ký client theo session: {}", machineId);
        }
    }
    
    /**
     * Gửi lệnh đến client
     * @param machineId ID của máy tính
     * @param command Lệnh cần gửi (KILL_PROCESS, SHUTDOWN, LOCK_KEYBOARD, SCREENSHOT)
     * @param parameter Tham số của lệnh (ví dụ: PID cho KILL_PROCESS)
     * @return true nếu gửi thành công
     */
    public boolean sendCommand(String machineId, String command, String parameter) {
        WebSocketSession session = clientSessions.get(machineId);
        
        if (session == null || !session.isOpen()) {
            logger.warn("Không tìm thấy session hoặc session đã đóng cho machine: {}", machineId);
            return false;
        }
        
        try {
            // Tạo payload lệnh
            Map<String, Object> commandData = new HashMap<>();
            commandData.put("command", parameter != null ? command + " " + parameter : command);
            commandData.put("machineId", machineId);
            commandData.put("timestamp", System.currentTimeMillis());
            
            String jsonCommand = gson.toJson(commandData);
            session.sendMessage(new TextMessage(jsonCommand));
            
            logger.info("Đã gửi lệnh {} đến machine: {}", command, machineId);
            return true;
            
        } catch (IOException e) {
            logger.error("Lỗi khi gửi lệnh đến machine {}: {}", machineId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Kiểm tra client có đang online không
     */
    public boolean isClientOnline(String machineId) {
        WebSocketSession session = clientSessions.get(machineId);
        return session != null && session.isOpen();
    }
    
    /**
     * Lấy danh sách các client đang online
     */
    public java.util.Set<String> getOnlineClients() {
        return clientSessions.keySet();
    }
}

