package com.monitor.client.websocket;

import com.google.gson.Gson;
import com.monitor.client.command.CommandHandler;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket Client để nhận lệnh từ server
 */
public class ClientWebSocket extends WebSocketClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientWebSocket.class);
    
    private String machineId;
    private CommandHandler commandHandler;
    private Gson gson;

    // Keepalive and reconnection helpers
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "WebSocket-KeepAlive");
        t.setDaemon(true);
        return t;
    });
    private ScheduledFuture<?> keepAliveFuture;
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private volatile boolean reconnecting = false;
    
    public ClientWebSocket(URI serverUri, String machineId, CommandHandler commandHandler) {
        super(serverUri);
        this.machineId = machineId;
        this.commandHandler = commandHandler;
        this.gson = new Gson();
    }
    
    @Override
    public void onOpen(ServerHandshake handshake) {
        logger.info("Đã kết nối WebSocket đến server");

        // Reset reconnect attempts
        reconnectAttempts.set(0);
        reconnecting = false;

        // Gửi thông tin đăng ký
        Map<String, Object> auth = new HashMap<>();
        auth.put("machineId", machineId);
        auth.put("type", "client");

        // Thêm thông tin hệ thống
        try {
            SystemInfo si = new SystemInfo();
            OperatingSystem os = si.getOperatingSystem();
            auth.put("name", System.getProperty("user.name"));
            auth.put("ipAddress", java.net.InetAddress.getLocalHost().getHostAddress());
            auth.put("osName", os.getFamily());
            auth.put("osVersion", os.getVersionInfo().getVersion());
        } catch (Exception e) {
            logger.warn("Không thể lấy thông tin hệ thống: {}", e.getMessage());
        }

        try {
            if (isOpen()) {
                send(gson.toJson(auth));
            } else {
                logger.warn("Socket chưa open khi gửi auth, bỏ qua gửi auth lần này");
            }
        } catch (Exception e) {
            logger.warn("Không thể gửi auth: {}", e.getMessage());
        }

        // Start keepalive ping (every 20s)
        try {
            if (keepAliveFuture != null && !keepAliveFuture.isCancelled()) {
                keepAliveFuture.cancel(true);
            }
            keepAliveFuture = scheduler.scheduleAtFixedRate(() -> {
                try {
                    if (!isOpen()) {
                        // If socket not open, skip send (reconnect thread will handle reconnect)
                        return;
                    }
                    Map<String, Object> ping = new HashMap<>();
                    ping.put("type", "KEEPALIVE");
                    ping.put("machineId", machineId);
                    ping.put("timestamp", System.currentTimeMillis());
                    send(gson.toJson(ping));
                } catch (Exception e) {
                    logger.warn("Keepalive send failed: {}", e.getMessage());
                }
            }, 20, 20, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Không thể bắt đầu keepalive: {}", e.getMessage());
        }
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
            
            // Xử lý commandId - có thể là số nguyên hoặc số thực từ JSON
            Long commandId = null;
            Object commandIdObj = commandData.get("commandId");
            if (commandIdObj != null) {
                if (commandIdObj instanceof Number) {
                    commandId = ((Number) commandIdObj).longValue();
                } else {
                    try {
                        // Thử parse từ string (xử lý cả "1" và "1.0")
                        double doubleValue = Double.parseDouble(commandIdObj.toString());
                        commandId = (long) doubleValue;
                    } catch (NumberFormatException e) {
                        logger.warn("Không thể parse commandId: {}", commandIdObj);
                    }
                }
            }
            
            // Kiểm tra xem lệnh có dành cho máy này không
            if (targetMachineId != null && !targetMachineId.equals(machineId)) {
                logger.debug("Lệnh không dành cho máy này: {}", targetMachineId);
                return;
            }
            
            // Lấy data từ commandData - xử lý cả trường hợp server gửi data dưới dạng String JSON
            Object dataObj = commandData.get("data");
            Map<String, Object> data = null;
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> tmp = (Map<String, Object>) dataObj;
                data = tmp;
            } else if (dataObj instanceof String) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> tmp = (Map<String, Object>) gson.fromJson((String) dataObj, Map.class);
                    data = tmp;
                } catch (Exception ex) {
                    logger.warn("Không thể parse field 'data' từ String: {}", ex.getMessage());
                }
            } else if (dataObj != null) {
                logger.debug("Data field có kiểu không mong đợi: {}", dataObj.getClass().getName());
            }

            // Nếu data null, thử lấy từ các trường top-level (server có thể gửi title/message/type ở top-level)
            if (data == null) {
                Map<String, Object> fallback = new HashMap<>();
                if (commandData.containsKey("title")) fallback.put("title", commandData.get("title"));
                if (commandData.containsKey("message")) fallback.put("message", commandData.get("message"));
                if (commandData.containsKey("type")) fallback.put("type", commandData.get("type"));
                if (commandData.containsKey("notificationId")) fallback.put("notificationId", commandData.get("notificationId"));
                if (!fallback.isEmpty()) {
                    data = fallback;
                }
            }

            // Xử lý lệnh
            Map<String, Object> result = commandHandler.handleCommand(command, data);
            
            // Thêm commandId vào result
            if (commandId != null) {
                result.put("commandId", commandId);
            }
            
            // Gửi kết quả về server
            Map<String, Object> response = new HashMap<>();
            response.put("machineId", machineId);
            response.put("command", command);
            response.put("commandId", commandId);
            response.put("result", result);
            
            send(gson.toJson(response));
            
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý lệnh từ server: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.warn("WebSocket đã đóng. Code: {}, Reason: {}", code, reason);

        // Stop keepalive
        try {
            if (keepAliveFuture != null) {
                keepAliveFuture.cancel(true);
                keepAliveFuture = null;
            }
        } catch (Exception ignored) {}

        // Exponential backoff reconnect
        scheduleReconnectWithBackoff();
    }
    
    @Override
    public void onError(Exception ex) {
        logger.error("Lỗi WebSocket: {}", ex.getMessage(), ex);
        // Nếu socket bị lỗi và đóng, thử reconnect
        if (!isOpen()) {
            scheduleReconnectWithBackoff();
        }
    }

    private void scheduleReconnectWithBackoff() {
        if (reconnecting) return;
        reconnecting = true;
        new Thread(() -> {
            try {
                int attempt = reconnectAttempts.incrementAndGet();
                long backoffSec = Math.min(60, (long) Math.pow(2, Math.min(6, attempt - 1)));
                logger.info("Sẽ thử kết nối lại trong {} giây (attempt #{})", backoffSec, attempt);
                Thread.sleep(backoffSec * 1000);

                if (!isOpen()) {
                    logger.info("Đang thử kết nối lại WebSocket (attempt #{})...", attempt);
                    try {
                        reconnect();
                    } catch (Exception e) {
                        logger.error("Lỗi khi reconnect: {}", e.getMessage());
                        reconnecting = false; // allow future attempts
                    }
                } else {
                    reconnecting = false;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                reconnecting = false;
            }
        }, "WebSocket-Reconnect").start();
    }
}
