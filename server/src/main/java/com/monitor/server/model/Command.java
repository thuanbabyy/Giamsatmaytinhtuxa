package com.monitor.server.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho lệnh điều khiển từ server đến client
 */
@Entity
@Table(name = "commands", indexes = {
    @Index(name = "idx_commands_machine", columnList = "machine_id"),
    @Index(name = "idx_commands_status", columnList = "status"),
    @Index(name = "idx_commands_type", columnList = "command_type"),
    @Index(name = "idx_commands_created", columnList = "created_at")
})
public class Command {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "machine_id", nullable = false, length = 100)
    private String machineId;
    
    @Column(name = "command_type", nullable = false, length = 50)
    private String commandType; // LOCK, UNLOCK, SCREEN_CAPTURE, NOTIFICATION
    
    @Column(name = "command_data", columnDefinition = "NVARCHAR(MAX)")
    private String commandData; // JSON data
    
    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING"; // PENDING, SENT, COMPLETED, FAILED
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "executed_at")
    private LocalDateTime executedAt;
    
    @Column(name = "response_data", columnDefinition = "NVARCHAR(MAX)")
    private String responseData; // Response từ client
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", insertable = false, updatable = false)
    private Machine machine;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
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
    
    public String getCommandType() {
        return commandType;
    }
    
    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }
    
    public String getCommandData() {
        return commandData;
    }
    
    public void setCommandData(String commandData) {
        this.commandData = commandData;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExecutedAt() {
        return executedAt;
    }
    
    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
    
    public String getResponseData() {
        return responseData;
    }
    
    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }
    
    public Machine getMachine() {
        return machine;
    }
    
    public void setMachine(Machine machine) {
        this.machine = machine;
    }
}

