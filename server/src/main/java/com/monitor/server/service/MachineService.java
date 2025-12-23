package com.monitor.server.service;

import com.monitor.server.model.Machine;
import com.monitor.server.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý máy tính
 */
@Service
public class MachineService {
    
    @Autowired
    private MachineRepository machineRepository;
    
    /**
     * Lấy tất cả máy tính
     */
    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }
    
    /**
     * Lấy máy tính theo ID
     */
    public Optional<Machine> getMachineById(String machineId) {
        return machineRepository.findByMachineId(machineId);
    }
    
    /**
     * Cập nhật trạng thái online/offline
     */
    public void updateMachineStatus(String machineId, boolean isOnline) {
        Optional<Machine> machineOpt = machineRepository.findByMachineId(machineId);
        if (machineOpt.isPresent()) {
            Machine machine = machineOpt.get();
            machine.setOnline(isOnline);
            machine.setLastHeartbeat(LocalDateTime.now());
            machineRepository.save(machine);
        }
    }
    
    /**
     * Lưu hoặc cập nhật máy tính
     */
    public Machine saveOrUpdateMachine(Machine machine) {
        return machineRepository.save(machine);
    }
}

