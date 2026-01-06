package com.monitor.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Application Class - Entry point của Spring Boot Server
 * 
 * Chức năng:
 * - Khởi động Spring Boot server
 * - Cấu hình WebSocket, REST API, Database
 * - Bật scheduling để chạy các task định kỳ (phân tích dữ liệu, kiểm tra offline)
 */
@SpringBootApplication
@EnableScheduling
public class MonitorServerApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(MonitorServerApplication.class);
    
    public static void main(String[] args) {
        logger.info("========================================");
        logger.info("  Hệ Thống Giám Sát Máy Tính - SERVER");
        logger.info("========================================");
        logger.info("Đang khởi động server...");
        
        SpringApplication.run(MonitorServerApplication.class, args);
        
        logger.info("Server đã khởi động thành công!");
        logger.info("REST API: http://localhost:8080/api");
        logger.info("Giao diện quản lý: http://localhost:8080/server");
        logger.info("WebSocket (raw client): ws://localhost:8080/ws-client");
        logger.info("WebSocket (STOMP): ws://localhost:8080/ws-stomp");
    }
}

