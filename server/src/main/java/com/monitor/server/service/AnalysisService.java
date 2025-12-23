package com.monitor.server.service;

import com.monitor.server.model.Metric;
import com.monitor.server.repository.MetricRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service phân tích dữ liệu và tạo cảnh báo tự động
 * - Phân tích metrics để phát hiện vấn đề
 * - Chạy định kỳ để kiểm tra các điều kiện cảnh báo
 */
@Service
public class AnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);
    
    @Autowired
    private MetricRepository metricRepository;
    
    @Autowired
    private AlertService alertService;
    
    @Value("${monitor.alert.cpu.threshold:90}")
    private double cpuThreshold;
    
    @Value("${monitor.alert.cpu.duration:30}")
    private int cpuDurationSeconds;
    
    /**
     * Phân tích metrics và tạo cảnh báo
     * Chạy mỗi 10 giây
     */
    @Scheduled(fixedRate = 10000) // 10 giây
    public void analyzeMetrics() {
        try {
            // Lấy tất cả machines từ metrics
            List<String> machineIds = metricRepository.findAll()
                .stream()
                .map(Metric::getMachineId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
            
            for (String machineId : machineIds) {
                analyzeMachineMetrics(machineId);
            }
            
            // Kiểm tra máy offline
            alertService.checkOfflineMachines();
            
        } catch (Exception e) {
            logger.error("Lỗi khi phân tích metrics: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Phân tích metrics của một machine
     */
    private void analyzeMachineMetrics(String machineId) {
        try {
            // Lấy metrics trong khoảng thời gian gần đây
            LocalDateTime since = LocalDateTime.now().minusSeconds(cpuDurationSeconds + 10);
            List<Metric> recentMetrics = metricRepository.findByMachineIdSince(machineId, since);
            
            if (recentMetrics.isEmpty()) {
                return;
            }
            
            // Kiểm tra CPU cao trong khoảng thời gian
            boolean cpuHighForDuration = recentMetrics.stream()
                .filter(m -> m.getCpuUsage() != null)
                .allMatch(m -> m.getCpuUsage() > cpuThreshold);
            
            if (cpuHighForDuration && recentMetrics.size() >= 3) {
                // CPU cao liên tục trong khoảng thời gian
                double avgCpu = recentMetrics.stream()
                    .filter(m -> m.getCpuUsage() != null)
                    .mapToDouble(Metric::getCpuUsage)
                    .average()
                    .orElse(0);
                
                alertService.checkCpuAlert(machineId, avgCpu);
            }
            
            // Kiểm tra RAM cao
            Metric latestMetric = recentMetrics.get(0);
            if (latestMetric.getMemoryUsagePercent() != null) {
                alertService.checkRamAlert(machineId, latestMetric.getMemoryUsagePercent());
            }
            
        } catch (Exception e) {
            logger.error("Lỗi khi phân tích metrics cho machine {}: {}", machineId, e.getMessage());
        }
    }
}

