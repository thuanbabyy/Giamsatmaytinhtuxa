# 📚 HƯỚNG DẪN ÔN TẬP - NGƯỜI 2
## Server Controller + REST API

---

## 🎯 Phần Phụ Trách
Bạn chịu trách nhiệm hiểu và trình bày về **REST API Controllers** - các endpoint xử lý request từ client/web.

---

## 1. Tổng Quan Controller Layer

Controllers nằm tại: `server/src/main/java/com/monitor/server/controller/`

| Controller | File | Chức năng |
|------------|------|-----------|
| MachineController | `MachineController.java` | Quản lý danh sách máy tính |
| CommandController | `CommandController.java` | Gửi lệnh điều khiển |
| NotificationController | `NotificationController.java` | Gửi thông báo |
| ScreenController | `ScreenController.java` | Quản lý ảnh màn hình |
| HeartbeatController | `HeartbeatController.java` | Kiểm tra trạng thái online |
| AlertController | `AlertController.java` | Quản lý cảnh báo |
| MetricController | `MetricController.java` | Thông tin hệ thống |
| WebController | `WebController.java` | Điều hướng trang web |

---

## 2. Chi Tiết Các Endpoint

### 2.1. MachineController - Quản lý máy tính

```java
@RestController
@RequestMapping("/api/machines")
public class MachineController {
    
    // Lấy danh sách tất cả máy tính
    @GetMapping
    public List<Machine> getAllMachines();
    
    // Đăng ký máy tính mới
    @PostMapping("/register")
    public ResponseEntity<?> registerMachine(@RequestBody MachineRegistration request);
    
    // Lấy thông tin một máy
    @GetMapping("/{machineId}")
    public Machine getMachine(@PathVariable String machineId);
}
```

**Ví dụ Request - Đăng ký máy:**
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
    "isOnline": true
  }
}
```

---

### 2.2. CommandController - Gửi lệnh điều khiển

```java
@RestController
@RequestMapping("/api/commands")
public class CommandController {
    
    // Khóa bàn phím và chuột
    @PostMapping("/{machineId}/lock")
    public ResponseEntity<?> lockMachine(@PathVariable String machineId);
    
    // Mở khóa bàn phím và chuột
    @PostMapping("/{machineId}/unlock")
    public ResponseEntity<?> unlockMachine(@PathVariable String machineId);
    
    // Yêu cầu chụp màn hình
    @PostMapping("/{machineId}/screen-capture")
    public ResponseEntity<?> captureScreen(@PathVariable String machineId);
    
    // Lấy danh sách lệnh của máy
    @GetMapping("/{machineId}")
    public List<Command> getCommands(@PathVariable String machineId);
}
```

**Ví dụ - Khóa máy:**
```json
POST /api/commands/MAY-TINH-01/lock

Response:
{
  "success": true,
  "message": "Đã gửi lệnh khóa",
  "commandId": 123
}
```

---

### 2.3. NotificationController - Gửi thông báo

```java
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    // Gửi thông báo đến máy tính
    @PostMapping("/{machineId}/send")
    public ResponseEntity<?> sendNotification(
        @PathVariable String machineId,
        @RequestBody NotificationRequest request);
    
    // Lấy lịch sử thông báo
    @GetMapping("/{machineId}")
    public List<Notification> getNotifications(@PathVariable String machineId);
}
```

**Ví dụ - Gửi thông báo:**
```json
POST /api/notifications/MAY-TINH-01/send
{
  "title": "Thông Báo Quan Trọng",
  "message": "Vui lòng tắt các ứng dụng không cần thiết",
  "type": "WARNING"
}

Response:
{
  "success": true,
  "message": "Đã gửi thông báo"
}
```

**Các loại thông báo (type):**
- `INFO` - Thông tin thông thường
- `WARNING` - Cảnh báo
- `ERROR` - Lỗi

---

### 2.4. ScreenController - Quản lý ảnh màn hình

```java
@RestController
@RequestMapping("/api/screen")
public class ScreenController {
    
    // Client upload ảnh màn hình
    @PostMapping("/{machineId}/upload")
    public ResponseEntity<?> uploadScreen(
        @PathVariable String machineId,
        @RequestBody ScreenUploadRequest request);
    
    // Lấy ảnh màn hình mới nhất
    @GetMapping("/{machineId}/latest")
    public ResponseEntity<ScreenData> getLatestScreen(@PathVariable String machineId);
    
    // Lấy ảnh theo ID
    @GetMapping("/{machineId}/image/{screenId}")
    public ResponseEntity<byte[]> getScreenImage(
        @PathVariable String machineId, 
        @PathVariable Long screenId);
}
```

**Ví dụ - Upload ảnh:**
```json
POST /api/screen/MAY-TINH-01/upload
{
  "imageData": "iVBORw0KGgoAAAANSUhEUgAA...",  // Base64
  "imageFormat": "PNG",
  "commandId": 123
}
```

---

### 2.5. HeartbeatController - Kiểm tra trạng thái

```java
@RestController
@RequestMapping("/api/heartbeat")
public class HeartbeatController {
    
    // Client gửi heartbeat
    @PostMapping("/{machineId}")
    public ResponseEntity<?> heartbeat(@PathVariable String machineId);
    
    // Kiểm tra máy có online không
    @GetMapping("/{machineId}/status")
    public ResponseEntity<?> checkStatus(@PathVariable String machineId);
}
```

---

## 3. HTTP Methods Sử Dụng

| Method | Mục đích | Ví dụ |
|--------|----------|-------|
| `GET` | Lấy dữ liệu | `GET /api/machines` |
| `POST` | Tạo mới, gửi lệnh | `POST /api/commands/{id}/lock` |
| `PUT` | Cập nhật | `PUT /api/machines/{id}` |
| `DELETE` | Xóa | `DELETE /api/machines/{id}` |

---

## 4. Annotations Quan Trọng

```java
@RestController          // Đánh dấu class là REST Controller
@RequestMapping("/api")  // Base URL cho tất cả endpoint
@GetMapping("/path")     // Handle GET request
@PostMapping("/path")    // Handle POST request
@PathVariable            // Lấy giá trị từ URL path
@RequestBody             // Parse JSON body thành object
@Autowired               // Dependency Injection
```

---

## 5. Response Format Chuẩn

**Success Response:**
```json
{
  "success": true,
  "message": "Thao tác thành công",
  "data": { ... }
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Mô tả lỗi",
  "error": "ERROR_CODE"
}
```

---

## 📝 Câu Hỏi Ôn Tập

1. Liệt kê các Controller trong dự án và chức năng của từng Controller.
2. Giải thích sự khác nhau giữa GET và POST method.
3. Viết endpoint để gửi lệnh khóa máy tính.
4. `@PathVariable` và `@RequestBody` khác nhau như thế nào?
5. Khi nào dùng ResponseEntity? Tại sao?
6. Flow xử lý khi giáo viên gửi thông báo đến học sinh là gì?

---

## 📁 Files Cần Đọc
- `server/src/main/java/com/monitor/server/controller/*.java`
- `VI_DU_JSON.md` - Ví dụ request/response
- `HUONG_DAN.md` - Phần API examples
