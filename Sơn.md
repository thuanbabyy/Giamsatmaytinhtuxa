# 📚 HƯỚNG DẪN ÔN TẬP - NGƯỜI 4
## WebSocket + Giao Tiếp Real-time

---

## 🎯 Phần Phụ Trách
Bạn phụ trách **WebSocket** - cơ chế giao tiếp real-time Server ↔ Client.

---

## 1. REST API vs WebSocket

| Đặc điểm | REST API | WebSocket |
|----------|----------|-----------|
| Kết nối | Request-Response | Persistent |
| Hướng giao tiếp | Client → Server | Hai chiều |
| Use case | CRUD | Real-time, push |

---

## 2. Server WebSocket Handler

File: `server/src/main/java/com/monitor/server/websocket/ClientWebSocketHandler.java`

```java
@Component
public class ClientWebSocketHandler extends TextWebSocketHandler {
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Khi client kết nối
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Xử lý message từ client
        // - Đăng ký client
        // - Nhận response từ lệnh đã gửi
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Khi client ngắt kết nối
    }
}
```

---

## 3. Message Format

### Client → Server: Đăng ký
```json
{
  "type": "client",
  "machineId": "MAY-TINH-01",
  "name": "Máy Tính 01",
  "ipAddress": "192.168.1.101"
}
```

### Server → Client: Gửi lệnh
```json
{
  "command": "NOTIFICATION",
  "commandId": 123,
  "data": { "title": "Thông Báo", "message": "Nội dung" }
}
```

### Client → Server: Response
```json
{
  "machineId": "MAY-TINH-01",
  "command": "NOTIFICATION",
  "commandId": 123,
  "result": { "success": true }
}
```

---

## 4. Heartbeat
- Kiểm tra client còn online không
- Gửi mỗi 30 giây

---

## 📝 Câu Hỏi Ôn Tập
1. WebSocket khác REST API thế nào?
2. Giải thích các method của TextWebSocketHandler
3. Tại sao cần lưu WebSocketSession?
4. Flow gửi lệnh từ server đến client là gì?

## 📁 Files Cần Đọc
- `server/websocket/ClientWebSocketHandler.java`
- `client/websocket/ClientWebSocket.java`
- `client/heartbeat/HeartbeatManager.java`
