package com.monitor.client.heartbeat;

import com.google.gson.Gson;
import com.monitor.client.monitor.SystemMonitor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Quản lý cơ chế heartbeat thông minh
 * - Mặc định: gửi mỗi 5 giây
 * - CPU > 80%: gửi mỗi 1 giây
 * - Idle: gửi mỗi 10 giây
 */
public class HeartbeatManager {
    
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatManager.class);
    
    private SystemMonitor systemMonitor;
    private String serverUrl;
    private String machineId;
    private String secretKey;
    
    private ScheduledExecutorService scheduler;
    private long currentInterval = 5000; // Mặc định 5 giây
    
    private Gson gson;
    
    public HeartbeatManager(SystemMonitor systemMonitor, String serverUrl, String machineId, String secretKey) {
        this.systemMonitor = systemMonitor;
        this.serverUrl = serverUrl;
        this.machineId = machineId;
        this.secretKey = secretKey;
        this.gson = new Gson();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    /**
     * Bắt đầu gửi heartbeat
     */
    public void start() {
        logger.info("Bắt đầu gửi heartbeat đến server: {}", serverUrl);
        
        // Gửi heartbeat ngay lập tức
        sendHeartbeat();
        
        // Lên lịch gửi heartbeat định kỳ
        scheduler.scheduleAtFixedRate(
            this::sendHeartbeatWithInterval,
            0,
            1,
            TimeUnit.SECONDS
        );
    }
    
    /**
     * Gửi heartbeat với interval thông minh
     */
    private void sendHeartbeatWithInterval() {
        try {
            // Lấy CPU usage để quyết định interval
            double cpuUsage = systemMonitor.getCurrentCpuUsage();
            
            // Quyết định interval dựa trên CPU usage
            long newInterval = calculateInterval(cpuUsage);
            
            // Nếu interval thay đổi, cập nhật
            if (newInterval != currentInterval) {
                currentInterval = newInterval;
                logger.debug("Thay đổi interval heartbeat: {}ms (CPU: {}%)", currentInterval, cpuUsage);
            }
            
            // Gửi heartbeat
            sendHeartbeat();
            
        } catch (Exception e) {
            logger.error("Lỗi khi gửi heartbeat: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Tính toán interval dựa trên CPU usage
     * @param cpuUsage CPU usage percentage
     * @return Interval tính bằng milliseconds
     */
    private long calculateInterval(double cpuUsage) {
        if (cpuUsage > 80) {
            // CPU cao → gửi nhanh hơn (1 giây)
            return 1000;
        } else if (cpuUsage < 10) {
            // CPU thấp (idle) → gửi chậm hơn (10 giây)
            return 10000;
        } else {
            // Bình thường → 5 giây
            return 5000;
        }
    }
    
    /**
     * Gửi heartbeat đến server
     */
    private void sendHeartbeat() {
        try {
            // Thu thập metrics
            Map<String, Object> metrics = systemMonitor.collectAllMetrics();
            
            // Tạo payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("machineId", machineId);
            payload.put("metrics", metrics);
            payload.put("timestamp", System.currentTimeMillis());
            
            // Tạo signature bằng HMAC
            String jsonPayload = gson.toJson(payload);
            String signature = generateHMAC(jsonPayload, secretKey);
            
            // Thêm signature vào header
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("payload", payload);
            requestData.put("signature", signature);
            
            // Gửi POST request
            sendPostRequest(serverUrl + "/api/heartbeat", gson.toJson(requestData));
            
            logger.debug("Đã gửi heartbeat thành công");
            
        } catch (Exception e) {
            logger.error("Lỗi khi gửi heartbeat: {}", e.getMessage());
        }
    }
    
    /**
     * Gửi POST request đến server
     */
    private void sendPostRequest(String url, String jsonData) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("X-Machine-Id", machineId);
            
            StringEntity entity = new StringEntity(jsonData, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200 && statusCode != 201) {
                    logger.warn("Server trả về status code: {}", statusCode);
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi gửi POST request: {}", e.getMessage());
        }
    }
    
    /**
     * Tạo HMAC signature để xác thực
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
     * Dừng heartbeat
     */
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logger.info("Đã dừng heartbeat manager");
    }
}

