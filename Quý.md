# 📚 HƯỚNG DẪN ÔN TẬP - NGƯỜI 5
## Client Agent + Các Chức Năng

---

## 🎯 Phần Phụ Trách
Bạn phụ trách **Client Agent** - ứng dụng Java chạy trên Windows.

---

## 1. Cấu Trúc Client

```
client/src/main/java/com/monitor/client/
├── Main.java              # Entry point
├── command/
│   └── CommandHandler.java   # Xử lý lệnh
├── websocket/
│   └── ClientWebSocket.java  # Kết nối WebSocket
├── heartbeat/
│   └── HeartbeatManager.java # Giữ kết nối
└── monitor/
    └── SystemMonitor.java    # Thông tin hệ thống
```

---

## 2. Main.java - Entry Point

```java
public class Main {
    public static void main(String[] args) {
        Main app = new Main();
        app.start(args);
    }
    
    public void start(String[] args) {
        // 1. Lấy cấu hình
        String serverUrl = getArgValue(args, "--server.url");
        String machineId = getArgValue(args, "--machine.id");
        
        // 2. Tạo Machine ID nếu chưa có
        if (machineId == null) {
            machineId = generateStableMachineId();
        }
        
        // 3. Đăng ký với server qua REST API
        registerWithServer(machineId, ...);
        
        // 4. Kết nối WebSocket
        ClientWebSocket ws = new ClientWebSocket();
        ws.connect(serverUrl, machineId);
        
        // 5. Bắt đầu heartbeat
        HeartbeatManager heartbeat = new HeartbeatManager();
        heartbeat.start(serverUrl, machineId);
    }
}
```

**Cách chạy:**
```bash
java -jar client-1.0.jar --server.url=http://192.168.1.100:8080
```

---

## 3. CommandHandler - Xử Lý Lệnh

```java
public class CommandHandler {
    
    public Object handleCommand(String commandType, Map<String, Object> data) {
        switch (commandType) {
            case "NOTIFICATION":
                return showNotification(data);
            case "LOCK":
                return lockInputDevices();
            case "UNLOCK":
                return unlockInputDevices();
            case "SCREEN_CAPTURE":
                return captureScreen();
            default:
                return Map.of("success", false, "error", "Unknown command");
        }
    }
}
```

---

## 4. Các Chức Năng Client

### 4.1. Hiển thị Popup Thông Báo
```java
private Map<String, Object> showNotification(Map<String, Object> data) {
    String title = (String) data.get("title");
    String message = (String) data.get("message");
    String type = (String) data.get("type");
    
    // Dùng JOptionPane để hiển thị popup
    int messageType = switch (type) {
        case "WARNING" -> JOptionPane.WARNING_MESSAGE;
        case "ERROR" -> JOptionPane.ERROR_MESSAGE;
        default -> JOptionPane.INFORMATION_MESSAGE;
    };
    
    JOptionPane.showMessageDialog(null, message, title, messageType);
    
    return Map.of("success", true, "message", "Đã hiển thị thông báo");
}
```

### 4.2. Chụp Màn Hình
```java
private Map<String, Object> captureScreen() {
    Robot robot = new Robot();
    Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    BufferedImage image = robot.createScreenCapture(screenRect);
    
    // Convert to Base64
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "PNG", baos);
    String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
    
    return Map.of(
        "success", true,
        "imageData", base64Image,
        "imageFormat", "PNG"
    );
}
```

### 4.3. Khóa/Mở Khóa Bàn Phím Chuột
```java
// Sử dụng JNI và Windows API để block input
// Hoặc dùng thư viện JNativeHook

private Map<String, Object> lockInputDevices() {
    // Block keyboard và mouse
    inputBlocked = true;
    showLockPopup("Máy tính đã bị khóa bởi giáo viên");
    return Map.of("success", true);
}

private Map<String, Object> unlockInputDevices() {
    inputBlocked = false;
    hideLockPopup();
    return Map.of("success", true, "message", "Đã mở khóa");
}
```

---

## 5. SystemMonitor - Lấy Thông Tin Hệ Thống

```java
public class SystemMonitor {
    private SystemInfo systemInfo = new SystemInfo();  // OSHI library
    
    public Map<String, Object> getSystemInfo() {
        return Map.of(
            "hostname", getHostname(),
            "ipAddress", getLocalIpAddress(),
            "osName", System.getProperty("os.name"),
            "osVersion", System.getProperty("os.version"),
            "cpuUsage", getCpuUsage(),
            "memoryUsage", getMemoryUsage()
        );
    }
    
    private String getLocalIpAddress() {
        InetAddress localhost = InetAddress.getLocalHost();
        return localhost.getHostAddress();
    }
}
```

---

## 6. Flow Hoạt Động

```
1. Client khởi động (java -jar client.jar)
       ↓
2. Lấy thông tin hệ thống (hostname, IP, OS)
       ↓
3. Đăng ký với server qua REST API
       ↓
4. Kết nối WebSocket (duy trì kết nối)
       ↓
5. Bắt đầu heartbeat (mỗi 30 giây)
       ↓
6. Chờ nhận lệnh từ server qua WebSocket
       ↓
7. Khi nhận lệnh → CommandHandler xử lý
       ↓
8. Gửi kết quả về server qua WebSocket
```

---

## 📝 Câu Hỏi Ôn Tập

1. Client có những chức năng gì?
2. Giải thích flow hoạt động của client từ khi khởi động.
3. Làm sao để chụp màn hình trong Java?
4. Tại sao cần heartbeat?
5. CommandHandler xử lý những lệnh nào?

---

## 📁 Files Cần Đọc
- `client/src/main/java/com/monitor/client/Main.java`
- `client/src/main/java/com/monitor/client/command/CommandHandler.java`
- `client/src/main/java/com/monitor/client/monitor/SystemMonitor.java`
