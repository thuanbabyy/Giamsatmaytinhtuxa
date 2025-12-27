package com.monitor.server.repository;

import com.monitor.server.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByMachineIdOrderBySentAtDesc(String machineId);
    
    @Query("SELECT n FROM Notification n WHERE n.machineId = :machineId ORDER BY n.sentAt DESC")
    List<Notification> findRecentByMachineId(@Param("machineId") String machineId);
}

