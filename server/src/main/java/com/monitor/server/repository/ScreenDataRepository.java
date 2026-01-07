package com.monitor.server.repository;

import com.monitor.server.model.ScreenData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreenDataRepository extends JpaRepository<ScreenData, Long> {

    List<ScreenData> findByMachineIdOrderByCapturedAtDesc(String machineId);

    @Query(value = "SELECT TOP 1 * FROM screen_data WHERE machine_id = :machineId ORDER BY captured_at DESC", nativeQuery = true)
    Optional<ScreenData> findLatestByMachineId(@Param("machineId") String machineId);

    @Query("SELECT s FROM ScreenData s WHERE s.commandId = :commandId")
    Optional<ScreenData> findByCommandId(@Param("commandId") Long commandId);

    // Xóa tất cả screen data của một máy
    void deleteByMachineId(String machineId);
}
