package com.monitor.server.controller;

import com.google.gson.Gson;
import com.monitor.server.security.AuthenticationService;
import com.monitor.server.service.MetricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller nhận heartbeat từ client
 * 
 * Endpoint: POST /api/heartbeat
 * 
 * Request Body:
 * {
 *   "payload": {
 *     "machineId": "MACHINE-001",
 *     "metrics": { ... },
 *     "timestamp": 1234567890
 *   },
 *   "signature": "HMAC_SIGNATURE"
 * }
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "*")
public class HeartbeatController {
    
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatController.class);
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private MetricService metricService;
    
    private Gson gson = new Gson();
    
    /**
     * API Info - Trả về thông tin về API
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Hệ Thống Giám Sát Máy Tính - API");
        info.put("version", "1.0");
        info.put("endpoints", Map.of(
            "machines", "/api/machines",
            "commands", "/api/commands",
            "notifications", "/api/notifications",
            "screen", "/api/screen",
            "heartbeat", "/api/heartbeat",
            "alerts", "/api/alerts"
        ));
        return ResponseEntity.ok(info);
    }
    
    /**
     * Nhận heartbeat từ client
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, Object>> receiveHeartbeat(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Parse request
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) request.get("payload");
            String signature = (String) request.get("signature");
            
            if (payload == null || signature == null) {
                response.put("success", false);
                response.put("message", "Thiếu payload hoặc signature");
                return ResponseEntity.badRequest().body(response);
            }
            
            String machineId = (String) payload.get("machineId");
            if (machineId == null || machineId.isEmpty()) {
                response.put("success", false);
                response.put("message", "Thiếu machineId");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Xác thực
            // Cần serialize payload theo cùng cách với client để signature khớp
            // Sử dụng Gson với cùng cấu hình để đảm bảo thứ tự field giống nhau
            String payloadJson = gson.toJson(payload);
            logger.info("Đang xác thực heartbeat từ machine: {}, payload length: {}, signature length: {}", 
                machineId, payloadJson.length(), signature != null ? signature.length() : 0);
            boolean authenticated = authenticationService.authenticate(machineId, payloadJson, signature);
            
            if (!authenticated) {
                logger.warn("Xác thực thất bại cho machine: {}", machineId);
                response.put("success", false);
                response.put("message", "Xác thực thất bại");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            logger.info("Xác thực thành công cho machine: {}", machineId);
            
            // Lưu metrics
            @SuppressWarnings("unchecked")
            Map<String, Object> metrics = (Map<String, Object>) payload.get("metrics");
            if (metrics != null) {
                metricService.saveMetrics(machineId, metrics);
            }
            
            // Response thành công
            response.put("success", true);
            response.put("message", "Đã nhận heartbeat");
            response.put("timestamp", System.currentTimeMillis());
            
            logger.debug("Đã nhận heartbeat từ machine: {}", machineId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý heartbeat: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

