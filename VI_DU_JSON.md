# Ví Dụ Request/Response JSON

## 1. API Đăng Ký Máy Tính

### Request
```http
POST /api/machines/register
Content-Type: application/json
```

```json
{
  "machineId": "MAY-TINH-01",
  "name": "Máy Tính 01",
  "ipAddress": "192.168.1.101",
  "osName": "Windows",
  "osVersion": "10.0"
}
```

### Response (200 OK)
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
    "lastResponseTime": "2024-01-15T10:30:00",
    "registeredAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

## 2. API Lấy Danh Sách Máy Tính

### Request
```http
GET /api/machines
```

### Response (200 OK)
```json
[
  {
    "machineId": "MAY-TINH-01",
    "name": "Máy Tính 01",
    "ipAddress": "192.168.1.101",
    "osName": "Windows",
    "osVersion": "10.0",
    "isOnline": true,
    "lastResponseTime": "2024-01-15T10:35:00",
    "registeredAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:35:00"
  },
  {
    "machineId": "MAY-TINH-02",
    "name": "Máy Tính 02",
    "ipAddress": "192.168.1.102",
    "osName": "Windows",
    "osVersion": "11.0",
    "isOnline": false,
    "lastResponseTime": "2024-01-15T10:20:00",
    "registeredAt": "2024-01-15T10:15:00",
    "updatedAt": "2024-01-15T10:20:00"
  }
]
```

## 3. API Gửi Thông Báo

### Request
```http
POST /api/notifications/MAY-TINH-01/send
Content-Type: application/json
```

```json
{
  "title": "Thông Báo Quan Trọng",
  "message": "Vui lòng tắt các ứng dụng không cần thiết và tập trung vào bài học",
  "type": "WARNING"
}
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Đã gửi thông báo"
}
```

### Response (400 Bad Request - thiếu message)
```json
{
  "success": false,
  "message": "Nội dung thông báo không được để trống"
}
```

## 4. API Khóa Bàn Phím Chuột

### Request
```http
POST /api/commands/MAY-TINH-01/lock
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Đã gửi lệnh khóa"
}
```

### Response (200 OK - máy offline)
```json
{
  "success": false,
  "message": "Không thể gửi lệnh (máy offline)"
}
```

## 5. API Mở Khóa Bàn Phím Chuột

### Request
```http
POST /api/commands/MAY-TINH-01/unlock
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Đã gửi lệnh mở khóa"
}
```

## 6. API Yêu Cầu Chụp Màn Hình

### Request
```http
POST /api/commands/MAY-TINH-01/screen-capture
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Đã gửi yêu cầu chụp màn hình"
}
```

## 7. API Lấy Ảnh Màn Hình Mới Nhất

### Request
```http
GET /api/screen/MAY-TINH-01/latest
```

### Response (200 OK - có ảnh)
```
Content-Type: image/png

[Binary image data]
```

### Response (200 OK - chưa có ảnh)
```json
{
  "success": false,
  "message": "Chưa có ảnh màn hình"
}
```

## 8. API Upload Ảnh Màn Hình (từ Client)

### Request
```http
POST /api/screen/MAY-TINH-01/upload
Content-Type: application/json
```

```json
{
  "imageData": "iVBORw0KGgoAAAANSUhEUgAA...",
  "imageFormat": "PNG",
  "commandId": 123
}
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Đã lưu ảnh màn hình",
  "screenDataId": 456
}
```

### Response (400 Bad Request)
```json
{
  "success": false,
  "message": "Lỗi khi lưu ảnh: Invalid base64 data"
}
```

## 9. WebSocket Messages

### 9.1. Client Đăng Ký (Client → Server)
```json
{
  "machineId": "MAY-TINH-01",
  "type": "client",
  "name": "user01",
  "ipAddress": "192.168.1.101",
  "osName": "Windows",
  "osVersion": "10.0"
}
```

### 9.2. Server Xác Nhận Kết Nối (Server → Client)
```json
{
  "status": "connected",
  "message": "Đã kết nối thành công"
}
```

### 9.3. Server Gửi Lệnh Thông Báo (Server → Client)
```json
{
  "command": "NOTIFICATION",
  "machineId": "MAY-TINH-01",
  "commandId": 123,
  "timestamp": 1705312200000,
  "data": {
    "title": "Thông Báo",
    "message": "Vui lòng tập trung vào bài học",
    "type": "INFO"
  }
}
```

### 9.4. Client Phản Hồi Thông Báo (Client → Server)
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

### 9.5. Server Gửi Lệnh Khóa (Server → Client)
```json
{
  "command": "LOCK",
  "machineId": "MAY-TINH-01",
  "commandId": 124,
  "timestamp": 1705312300000
}
```

### 9.6. Client Phản Hồi Khóa (Client → Server)
```json
{
  "machineId": "MAY-TINH-01",
  "command": "LOCK",
  "commandId": 124,
  "result": {
    "success": true,
    "message": "Đã khóa bàn phím và chuột",
    "timestamp": 1705312300100
  }
}
```

### 9.7. Server Gửi Lệnh Chụp Màn Hình (Server → Client)
```json
{
  "command": "SCREEN_CAPTURE",
  "machineId": "MAY-TINH-01",
  "commandId": 125,
  "timestamp": 1705312400000
}
```

### 9.8. Client Phản Hồi Chụp Màn Hình (Client → Server)
```json
{
  "machineId": "MAY-TINH-01",
  "command": "SCREEN_CAPTURE",
  "commandId": 125,
  "result": {
    "success": true,
    "message": "Đã chụp và gửi màn hình",
    "imageData": "iVBORw0KGgoAAAANSUhEUgAA...",
    "imageFormat": "PNG",
    "timestamp": 1705312400500
  }
}
```

## 10. Lỗi Thường Gặp

### 10.1. Máy Không Online
```json
{
  "success": false,
  "message": "Không thể gửi lệnh (máy offline)"
}
```

### 10.2. Máy Không Tồn Tại
```http
HTTP/1.1 404 Not Found
```

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Machine not found: MAY-TINH-99",
  "path": "/api/commands/MAY-TINH-99/lock"
}
```

### 10.3. Lỗi Validation
```http
HTTP/1.1 400 Bad Request
```

```json
{
  "success": false,
  "message": "Nội dung thông báo không được để trống"
}
```

### 10.4. Lỗi Server
```http
HTTP/1.1 500 Internal Server Error
```

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Database connection failed",
  "path": "/api/machines"
}
```

