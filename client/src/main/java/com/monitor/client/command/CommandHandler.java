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

/**
 * Xử lý các lệnh điều khiển từ server
 * Các lệnh hỗ trợ:
 * - NOTIFICATION: Hiển thị popup thông báo
 * - SCREEN_CAPTURE: Chụp màn hình và gửi về server
 */
public class CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private final String serverUrl;
    private final String machineId;

    public CommandHandler(String serverUrl, String machineId) {
        this.serverUrl = serverUrl;
        this.machineId = machineId;
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

                case "SCREEN_CAPTURE":
                    result.putAll(handleScreenCapture(commandData));
                    break;

                case "GET_PROCESSES":
                    result.putAll(handleGetProcesses());
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
            String title = data != null && data.containsKey("title") ? (String) data.get("title") : "Thông Báo";
            String message = data != null && data.containsKey("message") ? (String) data.get("message")
                    : "Bạn có thông báo mới";
            String type = data != null && data.containsKey("type") ? (String) data.get("type") : "INFO";

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
                        messageType);
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
     * Chụp màn hình và gửi về server
     */
    private Map<String, Object> handleScreenCapture(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("=== BẮT ĐẦU CHỤP MÀN HÌNH ===");

            // Hiển thị popup thông báo
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        null,
                        "Giáo viên đang quan sát màn hình của bạn.",
                        "Thông Báo",
                        JOptionPane.INFORMATION_MESSAGE);
            });

            // Chụp màn hình
            logger.info("Đang chụp màn hình...");
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenImage = robot.createScreenCapture(screenRect);
            logger.info("Đã chụp màn hình: {}x{}", screenRect.width, screenRect.height);

            // Convert sang base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(screenImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
            logger.info("Đã encode ảnh thành base64. Size: {} KB", imageBytes.length / 1024);

            // Gửi về server
            Long commandId = data != null && data.containsKey("commandId")
                    ? Long.parseLong(data.get("commandId").toString())
                    : null;

            logger.info("Đang upload ảnh lên server... CommandId: {}", commandId);
            boolean uploaded = uploadScreenData(imageBase64, "PNG", commandId);

            if (uploaded) {
                logger.info("✅ Upload ảnh thành công!");
                result.put("success", true);
                result.put("message", "Đã chụp và gửi màn hình");
                result.put("imageData", imageBase64);
                result.put("imageFormat", "PNG");
            } else {
                logger.error("❌ Upload ảnh thất bại!");
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
            logger.info("Chuẩn bị upload...");
            HttpClient client = HttpClient.newHttpClient();
            com.google.gson.Gson gson = new com.google.gson.Gson();

            Map<String, Object> request = new HashMap<>();
            request.put("imageData", imageDataBase64);
            request.put("imageFormat", imageFormat);
            if (commandId != null) {
                request.put("commandId", commandId);
            }

            String json = gson.toJson(request);
            logger.info("JSON payload size: {} KB", json.length() / 1024);

            String uploadUrl = serverUrl + "/api/screen/" + machineId + "/upload";
            logger.info("Upload URL: {}", uploadUrl);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            logger.info("Đang gửi request...");
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            logger.info("Response status: {}", response.statusCode());
            logger.info("Response body: {}", response.body());

            return response.statusCode() == 200;

        } catch (Exception e) {
            logger.error("❌ Lỗi khi upload ảnh màn hình: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Lấy danh sách tiến trình đang chạy
     */
    private Map<String, Object> handleGetProcesses() {
        Map<String, Object> result = new HashMap<>();

        try {
            oshi.SystemInfo si = new oshi.SystemInfo();
            oshi.software.os.OperatingSystem os = si.getOperatingSystem();
            java.util.List<oshi.software.os.OSProcess> processes = os.getProcesses();

            // Lấy top 100 processes theo CPU usage
            java.util.List<Map<String, Object>> processList = new java.util.ArrayList<>();
            processes.stream()
                    .sorted((p1, p2) -> Double.compare(p2.getProcessCpuLoadCumulative(),
                            p1.getProcessCpuLoadCumulative()))
                    .limit(100)
                    .forEach(p -> {
                        Map<String, Object> processInfo = new HashMap<>();
                        processInfo.put("pid", p.getProcessID());
                        processInfo.put("name", p.getName());
                        processInfo.put("cpuUsage", Math.round(p.getProcessCpuLoadCumulative() * 10000.0) / 100.0);
                        processInfo.put("memoryUsage", p.getResidentSetSize());
                        processInfo.put("path", p.getPath());
                        processList.add(processInfo);
                    });

            result.put("success", true);
            result.put("message", "Đã lấy danh sách tiến trình");
            result.put("processes", processList);
            result.put("totalCount", processes.size());

        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách tiến trình: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Lỗi: " + e.getMessage());
        }

        return result;
    }

}
