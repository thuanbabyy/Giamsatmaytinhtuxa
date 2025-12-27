package com.monitor.server.controller;

import com.monitor.server.model.Notification;
import com.monitor.server.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller quản lý thông báo
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Gửi thông báo đến máy
     */
    @PostMapping("/{machineId}/send")
    public ResponseEntity<Map<String, Object>> sendNotification(
            @PathVariable String machineId,
            @RequestBody Map<String, String> request) {
        
        String title = request.get("title");
        String message = request.get("message");
        String type = request.get("type"); // INFO, WARNING, ERROR
        
        if (message == null || message.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Nội dung thông báo không được để trống");
            return ResponseEntity.badRequest().body(error);
        }
        
        boolean sent = notificationService.sendNotification(machineId, title, message, type);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", sent);
        response.put("message", sent ? "Đã gửi thông báo" : "Không thể gửi thông báo (máy offline)");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy lịch sử thông báo của máy
     */
    @GetMapping("/{machineId}/history")
    public ResponseEntity<List<Notification>> getNotificationHistory(@PathVariable String machineId) {
        return ResponseEntity.ok(notificationService.getNotificationHistory(machineId));
    }
    
    /**
     * Lấy thông báo gần đây
     */
    @GetMapping("/{machineId}/recent")
    public ResponseEntity<List<Notification>> getRecentNotifications(
            @PathVariable String machineId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(notificationService.getRecentNotifications(machineId, limit));
    }
}

