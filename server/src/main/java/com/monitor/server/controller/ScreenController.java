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
        Optional<ScreenData> screenData = screenService.getLatestScreenData(machineId);
        
        if (screenData.isPresent()) {
            ScreenData data = screenData.get();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("image/" + data.getImageFormat().toLowerCase()));
            return new ResponseEntity<>(data.getImageData(), headers, HttpStatus.OK);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Chưa có ảnh màn hình");
            return ResponseEntity.ok(response);
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
            String imageDataBase64 = (String) request.get("imageData");
            String imageFormat = (String) request.get("imageFormat");
            Long commandId = request.get("commandId") != null ? 
                Long.parseLong(request.get("commandId").toString()) : null;
            
            // Decode base64
            byte[] imageData = java.util.Base64.getDecoder().decode(imageDataBase64);
            
            ScreenData screenData = screenService.saveScreenData(machineId, imageData, imageFormat, commandId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã lưu ảnh màn hình");
            response.put("screenDataId", screenData.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
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

