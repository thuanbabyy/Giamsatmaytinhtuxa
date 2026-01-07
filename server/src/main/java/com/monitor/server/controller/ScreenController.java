package com.monitor.server.controller;

import com.monitor.server.model.ScreenData;
import com.monitor.server.service.ScreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller quản lý màn hình
 */
@RestController
@RequestMapping("/api/screen")
@CrossOrigin(origins = "*")
public class ScreenController {

    @Autowired
    private ScreenService screenService;

    /**
     * Yêu cầu chụp màn hình
     */
    @PostMapping("/{machineId}/capture")
    public ResponseEntity<Map<String, Object>> requestScreenCapture(@PathVariable String machineId) {
        boolean sent = screenService.requestScreenCapture(machineId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", sent);
        response.put("message", sent ? "Đã gửi yêu cầu chụp màn hình" : "Không thể gửi yêu cầu (máy offline)");

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy ảnh màn hình mới nhất
     */
    @GetMapping("/{machineId}/latest")
    public ResponseEntity<?> getLatestScreen(@PathVariable String machineId) {
        try {
            System.out.println("=== GET LATEST SCREEN ===");
            System.out.println("Machine ID: " + machineId);

            Optional<ScreenData> screenData = screenService.getLatestScreenData(machineId);

            if (screenData.isPresent()) {
                ScreenData data = screenData.get();
                System.out.println("Found screen data ID: " + data.getId());
                System.out.println("Image format: " + data.getImageFormat());
                System.out.println(
                        "Image size: " + (data.getImageData() != null ? data.getImageData().length : 0) + " bytes");

                // Validate image format
                String imageFormat = data.getImageFormat();
                if (imageFormat == null || imageFormat.trim().isEmpty()) {
                    imageFormat = "png"; // Default to PNG
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("image/" + imageFormat.toLowerCase()));
                System.out.println("✅ Returning image with content-type: image/" + imageFormat.toLowerCase());
                return new ResponseEntity<>(data.getImageData(), headers, HttpStatus.OK);
            } else {
                System.out.println("⚠️ No screen data found for machine: " + machineId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Chưa có ảnh màn hình");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            System.err.println("❌ Error in getLatestScreen: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy ảnh: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Lưu ảnh màn hình từ client
     */
    @PostMapping("/{machineId}/upload")
    public ResponseEntity<Map<String, Object>> uploadScreenData(
            @PathVariable String machineId,
            @RequestBody Map<String, Object> request) {

        try {
            System.out.println("=== NHẬN REQUEST UPLOAD ẢNH ===");
            System.out.println("Machine ID: " + machineId);
            System.out.println("Request keys: " + request.keySet());

            String imageDataBase64 = (String) request.get("imageData");
            String imageFormat = (String) request.get("imageFormat");
            Long commandId = request.get("commandId") != null ? Long.parseLong(request.get("commandId").toString())
                    : null;

            System.out.println("Image format: " + imageFormat);
            System.out.println("Command ID: " + commandId);
            System.out.println("Base64 length: " + (imageDataBase64 != null ? imageDataBase64.length() : 0));

            // Decode base64
            byte[] imageData = java.util.Base64.getDecoder().decode(imageDataBase64);
            System.out.println(
                    "Decoded image size: " + imageData.length + " bytes (" + (imageData.length / 1024) + " KB)");

            ScreenData screenData = screenService.saveScreenData(machineId, imageData, imageFormat, commandId);
            System.out.println("✅ Lưu ảnh thành công! Screen Data ID: " + screenData.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã lưu ảnh màn hình");
            response.put("screenDataId", screenData.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lưu ảnh: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lưu ảnh: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy lịch sử ảnh màn hình
     */
    @GetMapping("/{machineId}/history")
    public ResponseEntity<List<ScreenData>> getScreenHistory(@PathVariable String machineId) {
        return ResponseEntity.ok(screenService.getScreenDataHistory(machineId));
    }
}
