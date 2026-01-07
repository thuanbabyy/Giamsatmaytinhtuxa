# 📊 GIẢI THÍCH DỰ ÁN GIÁM SÁT MÁY TÍNH TỪ XA

## 🎯 TỔNG QUAN

Hệ thống **Client-Server** để giám sát và điều khiển các máy tính trong mạng LAN (phù hợp cho phòng máy tính trường học).

### Thành Phần Chính

**SERVER (Spring Boot)**
- Vai trò: Máy chủ trung tâm, hiển thị giao diện web
- Công nghệ: Spring Boot, WebSocket, SQL Server
- Port: 8080
- Giao diện: `http://IP:8080/server`

**CLIENT (Java Application)**
- Vai trò: Chạy trên các máy học sinh
- Công nghệ: Java 11, OSHI (lấy thông tin hệ thống)
- Dạng: Console application (không có GUI)

---

## 🏗️ KIẾN TRÚC HỆ THỐNG

### Luồng Hoạt Động

```
CLIENT (Máy học sinh)
  ├─ 1. Đăng ký với Server qua REST API
  ├─ 2. Kết nối WebSocket để nhận lệnh
  ├─ 3. Gửi Heartbeat + Metrics mỗi 5 giây
  └─ 4. Thực thi lệnh từ Server

        ▼ REST API + WebSocket ▼

SERVER (Máy giáo viên)
  ├─ 1. Nhận đăng ký máy tính
  ├─ 2. Nhận metrics (CPU, RAM, Processes)
  ├─ 3. Gửi lệnh qua WebSocket
  └─ 4. Hiển thị giao diện web

        ▼ Lưu trữ ▼
        
SQL Server Database
```

### Các Kênh Giao Tiếp

| Kênh | Hướng | Mục đích | Tần suất |
|------|-------|----------|----------|
| REST API `/api/heartbeat` | Client → Server | Gửi metrics | 5s/lần |
| WebSocket `/ws-client` | Server → Client | Gửi lệnh điều khiển | Realtime |
| WebSocket Response | Client → Server | Trả kết quả lệnh | Khi có lệnh |

---

## 📦 CHI TIẾT CLIENT

### Cấu Trúc Code

```
client/src/main/java/com/monitor/client/
├── Main.java                    # Entry point
├── websocket/
│   └── ClientWebSocket.java     # Kết nối WebSocket, nhận lệnh
├── command/
│   └── CommandHandler.java      # Xử lý lệnh (lock, unlock, notify, screen)
├── monitor/
│   └── SystemMonitor.java       # Thu thập CPU, RAM, Disk, Processes
└── heartbeat/
    └── HeartbeatManager.java    # Gửi metrics định kỳ
```

### Các Module Quan Trọng

**1. Main.java**
```java
// Điểm khởi đầu
- Parse tham số: --server.url, --machine.id
- Tạo machineId ổn định (hostname + username)
- Đăng ký với server
- Khởi động WebSocket
- Khởi động HeartbeatManager
```

**2. SystemMonitor.java**
```java
// Thu thập thông tin hệ thống bằng OSHI
collectAllMetrics() {
    ├─ CPU: totalUsage, coreCount, perCoreUsage[]
    ├─ Memory: total, used, usagePercent
    ├─ Disk: total, used, usagePercent
    ├─ Network: bytesRecv, bytesSent
    └─ Top Processes: [{pid, name, cpuUsage, memoryUsage}]
}
```

**3. HeartbeatManager.java**
```java
// Gửi metrics thông minh
- CPU > 80%: gửi mỗi 1 giây (realtime)
- CPU < 10%: gửi mỗi 10 giây (tiết kiệm)
- Bình thường: gửi mỗi 5 giây

POST /api/heartbeat
{
  "machineId": "MACHINE-xxx",
  "metrics": { cpu, memory, disk, network, topProcesses }
}
```

**4. ClientWebSocket.java**
```java
// Nhận lệnh từ server
- Kết nối: ws://SERVER_IP:8080/ws-client
- Auto-reconnect khi mất kết nối
- Nhận JSON command → gọi CommandHandler
- Gửi kết quả về server
```

**5. CommandHandler.java**
```java
// Xử lý các lệnh
NOTIFICATION    → Hiển thị popup Windows
SCREEN_CAPTURE  → Chụp màn hình, upload lên server
LOCK            → Khóa bàn phím + chuột
UNLOCK          → Mở khóa
GET_PROCESSES   → Trả về list processes
```

---

## 📦 CHI TIẾT SERVER

### Cấu Trúc Code

```
server/src/main/java/com/monitor/server/
├── controller/              # REST API endpoints
│   ├── HeartbeatController.java    # POST /api/heartbeat
│   ├── MachineController.java      # GET /api/machines
│   ├── CommandController.java      # POST /api/commands/{id}/lock
│   ├── NotificationController.java # POST /api/notifications/{id}/send
│   └── MetricController.java       # GET /api/machines/{id}/metrics/latest
├── service/                 # Business logic
│   ├── MetricService.java         # Xử lý và lưu metrics
│   ├── MachineService.java        # Quản lý danh sách máy
│   ├── CommandService.java        # Gửi lệnh qua WebSocket
│   └── ScreenService.java         # Lưu ảnh màn hình
├── websocket/
│   └── ClientWebSocketHandler.java # Xử lý WebSocket connection
├── model/                   # Database entities
│   ├── Machine.java
│   ├── Metric.java
│   ├── Command.java
│   └── ScreenData.java
└── repository/              # JPA repositories
```

### API Endpoints Quan Trọng

**1. Heartbeat API**
```http
POST /api/heartbeat
Content-Type: application/json

{
  "machineId": "MACHINE-Lilmon-LILMON",
  "metrics": {
    "cpu": { "totalUsage": 45.2, "coreCount": 8 },
    "memory": { "total": 16GB, "used": 8GB, "usagePercent": 50 },
    "topProcesses": [
      { "pid": 1234, "name": "chrome.exe", "cpuUsage": 15.5, "memoryUsage": 512MB }
    ]
  }
}

→ Server lưu vào DB, cập nhật online status
```

**2. Get Metrics**
```http
GET /api/machines/{machineId}/metrics/latest

Response:
{
  "cpuUsage": 45.2,
  "memoryUsagePercent": 50.0,
  "rawData": "{\"cpu\":{...},\"topProcesses\":[...]}"  ← Chứa processes
}
```

**3. Send Command**
```http
POST /api/commands/{machineId}/lock

→ Server gửi qua WebSocket:
{
  "command": "LOCK",
  "machineId": "...",
  "commandId": 123
}
```

### Service Layer

**MetricService.java**
```java
saveMetrics(machineId, metricsData) {
    // Parse JSON
    Metric metric = new Metric();
    metric.setCpuUsage(metricsData.cpu.totalUsage);
    metric.setMemoryUsagePercent(...);
    
    // Lưu RAW DATA (chứa topProcesses)
    metric.setRawData(gson.toJson(metricsData));
    
    // Lưu DB
    metricRepository.save(metric);
}
```

**CommandService.java**
```java
sendCommand(machineId, command, data) {
    // Lấy WebSocket session của client
    WebSocketSession session = sessions.get(machineId);
    
    // Gửi JSON qua WebSocket
    session.sendMessage(new TextMessage(jsonCommand));
    
    // Lưu command vào DB với status SENT
}
```

**ClientWebSocketHandler.java**
```java
handleTextMessage(session, message) {
    // Parse JSON từ client
    Map data = gson.fromJson(message);
    
    if (data.type == "client") {
        // Client đăng ký
        registerClient(machineId, session);
    }
    
    if (data.containsKey("result")) {
        // Nhận kết quả thực thi lệnh
        updateCommandStatus(commandId, "COMPLETED");
    }
}
```

---

## 🗄️ DATABASE

### Bảng Quan Trọng

**machines** - Thông tin máy tính
```sql
machine_id         VARCHAR(100) PK  -- "MACHINE-Lilmon-LILMON"
name               VARCHAR(255)     -- "Máy 01"
ip_address         VARCHAR(50)      -- "192.168.0.107"
is_online          BIT              -- true/false
last_response_time DATETIME         -- Heartbeat cuối
registered_at      DATETIME
```

**metrics** - Lưu metrics
```sql
id                    BIGINT PK AUTO
machine_id            VARCHAR(100)
timestamp             DATETIME
cpu_usage             DOUBLE       -- %
memory_usage_percent  DOUBLE       -- %
raw_data              TEXT         -- JSON gốc (chứa topProcesses)
```

**commands** - Lịch sử lệnh
```sql
id             BIGINT PK AUTO
machine_id     VARCHAR(100)
command_type   VARCHAR(50)      -- LOCK, UNLOCK, SCREEN_CAPTURE
status         VARCHAR(20)      -- PENDING, SENT, COMPLETED, FAILED
created_at     DATETIME
response_data  TEXT             -- Kết quả từ client
```

**screen_data** - Ảnh màn hình
```sql
id           BIGINT PK AUTO
machine_id   VARCHAR(100)
image_data   VARBINARY(MAX)   -- Binary image
image_format VARCHAR(10)      -- PNG, JPEG
captured_at  DATETIME
```

---

## 🎨 FRONTEND

**server.html** - Single Page Application
```javascript
// Giao diện đơn giản
- Danh sách máy: Online (xanh) / Offline (đỏ)
- Metrics: CPU %, RAM %
- Buttons: 📢 Thông báo, 🖼️ Màn hình, 🔒 Khóa, 📊 Processes

// Lấy metrics
fetch(`/api/machines/${machineId}/metrics/latest`)
  .then(res => res.json())
  .then(metric => {
      // Hiển thị CPU, RAM
      cpuElement.textContent = metric.cpuUsage + '%';
      
      // Parse processes từ rawData
      const data = JSON.parse(metric.rawData);
      displayProcesses(data.topProcesses);
  });

// Hiển thị processes
displayProcesses(processes) {
    processes.forEach(proc => {
        // Tạo table row
        <tr>
          <td>{proc.pid}</td>
          <td>{proc.name}</td>
          <td>{proc.cpuUsage}%</td>
          <td>{proc.memoryUsage / 1024 / 1024} MB</td>
        </tr>
    });
}
```

---

## ⚠️ CÁC VẤN ĐỀ THƯỜNG GẶP

### 🔴 1. Lỗi 401 - Authentication Failed

**Nguyên nhân:**
```java
// Server code CŨ có xác thực HMAC
if (!authenticated) {
    return ResponseEntity.status(401).body("Xác thực thất bại");
}
```

**Giải pháp:**
```java
// Đã TẮT xác thực trong HeartbeatController
// Line 89-93: Comment out authentication check
logger.info("Nhận heartbeat từ machine: {} (bỏ qua xác thực)", machineId);
```

**Cách fix:**
1. Kill tất cả Java processes: `Get-Process java | Stop-Process -Force`
2. Build server: `cd server && mvn clean package`
3. Chạy server: `mvn spring-boot:run`
4. Chạy client: `cd client && java -jar target/client-1.0.jar --server.url=http://IP:8080`

### 🔴 2. Metrics Không Hiển Thị

**Kiểm tra theo thứ tự:**

1. **Client có gửi không?**
```powershell
# Xem log client
# Phải thấy: "Đã gửi heartbeat thành công"
```

2. **Server có nhận không?**
```powershell
# Xem log server
# Phải thấy: "Nhận heartbeat đơn giản từ machine: MACHINE-xxx"
# Phải thấy: "Đã lưu metrics cho machine: MACHINE-xxx"
```

3. **Database có lưu không?**
```http
GET http://192.168.0.107:8080/api/machines/MACHINE-Lilmon-LILMON/metrics/latest

# Phải trả về JSON với cpuUsage, memoryUsagePercent
```

4. **Frontend có parse đúng không?**
```javascript
// F12 Console → kiểm tra lỗi JS
// Hard refresh: Ctrl+Shift+R
```

### 🔴 3. Processes Không Hiển Thị

**Luồng đúng:**
```
Client SystemMonitor.collectAllMetrics()
  ├─ collectTopProcesses(10)
  └─ Tạo JSON: { cpu, memory, topProcesses: [...] }
        ▼
HeartbeatManager.sendHeartbeat()
  └─ POST /api/heartbeat với metrics
        ▼
Server HeartbeatController.receiveHeartbeat()
  └─ MetricService.saveMetrics()
      └─ metric.setRawData(gson.toJson(metrics))  ← Lưu topProcesses ở đây
            ▼
Frontend fetch /api/metrics/latest
  └─ const data = JSON.parse(metric.rawData)
      └─ displayProcesses(data.topProcesses)
```

**Debug:**
```http
# Gọi trực tiếp API
GET http://192.168.0.107:8080/api/machines/MACHINE-Lilmon-LILMON/metrics/latest

# Response phải có:
{
  "rawData": "{\"cpu\":{...},\"topProcesses\":[{\"pid\":1234,...}]}"
}

# Nếu có topProcesses → Frontend parse sai
# Nếu không có → Client không gửi hoặc server không lưu
```

### 🔴 4. Duplicate Machines

**Vấn đề:** Mỗi lần chạy client tạo machineId khác nhau

**Nguyên nhân:**
```java
// Code CŨ dùng random
String machineId = "MACHINE-" + UUID.randomUUID();
```

**Đã fix:**
```java
// Code MỚI dùng hostname + username
private static String generateStableMachineId() {
    String hostname = InetAddress.getLocalHost().getHostName();
    String username = System.getProperty("user.name");
    return "MACHINE-" + hostname + "-" + username;
}
// → Luôn giống nhau trên cùng một máy
```

---

## 🎯 CÁC PHẦN QUAN TRỌNG NHẤT

### Top 5 Code Cần Hiểu

| # | File | Vai trò | Độ quan trọng |
|---|------|---------|---------------|
| 1 | `SystemMonitor.java` | Thu thập metrics | ⭐⭐⭐⭐⭐ |
| 2 | `HeartbeatManager.java` | Gửi metrics định kỳ | ⭐⭐⭐⭐⭐ |
| 3 | `HeartbeatController.java` | Nhận metrics | ⭐⭐⭐⭐⭐ |
| 4 | `MetricService.java` | Lưu metrics | ⭐⭐⭐⭐ |
| 5 | `ClientWebSocketHandler.java` | WebSocket server | ⭐⭐⭐⭐ |

### Luồng Xử Lý Metrics (QUAN TRỌNG NHẤT)

```
[CLIENT]
SystemMonitor.collectAllMetrics()
  ├─ Dùng OSHI để lấy CPU, RAM, Processes
  └─ Return Map<String, Object>
        │
        ▼
HeartbeatManager.sendHeartbeat()
  ├─ Tạo JSON: { machineId, metrics, timestamp }
  ├─ POST /api/heartbeat
  └─ Gửi mỗi 5 giây (hoặc 1s nếu CPU cao)
        │
        ▼
[SERVER]
HeartbeatController.receiveHeartbeat()
  ├─ Parse JSON request
  ├─ Gọi metricService.saveMetrics()
  └─ Gọi machineService.updateOnlineStatus()
        │
        ▼
MetricService.saveMetrics()
  ├─ Parse metrics.cpu.totalUsage → metric.setCpuUsage()
  ├─ Parse metrics.memory → metric.setMemoryUsagePercent()
  ├─ Lưu TOÀN BỘ JSON vào metric.setRawData()  ← QUAN TRỌNG!
  └─ metricRepository.save(metric)
        │
        ▼
[FRONTEND]
fetch('/api/machines/{id}/metrics/latest')
  ├─ Lấy metric.rawData (JSON string)
  ├─ const data = JSON.parse(metric.rawData)
  ├─ Lấy data.topProcesses
  └─ Render table
```

---

## 📌 TÓM TẮT

### Điểm Mạnh
✅ Kiến trúc rõ ràng  
✅ Real-time (WebSocket)  
✅ Smart heartbeat (CPU-based)  
✅ Comprehensive metrics  
✅ Auto-reconnect  

### Điểm Yếu
⚠️ Không authentication  
⚠️ Không encryption  
⚠️ Không scalable  
⚠️ Frontend đơn giản  

### Checklist Khi Debug

```
□ Server đã chạy code MỚI NHẤT chưa? (mvn spring-boot:run)
□ Client đã build JAR MỚI chưa? (mvn clean package)
□ Database có kết nối không? (kiểm tra application.yml)
□ Firewall có block port 8080 không?
□ Client log có lỗi gì không?
□ Server log có "Nhận heartbeat" không?
□ API trả về metrics đúng không? (test bằng curl/Postman)
□ Frontend có lỗi JS không? (F12 Console)
```

---

**Tác giả:** AI Assistant  
**Ngày tạo:** 2026-01-06  
**Phiên bản:** 1.0
