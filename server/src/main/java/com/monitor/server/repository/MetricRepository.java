package com.monitor.server.repository;

import com.monitor.server.model.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MetricRepository extends JpaRepository<Metric, Long> {

    List<Metric> findByMachineIdOrderByTimestampDesc(String machineId);

    @Query("SELECT m FROM Metric m WHERE m.machineId = :machineId AND m.timestamp >= :since ORDER BY m.timestamp DESC")
    List<Metric> findByMachineIdSince(@Param("machineId") String machineId, @Param("since") LocalDateTime since);

    @Query(value = "SELECT * FROM metric WHERE machine_id = :machineId ORDER BY timestamp DESC LIMIT :limit", nativeQuery = true)
    List<Metric> findLatestByMachineId(@Param("machineId") String machineId, @Param("limit") int limit);

    // Xóa tất cả metrics của một máy
    void deleteByMachineId(String machineId);
}
