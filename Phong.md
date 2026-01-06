# 📚 HƯỚNG DẪN ÔN TẬP - NGƯỜI 1
## Kiến Trúc Tổng Quan + Database

---

## 🎯 Phần Phụ Trách
Bạn chịu trách nhiệm hiểu và trình bày về **kiến trúc hệ thống** và **cơ sở dữ liệu**.

---

## 1. Kiến Trúc Hệ Thống

### 1.1. Mô Hình Client-Server
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

**Giải thích:**
- **Server**: Spring Boot chạy trên máy quản lý (giáo viên)
- **Client Agent**: Java app chạy trên Windows (máy học sinh)
- **Giao tiếp**: REST API + WebSocket trong mạng LAN/Wi-Fi nội bộ

### 1.2. Các Thành Phần Chính

| Thành phần | Công nghệ | Chức năng |
|------------|-----------|-----------|
| Server | Spring Boot 2.7.14 | Quản lý, điều khiển |
| Client | Java 11 | Nhận lệnh, thực thi |
| Database | SQL Server | Lưu trữ dữ liệu |
| Giao tiếp | WebSocket + REST | Real-time + Request/Response |

---

## 2. Công Nghệ Sử Dụng

### Server Stack:
- **Spring Boot 2.7.14** - Framework chính
- **Spring WebSocket** - Giao tiếp real-time
- **Spring Data JPA** - ORM cho database
- **SQL Server** - Cơ sở dữ liệu
- **HTML/CSS/JavaScript** - Giao diện web

### Client Stack:
- **Java 11** - Ngôn ngữ chính
- **Java WebSocket Client** - Kết nối WebSocket
- **OSHI** - Lấy thông tin hệ thống
- **Java AWT** - Chụp màn hình, hiển thị popup

---

## 3. Cơ Sở Dữ Liệu

### 3.1. Tạo Database
```sql
CREATE DATABASE Giamsatmaytinhtuxa;
```

### 3.2. Các Bảng Chính

#### Bảng `machines` - Thông tin máy tính
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `machine_id` | VARCHAR (PK) | ID duy nhất của máy |
| `name` | VARCHAR | Tên máy tính |
| `ip_address` | VARCHAR | Địa chỉ IP |
| `os_name` | VARCHAR | Tên hệ điều hành |
| `os_version` | VARCHAR | Phiên bản OS |
| `is_online` | BIT | Trạng thái online/offline |
| `last_response_time` | DATETIME | Lần phản hồi cuối |
| `registered_at` | DATETIME | Thời gian đăng ký |

#### Bảng `commands` - Lệnh điều khiển
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `id` | BIGINT (PK) | ID lệnh |
| `machine_id` | VARCHAR (FK) | Máy nhận lệnh |
| `command_type` | VARCHAR | LOCK/UNLOCK/SCREEN_CAPTURE/NOTIFICATION |
| `command_data` | NVARCHAR | Dữ liệu JSON |
| `status` | VARCHAR | PENDING/SENT/COMPLETED/FAILED |
| `created_at` | DATETIME | Thời gian tạo |
| `executed_at` | DATETIME | Thời gian thực thi |

#### Bảng `notifications` - Thông báo
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `id` | BIGINT (PK) | ID thông báo |
| `machine_id` | VARCHAR (FK) | Máy nhận thông báo |
| `title` | NVARCHAR | Tiêu đề |
| `message` | NVARCHAR | Nội dung |
| `notification_type` | VARCHAR | INFO/WARNING/ERROR |
| `sent_at` | DATETIME | Thời gian gửi |

#### Bảng `screen_data` - Dữ liệu màn hình
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `id` | BIGINT (PK) | ID ảnh |
| `machine_id` | VARCHAR (FK) | Máy được chụp |
| `image_data` | VARBINARY | Dữ liệu ảnh binary |
| `image_format` | VARCHAR | PNG/JPEG |
| `captured_at` | DATETIME | Thời gian chụp |

---

## 4. Cấu Hình Kết Nối Database

File: `server/src/main/resources/application.yml`
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://YOUR_IP:1433;databaseName=Giamsatmaytinhtuxa;encrypt=true;trustServerCertificate=true;
    username: YOUR_USERNAME
    password: YOUR_PASSWORD
  jpa:
    hibernate:
      ddl-auto: update
```

---

## 5. Cách Chạy Hệ Thống

### Bước 1: Tạo Database
```sql
CREATE DATABASE Giamsatmaytinhtuxa;
-- Chạy file schema.sql
```

### Bước 2: Chạy Server
```bash
cd server
mvn clean package
java -jar target/server-1.0.jar
```

### Bước 3: Chạy Client
```bash
cd client
mvn clean package
java -jar target/client-1.0.jar --server.url=http://SERVER_IP:8080
```

---

## 📝 Câu Hỏi Ôn Tập

1. Hệ thống sử dụng mô hình gì? Có những thành phần nào?
2. Server và Client sử dụng những công nghệ gì?
3. Tại sao dùng WebSocket thay vì chỉ dùng REST API?
4. Liệt kê các bảng trong database và mối quan hệ giữa chúng.
5. Giải thích các trạng thái của lệnh (command status).
6. Cách cấu hình kết nối SQL Server trong Spring Boot?

---

## 📁 Files Cần Đọc
- `README.md` - Tổng quan dự án
- `HUONG_DAN.md` - Hướng dẫn chi tiết
- `server/src/main/resources/application.yml` - Cấu hình
- `server/src/main/resources/database/schema.sql` - SQL Schema
