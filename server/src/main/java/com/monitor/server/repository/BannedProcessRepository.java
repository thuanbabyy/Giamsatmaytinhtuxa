package com.monitor.server.repository;

import com.monitor.server.model.BannedProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BannedProcessRepository extends JpaRepository<BannedProcess, Long> {
    Optional<BannedProcess> findByProcessName(String processName);
    boolean existsByProcessName(String processName);
}

