package com.monitor.server.repository;

import com.monitor.server.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByMachineIdOrderByTimestampDesc(String machineId);
    List<Alert> findByResolvedFalseOrderByTimestampDesc();
    List<Alert> findByMachineIdAndResolvedFalseOrderByTimestampDesc(String machineId);
}

