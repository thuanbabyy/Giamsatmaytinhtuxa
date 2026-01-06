package com.monitor.client.monitor;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread giám sát hệ thống và gửi metrics về server định kỳ
 */
public class SystemMonitorThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(SystemMonitorThread.class);

    private final String serverUrl;
    private final String machineId;
    private volatile boolean running = true;
    private final int intervalSeconds;

    private SystemInfo systemInfo;
    private HardwareAbstractionLayer hardware;
    private CentralProcessor processor;
    private GlobalMemory memory;
    private OperatingSystem os;

    public SystemMonitorThread(String serverUrl, String machineId, int intervalSeconds) {
        super("SystemMonitor");
        setDaemon(true);
        this.serverUrl = serverUrl;
        this.machineId = machineId;
        this.intervalSeconds = intervalSeconds;

        // Khởi tạo OSHI
        try {
            systemInfo = new SystemInfo();
            hardware = systemInfo.getHardware();
            processor = hardware.getProcessor();
            memory = hardware.getMemory();
            os = systemInfo.getOperatingSystem();
        } catch (Exception e) {
            logger.error("Lỗi khi khởi tạo OSHI: {}", e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        logger.info("Bắt đầu giám sát hệ thống (mỗi {} giây)", intervalSeconds);

        while (running) {
            try {
                Map<String, Object> metrics = collectMetrics();
                sendMetrics(metrics);

                Thread.sleep(intervalSeconds * 1000L);
            } catch (InterruptedException e) {
                logger.info("Monitor thread bị ngắt");
                break;
            } catch (Exception e) {
                logger.error("Lỗi khi thu thập metrics: {}", e.getMessage(), e);
                try {
                    Thread.sleep(intervalSeconds * 1000L);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }

        logger.info("Dừng giám sát hệ thống");
    }

    /**
     * Thu thập metrics từ hệ thống
     */
    private Map<String, Object> collectMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            // CPU metrics
            Map<String, Object> cpuData = new HashMap<>();
            double cpuLoad = processor.getSystemCpuLoad(1000) * 100;
            cpuData.put("totalUsage", Math.round(cpuLoad * 100.0) / 100.0);
            cpuData.put("coreCount", processor.getLogicalProcessorCount());
            metrics.put("cpu", cpuData);

            // Memory metrics
            Map<String, Object> memoryData = new HashMap<>();
            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            long usedMemory = totalMemory - availableMemory;
            double memoryUsagePercent = (usedMemory * 100.0) / totalMemory;

            memoryData.put("total", totalMemory);
            memoryData.put("used", usedMemory);
            memoryData.put("available", availableMemory);
            memoryData.put("usagePercent", Math.round(memoryUsagePercent * 100.0) / 100.0);
            metrics.put("memory", memoryData);

            // Disk metrics
            Map<String, Object> diskData = new HashMap<>();
            FileSystem fileSystem = os.getFileSystem();
            long totalDisk = 0;
            long usedDisk = 0;

            for (OSFileStore store : fileSystem.getFileStores()) {
                totalDisk += store.getTotalSpace();
                usedDisk += (store.getTotalSpace() - store.getUsableSpace());
            }

            diskData.put("total", totalDisk);
            diskData.put("used", usedDisk);
            diskData.put("usagePercent",
                    totalDisk > 0 ? Math.round((usedDisk * 100.0 / totalDisk) * 100.0) / 100.0 : 0);
            metrics.put("disk", diskData);

        } catch (Exception e) {
            logger.error("Lỗi khi thu thập metrics: {}", e.getMessage());
        }

        return metrics;
    }

    /**
     * Gửi metrics về server qua WebSocket hoặc HTTP
     */
    private void sendMetrics(Map<String, Object> metrics) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            Gson gson = new Gson();

            Map<String, Object> request = new HashMap<>();
            request.put("machineId", machineId);
            request.put("metrics", metrics);

            String json = gson.toJson(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/api/heartbeat"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.warn("Gửi metrics thất bại. Status code: {}", response.statusCode());
            }

        } catch (Exception e) {
            logger.error("Lỗi khi gửi metrics: {}", e.getMessage());
        }
    }

    /**
     * Dừng monitor thread
     */
    public void shutdown() {
        running = false;
        interrupt();
    }
}
