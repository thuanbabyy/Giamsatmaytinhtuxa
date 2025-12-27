package com.monitor.server.repository;

import com.monitor.server.model.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MachineRepository extends JpaRepository<Machine, String> {
    
    Optional<Machine> findByMachineId(String machineId);
    
    List<Machine> findByIsOnline(Boolean isOnline);
    
    @Modifying
    @Query("UPDATE Machine m SET m.isOnline = :isOnline, m.lastResponseTime = :lastResponseTime WHERE m.machineId = :machineId")
    void updateOnlineStatus(@Param("machineId") String machineId, 
                           @Param("isOnline") Boolean isOnline, 
                           @Param("lastResponseTime") LocalDateTime lastResponseTime);
    
    @Query("SELECT m FROM Machine m ORDER BY m.registeredAt DESC")
    List<Machine> findAllOrderByRegisteredAtDesc();
}
