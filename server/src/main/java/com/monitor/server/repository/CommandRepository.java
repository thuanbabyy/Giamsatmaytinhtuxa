package com.monitor.server.repository;

import com.monitor.server.model.Command;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandRepository extends JpaRepository<Command, Long> {

    List<Command> findByMachineIdOrderByCreatedAtDesc(String machineId);

    List<Command> findByMachineIdAndStatus(String machineId, String status);

    List<Command> findByStatusOrderByCreatedAtAsc(String status);

    @Query("SELECT c FROM Command c WHERE c.machineId = :machineId AND c.commandType = :commandType ORDER BY c.createdAt DESC")
    List<Command> findByMachineIdAndCommandType(@Param("machineId") String machineId,
            @Param("commandType") String commandType);

    // Xóa tất cả commands của một máy
    void deleteByMachineId(String machineId);
}
