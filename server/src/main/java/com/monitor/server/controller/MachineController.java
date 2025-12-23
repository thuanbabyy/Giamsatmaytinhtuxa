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
 * REST Controller quản lý máy tính
 * 
 * Endpoints:
 * - GET /api/machines - Lấy danh sách tất cả máy tính
 * - GET /api/machines/{id} - Lấy thông tin một máy tính
 */
@RestController
@RequestMapping("/api/machines")
@CrossOrigin(origins = "*")
public class MachineController {
    
    @Autowired
    private MachineService machineService;
    
    /**
     * Lấy danh sách tất cả máy tính
     * 
     * Response:
     * [
     *   {
     *     "machineId": "MACHINE-001",
     *     "name": "Máy tính 1",
     *     "isOnline": true,
     *     "lastHeartbeat": "2024-01-01T10:00:00",
     *     ...
     *   }
     * ]
     */
    @GetMapping
    public ResponseEntity<List<Machine>> getAllMachines() {
        List<Machine> machines = machineService.getAllMachines();
        return ResponseEntity.ok(machines);
    }
    
    /**
     * Lấy thông tin một máy tính
     * 
     * Response:
     * {
     *   "machineId": "MACHINE-001",
     *   "name": "Máy tính 1",
     *   "isOnline": true,
     *   ...
     * }
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMachine(@PathVariable String id) {
        Optional<Machine> machine = machineService.getMachineById(id);
        
        if (machine.isPresent()) {
            return ResponseEntity.ok(machine.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Không tìm thấy máy tính với ID: " + id);
            return ResponseEntity.notFound().build();
        }
    }
}

