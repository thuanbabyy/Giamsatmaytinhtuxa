# Hướng Dẫn Sử Dụng Hệ Thống Giám Sát Máy Tính

## 1. Cấu Hình Database SQL Server

### 1.1. Tạo Database
```sql
CREATE DATABASE Giamsatmaytinhtuxa;
```

### 1.2. Chạy Script Tạo Bảng
Mở SQL Server Management Studio và chạy file `server/src/main/resources/database/schema.sql`

Hoặc chạy trực tiếp:
```sql
-- Xem file server/src/main/resources/database/schema.sql
```

### 1.3. Cấu Hình Kết Nối
Sửa file `server/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://YOUR_SERVER_IP:1433;databaseName=Giamsatmaytinhtuxa;encrypt=true;trustServerCertificate=true;
    username: YOUR_USERNAME
    password: YOUR_PASSWORD
```

## 2. Cấu Hình IP LAN và Port

### 2.1. Server
Sửa file `server/src/main/resources/application.yml`:
```yaml
server:
  port: 8080  # Thay đổi port nếu cần
```

**Lưu ý:** Đảm bảo firewall cho phép port 8080.

### 2.2. Lấy IP LAN của Server
- **Windows:** Mở Command Prompt, gõ `ipconfig`, tìm IPv4 Address
- **Ví dụ:** `192.168.1.100`

## 3. Cách Chạy Server

### 3.1. Build Server
```bash
cd server
mvn clean package
```

### 3.2. Chạy Server
```bash
# Windows
java -jar target/server-1.0.jar

# Hoặc với Maven
mvn spring-boot:run
```

### 3.3. Truy Cập Giao Diện Web
Mở trình duyệt và truy cập:
```
http://YOUR_SERVER_IP:8080/server
```

Ví dụ: `http://192.168.1.100:8080/server`

## 4. Cách Chạy Client

### 4.1. Build Client
```bash
cd client
mvn clean package
```

File JAR sẽ được tạo tại: `client/target/client-1.0.jar`

### 4.2. Chạy Client Agent
Trên máy tính Windows cần giám sát, mở Command Prompt và chạy:

```bash
# Cách 1: Chỉ định server URL và machine ID
java -jar client-1.0.jar --server.url=http://192.168.1.100:8080 --machine.id=MAY-TINH-01

# Cách 2: Chỉ định server URL (machine ID sẽ tự động tạo)
java -jar client-1.0.jar --server.url=http://192.168.1.100:8080

# Cách 3: Dùng giá trị mặc định (localhost:8080)
java -jar client-1.0.jar
```

### 4.3. Chạy Client với Biến Môi Trường
```bash
# Windows CMD
set SERVER_URL=http://192.168.1.100:8080
set MACHINE_ID=MAY-TINH-01
java -jar client-1.0.jar

# Windows PowerShell
$env:SERVER_URL="http://192.168.1.100:8080"
$env:MACHINE_ID="MAY-TINH-01"
java -jar client-1.0.jar
```

**Lưu ý:** 
- Client phải chạy trên Windows
- Client phải có kết nối mạng đến server
- Người dùng phải chủ động chạy client, không tự động khởi động

## 5. Kịch Bản Demo Các Chức Năng

### 5.1. Đăng Ký Máy Tính
1. Chạy client agent trên máy tính Windows
2. Client tự động đăng ký với server qua REST API
3. Máy tính xuất hiện trên giao diện web tại `/server`

### 5.2. Xem Danh Sách Máy Tính
1. Truy cập `http://YOUR_SERVER_IP:8080/server`
2. Xem danh sách tất cả máy tính đã đăng ký
3. Trạng thái online/offline được hiển thị bằng màu xanh/đỏ

### 5.3. Gửi Thông Báo
1. Trên giao diện web, click nút **"📢 Thông Báo"** trên máy tính muốn gửi
2. Nhập tiêu đề và nội dung thông báo
3. Chọn loại thông báo (Thông tin, Cảnh báo, Lỗi)
4. Click **"Gửi Thông Báo"**
5. Popup sẽ hiển thị trên màn hình Windows của máy client

### 5.4. Quan Sát Màn Hình
1. Trên giao diện web, click nút **"🖼️ Màn Hình"** trên máy tính muốn quan sát
2. Click **"Chụp Màn Hình"** để chụp một lần
3. Click **"Bắt Đầu Quan Sát"** để tự động chụp mỗi 5 giây
4. Ảnh màn hình sẽ hiển thị trên giao diện web
5. Popup thông báo sẽ hiển thị trên máy client khi bắt đầu quan sát

### 5.5. Khóa/Mở Khóa Bàn Phím Chuột
1. **Khóa:** Click nút **"🔒 Khóa"** trên máy tính muốn khóa
2. Bàn phím và chuột của máy client sẽ bị khóa
3. Popup thông báo sẽ hiển thị trên máy client
4. **Mở khóa:** Click nút **"🔓 Mở Khóa"** để mở khóa
5. Popup thông báo mở khóa sẽ hiển thị trên máy client

## 6. Ví Dụ Request/Response JSON

### 6.1. Đăng Ký Máy Tính
**Request:**
```json
POST /api/machines/register
{
  "machineId": "MAY-TINH-01",
  "name": "Máy Tính 01",
  "ipAddress": "192.168.1.101",
  "osName": "Windows",
  "osVersion": "10.0"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "machine": {
    "machineId": "MAY-TINH-01",
    "name": "Máy Tính 01",
    "ipAddress": "192.168.1.101",
    "osName": "Windows",
    "osVersion": "10.0",
    "isOnline": true,
    "registeredAt": "2024-01-15T10:30:00"
  }
}
```

### 6.2. Gửi Thông Báo
**Request:**
```json
POST /api/notifications/MAY-TINH-01/send
{
  "title": "Thông Báo Quan Trọng",
  "message": "Vui lòng tắt các ứng dụng không cần thiết",
  "type": "WARNING"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đã gửi thông báo"
}
```

### 6.3. Khóa Máy Tính
**Request:**
```json
POST /api/commands/MAY-TINH-01/lock
```

**Response:**
```json
{
  "success": true,
  "message": "Đã gửi lệnh khóa"
}
```

### 6.4. Chụp Màn Hình
**Request:**
```json
POST /api/commands/MAY-TINH-01/screen-capture
```

**Response:**
```json
{
  "success": true,
  "message": "Đã gửi yêu cầu chụp màn hình"
}
```

### 6.5. Upload Ảnh Màn Hình (từ Client)
**Request:**
```json
POST /api/screen/MAY-TINH-01/upload
{
  "imageData": "iVBORw0KGgoAAAANSUhEUgAA...",
  "imageFormat": "PNG",
  "commandId": 123
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đã lưu ảnh màn hình",
  "screenDataId": 456
}
```

### 6.6. WebSocket Command (Server → Client)
**Message:**
```json
{
  "command": "NOTIFICATION",
  "machineId": "MAY-TINH-01",
  "commandId": 123,
  "timestamp": 1705312200000,
  "data": {
    "title": "Thông Báo",
    "message": "Nội dung thông báo",
    "type": "INFO"
  }
}
```

**Response (Client → Server):**
```json
{
  "machineId": "MAY-TINH-01",
  "command": "NOTIFICATION",
  "commandId": 123,
  "result": {
    "success": true,
    "message": "Đã hiển thị thông báo",
    "timestamp": 1705312201000
  }
}
```

## 7. Cấu Trúc Database

### 7.1. Bảng machines
- `machine_id` (PK): ID duy nhất của máy tính
- `name`: Tên máy tính
- `ip_address`: Địa chỉ IP
- `os_name`: Tên hệ điều hành
- `os_version`: Phiên bản hệ điều hành
- `is_online`: Trạng thái online/offline
- `last_response_time`: Thời gian phản hồi cuối cùng
- `registered_at`: Thời gian đăng ký
- `updated_at`: Thời gian cập nhật cuối

### 7.2. Bảng commands
- `id` (PK): ID lệnh
- `machine_id` (FK): ID máy tính
- `command_type`: Loại lệnh (LOCK, UNLOCK, SCREEN_CAPTURE, NOTIFICATION)
- `command_data`: Dữ liệu lệnh (JSON)
- `status`: Trạng thái (PENDING, SENT, COMPLETED, FAILED)
- `created_at`: Thời gian tạo
- `executed_at`: Thời gian thực thi
- `response_data`: Dữ liệu phản hồi (JSON)

### 7.3. Bảng notifications
- `id` (PK): ID thông báo
- `machine_id` (FK): ID máy tính
- `message`: Nội dung thông báo
- `title`: Tiêu đề
- `notification_type`: Loại (INFO, WARNING, ERROR)
- `sent_at`: Thời gian gửi
- `displayed_at`: Thời gian hiển thị

### 7.4. Bảng screen_data
- `id` (PK): ID dữ liệu màn hình
- `machine_id` (FK): ID máy tính
- `image_data`: Dữ liệu ảnh (binary)
- `image_format`: Định dạng (PNG, JPEG)
- `captured_at`: Thời gian chụp
- `command_id` (FK): ID lệnh (nếu có)

## 8. Xử Lý Lỗi Thường Gặp

### 8.1. Server không khởi động được
- Kiểm tra port 8080 có bị chiếm không
- Kiểm tra kết nối SQL Server
- Kiểm tra file `application.yml`

### 8.2. Client không kết nối được server
- Kiểm tra IP và port của server
- Kiểm tra firewall
- Kiểm tra kết nối mạng

### 8.3. Máy tính không hiển thị trên server
- Kiểm tra client đã chạy chưa
- Kiểm tra log của client
- Kiểm tra kết nối WebSocket

### 8.4. Không thể khóa/mở khóa bàn phím chuột
- Chức năng này cần quyền administrator
- Trên Windows, có thể cần cấu hình thêm

## 9. Lưu Ý Bảo Mật

- Hệ thống này chỉ dùng trong mạng LAN nội bộ
- Không nên expose server ra internet
- Có thể thêm authentication nếu cần
- Đảm bảo firewall được cấu hình đúng

## 10. Hỗ Trợ

Nếu gặp vấn đề, kiểm tra:
- Log của server: `server/logs/`
- Log của client: Console output
- Database connection
- Network connectivity

