package com.monitor.server.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho dữ liệu ảnh màn hình từ client
 */
@Entity
@Table(name = "screen_data", indexes = {
    @Index(name = "idx_screen_data_machine", columnList = "machine_id"),
    @Index(name = "idx_screen_data_captured", columnList = "captured_at"),
    @Index(name = "idx_screen_data_command", columnList = "command_id")
})
public class ScreenData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "machine_id", nullable = false, length = 100)
    private String machineId;
    
    @Column(name = "image_data", nullable = false, columnDefinition = "VARBINARY(MAX)")
    private byte[] imageData; // Binary image data
    
    @Column(name = "image_format", length = 20, nullable = false)
    private String imageFormat = "PNG"; // PNG, JPEG
    
    @Column(name = "captured_at", nullable = false, updatable = false)
    private LocalDateTime capturedAt;
    
    @Column(name = "command_id")
    private Long commandId; // Liên kết với command nếu có
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", insertable = false, updatable = false)
    private Machine machine;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "command_id", insertable = false, updatable = false)
    private Command command;
    
    @PrePersist
    protected void onCreate() {
        if (capturedAt == null) {
            capturedAt = LocalDateTime.now();
        }
        if (imageFormat == null) {
            imageFormat = "PNG";
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
    
    public byte[] getImageData() {
        return imageData;
    }
    
    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
    
    public String getImageFormat() {
        return imageFormat;
    }
    
    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }
    
    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }
    
    public void setCapturedAt(LocalDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }
    
    public Long getCommandId() {
        return commandId;
    }
    
    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }
    
    public Machine getMachine() {
        return machine;
    }
    
    public void setMachine(Machine machine) {
        this.machine = machine;
    }
    
    public Command getCommand() {
        return command;
    }
    
    public void setCommand(Command command) {
        this.command = command;
    }
}

