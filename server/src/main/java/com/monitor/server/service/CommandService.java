package com.monitor.server.service;

import com.google.gson.Gson;
import com.monitor.server.model.Command;
import com.monitor.server.repository.CommandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service quản lý gửi lệnh đến client qua WebSocket
 */
@Service
public class CommandService {

    private static final Logger logger = LoggerFactory.getLogger(CommandService.class);

    // Lưu trữ WebSocket sessions theo machineId
    private final Map<String, WebSocketSession> clientSessions = new ConcurrentHashMap<>();
    // Lưu trữ mapping session -> machineId
    private final Map<WebSocketSession, String> sessionToMachineId = new ConcurrentHashMap<>();

    @Autowired
    private CommandRepository commandRepository;

    @Autowired
    private MachineService machineService;

    private Gson gson = new Gson();

    /**
     * Đăng ký WebSocket session của client
     */
    public void registerClient(String machineId, WebSocketSession session) {
        clientSessions.put(machineId, session);
        sessionToMachineId.put(session, machineId);
        machineService.updateOnlineStatus(machineId, true);
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
        machineService.updateOnlineStatus(machineId, false);
        logger.info("Đã hủy đăng ký client: {}", machineId);
    }

    /**
     * Hủy đăng ký WebSocket session theo session
     */
    public void unregisterClientBySession(WebSocketSession session) {
        String machineId = sessionToMachineId.remove(session);
        if (machineId != null) {
            clientSessions.remove(machineId);
            machineService.updateOnlineStatus(machineId, false);
            logger.info("Đã hủy đăng ký client theo session: {}", machineId);
        }
    }

    /**
     * Gửi lệnh khóa bàn phím chuột
     */
    @Transactional
    public boolean sendLockCommand(String machineId) {
        return sendCommand(machineId, "LOCK", null, null);
    }

    /**
     * Gửi lệnh mở khóa bàn phím chuột
     */
    @Transactional
    public boolean sendUnlockCommand(String machineId) {
        return sendCommand(machineId, "UNLOCK", null, null);
    }

    /**
     * Gửi lệnh chụp màn hình
     */
    @Transactional
    public boolean sendScreenCaptureCommand(String machineId) {
        return sendCommand(machineId, "SCREEN_CAPTURE", null, null);
    }

    /**
     * Gửi lệnh đến client
     */
    @Transactional
    public boolean sendCommand(String machineId, String commandType, String commandData,
            Map<String, Object> extraData) {
        WebSocketSession session = clientSessions.get(machineId);

        if (session == null || !session.isOpen()) {
            logger.warn("Không tìm thấy session hoặc session đã đóng cho machine: {}", machineId);
            // Tạo command với status FAILED
            createCommand(machineId, commandType, commandData, "FAILED", "Client không online");
            return false;
        }

        try {
            // Tạo command trong database
            Command command = createCommand(machineId, commandType, commandData, "SENT", null);

            // Tạo payload lệnh
            Map<String, Object> payload = new HashMap<>();
            payload.put("command", commandType);
            payload.put("machineId", machineId);
            payload.put("commandId", command.getId());
            payload.put("timestamp", System.currentTimeMillis());

            if (commandData != null) {
                payload.put("data", commandData);
            }

            if (extraData != null) {
                payload.putAll(extraData);
            }

            String jsonCommand = gson.toJson(payload);
            session.sendMessage(new TextMessage(jsonCommand));

            // Cập nhật trạng thái online
            machineService.updateOnlineStatus(machineId, true);

            logger.info("Đã gửi lệnh {} đến machine: {}", commandType, machineId);
            return true;

        } catch (IOException e) {
            logger.error("Lỗi khi gửi lệnh đến machine {}: {}", machineId, e.getMessage());
            updateCommandStatus(machineId, commandType, "FAILED", "Lỗi gửi: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tạo command mới trong database
     */
    private Command createCommand(String machineId, String commandType, String commandData, String status,
            String responseData) {
        Command command = new Command();
        command.setMachineId(machineId);
        command.setCommandType(commandType);
        command.setCommandData(commandData);
        command.setStatus(status);
        command.setResponseData(responseData);
        return commandRepository.save(command);
    }

    /**
     * Cập nhật trạng thái command
     */
    @Transactional
    public void updateCommandStatus(String machineId, String commandType, String status, String responseData) {
        List<Command> commands = commandRepository.findByMachineIdAndCommandType(machineId, commandType);
        if (!commands.isEmpty()) {
            Command latest = commands.get(0);
            latest.setStatus(status);
            latest.setResponseData(responseData);
            if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                latest.setExecutedAt(LocalDateTime.now());
            }
            commandRepository.save(latest);
        }
    }

    /**
     * Xử lý response từ client
     */
    @Transactional
    public void handleCommandResponse(String machineId, Long commandId, String status, String responseData) {
        commandRepository.findById(commandId).ifPresent(command -> {
            command.setStatus(status);
            command.setResponseData(responseData);
            command.setExecutedAt(LocalDateTime.now());
            commandRepository.save(command);
            machineService.updateOnlineStatus(machineId, true);
        });
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

    /**
     * Lấy lịch sử lệnh của máy
     */
    public List<Command> getCommandHistory(String machineId) {
        return commandRepository.findByMachineIdOrderByCreatedAtDesc(machineId);
    }

    /**
     * Gửi lệnh lấy danh sách processes và trả về kết quả
     */
    @Transactional
    public Map<String, Object> sendGetProcessesCommand(String machineId) {
        WebSocketSession session = clientSessions.get(machineId);

        if (session == null || !session.isOpen()) {
            logger.warn("Không tìm thấy session cho machine: {}", machineId);
            return null;
        }

        try {
            // Tạo command trong database
            Command command = createCommand(machineId, "GET_PROCESSES", null, "SENT", null);

            // Tạo payload lệnh
            Map<String, Object> payload = new HashMap<>();
            payload.put("command", "GET_PROCESSES");
            payload.put("machineId", machineId);
            payload.put("commandId", command.getId());
            payload.put("timestamp", System.currentTimeMillis());

            String jsonCommand = gson.toJson(payload);
            session.sendMessage(new TextMessage(jsonCommand));

            logger.info("Đã gửi lệnh GET_PROCESSES đến machine: {}", machineId);

            // Đợi response (tối đa 5 giây)
            for (int i = 0; i < 50; i++) {
                Thread.sleep(100);
                Command updatedCommand = commandRepository.findById(command.getId()).orElse(null);
                if (updatedCommand != null && updatedCommand.getResponseData() != null) {
                    // Parse response JSON
                    Map<String, Object> response = gson.fromJson(updatedCommand.getResponseData(), Map.class);
                    return response;
                }
            }

            logger.warn("Timeout chờ response GET_PROCESSES từ machine: {}", machineId);
            Map<String, Object> timeoutResponse = new HashMap<>();
            timeoutResponse.put("success", false);
            timeoutResponse.put("message", "Timeout chờ response");
            return timeoutResponse;

        } catch (Exception e) {
            logger.error("Lỗi khi gửi lệnh GET_PROCESSES: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi: " + e.getMessage());
            return errorResponse;
        }
    }
}
