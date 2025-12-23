package com.monitor.server.controller;

import com.monitor.server.model.Alert;
import com.monitor.server.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller quản lý cảnh báo
 * 
 * Endpoints:
 * - GET /api/alerts - Lấy tất cả cảnh báo chưa được giải quyết
 * - GET /api/machines/{id}/alerts - Lấy cảnh báo của một máy tính
 * - POST /api/alerts/{id}/resolve - Đánh dấu cảnh báo đã được giải quyết
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "*")
public class AlertController {
    
    @Autowired
    private AlertService alertService;
    
    /**
     * Lấy tất cả cảnh báo chưa được giải quyết
     * 
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "machineId": "MACHINE-001",
     *     "alertType": "CPU_HIGH",
     *     "message": "CPU usage cao: 95.5%",
     *     "severity": "WARNING",
     *     "timestamp": "2024-01-01T10:00:00",
     *     "resolved": false
     *   }
     * ]
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> getAlerts() {
        List<Alert> alerts = alertService.getUnresolvedAlerts();
        return ResponseEntity.ok(alerts);
    }
    
    /**
     * Lấy cảnh báo của một máy tính
     */
    @GetMapping("/machines/{id}/alerts")
    public ResponseEntity<List<Alert>> getMachineAlerts(@PathVariable String id) {
        List<Alert> alerts = alertService.getAlertsByMachine(id);
        return ResponseEntity.ok(alerts);
    }
    
    /**
     * Đánh dấu cảnh báo đã được giải quyết
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Đã giải quyết cảnh báo"
     * }
     */
    @PostMapping("/alerts/{id}/resolve")
    public ResponseEntity<Map<String, Object>> resolveAlert(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            alertService.resolveAlert(id);
            response.put("success", true);
            response.put("message", "Đã giải quyết cảnh báo");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

