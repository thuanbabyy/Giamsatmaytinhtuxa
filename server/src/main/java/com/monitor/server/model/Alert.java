package com.monitor.server.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho cảnh báo
 */
@Entity
@Table(name = "alerts")
public class Alert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String machineId;
    
    @Column(nullable = false)
    private String alertType; // CPU_HIGH, RAM_HIGH, OFFLINE, etc.
    
    @Column(nullable = false)
    private String message;
    
    @Column(nullable = false)
    private String severity; // INFO, WARNING, CRITICAL
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    private boolean resolved;
    private LocalDateTime resolvedAt;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        resolved = false;
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
    
    public String getAlertType() {
        return alertType;
    }
    
    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isResolved() {
        return resolved;
    }
    
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}

