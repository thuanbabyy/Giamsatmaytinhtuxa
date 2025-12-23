package com.monitor.client;

import com.monitor.client.command.CommandHandler;
import com.monitor.client.heartbeat.HeartbeatManager;
import com.monitor.client.monitor.SystemMonitor;
import com.monitor.client.websocket.ClientWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Main class - Entry point của Client Application
 * 
 * Cấu hình:
 * - SERVER_URL: URL của server (mặc định: http://localhost:8080)
 * - MACHINE_ID: ID duy nhất của máy tính
 * - SECRET_KEY: Khóa bí mật để xác thực
 */
public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    // Cấu hình mặc định
    private static final String DEFAULT_SERVER_URL = "http://localhost:8080";
    private static final String DEFAULT_MACHINE_ID = "MACHINE-" + System.getProperty("user.name");
    private static final String DEFAULT_SECRET_KEY = "default-secret-key-change-me";
    
    private SystemMonitor systemMonitor;
    private HeartbeatManager heartbeatManager;
    private ClientWebSocket webSocketClient;
    private CommandHandler commandHandler;
    
    public static void main(String[] args) {
        Main main = new Main();
        main.start();
        
        // Đăng ký shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(main::stop));
    }
    
    /**
     * Khởi động client
     */
    public void start() {
        logger.info("========================================");
        logger.info("  Hệ Thống Giám Sát Máy Tính - CLIENT");
        logger.info("========================================");
        
        // Lấy cấu hình từ biến môi trường hoặc dùng giá trị mặc định
        String serverUrl = System.getenv("SERVER_URL");
        if (serverUrl == null || serverUrl.isEmpty()) {
            serverUrl = DEFAULT_SERVER_URL;
        }
        
        String machineId = System.getenv("MACHINE_ID");
        if (machineId == null || machineId.isEmpty()) {
            machineId = DEFAULT_MACHINE_ID;
        }
        
        String secretKey = System.getenv("SECRET_KEY");
        if (secretKey == null || secretKey.isEmpty()) {
            secretKey = DEFAULT_SECRET_KEY;
            logger.warn("Đang sử dụng SECRET_KEY mặc định. Hãy thay đổi trong production!");
        }
        
        logger.info("Server URL: {}", serverUrl);
        logger.info("Machine ID: {}", machineId);
        logger.info("Secret Key: {}***", secretKey.substring(0, Math.min(10, secretKey.length())));
        
        try {
            // Khởi tạo các component
            systemMonitor = new SystemMonitor();
            commandHandler = new CommandHandler();
            
            // Khởi động Heartbeat Manager
            heartbeatManager = new HeartbeatManager(
                systemMonitor,
                serverUrl,
                machineId,
                secretKey
            );
            heartbeatManager.start();
            
            // Kết nối WebSocket để nhận lệnh
            String wsUrl = serverUrl.replace("http://", "ws://").replace("https://", "wss://");
            URI wsUri = new URI(wsUrl + "/ws/client");
            
            webSocketClient = new ClientWebSocket(wsUri, machineId, secretKey, commandHandler);
            webSocketClient.connect();
            
            logger.info("Client đã khởi động thành công!");
            logger.info("Đang gửi heartbeat và lắng nghe lệnh từ server...");
            
        } catch (Exception e) {
            logger.error("Lỗi khi khởi động client: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
    
    /**
     * Dừng client
     */
    public void stop() {
        logger.info("Đang dừng client...");
        
        if (heartbeatManager != null) {
            heartbeatManager.stop();
        }
        
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        
        logger.info("Client đã dừng.");
    }
}

