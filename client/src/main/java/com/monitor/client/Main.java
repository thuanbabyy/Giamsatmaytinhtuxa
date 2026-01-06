package com.monitor.client;

import com.monitor.client.command.CommandHandler;
import com.monitor.client.websocket.ClientWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

/**
 * Main class - Entry point của Client Application
 * 
 * Cách chạy:
 * java -jar client.jar --server.url=http://IP_SERVER:8080 --machine.id=MACHINE_ID
 * 
 * Hoặc:
 * java -jar client.jar
 * (sẽ dùng giá trị mặc định: localhost:8080)
 */
public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    // Cấu hình mặc định
    private static final String DEFAULT_SERVER_URL = "http://localhost:8080";
    private static final String DEFAULT_MACHINE_ID = "MACHINE-" + System.getProperty("user.name") + "-" + 
        System.getProperty("user.name") + System.currentTimeMillis();
    
    private ClientWebSocket webSocketClient;
    private CommandHandler commandHandler;
    private String serverUrl;
    private String machineId;
    
    public static void main(String[] args) {
        // Thiết lập encoding UTF-8 cho console (tránh lỗi dấu '?' trên Windows)
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            // Nếu không thiết lập được, tiếp tục với mặc định
        }

        Main main = new Main();
        main.start(args);
        
        // Đăng ký shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(main::stop));
    }
    
    /**
     * Khởi động client
     */
    public void start(String[] args) {
        logger.info("========================================");
        logger.info("  Hệ Thống Giám Sát Máy Tính - CLIENT");
        logger.info("========================================");
        
        // Lấy cấu hình từ tham số dòng lệnh hoặc biến môi trường
        serverUrl = getArgValue(args, "--server.url");
        if (serverUrl == null || serverUrl.isEmpty()) {
            serverUrl = System.getenv("SERVER_URL");
        }
        if (serverUrl == null || serverUrl.isEmpty()) {
            serverUrl = DEFAULT_SERVER_URL;
        }

        while (serverUrl.endsWith("/")) {
            serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        }
        
        machineId = getArgValue(args, "--machine.id");
        if (machineId == null || machineId.isEmpty()) {
            machineId = System.getenv("MACHINE_ID");
        }
        if (machineId == null || machineId.isEmpty()) {
            machineId = DEFAULT_MACHINE_ID;
        }
        
        logger.info("Server URL: {}", serverUrl);
        logger.info("Machine ID: {}", machineId);
        
        try {
            // Lấy thông tin hệ thống
            SystemInfo systemInfo = new SystemInfo();
            OperatingSystem os = systemInfo.getOperatingSystem();
            String osName = os.getFamily();
            String osVersion = os.getVersionInfo().getVersion();
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            String computerName = System.getProperty("user.name");
            
            // Đăng ký với server
            boolean registered = registerWithServer(machineId, computerName, ipAddress, osName, osVersion);
            
            if (!registered) {
                logger.error("Không thể đăng ký với server. Kiểm tra kết nối mạng và địa chỉ server.");
                System.exit(1);
            }
            
            // Khởi tạo command handler
            commandHandler = new CommandHandler(serverUrl, machineId);
            
            // Kết nối WebSocket để nhận lệnh
            String wsUrl = serverUrl.replace("http://", "ws://").replace("https://", "wss://");
            URI wsUri = new URI(wsUrl + "/ws-client");

            logger.info("WebSocket URL: {}", wsUri);
            
            webSocketClient = new ClientWebSocket(wsUri, machineId, commandHandler);
            webSocketClient.connect();
            
            // Đợi kết nối WebSocket
            int retries = 0;
            while (!webSocketClient.isOpen() && retries < 10) {
                Thread.sleep(1000);
                retries++;
            }
            
            if (!webSocketClient.isOpen()) {
                logger.error("Không thể kết nối WebSocket đến server");
                System.exit(1);
            }
            
            logger.info("Client đã khởi động thành công!");
            logger.info("Đang lắng nghe lệnh từ server...");
            logger.info("Nhấn Ctrl+C để dừng client");
            
            // Giữ chương trình chạy
            while (true) {
                Thread.sleep(1000);
            }
            
        } catch (Exception e) {
            logger.error("Lỗi khi khởi động client: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * Đăng ký với server qua REST API
     */
    private boolean registerWithServer(String machineId, String name, String ipAddress, String osName, String osVersion) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            Gson gson = new Gson();
            
            Map<String, String> request = new HashMap<>();
            request.put("machineId", machineId);
            request.put("name", name);
            request.put("ipAddress", ipAddress);
            request.put("osName", osName);
            request.put("osVersion", osVersion);
            
            String json = gson.toJson(request);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/machines/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                logger.info("Đã đăng ký thành công với server");
                return true;
            } else {
                logger.warn("Đăng ký không thành công. Status code: {}", response.statusCode());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Lỗi khi đăng ký với server: {}", e.getMessage());
            return false;
        }
    }

    private String getArgValue(String[] args, String key) {
        if (args == null || args.length == 0 || key == null || key.isEmpty()) {
            return null;
        }
        String prefix = key + "=";
        for (String arg : args) {
            if (arg == null) {
                continue;
            }
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length()).trim();
            }
        }
        return null;
    }
    
    /**
     * Dừng client
     */
    public void stop() {
        logger.info("Đang dừng client...");
        
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        
        logger.info("Client đã dừng.");
    }
}
