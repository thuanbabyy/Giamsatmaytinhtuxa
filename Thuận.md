# 📚 HƯỚNG DẪN ÔN TẬP - NGƯỜI 3
## Server Service + Repository + Model

---

## 🎯 Phần Phụ Trách
Bạn chịu trách nhiệm hiểu và trình bày về **Business Logic (Service)**, **Data Access (Repository)**, và **Data Model (Entity)**.

---

## 1. Kiến Trúc 3 Layer

```
┌─────────────────────┐
│     Controller      │  ← Nhận request từ client
├─────────────────────┤
│      Service        │  ← Xử lý business logic
├─────────────────────┤
│     Repository      │  ← Truy cập database
├─────────────────────┤
│    Model/Entity     │  ← Định nghĩa cấu trúc dữ liệu
└─────────────────────┘
```

---

## 2. Model/Entity Layer

**Vị trí:** `server/src/main/java/com/monitor/server/model/`

### 2.1. Machine.java - Entity máy tính
```java
@Entity
@Table(name = "machines")
public class Machine {
    @Id
    private String machineId;      // ID duy nhất
    
    private String name;           // Tên máy
    private String ipAddress;      // Địa chỉ IP
    private String osName;         // Tên OS
    private String osVersion;      // Version OS
    private Boolean isOnline;      // Trạng thái
    private LocalDateTime lastResponseTime;  // Lần phản hồi cuối
    private LocalDateTime registeredAt;      // Thời gian đăng ký
    
    // Getters & Setters
}
```

### 2.2. Command.java - Entity lệnh điều khiển
```java
@Entity
@Table(name = "commands")
public class Command {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String machineId;      // Máy nhận lệnh
    private String commandType;    // LOCK, UNLOCK, SCREEN_CAPTURE, NOTIFICATION
    private String commandData;    // JSON data
    private String status;         // PENDING, SENT, COMPLETED, FAILED
    private LocalDateTime createdAt;
    private LocalDateTime executedAt;
    private String responseData;   // Kết quả từ client
}
```

### 2.3. Notification.java - Entity thông báo
```java
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String machineId;
    private String title;
    private String message;
    private String notificationType;  // INFO, WARNING, ERROR
    private LocalDateTime sentAt;
    private LocalDateTime displayedAt;
}
```

### 2.4. ScreenData.java - Entity ảnh màn hình
```java
@Entity
@Table(name = "screen_data")
public class ScreenData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String machineId;
    
    @Lob
    private byte[] imageData;      // Dữ liệu ảnh binary
    
    private String imageFormat;    // PNG, JPEG
    private LocalDateTime capturedAt;
    private Long commandId;
}
```

---

## 3. Repository Layer

**Vị trí:** `server/src/main/java/com/monitor/server/repository/`

### 3.1. MachineRepository
```java
@Repository
public interface MachineRepository extends JpaRepository<Machine, String> {
    
    // Tìm máy theo ID
    Optional<Machine> findByMachineId(String machineId);
    
    // Lấy tất cả máy online
    List<Machine> findByIsOnlineTrue();
    
    // Tìm máy theo IP
    Optional<Machine> findByIpAddress(String ipAddress);
}
```

### 3.2. CommandRepository
```java
@Repository
public interface CommandRepository extends JpaRepository<Command, Long> {
    
    // Lấy lệnh của một máy
    List<Command> findByMachineId(String machineId);
    
    // Lấy lệnh đang pending
    List<Command> findByMachineIdAndStatus(String machineId, String status);
    
    // Lấy lệnh mới nhất
    Optional<Command> findTopByMachineIdOrderByCreatedAtDesc(String machineId);
}
```

### 3.3. ScreenDataRepository
```java
@Repository
public interface ScreenDataRepository extends JpaRepository<ScreenData, Long> {
    
    // Lấy ảnh mới nhất của máy
    Optional<ScreenData> findTopByMachineIdOrderByCapturedAtDesc(String machineId);
    
    // Lấy ảnh theo command
    Optional<ScreenData> findByCommandId(Long commandId);
}
```

### JPA Query Methods - Quy Tắc Đặt Tên
| Method | SQL Tương đương |
|--------|-----------------|
| `findByName(name)` | `WHERE name = ?` |
| `findByIsOnlineTrue()` | `WHERE is_online = true` |
| `findByMachineIdAndStatus(id, status)` | `WHERE machine_id = ? AND status = ?` |
| `findTopByMachineIdOrderByCreatedAtDesc(id)` | `WHERE machine_id = ? ORDER BY created_at DESC LIMIT 1` |

---

## 4. Service Layer

**Vị trí:** `server/src/main/java/com/monitor/server/service/`

### 4.1. MachineService - Quản lý máy tính
```java
@Service
public class MachineService {
    
    @Autowired
    private MachineRepository machineRepository;
    
    // Đăng ký máy mới hoặc cập nhật
    public Machine registerMachine(String machineId, String name, 
                                   String ipAddress, String osName, String osVersion) {
        Machine machine = machineRepository.findByMachineId(machineId)
            .orElse(new Machine());
        
        machine.setMachineId(machineId);
        machine.setName(name);
        machine.setIpAddress(ipAddress);
        machine.setOsName(osName);
        machine.setOsVersion(osVersion);
        machine.setIsOnline(true);
        machine.setLastResponseTime(LocalDateTime.now());
        
        return machineRepository.save(machine);
    }
    
    // Cập nhật trạng thái online
    public void updateOnlineStatus(String machineId, boolean isOnline) {
        machineRepository.findByMachineId(machineId).ifPresent(machine -> {
            machine.setIsOnline(isOnline);
            machine.setLastResponseTime(LocalDateTime.now());
            machineRepository.save(machine);
        });
    }
    
    // Lấy tất cả máy
    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }
}
```

### 4.2. CommandService - Xử lý lệnh
```java
@Service
public class CommandService {
    
    @Autowired
    private CommandRepository commandRepository;
    
    // Map lưu WebSocket sessions
    private Map<String, WebSocketSession> clientSessions = new ConcurrentHashMap<>();
    
    // Đăng ký client session
    public void registerClient(String machineId, WebSocketSession session) {
        clientSessions.put(machineId, session);
    }
    
    // Gửi lệnh đến client
    public Command sendCommand(String machineId, String commandType, Object data) {
        // 1. Lưu command vào database
        Command command = new Command();
        command.setMachineId(machineId);
        command.setCommandType(commandType);
        command.setCommandData(gson.toJson(data));
        command.setStatus("PENDING");
        command.setCreatedAt(LocalDateTime.now());
        command = commandRepository.save(command);
        
        // 2. Gửi qua WebSocket nếu client online
        WebSocketSession session = clientSessions.get(machineId);
        if (session != null && session.isOpen()) {
            // Gửi message qua WebSocket
            Map<String, Object> message = new HashMap<>();
            message.put("command", commandType);
            message.put("commandId", command.getId());
            message.put("data", data);
            session.sendMessage(new TextMessage(gson.toJson(message)));
            
            command.setStatus("SENT");
            commandRepository.save(command);
        }
        
        return command;
    }
    
    // Xử lý response từ client
    public void handleCommandResponse(String machineId, Long commandId, 
                                       String status, String responseData) {
        commandRepository.findById(commandId).ifPresent(cmd -> {
            cmd.setStatus(status);
            cmd.setResponseData(responseData);
            cmd.setExecutedAt(LocalDateTime.now());
            commandRepository.save(cmd);
        });
    }
}
```

### 4.3. NotificationService - Gửi thông báo
```java
@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private CommandService commandService;
    
    public Notification sendNotification(String machineId, String title, 
                                         String message, String type) {
        // 1. Lưu notification vào DB
        Notification notification = new Notification();
        notification.setMachineId(machineId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(type);
        notification.setSentAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);
        
        // 2. Gửi command đến client
        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("message", message);
        data.put("type", type);
        commandService.sendCommand(machineId, "NOTIFICATION", data);
        
        return notification;
    }
}
```

---

## 5. Annotations Quan Trọng

### Entity Annotations:
```java
@Entity              // Đánh dấu là JPA Entity
@Table(name = "x")   // Tên bảng trong database
@Id                  // Primary key
@GeneratedValue      // Auto-generate ID
@Column              // Cấu hình cột
@Lob                 // Large Object (binary data)
```

### Repository Annotations:
```java
@Repository          // Đánh dấu là Repository
// extends JpaRepository<Entity, ID> để có sẵn CRUD methods
```

### Service Annotations:
```java
@Service             // Đánh dấu là Service
@Autowired           // Dependency Injection
@Transactional       // Quản lý transaction
```

---

## 6. Flow Xử Lý - Ví dụ Gửi Thông Báo

```
1. Controller nhận POST /api/notifications/{id}/send
       ↓
2. NotificationController.sendNotification()
       ↓
3. NotificationService.sendNotification()
       ↓
4. NotificationRepository.save() → Lưu DB
       ↓
5. CommandService.sendCommand() → Gửi WebSocket
       ↓
6. Client nhận và hiển thị popup
```

---

## 📝 Câu Hỏi Ôn Tập

1. Giải thích kiến trúc 3 layer: Controller → Service → Repository.
2. Sự khác nhau giữa Entity và DTO là gì?
3. JpaRepository cung cấp những method nào sẵn có?
4. Giải thích cách đặt tên method trong Repository để tạo query tự động.
5. Tại sao cần Service layer? Không gọi trực tiếp Repository từ Controller được không?
6. Giải thích flow xử lý khi gửi lệnh khóa máy.

---

## 📁 Files Cần Đọc
- `server/src/main/java/com/monitor/server/model/*.java`
- `server/src/main/java/com/monitor/server/repository/*.java`
- `server/src/main/java/com/monitor/server/service/*.java`
