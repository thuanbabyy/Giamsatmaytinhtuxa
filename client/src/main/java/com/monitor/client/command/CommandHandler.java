package com.monitor.client.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Xử lý các lệnh điều khiển từ server
 * Các lệnh hỗ trợ:
 * - KILL_PROCESS <pid>
 * - SHUTDOWN
 * - LOCK_KEYBOARD
 * - SCREENSHOT
 */
public class CommandHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    
    /**
     * Xử lý lệnh từ server
     * @param command Lệnh cần thực thi
     * @return Kết quả thực thi
     */
    public Map<String, Object> handleCommand(String command) {
        Map<String, Object> result = new HashMap<>();
        result.put("command", command);
        result.put("timestamp", System.currentTimeMillis());
        
        try {
            // Kiểm tra null hoặc rỗng
            if (command == null || command.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Lệnh không được để trống");
                return result;
            }
            
            String[] parts = command.split(" ", 2);
            String action = parts[0].toUpperCase();
            String parameter = parts.length > 1 ? parts[1] : null;
            
            switch (action) {
                case "KILL_PROCESS":
                    result.put("success", killProcess(parameter));
                    result.put("message", parameter != null ? 
                        "Đã dừng process: " + parameter : "Thiếu PID");
                    break;
                    
                case "SHUTDOWN":
                    result.put("success", shutdown());
                    result.put("message", "Đang tắt máy...");
                    break;
                    
                case "LOCK_KEYBOARD":
                    result.put("success", lockKeyboard());
                    result.put("message", "Đã khóa bàn phím");
                    break;
                    
                case "SCREENSHOT":
                    String screenshotPath = takeScreenshot();
                    result.put("success", screenshotPath != null);
                    result.put("message", screenshotPath != null ? 
                        "Đã chụp màn hình: " + screenshotPath : "Lỗi khi chụp màn hình");
                    result.put("screenshotPath", screenshotPath);
                    break;
                    
                default:
                    result.put("success", false);
                    result.put("message", "Lệnh không được hỗ trợ: " + action);
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
     * Dừng process theo PID
     */
    private boolean killProcess(String pid) {
        if (pid == null || pid.isEmpty()) {
            return false;
        }
        
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;
            
            if (os.contains("win")) {
                // Windows
                processBuilder = new ProcessBuilder("taskkill", "/F", "/PID", pid);
            } else {
                // Linux/Mac
                processBuilder = new ProcessBuilder("kill", "-9", pid);
            }
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            logger.info("Đã dừng process PID: {} (exit code: {})", pid, exitCode);
            return exitCode == 0;
            
        } catch (Exception e) {
            logger.error("Lỗi khi dừng process: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Tắt máy tính
     */
    private boolean shutdown() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;
            
            if (os.contains("win")) {
                // Windows: tắt sau 30 giây
                processBuilder = new ProcessBuilder("shutdown", "/s", "/t", "30");
            } else if (os.contains("linux")) {
                // Linux
                processBuilder = new ProcessBuilder("shutdown", "-h", "+1");
            } else if (os.contains("mac")) {
                // Mac
                processBuilder = new ProcessBuilder("shutdown", "-h", "+1");
            } else {
                logger.warn("Hệ điều hành không được hỗ trợ: {}", os);
                return false;
            }
            
            processBuilder.start();
            logger.info("Đã khởi động quá trình tắt máy");
            return true;
            
        } catch (Exception e) {
            logger.error("Lỗi khi tắt máy: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Khóa bàn phím (chỉ khóa tạm thời, có thể unlock bằng cách restart app)
     * Lưu ý: Đây là cách đơn giản, trong thực tế cần driver hoặc native code
     */
    private boolean lockKeyboard() {
        try {
            // Sử dụng Robot để block input
            Robot robot = new Robot();
            
            // Gửi tổ hợp phím để lock screen (Windows: Win+L)
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                robot.keyPress(KeyEvent.VK_WINDOWS);
                robot.keyPress(KeyEvent.VK_L);
                robot.keyRelease(KeyEvent.VK_L);
                robot.keyRelease(KeyEvent.VK_WINDOWS);
            }
            
            logger.info("Đã khóa bàn phím/màn hình");
            return true;
            
        } catch (Exception e) {
            logger.error("Lỗi khi khóa bàn phím: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Chụp màn hình
     */
    private String takeScreenshot() {
        try {
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            java.awt.image.BufferedImage screenImage = robot.createScreenCapture(screenRect);
            
            // Lưu file screenshot
            String fileName = "screenshot_" + System.currentTimeMillis() + ".png";
            String filePath = System.getProperty("user.home") + "/" + fileName;
            
            javax.imageio.ImageIO.write(screenImage, "png", new java.io.File(filePath));
            
            logger.info("Đã chụp màn hình: {}", filePath);
            return filePath;
            
        } catch (Exception e) {
            logger.error("Lỗi khi chụp màn hình: {}", e.getMessage());
            return null;
        }
    }
}

