package com.monitor.server.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho thông báo đã gửi đến client
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_machine", columnList = "machine_id"),
    @Index(name = "idx_notifications_sent", columnList = "sent_at")
})
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "machine_id", nullable = false, length = 100)
    private String machineId;
    
    @Column(name = "message", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String message;
    
    @Column(name = "title", length = 255)
    private String title;
    
    @Column(name = "notification_type", length = 50, nullable = false)
    private String notificationType = "INFO"; // INFO, WARNING, ERROR
    
    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;
    
    @Column(name = "displayed_at")
    private LocalDateTime displayedAt; // Thời gian client hiển thị
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", insertable = false, updatable = false)
    private Machine machine;
    
    @PrePersist
    protected void onCreate() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
        if (notificationType == null) {
            notificationType = "INFO";
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
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getNotificationType() {
        return notificationType;
    }
    
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public LocalDateTime getDisplayedAt() {
        return displayedAt;
    }
    
    public void setDisplayedAt(LocalDateTime displayedAt) {
        this.displayedAt = displayedAt;
    }
    
    public Machine getMachine() {
        return machine;
    }
    
    public void setMachine(Machine machine) {
        this.machine = machine;
    }
}

