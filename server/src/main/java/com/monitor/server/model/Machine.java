package com.monitor.server.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho một máy tính client được quản lý
 */
@Entity
@Table(name = "machines", indexes = {
    @Index(name = "idx_machines_online", columnList = "is_online"),
    @Index(name = "idx_machines_ip", columnList = "ip_address")
})
public class Machine {
    
    @Id
    @Column(name = "machine_id", length = 100, nullable = false)
    private String machineId;
    
    @Column(name = "name", length = 255)
    private String name;
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "os_name", length = 100)
    private String osName;
    
    @Column(name = "os_version", length = 100)
    private String osVersion;
    
    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;
    
    @Column(name = "last_response_time")
    private LocalDateTime lastResponseTime;
    
    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "secret_key", length = 255)
    private String secretKey;
    
    @PrePersist
    protected void onCreate() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (isOnline == null) {
            isOnline = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getMachineId() {
        return machineId;
    }
    
    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getOsName() {
        return osName;
    }
    
    public void setOsName(String osName) {
        this.osName = osName;
    }
    
    public String getOsVersion() {
        return osVersion;
    }
    
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }
    
    public Boolean getIsOnline() {
        return isOnline;
    }
    
    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }
    
    public LocalDateTime getLastResponseTime() {
        return lastResponseTime;
    }
    
    public void setLastResponseTime(LocalDateTime lastResponseTime) {
        this.lastResponseTime = lastResponseTime;
    }
    
    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
    
    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getSecretKey() {
        return secretKey;
    }
    
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
    
    // Helper methods for compatibility
    public boolean isOnline() {
        return isOnline != null && isOnline;
    }
    
    public void setOnline(boolean online) {
        this.isOnline = online;
    }
    
    public LocalDateTime getLastHeartbeat() {
        return lastResponseTime;
    }
    
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastResponseTime = lastHeartbeat;
    }
}
