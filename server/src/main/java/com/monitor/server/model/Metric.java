package com.monitor.server.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity lưu trữ metrics từ client
 */
@Entity
@Table(name = "metrics")
public class Metric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String machineId;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    // CPU Metrics
    private Double cpuUsage;
    private Integer coreCount;
    
    // Memory Metrics
    private Long memoryTotal;
    private Long memoryUsed;
    private Double memoryUsagePercent;
    
    // Disk Metrics
    private Long diskTotal;
    private Long diskUsed;
    private Double diskUsagePercent;
    
    // Network Metrics
    private Long networkBytesRecv;
    private Long networkBytesSent;
    
    // Raw JSON data
    @Column(columnDefinition = "TEXT")
    private String rawData;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getMachineId() {
        return machineId;
    }
    
    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Double getCpuUsage() {
        return cpuUsage;
    }
    
    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }
    
    public Integer getCoreCount() {
        return coreCount;
    }
    
    public void setCoreCount(Integer coreCount) {
        this.coreCount = coreCount;
    }
    
    public Long getMemoryTotal() {
        return memoryTotal;
    }
    
    public void setMemoryTotal(Long memoryTotal) {
        this.memoryTotal = memoryTotal;
    }
    
    public Long getMemoryUsed() {
        return memoryUsed;
    }
    
    public void setMemoryUsed(Long memoryUsed) {
        this.memoryUsed = memoryUsed;
    }
    
    public Double getMemoryUsagePercent() {
        return memoryUsagePercent;
    }
    
    public void setMemoryUsagePercent(Double memoryUsagePercent) {
        this.memoryUsagePercent = memoryUsagePercent;
    }
    
    public Long getDiskTotal() {
        return diskTotal;
    }
    
    public void setDiskTotal(Long diskTotal) {
        this.diskTotal = diskTotal;
    }
    
    public Long getDiskUsed() {
        return diskUsed;
    }
    
    public void setDiskUsed(Long diskUsed) {
        this.diskUsed = diskUsed;
    }
    
    public Double getDiskUsagePercent() {
        return diskUsagePercent;
    }
    
    public void setDiskUsagePercent(Double diskUsagePercent) {
        this.diskUsagePercent = diskUsagePercent;
    }
    
    public Long getNetworkBytesRecv() {
        return networkBytesRecv;
    }
    
    public void setNetworkBytesRecv(Long networkBytesRecv) {
        this.networkBytesRecv = networkBytesRecv;
    }
    
    public Long getNetworkBytesSent() {
        return networkBytesSent;
    }
    
    public void setNetworkBytesSent(Long networkBytesSent) {
        this.networkBytesSent = networkBytesSent;
    }
    
    public String getRawData() {
        return rawData;
    }
    
    public void setRawData(String rawData) {
        this.rawData = rawData;
    }
}

