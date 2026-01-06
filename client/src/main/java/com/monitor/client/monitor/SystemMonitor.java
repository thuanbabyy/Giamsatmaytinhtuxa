package com.monitor.client.monitor;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OSProcess;
import oshi.util.FormatUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lớp thu thập thông tin hệ thống sử dụng OSHI
 * Thu thập: CPU, RAM, Disk, Network, và Top processes
 */
public class SystemMonitor {

    private SystemInfo systemInfo;
    private HardwareAbstractionLayer hal;
    private OperatingSystem os;
    private CentralProcessor processor;
    private GlobalMemory memory;

    // Lưu trữ giá trị CPU trước đó để tính toán usage
    private long[] prevTicks;
    private long[][] prevProcTicks;

    public SystemMonitor() {
        this.systemInfo = new SystemInfo();
        this.hal = systemInfo.getHardware();
        this.os = systemInfo.getOperatingSystem();
        this.processor = hal.getProcessor();
        this.memory = hal.getMemory();
        this.prevTicks = processor.getSystemCpuLoadTicks();
        this.prevProcTicks = processor.getProcessorCpuLoadTicks();
    }

    /**
     * Thu thập tất cả thông tin hệ thống
     * 
     * @return Map chứa tất cả metrics
     */
    public Map<String, Object> collectAllMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // CPU Usage
        metrics.put("cpu", collectCpuMetrics());

        // RAM Usage
        metrics.put("memory", collectMemoryMetrics());

        // Disk Usage
        metrics.put("disk", collectDiskMetrics());

        // Network I/O
        metrics.put("network", collectNetworkMetrics());

        // Top 5 processes
        metrics.put("topProcesses", collectTopProcesses(10));

        // Timestamp
        metrics.put("timestamp", System.currentTimeMillis());

        return metrics;
    }

    /**
     * Thu thập thông tin CPU
     * 
     * @return Map chứa CPU usage tổng và theo từng core
     */
    public Map<String, Object> collectCpuMetrics() {
        Map<String, Object> cpuData = new HashMap<>();

        try {
            // CPU Usage tổng (tính theo %)
            // OSHI cần 2 lần đo cách nhau ít nhất 500ms
            long[] firstTicks = processor.getSystemCpuLoadTicks();
            Thread.sleep(1000); // Đợi 1 giây
            long[] secondTicks = processor.getSystemCpuLoadTicks();
            double cpuUsage = processor.getSystemCpuLoadBetweenTicks(firstTicks) * 100;
            prevTicks = secondTicks;

            cpuData.put("totalUsage", Math.round(cpuUsage * 100.0) / 100.0);

            // CPU Usage theo từng core
            long[][] firstProcTicks = processor.getProcessorCpuLoadTicks();
            double[] loadPerCore = processor.getProcessorCpuLoadBetweenTicks(prevProcTicks);
            prevProcTicks = firstProcTicks;
            List<Double> coreUsages = new ArrayList<>();
            for (double load : loadPerCore) {
                coreUsages.add(Math.round(load * 10000.0) / 100.0);
            }
            cpuData.put("coreUsages", coreUsages);
            cpuData.put("coreCount", processor.getLogicalProcessorCount());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cpuData.put("totalUsage", 0.0);
            cpuData.put("coreCount", processor.getLogicalProcessorCount());
        }

        return cpuData;
    }

    /**
     * Thu thập thông tin RAM
     * 
     * @return Map chứa RAM usage
     */
    public Map<String, Object> collectMemoryMetrics() {
        Map<String, Object> memoryData = new HashMap<>();

        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        long usedMemory = totalMemory - availableMemory;

        double usagePercent = (double) usedMemory / totalMemory * 100;

        memoryData.put("total", totalMemory);
        memoryData.put("used", usedMemory);
        memoryData.put("available", availableMemory);
        memoryData.put("usagePercent", Math.round(usagePercent * 100.0) / 100.0);
        memoryData.put("totalFormatted", FormatUtil.formatBytes(totalMemory));
        memoryData.put("usedFormatted", FormatUtil.formatBytes(usedMemory));
        memoryData.put("availableFormatted", FormatUtil.formatBytes(availableMemory));

        return memoryData;
    }

    /**
     * Thu thập thông tin Disk
     * 
     * @return Map chứa Disk usage
     */
    public Map<String, Object> collectDiskMetrics() {
        Map<String, Object> diskData = new HashMap<>();

        List<Map<String, Object>> disks = new ArrayList<>();

        // Lấy danh sách file systems
        oshi.software.os.FileSystem fileSystem = os.getFileSystem();
        List<oshi.software.os.OSFileStore> fileStores = fileSystem.getFileStores();

        long totalSpace = 0;
        long usedSpace = 0;
        long freeSpace = 0;

        for (oshi.software.os.OSFileStore fs : fileStores) {
            long total = fs.getTotalSpace();
            long free = fs.getFreeSpace();
            long used = total - free;

            totalSpace += total;
            usedSpace += used;
            freeSpace += free;

            Map<String, Object> disk = new HashMap<>();
            disk.put("name", fs.getName());
            disk.put("mount", fs.getMount());
            disk.put("total", total);
            disk.put("used", used);
            disk.put("free", free);
            disk.put("usagePercent", Math.round((double) used / total * 10000.0) / 100.0);
            disk.put("type", fs.getType());

            disks.add(disk);
        }

        diskData.put("disks", disks);
        diskData.put("total", totalSpace);
        diskData.put("used", usedSpace);
        diskData.put("free", freeSpace);
        diskData.put("totalFormatted", FormatUtil.formatBytes(totalSpace));
        diskData.put("usedFormatted", FormatUtil.formatBytes(usedSpace));
        diskData.put("freeFormatted", FormatUtil.formatBytes(freeSpace));

        return diskData;
    }

    /**
     * Thu thập thông tin Network I/O
     * 
     * @return Map chứa Network metrics
     */
    public Map<String, Object> collectNetworkMetrics() {
        Map<String, Object> networkData = new HashMap<>();

        List<oshi.hardware.NetworkIF> networkIFs = hal.getNetworkIFs();

        long totalBytesRecv = 0;
        long totalBytesSent = 0;
        long totalPacketsRecv = 0;
        long totalPacketsSent = 0;

        List<Map<String, Object>> interfaces = new ArrayList<>();

        for (oshi.hardware.NetworkIF netIF : networkIFs) {
            netIF.updateAttributes();

            long bytesRecv = netIF.getBytesRecv();
            long bytesSent = netIF.getBytesSent();
            long packetsRecv = netIF.getPacketsRecv();
            long packetsSent = netIF.getPacketsSent();

            totalBytesRecv += bytesRecv;
            totalBytesSent += bytesSent;
            totalPacketsRecv += packetsRecv;
            totalPacketsSent += packetsSent;

            Map<String, Object> iface = new HashMap<>();
            iface.put("name", netIF.getName());
            iface.put("displayName", netIF.getDisplayName());
            iface.put("bytesRecv", bytesRecv);
            iface.put("bytesSent", bytesSent);
            iface.put("packetsRecv", packetsRecv);
            iface.put("packetsSent", packetsSent);
            iface.put("bytesRecvFormatted", FormatUtil.formatBytes(bytesRecv));
            iface.put("bytesSentFormatted", FormatUtil.formatBytes(bytesSent));

            interfaces.add(iface);
        }

        networkData.put("interfaces", interfaces);
        networkData.put("totalBytesRecv", totalBytesRecv);
        networkData.put("totalBytesSent", totalBytesSent);
        networkData.put("totalPacketsRecv", totalPacketsRecv);
        networkData.put("totalPacketsSent", totalPacketsSent);
        networkData.put("totalBytesRecvFormatted", FormatUtil.formatBytes(totalBytesRecv));
        networkData.put("totalBytesSentFormatted", FormatUtil.formatBytes(totalBytesSent));

        return networkData;
    }

    /**
     * Thu thập Top N processes sử dụng CPU nhiều nhất
     * 
     * @param topN Số lượng process cần lấy
     * @return List các process
     */
    public List<Map<String, Object>> collectTopProcesses(int topN) {
        List<OSProcess> processes = os.getProcesses(
                null,
                null,
                0);

        // Sắp xếp theo CPU usage giảm dần
        List<OSProcess> sortedProcesses = processes.stream()
                .sorted((p1, p2) -> Double.compare(
                        p2.getProcessCpuLoadBetweenTicks(p1),
                        p1.getProcessCpuLoadBetweenTicks(p2)))
                .limit(topN)
                .collect(Collectors.toList());

        List<Map<String, Object>> topProcesses = new ArrayList<>();

        for (OSProcess process : sortedProcesses) {
            Map<String, Object> proc = new HashMap<>();
            proc.put("pid", process.getProcessID());
            proc.put("name", process.getName());
            proc.put("cpuUsage", Math.round(process.getProcessCpuLoadCumulative() * 10000.0) / 100.0);
            proc.put("memoryUsage", process.getResidentSetSize());
            proc.put("memoryUsageFormatted", FormatUtil.formatBytes(process.getResidentSetSize()));
            proc.put("state", process.getState().toString());

            topProcesses.add(proc);
        }

        return topProcesses;
    }

    /**
     * Lấy CPU usage hiện tại (để quyết định interval heartbeat)
     * 
     * @return CPU usage percentage
     */
    public double getCurrentCpuUsage() {
        long[] currentTicks = processor.getSystemCpuLoadTicks();
        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        prevTicks = currentTicks;
        return cpuUsage;
    }
}
