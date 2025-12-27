# Hệ Thống Giám Sát Máy Tính (Remote Computer Monitoring System)

Hệ thống quản lý phòng máy tính hoạt động trong mạng LAN/Wi-Fi nội bộ theo mô hình client-server, phù hợp cho phòng tin học trong trường học.

## 📋 Mô Tả

Hệ thống gồm hai ứng dụng độc lập:
- **Server**: Ứng dụng Spring Boot chạy trên máy quản lý, cung cấp giao diện web để giáo viên quản lý và điều khiển các máy client
- **Client Agent**: Ứng dụng Java chạy trên Windows, không có giao diện web, người dùng phải chủ động chạy bằng lệnh

## ✨ Chức Năng

### Server
- ✅ Xem danh sách các máy client đã đăng ký
- ✅ Xem trạng thái online/offline của từng máy
- ✅ Gửi thông báo đến máy client (hiển thị popup trên Windows)
- ✅ Quan sát màn hình máy client (chụp ảnh và hiển thị gần realtime)
- ✅ Khóa/mở khóa bàn phím và chuột của máy client

### Client Agent
- ✅ Tự động đăng ký với server khi khởi động
- ✅ Nhận và xử lý lệnh từ server qua WebSocket
- ✅ Hiển thị popup thông báo trên Windows
- ✅ Chụp màn hình và gửi về server
- ✅ Khóa/mở khóa bàn phím và chuột khi nhận lệnh

## 🏗️ Kiến Trúc

```
┌─────────────────┐         ┌─────────────────┐
│   Server        │         │   Client Agent  │
│  (Spring Boot)  │◄───────►│    (Java)       │
│                 │         │                 │
│  - Web UI       │         │  - Đăng ký      │
│  - REST API     │         │  - Nhận lệnh    │
│  - WebSocket    │         │  - Xử lý lệnh   │
│  - SQL Server   │         │  - Popup        │
└─────────────────┘         └─────────────────┘
         │
         ▼
┌─────────────────┐
│   SQL Server    │
│   Database      │
└─────────────────┘
```

## 🛠️ Công Nghệ Sử Dụng

### Server
- Spring Boot 2.7.14
- Spring WebSocket
- Spring Data JPA
- SQL Server
- HTML/CSS/JavaScript (Giao diện web)

### Client
- Java 11
- Java WebSocket Client
- OSHI (System Information)
- Java AWT (Screen capture, Popup)

## 📦 Cài Đặt

### Yêu Cầu
- Java 11 hoặc cao hơn
- SQL Server
- Maven 3.6+
- Windows (cho client agent)

### 1. Cấu Hình Database

Tạo database và chạy script tạo bảng:
```sql
CREATE DATABASE Giamsatmaytinhtuxa;
-- Chạy file server/src/main/resources/database/schema.sql
```

### 2. Cấu Hình Server

Sửa file `server/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://YOUR_IP:1433;databaseName=Giamsatmaytinhtuxa;...
    username: YOUR_USERNAME
    password: YOUR_PASSWORD
```

### 3. Build và Chạy

**Server:**
```bash
cd server
mvn clean package
java -jar target/server-1.0.jar
```

**Client:**
```bash
cd client
mvn clean package
java -jar target/client-1.0.jar --server.url=http://SERVER_IP:8080
```

## 📖 Hướng Dẫn Sử Dụng

Xem file [HUONG_DAN.md](HUONG_DAN.md) để biết chi tiết:
- Cấu hình database
- Cấu hình IP LAN và port
- Cách chạy server và client
- Kịch bản demo các chức năng
- Xử lý lỗi thường gặp

## 📝 Ví Dụ API

Xem file [VI_DU_JSON.md](VI_DU_JSON.md) để xem các ví dụ:
- Request/Response JSON cho tất cả API
- WebSocket messages
- Lỗi thường gặp

## 🗂️ Cấu Trúc Project

```
Giamsatmaytinhtuxa/
├── server/                          # Server Spring Boot
│   ├── src/main/java/com/monitor/server/
│   │   ├── controller/             # REST Controllers
│   │   ├── service/                # Business Logic
│   │   ├── repository/             # Data Access
│   │   ├── model/                   # Entities
│   │   ├── websocket/               # WebSocket Handlers
│   │   └── config/                   # Configuration
│   ├── src/main/resources/
│   │   ├── application.yml          # Cấu hình
│   │   ├── database/schema.sql      # SQL Schema
│   │   └── static/server.html       # Giao diện web
│   └── pom.xml
├── client/                          # Client Agent
│   ├── src/main/java/com/monitor/client/
│   │   ├── Main.java                # Entry point
│   │   ├── command/                 # Command Handler
│   │   └── websocket/               # WebSocket Client
│   └── pom.xml
├── HUONG_DAN.md                     # Hướng dẫn chi tiết
├── VI_DU_JSON.md                    # Ví dụ JSON
└── README.md                         # File này
```

## 🔑 Các Endpoint Chính

### Server Web UI
- `http://SERVER_IP:8080/server` - Giao diện quản lý

### REST API
- `GET /api/machines` - Lấy danh sách máy tính
- `POST /api/machines/register` - Đăng ký máy tính
- `POST /api/notifications/{machineId}/send` - Gửi thông báo
- `POST /api/commands/{machineId}/lock` - Khóa máy
- `POST /api/commands/{machineId}/unlock` - Mở khóa máy
- `POST /api/commands/{machineId}/screen-capture` - Chụp màn hình
- `GET /api/screen/{machineId}/latest` - Lấy ảnh màn hình mới nhất

### WebSocket
- `ws://SERVER_IP:8080/ws-client` - Kết nối WebSocket từ client

## 🎯 Kịch Bản Sử Dụng

1. **Khởi động Server**: Chạy server trên máy quản lý
2. **Chạy Client**: Trên mỗi máy tính cần giám sát, chạy client agent
3. **Truy cập Web UI**: Mở trình duyệt và truy cập `http://SERVER_IP:8080/server`
4. **Quản lý**: Sử dụng giao diện web để:
   - Xem danh sách máy tính
   - Gửi thông báo
   - Quan sát màn hình
   - Khóa/mở khóa bàn phím chuột

## ⚠️ Lưu Ý

- Hệ thống chỉ hoạt động trong mạng LAN/Wi-Fi nội bộ
- Client phải chạy trên Windows
- Người dùng phải chủ động chạy client agent
- Trạng thái online/offline dựa trên khả năng phản hồi, không dùng heartbeat
- Không có cơ chế bảo mật nâng cao (phù hợp mạng nội bộ)

## 📄 License

Dự án này được tạo cho mục đích học tập và đồ án môn học.

## 👨‍💻 Tác Giả

Hệ thống được thiết kế và phát triển cho phòng tin học trong trường học.

---

**Lưu ý**: Đây là hệ thống demo phù hợp cho đồ án môn học. Trong môi trường production, cần thêm các biện pháp bảo mật và xử lý lỗi nâng cao.
