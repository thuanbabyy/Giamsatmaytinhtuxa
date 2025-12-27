package com.monitor.server.service;

import com.monitor.server.model.Notification;
import com.monitor.server.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service quản lý thông báo
 */
@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private CommandService commandService;
    
    /**
     * Gửi thông báo đến client
     */
    @Transactional
    public boolean sendNotification(String machineId, String title, String message, String notificationType) {
        // Tạo notification trong database
        Notification notification = new Notification();
        notification.setMachineId(machineId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(notificationType != null ? notificationType : "INFO");
        notification = notificationRepository.save(notification);
        
        // Gửi lệnh qua WebSocket
        Map<String, Object> extraData = new java.util.HashMap<>();
        extraData.put("notificationId", notification.getId());
        extraData.put("title", title);
        extraData.put("message", message);
        extraData.put("type", notificationType);
        
        boolean sent = commandService.sendCommand(machineId, "NOTIFICATION", 
            "{\"title\":\"" + title + "\",\"message\":\"" + message + "\",\"type\":\"" + notificationType + "\"}", 
            extraData);
        
        if (sent) {
            notification.setDisplayedAt(java.time.LocalDateTime.now());
            notificationRepository.save(notification);
        }
        
        return sent;
    }
    
    /**
     * Lấy lịch sử thông báo của máy
     */
    public List<Notification> getNotificationHistory(String machineId) {
        return notificationRepository.findByMachineIdOrderBySentAtDesc(machineId);
    }
    
    /**
     * Lấy thông báo gần đây của máy
     */
    public List<Notification> getRecentNotifications(String machineId, int limit) {
        List<Notification> all = notificationRepository.findByMachineIdOrderBySentAtDesc(machineId);
        return all.stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }
}

