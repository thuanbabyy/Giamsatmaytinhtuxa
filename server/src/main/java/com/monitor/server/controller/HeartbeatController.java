package com.monitor.server.controller;

import com.google.gson.Gson;
import com.monitor.server.security.AuthenticationService;
import com.monitor.server.service.MachineService;
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
 * "payload": {
 * "machineId": "MACHINE-001",
 * "metrics": { ... },
 * "timestamp": 1234567890
 * },
 * "signature": "HMAC_SIGNATURE"
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

    @Autowired
    private MachineService machineService;

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
                "alerts", "/api/alerts"));
        return ResponseEntity.ok(info);
    }

    /**
     * Nhận heartbeat từ client
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, Object>> receiveHeartbeat(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String machineId = null;
            Map<String, Object> metrics = null;

            // Kiểm tra xem có phải format có signature không
            if (request.containsKey("payload") && request.containsKey("signature")) {
                // Format cũ với signature (HeartbeatManager)
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) request.get("payload");
                String signature = (String) request.get("signature");

                machineId = (String) payload.get("machineId");

                // Tạm thời tắt xác thực để debug
                // String payloadJson = gson.toJson(payload);
                // boolean authenticated = authenticationService.authenticate(machineId,
                // payloadJson, signature);
                // if (!authenticated) {
                // logger.warn("Xác thực thất bại cho machine: {} - Cho phép tiếp tục",
                // machineId);
                // }
                logger.info("Nhận heartbeat từ machine: {} (bỏ qua xác thực)", machineId);

                @SuppressWarnings("unchecked")
                Map<String, Object> metricsData = (Map<String, Object>) payload.get("metrics");
                metrics = metricsData;

            } else if (request.containsKey("machineId") && request.containsKey("metrics")) {
                // Format đơn giản (không có signature)
                machineId = (String) request.get("machineId");
                @SuppressWarnings("unchecked")
                Map<String, Object> metricsData = (Map<String, Object>) request.get("metrics");
                metrics = metricsData;
                logger.info("Nhận heartbeat đơn giản từ machine: {}", machineId);
            } else {
                response.put("success", false);
                response.put("message", "Format request không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }

            if (machineId == null || machineId.isEmpty()) {
                response.put("success", false);
                response.put("message", "Thiếu machineId");
                return ResponseEntity.badRequest().body(response);
            }

            // Lưu metrics
            if (metrics != null) {
                metricService.saveMetrics(machineId, metrics);
                logger.debug("Đã lưu metrics cho machine: {}", machineId);
            }

            // Cập nhật trạng thái online
            machineService.updateOnlineStatus(machineId, true);

            // Response thành công
            response.put("success", true);
            response.put("message", "Đã nhận heartbeat");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Lỗi khi xử lý heartbeat: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
