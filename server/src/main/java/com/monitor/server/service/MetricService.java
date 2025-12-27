package com.monitor.server.service;

import com.google.gson.Gson;
import com.monitor.server.model.Machine;
import com.monitor.server.model.Metric;
import com.monitor.server.repository.MachineRepository;
import com.monitor.server.repository.MetricRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service xử lý metrics từ client
 * - Lưu metrics vào database
 * - Lấy metrics theo machineId
 * - Chuyển đổi dữ liệu JSON thành Metric entity
 */
@Service
public class MetricService {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricService.class);
    
    @Autowired
    private MetricRepository metricRepository;
    
    @Autowired
    private MachineRepository machineRepository;
    
    private Gson gson = new Gson();
    
    /**
     * Lưu metrics từ heartbeat
     * @param machineId ID của máy tính
     * @param metricsData Dữ liệu metrics dạng Map
     * @return Metric đã lưu
     */
    public Metric saveMetrics(String machineId, Map<String, Object> metricsData) {
        try {
            Metric metric = new Metric();
            metric.setMachineId(machineId);
            metric.setTimestamp(LocalDateTime.now());
            
            // Parse CPU metrics
            if (metricsData.containsKey("cpu")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cpuData = (Map<String, Object>) metricsData.get("cpu");
                if (cpuData.containsKey("totalUsage")) {
                    metric.setCpuUsage(((Number) cpuData.get("totalUsage")).doubleValue());
                }
                if (cpuData.containsKey("coreCount")) {
                    metric.setCoreCount(((Number) cpuData.get("coreCount")).intValue());
                }
            }
            
            // Parse Memory metrics
            if (metricsData.containsKey("memory")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> memoryData = (Map<String, Object>) metricsData.get("memory");
                if (memoryData.containsKey("total")) {
                    metric.setMemoryTotal(((Number) memoryData.get("total")).longValue());
                }
                if (memoryData.containsKey("used")) {
                    metric.setMemoryUsed(((Number) memoryData.get("used")).longValue());
                }
                if (memoryData.containsKey("usagePercent")) {
                    metric.setMemoryUsagePercent(((Number) memoryData.get("usagePercent")).doubleValue());
                }
            }
            
            // Parse Disk metrics
            if (metricsData.containsKey("disk")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> diskData = (Map<String, Object>) metricsData.get("disk");
                if (diskData.containsKey("total")) {
                    metric.setDiskTotal(((Number) diskData.get("total")).longValue());
                }
                if (diskData.containsKey("used")) {
                    metric.setDiskUsed(((Number) diskData.get("used")).longValue());
                }
                if (diskData.containsKey("total") && diskData.containsKey("used")) {
                    long total = ((Number) diskData.get("total")).longValue();
                    long used = ((Number) diskData.get("used")).longValue();
                    if (total > 0) {
                        metric.setDiskUsagePercent((double) used / total * 100);
                    }
                }
            }
            
            // Parse Network metrics
            if (metricsData.containsKey("network")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> networkData = (Map<String, Object>) metricsData.get("network");
                if (networkData.containsKey("totalBytesRecv")) {
                    metric.setNetworkBytesRecv(((Number) networkData.get("totalBytesRecv")).longValue());
                }
                if (networkData.containsKey("totalBytesSent")) {
                    metric.setNetworkBytesSent(((Number) networkData.get("totalBytesSent")).longValue());
                }
            }
            
            // Lưu raw data dạng JSON
            metric.setRawData(gson.toJson(metricsData));
            
            // Lưu vào database
            Metric savedMetric = metricRepository.save(metric);
            
            // Cập nhật thông tin machine
            updateMachineInfo(machineId, metricsData);
            
            logger.debug("Đã lưu metrics cho machine: {}", machineId);
            return savedMetric;
            
        } catch (Exception e) {
            logger.error("Lỗi khi lưu metrics: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Cập nhật thông tin machine từ metrics
     */
    private void updateMachineInfo(String machineId, Map<String, Object> metricsData) {
        try {
            Optional<Machine> machineOpt = machineRepository.findByMachineId(machineId);
            if (machineOpt.isPresent()) {
                Machine machine = machineOpt.get();
                machine.setLastHeartbeat(LocalDateTime.now());
                machine.setIsOnline(true);
                machineRepository.save(machine);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật machine info: {}", e.getMessage());
        }
    }
    
    /**
     * Lấy metrics mới nhất của một machine
     * @param machineId ID của máy tính
     * @param limit Số lượng metrics cần lấy
     * @return Danh sách metrics
     */
    public List<Metric> getLatestMetrics(String machineId, int limit) {
        return metricRepository.findByMachineIdOrderByTimestampDesc(machineId)
            .stream()
            .limit(limit)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Lấy metrics từ một thời điểm
     * @param machineId ID của máy tính
     * @param since Thời điểm bắt đầu
     * @return Danh sách metrics
     */
    public List<Metric> getMetricsSince(String machineId, LocalDateTime since) {
        return metricRepository.findByMachineIdSince(machineId, since);
    }
    
    /**
     * Lấy metric mới nhất của một machine
     */
    public Optional<Metric> getLatestMetric(String machineId) {
        List<Metric> metrics = getLatestMetrics(machineId, 1);
        return metrics.isEmpty() ? Optional.empty() : Optional.of(metrics.get(0));
    }
}

