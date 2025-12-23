package com.monitor.server.controller;

import com.monitor.server.model.Metric;
import com.monitor.server.service.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller quản lý metrics
 * 
 * Endpoints:
 * - GET /api/machines/{id}/metrics - Lấy metrics của một máy tính
 * - GET /api/machines/{id}/metrics/latest - Lấy metric mới nhất
 */
@RestController
@RequestMapping("/api/machines")
@CrossOrigin(origins = "*")
public class MetricController {
    
    @Autowired
    private MetricService metricService;
    
    /**
     * Lấy metrics của một máy tính
     * 
     * Query Parameters:
     * - limit: Số lượng metrics cần lấy (mặc định: 100)
     * 
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "machineId": "MACHINE-001",
     *     "cpuUsage": 45.5,
     *     "memoryUsagePercent": 60.2,
     *     "timestamp": "2024-01-01T10:00:00",
     *     ...
     *   }
     * ]
     */
    @GetMapping("/{id}/metrics")
    public ResponseEntity<List<Metric>> getMetrics(
            @PathVariable String id,
            @RequestParam(defaultValue = "100") int limit) {
        
        List<Metric> metrics = metricService.getLatestMetrics(id, limit);
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Lấy metric mới nhất của một máy tính
     * 
     * Response:
     * {
     *   "id": 1,
     *   "machineId": "MACHINE-001",
     *   "cpuUsage": 45.5,
     *   "memoryUsagePercent": 60.2,
     *   "timestamp": "2024-01-01T10:00:00",
     *   ...
     * }
     */
    @GetMapping("/{id}/metrics/latest")
    public ResponseEntity<?> getLatestMetric(@PathVariable String id) {
        Optional<Metric> metric = metricService.getLatestMetric(id);
        
        if (metric.isPresent()) {
            return ResponseEntity.ok(metric.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Không tìm thấy metrics cho máy tính: " + id);
            return ResponseEntity.notFound().build();
        }
    }
}

