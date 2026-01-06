package com.monitor.server.controller;

import com.monitor.server.model.Command;
import com.monitor.server.service.CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller quản lý lệnh điều khiển
 */
@RestController
@RequestMapping("/api/commands")
@CrossOrigin(origins = "*")
public class CommandController {

    @Autowired
    private CommandService commandService;

    /**
     * Khóa bàn phím chuột
     */
    @PostMapping("/{machineId}/lock")
    public ResponseEntity<Map<String, Object>> lockMachine(@PathVariable String machineId) {
        boolean sent = commandService.sendLockCommand(machineId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", sent);
        response.put("message", sent ? "Đã gửi lệnh khóa" : "Không thể gửi lệnh (máy offline)");

        return ResponseEntity.ok(response);
    }

    /**
     * Mở khóa bàn phím chuột
     */
    @PostMapping("/{machineId}/unlock")
    public ResponseEntity<Map<String, Object>> unlockMachine(@PathVariable String machineId) {
        boolean sent = commandService.sendUnlockCommand(machineId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", sent);
        response.put("message", sent ? "Đã gửi lệnh mở khóa" : "Không thể gửi lệnh (máy offline)");

        return ResponseEntity.ok(response);
    }

    /**
     * Yêu cầu chụp màn hình
     */
    @PostMapping("/{machineId}/screen-capture")
    public ResponseEntity<Map<String, Object>> requestScreenCapture(@PathVariable String machineId) {
        boolean sent = commandService.sendScreenCaptureCommand(machineId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", sent);
        response.put("message", sent ? "Đã gửi yêu cầu chụp màn hình" : "Không thể gửi lệnh (máy offline)");

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy lịch sử lệnh của máy
     */
    @GetMapping("/{machineId}/history")
    public ResponseEntity<List<Command>> getCommandHistory(@PathVariable String machineId) {
        return ResponseEntity.ok(commandService.getCommandHistory(machineId));
    }

    /**
     * Kiểm tra máy có online không
     */
    @GetMapping("/{machineId}/status")
    public ResponseEntity<Map<String, Object>> checkMachineStatus(@PathVariable String machineId) {
        boolean online = commandService.isClientOnline(machineId);

        Map<String, Object> response = new HashMap<>();
        response.put("machineId", machineId);
        response.put("online", online);

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách tiến trình đang chạy
     */
    @PostMapping("/{machineId}/processes")
    public ResponseEntity<Map<String, Object>> getProcesses(@PathVariable String machineId) {
        Map<String, Object> result = commandService.sendGetProcessesCommand(machineId);

        if (result == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Không thể gửi lệnh (máy offline)");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok(result);
    }
}
