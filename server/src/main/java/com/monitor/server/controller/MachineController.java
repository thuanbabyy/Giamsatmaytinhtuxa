package com.monitor.server.controller;

import com.monitor.server.model.Machine;
import com.monitor.server.service.MachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller quản lý máy tính
 */
@RestController
@RequestMapping("/api/machines")
@CrossOrigin(origins = "*")
public class MachineController {

    @Autowired
    private MachineService machineService;

    /**
     * Lấy tất cả máy tính
     */
    @GetMapping
    public ResponseEntity<List<Machine>> getAllMachines() {
        return ResponseEntity.ok(machineService.getAllMachines());
    }

    /**
     * Lấy máy tính theo ID
     */
    @GetMapping("/{machineId}")
    public ResponseEntity<Machine> getMachineById(@PathVariable String machineId) {
        Optional<Machine> machine = machineService.getMachineById(machineId);
        return machine.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Đăng ký máy tính mới (từ client)
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerMachine(@RequestBody Map<String, String> request) {
        String machineId = request.get("machineId");
        String name = request.get("name");
        String ipAddress = request.get("ipAddress");
        String osName = request.get("osName");
        String osVersion = request.get("osVersion");

        Machine machine = machineService.registerMachine(machineId, name, ipAddress, osName, osVersion);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đăng ký thành công");
        response.put("machine", machine);

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách máy online
     */
    @GetMapping("/online")
    public ResponseEntity<List<Machine>> getOnlineMachines() {
        return ResponseEntity.ok(machineService.getOnlineMachines());
    }

    /**
     * Lấy danh sách máy offline
     */
    @GetMapping("/offline")
    public ResponseEntity<List<Machine>> getOfflineMachines() {
        return ResponseEntity.ok(machineService.getOfflineMachines());
    }

    /**
     * Xóa máy tính và tất cả dữ liệu liên quan
     */
    @DeleteMapping("/{machineId}")
    public ResponseEntity<Map<String, Object>> deleteMachine(@PathVariable String machineId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Kiểm tra máy có tồn tại không
            Optional<Machine> machine = machineService.getMachineById(machineId);
            if (!machine.isPresent()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy máy tính");
                return ResponseEntity.notFound().build();
            }

            // Xóa máy
            machineService.deleteMachine(machineId);

            response.put("success", true);
            response.put("message", "Đã xóa máy thành công");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi xóa máy: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
