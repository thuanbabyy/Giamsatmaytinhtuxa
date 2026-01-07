package com.monitor.server.websocket;

import com.google.gson.Gson;
import com.monitor.server.service.CommandService;
import com.monitor.server.service.MachineService;
import com.monitor.server.service.ScreenService;
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

    @Autowired
    private MachineService machineService;

    @Autowired
    private ScreenService screenService;

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

            // Đăng ký client
            if ("client".equals(type) && machineId != null) {
                // Lấy thông tin từ request
                String name = (String) data.get("name");
                String ipAddress = (String) data.get("ipAddress");
                String osName = (String) data.get("osName");
                String osVersion = (String) data.get("osVersion");

                // Đăng ký máy tính
                machineService.registerMachine(machineId, name, ipAddress, osName, osVersion);

                // Đăng ký WebSocket session
                commandService.registerClient(machineId, session);

                // Cập nhật trạng thái online
                machineService.updateOnlineStatus(machineId, true);

                // Gửi xác nhận
                Map<String, Object> response = new HashMap<>();
                response.put("status", "connected");
                response.put("message", "Đã kết nối thành công");
                session.sendMessage(new TextMessage(gson.toJson(response)));
            }

            // Cập nhật lastResponseTime mỗi khi nhận message từ client (giữ máy online)
            if (machineId != null) {
                machineService.updateOnlineStatus(machineId, true);
            }

            // Xử lý response từ client (sau khi thực thi lệnh)
            if (data.containsKey("result")) {
                String command = (String) data.get("command");
                Object result = data.get("result");

                // Parse commandId - có thể là Integer, Long, hoặc Double
                Long commandId = null;
                if (data.get("commandId") != null) {
                    try {
                        Object cmdIdObj = data.get("commandId");
                        if (cmdIdObj instanceof Number) {
                            commandId = ((Number) cmdIdObj).longValue();
                        } else {
                            // Parse từ string, loại bỏ phần thập phân nếu có
                            String cmdIdStr = cmdIdObj.toString();
                            if (cmdIdStr.contains(".")) {
                                commandId = (long) Double.parseDouble(cmdIdStr);
                            } else {
                                commandId = Long.parseLong(cmdIdStr);
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Không thể parse commandId: {}", data.get("commandId"));
                    }
                }

                logger.info("Nhận kết quả từ client {} - command: {}, result: {}", machineId, command, result);

                // Cập nhật trạng thái command
                if (commandId != null) {
                    String status = result instanceof Map && ((Map<?, ?>) result).containsKey("success") &&
                            Boolean.TRUE.equals(((Map<?, ?>) result).get("success")) ? "COMPLETED" : "FAILED";
                    String responseData = gson.toJson(result);
                    commandService.handleCommandResponse(machineId, commandId, status, responseData);
                }

                // Xử lý ảnh màn hình nếu có
                if ("SCREEN_CAPTURE".equals(command) && result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> resultMap = (Map<String, Object>) result;
                    if (resultMap.containsKey("imageData")) {
                        String imageData = (String) resultMap.get("imageData");
                        String imageFormat = (String) resultMap.get("imageFormat");
                        screenService.saveScreenData(machineId,
                                java.util.Base64.getDecoder().decode(imageData),
                                imageFormat, commandId);
                    }
                }
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
