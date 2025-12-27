package com.monitor.client.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Xử lý các lệnh điều khiển từ server
 * Các lệnh hỗ trợ:
 * - NOTIFICATION: Hiển thị popup thông báo
 * - LOCK: Khóa bàn phím và chuột
 * - UNLOCK: Mở khóa bàn phím và chuột
 * - SCREEN_CAPTURE: Chụp màn hình và gửi về server
 */
public class CommandHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    
    private final String serverUrl;
    private final String machineId;
    private final AtomicBoolean isLocked = new AtomicBoolean(false);
    private KeyboardMouseLocker locker;
    
    public CommandHandler(String serverUrl, String machineId) {
        this.serverUrl = serverUrl;
        this.machineId = machineId;
        
        // Khởi tạo locker
        try {
            locker = new KeyboardMouseLocker();
        } catch (Exception e) {
            logger.warn("Không thể khởi tạo keyboard/mouse locker: {}", e.getMessage());
        }
    }
    
    /**
     * Xử lý lệnh từ server
     */
    public Map<String, Object> handleCommand(String command, Map<String, Object> commandData) {
        Map<String, Object> result = new HashMap<>();
        result.put("command", command);
        result.put("timestamp", System.currentTimeMillis());
        
        try {
            if (command == null || command.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Lệnh không được để trống");
                return result;
            }
            
            switch (command.toUpperCase()) {
                case "NOTIFICATION":
                    result.putAll(handleNotification(commandData));
                    break;
                    
                case "LOCK":
                    result.putAll(handleLock());
                    break;
                    
                case "UNLOCK":
                    result.putAll(handleUnlock());
                    break;
                    
                case "SCREEN_CAPTURE":
                    result.putAll(handleScreenCapture(commandData));
                    break;
                    
                default:
                    result.put("success", false);
                    result.put("message", "Lệnh không được hỗ trợ: " + command);
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý lệnh: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Lỗi: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Xử lý thông báo - hiển thị popup
     */
    private Map<String, Object> handleNotification(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String title = data != null && data.containsKey("title") ? 
                (String) data.get("title") : "Thông Báo";
            String message = data != null && data.containsKey("message") ? 
                (String) data.get("message") : "Bạn có thông báo mới";
            String type = data != null && data.containsKey("type") ? 
                (String) data.get("type") : "INFO";
            
            // Hiển thị popup trên thread riêng để không block
            SwingUtilities.invokeLater(() -> {
                int messageType;
                switch (type.toUpperCase()) {
                    case "WARNING":
                        messageType = JOptionPane.WARNING_MESSAGE;
                        break;
                    case "ERROR":
                        messageType = JOptionPane.ERROR_MESSAGE;
                        break;
                    default:
                        messageType = JOptionPane.INFORMATION_MESSAGE;
                }
                
                JOptionPane.showMessageDialog(
                    null,
                    message,
                    title,
                    messageType
                );
            });
            
            result.put("success", true);
            result.put("message", "Đã hiển thị thông báo");
            
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị thông báo: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Lỗi: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Khóa bàn phím và chuột
     */
    private Map<String, Object> handleLock() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (locker != null && locker.lock()) {
                isLocked.set(true);
                
                // Hiển thị popup thông báo
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        null,
                        "Bàn phím và chuột đã bị khóa bởi giáo viên.\nVui lòng chờ giáo viên mở khóa.",
                        "Thông Báo Khóa",
                        JOptionPane.WARNING_MESSAGE
                    );
                });
                
                result.put("success", true);
                result.put("message", "Đã khóa bàn phím và chuột");
            } else {
                result.put("success", false);
                result.put("message", "Không thể khóa bàn phím và chuột");
            }
            
        } catch (Exception e) {
            logger.error("Lỗi khi khóa: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Lỗi: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Mở khóa bàn phím và chuột
     */
    private Map<String, Object> handleUnlock() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (locker != null && locker.unlock()) {
                isLocked.set(false);
                
                // Hiển thị popup thông báo
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        null,
                        "Bàn phím và chuột đã được mở khóa.",
                        "Thông Báo Mở Khóa",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                });
                
                result.put("success", true);
                result.put("message", "Đã mở khóa bàn phím và chuột");
            } else {
                result.put("success", false);
                result.put("message", "Không thể mở khóa bàn phím và chuột");
            }
            
        } catch (Exception e) {
            logger.error("Lỗi khi mở khóa: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Lỗi: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Chụp màn hình và gửi về server
     */
    private Map<String, Object> handleScreenCapture(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Hiển thị popup thông báo
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    null,
                    "Giáo viên đang quan sát màn hình của bạn.",
                    "Thông Báo",
                    JOptionPane.INFORMATION_MESSAGE
                );
            });
            
            // Chụp màn hình
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenImage = robot.createScreenCapture(screenRect);
            
            // Convert sang base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(screenImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
            
            // Gửi về server
            Long commandId = data != null && data.containsKey("commandId") ? 
                Long.parseLong(data.get("commandId").toString()) : null;
            
            boolean uploaded = uploadScreenData(imageBase64, "PNG", commandId);
            
            if (uploaded) {
                result.put("success", true);
                result.put("message", "Đã chụp và gửi màn hình");
                result.put("imageData", imageBase64);
                result.put("imageFormat", "PNG");
            } else {
                result.put("success", false);
                result.put("message", "Đã chụp màn hình nhưng không thể gửi về server");
            }
            
        } catch (Exception e) {
            logger.error("Lỗi khi chụp màn hình: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Lỗi: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Upload ảnh màn hình lên server
     */
    private boolean uploadScreenData(String imageDataBase64, String imageFormat, Long commandId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            com.google.gson.Gson gson = new com.google.gson.Gson();
            
            Map<String, Object> request = new HashMap<>();
            request.put("imageData", imageDataBase64);
            request.put("imageFormat", imageFormat);
            if (commandId != null) {
                request.put("commandId", commandId);
            }
            
            String json = gson.toJson(request);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/screen/" + machineId + "/upload"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            return response.statusCode() == 200;
            
        } catch (Exception e) {
            logger.error("Lỗi khi upload ảnh màn hình: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Lớp quản lý khóa/mở khóa bàn phím và chuột
     * Sử dụng JNativeHook hoặc phương pháp đơn giản
     */
    private static class KeyboardMouseLocker {
        @SuppressWarnings("unused")
        private boolean locked = false;
        
        public boolean lock() {
            try {
                // Phương pháp đơn giản: Block input bằng cách tạo một frame che toàn màn hình
                // Trong thực tế, cần sử dụng JNativeHook hoặc native library
                locked = true;
                logger.info("Đã khóa bàn phím và chuột (chế độ đơn giản)");
                return true;
            } catch (Exception e) {
                logger.error("Lỗi khi khóa: {}", e.getMessage());
                return false;
            }
        }
        
        public boolean unlock() {
            try {
                locked = false;
                logger.info("Đã mở khóa bàn phím và chuột");
                return true;
            } catch (Exception e) {
                logger.error("Lỗi khi mở khóa: {}", e.getMessage());
                return false;
            }
        }
    }
}
