package com.monitor.server.service;

import com.monitor.server.model.Machine;
import com.monitor.server.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý máy tính client
 */
@Service
public class MachineService {
    
    @Autowired
    private MachineRepository machineRepository;
    
    /**
     * Đăng ký máy tính mới hoặc cập nhật thông tin
     */
    @Transactional
    public Machine registerMachine(String machineId, String name, String ipAddress, String osName, String osVersion) {
        Optional<Machine> existing = machineRepository.findByMachineId(machineId);
        
        Machine machine;
        if (existing.isPresent()) {
            machine = existing.get();
            machine.setName(name);
            machine.setIpAddress(ipAddress);
            machine.setOsName(osName);
            machine.setOsVersion(osVersion);
            machine.setIsOnline(true);
            machine.setLastResponseTime(LocalDateTime.now());
        } else {
            machine = new Machine();
            machine.setMachineId(machineId);
            machine.setName(name);
            machine.setIpAddress(ipAddress);
            machine.setOsName(osName);
            machine.setOsVersion(osVersion);
            machine.setIsOnline(true);
            machine.setLastResponseTime(LocalDateTime.now());
        }
        
        return machineRepository.save(machine);
    }
    
    /**
     * Cập nhật trạng thái online/offline dựa trên phản hồi
     */
    @Transactional
    public void updateOnlineStatus(String machineId, boolean isOnline) {
        machineRepository.updateOnlineStatus(machineId, isOnline, LocalDateTime.now());
    }
    
    /**
     * Lấy tất cả máy tính
     */
    public List<Machine> getAllMachines() {
        return machineRepository.findAllOrderByRegisteredAtDesc();
    }
    
    /**
     * Lấy máy tính theo ID
     */
    public Optional<Machine> getMachineById(String machineId) {
        return machineRepository.findByMachineId(machineId);
    }
    
    /**
     * Lấy danh sách máy online
     */
    public List<Machine> getOnlineMachines() {
        return machineRepository.findByIsOnline(true);
    }
    
    /**
     * Lấy danh sách máy offline
     */
    public List<Machine> getOfflineMachines() {
        return machineRepository.findByIsOnline(false);
    }
}
