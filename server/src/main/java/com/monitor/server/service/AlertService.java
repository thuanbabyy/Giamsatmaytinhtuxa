package com.monitor.server.service;

import com.monitor.server.model.Alert;
import com.monitor.server.model.Machine;
import com.monitor.server.repository.AlertRepository;
import com.monitor.server.repository.MachineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý cảnh báo
 * - Tạo cảnh báo tự động dựa trên metrics
 * - Quản lý trạng thái cảnh báo (resolved/unresolved)
 * - Kiểm tra máy offline
 */
@Service
public class AlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);
    
    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private MachineRepository machineRepository;
    
    // Cấu hình từ application.properties
    @Value("${monitor.alert.cpu.threshold:90}")
    private double cpuThreshold;
    
    @Value("${monitor.alert.cpu.duration:30}")
    private int cpuDurationSeconds;
    
    @Value("${monitor.alert.ram.threshold:85}")
    private double ramThreshold;
    
    @Value("${monitor.alert.offline.timeout:15}")
    private int offlineTimeoutSeconds;
    
    /**
     * Tạo cảnh báo
     */
    public Alert createAlert(String machineId, String alertType, String message, String severity) {
        Alert alert = new Alert();
        alert.setMachineId(machineId);
        alert.setAlertType(alertType);
        alert.setMessage(message);
        alert.setSeverity(severity);
        alert.setTimestamp(LocalDateTime.now());
        alert.setResolved(false);
        
        Alert saved = alertRepository.save(alert);
        logger.info("Đã tạo cảnh báo: {} - {} - {}", machineId, alertType, message);
        return saved;
    }
    
    /**
     * Kiểm tra và tạo cảnh báo CPU cao
     * @param machineId ID của máy tính
     * @param cpuUsage CPU usage hiện tại (%)
     */
    public void checkCpuAlert(String machineId, double cpuUsage) {
        if (cpuUsage > cpuThreshold) {
            // Kiểm tra xem đã có cảnh báo CPU cao chưa được giải quyết chưa
            List<Alert> existingAlerts = alertRepository.findByMachineIdAndResolvedFalseOrderByTimestampDesc(machineId);
            boolean hasCpuAlert = existingAlerts.stream()
                .anyMatch(a -> a.getAlertType().equals("CPU_HIGH"));
            
            if (!hasCpuAlert) {
                createAlert(
                    machineId,
                    "CPU_HIGH",
                    String.format("CPU usage cao: %.2f%% (ngưỡng: %.2f%%)", cpuUsage, cpuThreshold),
                    "WARNING"
                );
            }
        }
    }
    
    /**
     * Kiểm tra và tạo cảnh báo RAM cao
     * @param machineId ID của máy tính
     * @param ramUsage RAM usage hiện tại (%)
     */
    public void checkRamAlert(String machineId, double ramUsage) {
        if (ramUsage > ramThreshold) {
            // Kiểm tra xem đã có cảnh báo RAM cao chưa được giải quyết chưa
            List<Alert> existingAlerts = alertRepository.findByMachineIdAndResolvedFalseOrderByTimestampDesc(machineId);
            boolean hasRamAlert = existingAlerts.stream()
                .anyMatch(a -> a.getAlertType().equals("RAM_HIGH"));
            
            if (!hasRamAlert) {
                createAlert(
                    machineId,
                    "RAM_HIGH",
                    String.format("RAM usage cao: %.2f%% (ngưỡng: %.2f%%)", ramUsage, ramThreshold),
                    "WARNING"
                );
            }
        }
    }
    
    /**
     * Kiểm tra máy offline
     * Chạy định kỳ để kiểm tra các máy không gửi heartbeat
     */
    public void checkOfflineMachines() {
        List<Machine> machines = machineRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        
        for (Machine machine : machines) {
            if (machine.isOnline()) {
                LocalDateTime lastHeartbeat = machine.getLastHeartbeat();
                if (lastHeartbeat != null) {
                    long secondsSinceLastHeartbeat = java.time.Duration.between(lastHeartbeat, now).getSeconds();
                    
                    if (secondsSinceLastHeartbeat > offlineTimeoutSeconds) {
                        // Máy đã offline
                        machine.setIsOnline(false);
                        machineRepository.save(machine);
                        
                        // Tạo cảnh báo
                        List<Alert> existingAlerts = alertRepository.findByMachineIdAndResolvedFalseOrderByTimestampDesc(machine.getMachineId());
                        boolean hasOfflineAlert = existingAlerts.stream()
                            .anyMatch(a -> a.getAlertType().equals("OFFLINE"));
                        
                        if (!hasOfflineAlert) {
                            createAlert(
                                machine.getMachineId(),
                                "OFFLINE",
                                String.format("Máy tính không phản hồi trong %d giây", secondsSinceLastHeartbeat),
                                "CRITICAL"
                            );
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Lấy tất cả cảnh báo chưa được giải quyết
     */
    public List<Alert> getUnresolvedAlerts() {
        return alertRepository.findByResolvedFalseOrderByTimestampDesc();
    }
    
    /**
     * Lấy cảnh báo của một machine
     */
    public List<Alert> getAlertsByMachine(String machineId) {
        return alertRepository.findByMachineIdOrderByTimestampDesc(machineId);
    }
    
    /**
     * Đánh dấu cảnh báo đã được giải quyết
     */
    public void resolveAlert(Long alertId) {
        Optional<Alert> alertOpt = alertRepository.findById(alertId);
        if (alertOpt.isPresent()) {
            Alert alert = alertOpt.get();
            alert.setResolved(true);
            alert.setResolvedAt(LocalDateTime.now());
            alertRepository.save(alert);
            logger.info("Đã giải quyết cảnh báo: {}", alertId);
        }
    }
}

