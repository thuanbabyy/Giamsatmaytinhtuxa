package com.monitor.server.service;

import com.monitor.server.model.ScreenData;
import com.monitor.server.repository.ScreenDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service quản lý dữ liệu màn hình
 */
@Service
public class ScreenService {
    
    @Autowired
    private ScreenDataRepository screenDataRepository;
    
    @Autowired
    private CommandService commandService;
    
    /**
     * Lưu ảnh màn hình từ client
     */
    @Transactional
    public ScreenData saveScreenData(String machineId, byte[] imageData, String imageFormat, Long commandId) {
        ScreenData screenData = new ScreenData();
        screenData.setMachineId(machineId);
        screenData.setImageData(imageData);
        screenData.setImageFormat(imageFormat != null ? imageFormat : "PNG");
        screenData.setCommandId(commandId);
        return screenDataRepository.save(screenData);
    }
    
    /**
     * Lấy ảnh màn hình mới nhất của máy
     */
    public Optional<ScreenData> getLatestScreenData(String machineId) {
        return screenDataRepository.findLatestByMachineId(machineId);
    }
    
    /**
     * Lấy lịch sử ảnh màn hình của máy
     */
    public List<ScreenData> getScreenDataHistory(String machineId) {
        return screenDataRepository.findByMachineIdOrderByCapturedAtDesc(machineId);
    }
    
    /**
     * Lấy ảnh màn hình theo command ID
     */
    public Optional<ScreenData> getScreenDataByCommandId(Long commandId) {
        return screenDataRepository.findByCommandId(commandId);
    }
    
    /**
     * Yêu cầu client chụp màn hình
     */
    public boolean requestScreenCapture(String machineId) {
        return commandService.sendScreenCaptureCommand(machineId);
    }
}

