package com.monitor.server.controller;

import com.monitor.server.service.CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller gửi lệnh điều khiển đến client
 * 
 * Endpoints:
 * - POST /api/machines/{id}/commands - Gửi lệnh đến máy tính
 * 
 * Request Body:
 * {
 *   "command": "KILL_PROCESS",
 *   "parameter": "1234"
 * }
 * 
 * Hoặc:
 * {
 *   "command": "SHUTDOWN"
 * }
 */
@RestController
@RequestMapping("/api/machines")
@CrossOrigin(originPatterns = "*")
public class CommandController {
    
    @Autowired
    private CommandService commandService;
    
    /**
     * Gửi lệnh đến máy tính
     * 
     * Các lệnh hỗ trợ:
     * - KILL_PROCESS: Dừng process (cần parameter là PID)
     * - SHUTDOWN: Tắt máy
     * - LOCK_KEYBOARD: Khóa bàn phím
     * - SCREENSHOT: Chụp màn hình
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Đã gửi lệnh thành công"
     * }
     */
    @PostMapping("/{id}/commands")
    public ResponseEntity<Map<String, Object>> sendCommand(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String command = request.get("command");
            String parameter = request.get("parameter");
            
            if (command == null || command.isEmpty()) {
                response.put("success", false);
                response.put("message", "Thiếu lệnh");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Kiểm tra client có online không
            if (!commandService.isClientOnline(id)) {
                response.put("success", false);
                response.put("message", "Máy tính không online hoặc không kết nối WebSocket");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Gửi lệnh
            boolean sent = commandService.sendCommand(id, command, parameter);
            
            if (sent) {
                response.put("success", true);
                response.put("message", "Đã gửi lệnh thành công");
            } else {
                response.put("success", false);
                response.put("message", "Không thể gửi lệnh");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

