package com.monitor.server;

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
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Hệ Thống Giám Sát Máy Tính - SERVER");
        System.out.println("========================================");
        System.out.println("Đang khởi động server...");
        
        SpringApplication.run(MonitorServerApplication.class, args);
        
        System.out.println("Server đã khởi động thành công!");
        System.out.println("REST API: http://localhost:8080/api");
        System.out.println("Giao diện quản lý: http://localhost:8080/server");
        System.out.println("WebSocket (raw client): ws://localhost:8080/ws-client");
        System.out.println("WebSocket (STOMP): ws://localhost:8080/ws-stomp");
    }
}

