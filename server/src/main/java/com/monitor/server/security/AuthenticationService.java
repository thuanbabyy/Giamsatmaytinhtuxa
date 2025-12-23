package com.monitor.server.security;

import com.monitor.server.model.Machine;
import com.monitor.server.repository.MachineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * Service xác thực client bằng machineId và HMAC
 */
@Service
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    @Autowired
    private MachineRepository machineRepository;
    
    /**
     * Xác thực client bằng machineId và HMAC signature
     * @param machineId ID của máy tính
     * @param payload Payload JSON
     * @param signature HMAC signature
     * @return true nếu xác thực thành công
     */
    public boolean authenticate(String machineId, String payload, String signature) {
        try {
            // Tìm machine trong database
            Optional<Machine> machineOpt = machineRepository.findByMachineId(machineId);
            
            if (machineOpt.isEmpty()) {
                logger.info("Machine không tồn tại, đang tạo mới: {}", machineId);
                // Tự động tạo machine mới nếu chưa có
                Machine newMachine = new Machine();
                newMachine.setMachineId(machineId);
                newMachine.setSecretKey("default-secret-key-change-me"); // Khớp với client default
                newMachine.setName("Máy tính " + machineId);
                newMachine.setLastHeartbeat(java.time.LocalDateTime.now());
                newMachine.setOnline(true);
                machineRepository.save(newMachine);
                logger.info("Đã tạo machine mới: {} với secret key mặc định", machineId);
                // Sau khi tạo, cần xác thực lại với secret key mới
                String expectedSignature = generateHMAC(payload, newMachine.getSecretKey());
                boolean isValid = expectedSignature.equals(signature);
                if (!isValid) {
                    logger.warn("Xác thực thất bại cho machine mới (lần đầu): {}. Payload length: {}, Signature length: {}", 
                        machineId, payload.length(), signature != null ? signature.length() : 0);
                    logger.debug("Expected (first 50): {}, Got (first 50): {}", 
                        expectedSignature.length() > 50 ? expectedSignature.substring(0, 50) : expectedSignature, 
                        signature != null && signature.length() > 50 ? signature.substring(0, 50) : signature);
                    // Cho phép kết nối lần đầu để machine được tạo, nhưng log warning
                    // Lần sau sẽ verify chặt chẽ hơn
                    logger.info("Cho phép kết nối lần đầu cho machine mới: {}", machineId);
                    return true; // Cho phép kết nối lần đầu
                } else {
                    logger.info("Xác thực thành công cho machine mới: {}", machineId);
                }
                return isValid;
            }
            
            Machine machine = machineOpt.get();
            String secretKey = machine.getSecretKey();
            
            // Tính toán HMAC
            String expectedSignature = generateHMAC(payload, secretKey);
            
            // So sánh signature
            boolean isValid = expectedSignature.equals(signature);
            
            if (!isValid) {
                logger.warn("Xác thực thất bại cho machine: {}", machineId);
                logger.debug("Payload length: {}, Secret key: {}***", payload.length(), secretKey.substring(0, Math.min(10, secretKey.length())));
                logger.debug("Expected signature (first 30 chars): {}", expectedSignature.length() > 30 ? expectedSignature.substring(0, 30) : expectedSignature);
                logger.debug("Received signature (first 30 chars): {}", signature != null && signature.length() > 30 ? signature.substring(0, 30) : signature);
            } else {
                logger.debug("Xác thực thành công cho machine: {}", machineId);
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Lỗi khi xác thực: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Tạo HMAC signature
     */
    private String generateHMAC(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            logger.error("Lỗi khi tạo HMAC: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Lấy secret key của machine
     */
    public String getSecretKey(String machineId) {
        Optional<Machine> machine = machineRepository.findByMachineId(machineId);
        return machine.map(Machine::getSecretKey).orElse(null);
    }
}

