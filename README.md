# Hệ Thống Giám Sát Máy Tính Từ Xa

## 📋 Mô Tả

Hệ thống giám sát máy tính từ xa trong phòng máy với mức độ nâng cao, sử dụng Java và Spring Boot. Hệ thống cho phép giám sát real-time các máy tính trong phòng máy, thu thập thông tin hệ thống (CPU, RAM, Disk, Network), phát hiện cảnh báo tự động, và điều khiển từ xa.

## 🏗️ Kiến Trúc Hệ Thống

```
┌─────────────────────────────────────────────────────────────┐
│                        DASHBOARD (Web UI)                    │
│  - Hiển thị danh sách máy tính                              │
│  - Xem metrics real-time                                    │
│  - Quản lý cảnh báo                                         │
│  - Gửi lệnh điều khiển                                      │
└────────────────────┬────────────────────────────────────────┘
                     │ REST API / WebSocket
                     │
┌────────────────────▼────────────────────────────────────────┐
│                    SERVER (Spring Boot)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  REST API    │  │  WebSocket   │  │  Analysis    │      │
│  │  Controllers │  │  Handlers    │  │  Service     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Metric      │  │  Alert       │  │  Command     │      │
│  │  Service     │  │  Service     │  │  Service     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐                        │
│  │  Security    │  │  Repository  │                        │
│  │  (HMAC/JWT)  │  │  (JPA)       │                        │
│  └──────────────┘  └──────────────┘                        │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ Database (H2/MySQL)
                     │
┌────────────────────▼────────────────────────────────────────┐
│                    CLIENT (Java Application)                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  System      │  │  Heartbeat   │  │  Command     │      │
│  │  Monitor     │  │  Manager     │  │  Handler     │      │
│  │  (OSHI)      │  │  (Smart)     │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐                                          │
│  │  WebSocket   │                                          │
│  │  Client      │                                          │
│  └──────────────┘                                          │
└────────────────────────────────────────────────────────────┘
```

### Luồng Dữ Liệu:

1. **Client → Server (Heartbeat)**:
   - Client thu thập metrics bằng OSHI
   - Gửi heartbeat qua REST API với HMAC signature
   - Server xác thực và lưu vào database

2. **Server → Client (Commands)**:
   - Admin gửi lệnh qua Dashboard
   - Server gửi lệnh đến client qua WebSocket
   - Client thực thi và gửi kết quả về

3. **Server → Dashboard (Real-time Updates)**:
   - Dashboard lấy dữ liệu qua REST API
   - Cập nhật tự động mỗi 5 giây

## 📁 Cấu Trúc Dự Án Chi Tiết

```
Giamsatmaytinhtuxa/
├── client/                           # Client Java Application
│   ├── src/main/java/com/monitor/client/
│   │   ├── Main.java                 # Entry point
│   │   ├── monitor/
│   │   │   └── SystemMonitor.java   # Thu thập thông tin hệ thống (OSHI)
│   │   ├── heartbeat/
│   │   │   └── HeartbeatManager.java # Cơ chế heartbeat thông minh
│   │   ├── command/
│   │   │   └── CommandHandler.java   # Xử lý lệnh từ server
│   │   └── websocket/
│   │       └── ClientWebSocket.java  # WebSocket client
│   └── pom.xml
│
├── server/                           # Server Spring Boot
│   ├── src/main/java/com/monitor/server/
│   │   ├── MonitorServerApplication.java  # Main class
│   │   ├── controller/               # REST API Controllers
│   │   │   ├── HeartbeatController.java
│   │   │   ├── MachineController.java
│   │   │   ├── MetricController.java
│   │   │   ├── AlertController.java
│   │   │   └── CommandController.java
│   │   ├── service/                  # Business Logic
│   │   │   ├── MachineService.java
│   │   │   ├── MetricService.java
│   │   │   ├── AlertService.java
│   │   │   ├── CommandService.java
│   │   │   └── AnalysisService.java  # Phân tích và cảnh báo tự động
│   │   ├── security/                 # Xác thực
│   │   │   └── AuthenticationService.java  # HMAC authentication
│   │   ├── websocket/                 # WebSocket Handlers
│   │   │   └── ClientWebSocketHandler.java
│   │   ├── config/                    # Configuration
│   │   │   ├── WebSocketConfig.java
│   │   │   └── WebSocketHandlerConfig.java
│   │   ├── model/                     # Database Entities
│   │   │   ├── Machine.java
│   │   │   ├── Metric.java
│   │   │   ├── Alert.java
│   │   │   └── BannedProcess.java
│   │   └── repository/               # Data Access Layer
│   │       ├── MachineRepository.java
│   │       ├── MetricRepository.java
│   │       ├── AlertRepository.java
│   │       └── BannedProcessRepository.java
│   └── src/main/resources/
│       ├── application.properties     # Cấu hình server
│       └── static/
│           └── dashboard.html         # Dashboard web UI
│
└── README.md
```

## 🚀 Hướng Dẫn Chạy Chi Tiết

### Yêu Cầu Hệ Thống:
- **Java**: JDK 11 hoặc cao hơn
- **Maven**: 3.6+ 
- **Database**: H2 Database (tự động tải, không cần cài đặt)
- **OS**: Windows, Linux, hoặc macOS

### Bước 1: Clone và Build Project

```bash
# Di chuyển vào thư mục project
cd Giamsatmaytinhtuxa
```

### Bước 2: Chạy Server

```bash
cd server
mvn clean install
mvn spring-boot:run
```

Server sẽ khởi động tại: `http://localhost:8080`

**Kiểm tra server đã chạy:**
- Mở trình duyệt: `http://localhost:8080/api/machines`
- Nếu thấy `[]` (mảng rỗng) là server đã chạy thành công

**H2 Console** (để xem database):
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/monitordb`
- Username: `sa`
- Password: (để trống)

**Lưu ý**: Dữ liệu được lưu vào file `./data/monitordb.mv.db` trong thư mục server, không mất khi restart.

### Bước 3: Chạy Client

Mở terminal mới:

```bash
cd client
mvn clean package
java -jar target/client-1.0.jar
```

**Hoặc chạy với cấu hình tùy chỉnh:**

```bash
# Windows (PowerShell)
$env:MACHINE_ID="MACHINE-001"
$env:SECRET_KEY="my-secret-key-123"
$env:SERVER_URL="http://localhost:8080"
java -jar target/client-1.0.jar

# Linux/Mac
export MACHINE_ID="MACHINE-001"
export SECRET_KEY="my-secret-key-123"
export SERVER_URL="http://localhost:8080"
java -jar target/client-1.0.jar
```

**Lưu ý**: 
- `MACHINE_ID`: ID duy nhất của máy tính (mặc định: `MACHINE-{username}`)
- `SECRET_KEY`: Khóa bí mật để xác thực (mặc định: `default-secret-key-change-me`)
- `SERVER_URL`: URL của server (mặc định: `http://localhost:8080`)

### Bước 4: Truy Cập Dashboard

Mở trình duyệt và truy cập: `http://localhost:8080/dashboard.html`

Dashboard sẽ hiển thị:
- Danh sách máy tính đang online/offline
- Metrics real-time (CPU, RAM, Disk)
- Cảnh báo tự động
- Panel điều khiển để gửi lệnh

## 🔐 Xác Thực và Bảo Mật

### Cơ Chế Xác Thực:

1. **HMAC Signature**: Mỗi heartbeat từ client được ký bằng HMAC-SHA256
2. **Machine ID**: Mỗi client có ID duy nhất
3. **Secret Key**: Khóa bí mật được lưu trong database

### Cách Hoạt Động:

```
Client:
1. Thu thập metrics
2. Tạo payload JSON
3. Tính HMAC signature: HMAC-SHA256(payload, secretKey)
4. Gửi {payload, signature} đến server

Server:
1. Nhận request
2. Lấy secretKey từ database theo machineId
3. Tính lại HMAC signature
4. So sánh với signature từ client
5. Nếu khớp → xác thực thành công
```

### Cấu Hình Secret Key:

**Lần đầu kết nối**: Server tự động tạo machine mới với secretKey mặc định.

**Thay đổi secretKey** (qua database hoặc API):
```sql
-- H2 Console
UPDATE machines SET secret_key = 'new-secret-key' WHERE machine_id = 'MACHINE-001';
```

## 📊 API Endpoints Chi Tiết

### 1. Heartbeat API

**POST** `/api/heartbeat`

Nhận heartbeat từ client.

**Request Body:**
```json
{
  "payload": {
    "machineId": "MACHINE-001",
    "metrics": {
      "cpu": {
        "totalUsage": 45.5,
        "coreUsages": [45.2, 46.1, 44.8, 45.9],
        "coreCount": 4
      },
      "memory": {
        "total": 8589934592,
        "used": 5153960755,
        "available": 3435973837,
        "usagePercent": 60.0,
        "totalFormatted": "8.0 GiB",
        "usedFormatted": "4.8 GiB",
        "availableFormatted": "3.2 GiB"
      },
      "disk": {
        "total": 500107862016,
        "used": 250053931008,
        "free": 250053931008,
        "totalFormatted": "465.8 GiB",
        "usedFormatted": "233.0 GiB",
        "freeFormatted": "233.0 GiB",
        "disks": [...]
      },
      "network": {
        "totalBytesRecv": 1024000,
        "totalBytesSent": 512000,
        "totalBytesRecvFormatted": "1000.0 KiB",
        "totalBytesSentFormatted": "500.0 KiB",
        "interfaces": [...]
      },
      "topProcesses": [
        {
          "pid": 1234,
          "name": "chrome.exe",
          "cpuUsage": 15.5,
          "memoryUsage": 524288000,
          "memoryUsageFormatted": "500.0 MiB",
          "state": "RUNNING"
        }
      ],
      "timestamp": 1704067200000
    },
    "timestamp": 1704067200000
  },
  "signature": "HMAC_SHA256_SIGNATURE_BASE64"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đã nhận heartbeat",
  "timestamp": 1704067201000
}
```

### 2. Machine API

**GET** `/api/machines`

Lấy danh sách tất cả máy tính.

**Response:**
```json
[
  {
    "machineId": "MACHINE-001",
    "name": "Máy tính 1",
    "description": null,
    "ipAddress": "192.168.1.100",
    "osName": "Windows 10",
    "osVersion": "10.0",
    "isOnline": true,
    "lastHeartbeat": "2024-01-01T10:00:00",
    "createdAt": "2024-01-01T09:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
]
```

**GET** `/api/machines/{id}`

Lấy thông tin một máy tính cụ thể.

### 3. Metric API

**GET** `/api/machines/{id}/metrics?limit=100`

Lấy metrics của một máy tính.

**Query Parameters:**
- `limit`: Số lượng metrics cần lấy (mặc định: 100)

**Response:**
```json
[
  {
    "id": 1,
    "machineId": "MACHINE-001",
    "timestamp": "2024-01-01T10:00:00",
    "cpuUsage": 45.5,
    "coreCount": 4,
    "memoryTotal": 8589934592,
    "memoryUsed": 5153960755,
    "memoryUsagePercent": 60.0,
    "diskTotal": 500107862016,
    "diskUsed": 250053931008,
    "diskUsagePercent": 50.0,
    "networkBytesRecv": 1024000,
    "networkBytesSent": 512000
  }
]
```

**GET** `/api/machines/{id}/metrics/latest`

Lấy metric mới nhất của một máy tính.

### 4. Alert API

**GET** `/api/alerts`

Lấy tất cả cảnh báo chưa được giải quyết.

**Response:**
```json
[
  {
    "id": 1,
    "machineId": "MACHINE-001",
    "alertType": "CPU_HIGH",
    "message": "CPU usage cao: 95.5% (ngưỡng: 90.0%)",
    "severity": "WARNING",
    "timestamp": "2024-01-01T10:00:00",
    "resolved": false,
    "resolvedAt": null
  }
]
```

**GET** `/api/machines/{id}/alerts`

Lấy cảnh báo của một máy tính.

**POST** `/api/alerts/{id}/resolve`

Đánh dấu cảnh báo đã được giải quyết.

### 5. Command API

**POST** `/api/machines/{id}/commands`

Gửi lệnh điều khiển đến máy tính.

**Request Body:**
```json
{
  "command": "KILL_PROCESS",
  "parameter": "1234"
}
```

Hoặc:
```json
{
  "command": "SHUTDOWN"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đã gửi lệnh thành công"
}
```

## 🎯 Các Lệnh Điều Khiển

### 1. KILL_PROCESS
Dừng một process theo PID.

**Cú pháp:** `KILL_PROCESS <pid>`

**Ví dụ:**
```json
{
  "command": "KILL_PROCESS",
  "parameter": "1234"
}
```

### 2. SHUTDOWN
Tắt máy tính (sau 30 giây trên Windows, 1 phút trên Linux/Mac).

**Cú pháp:** `SHUTDOWN`

**Ví dụ:**
```json
{
  "command": "SHUTDOWN"
}
```

### 3. LOCK_KEYBOARD
Khóa bàn phím/màn hình (Windows: Win+L).

**Cú pháp:** `LOCK_KEYBOARD`

**Ví dụ:**
```json
{
  "command": "LOCK_KEYBOARD"
}
```

### 4. SCREENSHOT
Chụp màn hình và lưu vào thư mục home của user.

**Cú pháp:** `SCREENSHOT`

**Ví dụ:**
```json
{
  "command": "SCREENSHOT"
}
```

## 🔔 Cảnh Báo Tự Động

Hệ thống tự động tạo cảnh báo khi:

### 1. CPU Cao
- **Điều kiện**: CPU usage > 90% liên tục trong 30 giây
- **Severity**: WARNING
- **Alert Type**: `CPU_HIGH`

### 2. RAM Cao
- **Điều kiện**: RAM usage > 85%
- **Severity**: WARNING
- **Alert Type**: `RAM_HIGH`

### 3. Máy Offline
- **Điều kiện**: Không nhận heartbeat > 15 giây
- **Severity**: CRITICAL
- **Alert Type**: `OFFLINE`

### Cấu Hình Cảnh Báo

Chỉnh sửa trong `server/src/main/resources/application.properties`:

```properties
# Ngưỡng CPU (%)
monitor.alert.cpu.threshold=90
# Thời gian CPU cao (giây)
monitor.alert.cpu.duration=30

# Ngưỡng RAM (%)
monitor.alert.ram.threshold=85

# Timeout offline (giây)
monitor.alert.offline.timeout=15
```

## 💡 Giải Thích Từng Module

### Client Side

#### 1. SystemMonitor (com.monitor.client.monitor)
- **Chức năng**: Thu thập thông tin hệ thống bằng OSHI
- **Dữ liệu thu thập**:
  - CPU usage (tổng + theo từng core)
  - RAM usage
  - Disk usage (tất cả ổ đĩa)
  - Network I/O (tất cả interface)
  - Top 5 processes sử dụng CPU nhiều nhất
- **Công nghệ**: OSHI (Operating System and Hardware Information)

#### 2. HeartbeatManager (com.monitor.client.heartbeat)
- **Chức năng**: Gửi heartbeat thông minh đến server
- **Cơ chế thông minh**:
  - CPU > 80% → gửi mỗi 1 giây
  - CPU < 10% (idle) → gửi mỗi 10 giây
  - Bình thường → gửi mỗi 5 giây
- **Xác thực**: HMAC-SHA256 signature
- **Giao thức**: REST API (POST)

#### 3. CommandHandler (com.monitor.client.command)
- **Chức năng**: Xử lý lệnh từ server
- **Lệnh hỗ trợ**: KILL_PROCESS, SHUTDOWN, LOCK_KEYBOARD, SCREENSHOT
- **Giao thức**: WebSocket

#### 4. ClientWebSocket (com.monitor.client.websocket)
- **Chức năng**: Kết nối WebSocket để nhận lệnh
- **Tự động reconnect**: Nếu mất kết nối, tự động kết nối lại sau 5 giây

### Server Side

#### 1. Controllers (com.monitor.server.controller)
- **HeartbeatController**: Nhận heartbeat từ client
- **MachineController**: Quản lý thông tin máy tính
- **MetricController**: Lấy metrics từ database
- **AlertController**: Quản lý cảnh báo
- **CommandController**: Gửi lệnh đến client

#### 2. Services (com.monitor.server.service)
- **MachineService**: Quản lý máy tính (CRUD)
- **MetricService**: Lưu và truy vấn metrics
- **AlertService**: Tạo và quản lý cảnh báo
- **CommandService**: Quản lý WebSocket sessions và gửi lệnh
- **AnalysisService**: Phân tích metrics và tạo cảnh báo tự động (chạy định kỳ)

#### 3. Security (com.monitor.server.security)
- **AuthenticationService**: Xác thực client bằng HMAC

#### 4. WebSocket (com.monitor.server.websocket)
- **ClientWebSocketHandler**: Xử lý kết nối WebSocket từ client

## 🧠 Gợi Ý Mở Rộng AI/ML

### 1. Dự Đoán Sự Cố (Predictive Maintenance)
- **Ý tưởng**: Sử dụng Machine Learning để dự đoán khi nào máy tính có thể gặp sự cố
- **Dữ liệu**: Lịch sử metrics, patterns của CPU/RAM/Disk
- **Model**: Time Series Forecasting (LSTM, Prophet)
- **Ứng dụng**: Cảnh báo trước khi máy gặp sự cố

### 2. Phân Loại Process Bất Thường
- **Ý tưởng**: Tự động phát hiện process bất thường hoặc đáng ngờ
- **Dữ liệu**: Process name, CPU usage, Memory usage, Network I/O
- **Model**: Classification (Random Forest, Neural Network)
- **Ứng dụng**: Phát hiện malware hoặc process độc hại

### 3. Tối Ưu Hóa Tài Nguyên
- **Ý tưởng**: Đề xuất tối ưu hóa dựa trên lịch sử sử dụng
- **Dữ liệu**: Lịch sử sử dụng CPU/RAM/Disk theo thời gian
- **Model**: Clustering (K-means) để phân nhóm pattern sử dụng
- **Ứng dụng**: Đề xuất cấu hình tối ưu cho từng máy

### 4. Phát Hiện Anomaly
- **Ý tưởng**: Phát hiện hành vi bất thường trong hệ thống
- **Dữ liệu**: Metrics real-time
- **Model**: Isolation Forest, Autoencoder
- **Ứng dụng**: Phát hiện tấn công hoặc sự cố bất thường

### 5. Tự Động Hóa Quản Lý
- **Ý tưởng**: Tự động thực hiện hành động dựa trên phân tích
- **Ví dụ**: Tự động kill process nếu CPU quá cao, tự động restart service

## 📝 Ví Dụ Sử Dụng

### Ví Dụ 1: Giám Sát Phòng Máy

1. **Khởi động server**
2. **Chạy client trên mỗi máy học sinh** với `MACHINE_ID` khác nhau
3. **Mở Dashboard** để xem tất cả máy tính
4. **Theo dõi cảnh báo** khi có vấn đề

### Ví Dụ 2: Điều Khiển Từ Xa

1. **Chọn máy tính** trong Dashboard
2. **Chọn lệnh** (ví dụ: KILL_PROCESS)
3. **Nhập tham số** (ví dụ: PID = 1234)
4. **Gửi lệnh** → Client thực thi và báo kết quả

### Ví Dụ 3: Phân Tích Lịch Sử

1. **Truy vấn API** để lấy metrics trong khoảng thời gian
2. **Phân tích** xu hướng sử dụng tài nguyên
3. **Tối ưu hóa** cấu hình dựa trên dữ liệu

## 🐛 Troubleshooting

### Lỗi: Client không kết nối được server
- **Kiểm tra**: Server đã chạy chưa? (`http://localhost:8080/api/machines`)
- **Kiểm tra**: `SERVER_URL` trong client có đúng không?
- **Kiểm tra**: Firewall có chặn port 8080 không?

### Lỗi: Xác thực thất bại
- **Kiểm tra**: `SECRET_KEY` của client có khớp với database không?
- **Kiểm tra**: HMAC signature có được tính đúng không?

### Lỗi: Không nhận được lệnh
- **Kiểm tra**: WebSocket connection có thành công không?
- **Kiểm tra**: Client có đang online không?

### Lỗi: Database connection
- **H2**: Dữ liệu được lưu vào file `./data/monitordb.mv.db`
- Nếu lỗi, xóa thư mục `data` và restart server để tạo database mới

## 📚 Tài Liệu Tham Khảo

- [OSHI Documentation](https://github.com/oshi/oshi)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [WebSocket API](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket)
- [HMAC Authentication](https://en.wikipedia.org/wiki/HMAC)

## 📄 License

Dự án này được tạo cho mục đích giáo dục và học tập.

## 👥 Đóng Góp

Mọi đóng góp đều được chào đón! Vui lòng tạo issue hoặc pull request.

---

**Tác giả**: Senior Java Backend Engineer & System Architect  
**Ngày tạo**: 2024  
**Phiên bản**: 1.0
