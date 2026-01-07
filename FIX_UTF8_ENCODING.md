# Hướng Dẫn Fix Lỗi Font Tiếng Việt (UTF-8 Encoding)

## Vấn Đề

Windows console mặc định dùng CP1252 → Log tiếng Việt hiển thị sai:
```
─É├ú kß║┐t nß╗æi th├ánh c├┤ng  ❌ SAI
```

Thay vì:
```
Đã kết nối thành công  ✅ ĐÚNG
```

---

## Giải Pháp Đã Apply

### 1. Tạo File `logback.xml`

**Client:** `client/src/main/resources/logback.xml`  
**Server:** `server/src/main/resources/logback.xml`

Cả 2 file đều config:
- ✅ Charset: UTF-8
- ✅ Pattern layout đơn giản
- ✅ Level: INFO

---

## Cách Sử Dụng

### Option 1: Rebuild và Chạy (Khuyên dùng)

```powershell
# 1. Rebuild client
cd client
mvn clean package -DskipTests

# 2. Rebuild server
cd ../server
mvn clean install -DskipTests

# 3. Set console encoding trước khi chạy
chcp 65001

# 4. Chạy server
mvn spring-boot:run

# Terminal mới, cũng set encoding
chcp 65001

# 5. Chạy client
cd ../client
java -jar target/client-1.0.jar --server.url=http://192.168.1.196:8080
```

---

### Option 2: Chỉ Set Console Encoding

Nếu không muốn rebuild, chỉ cần:

```powershell
# Trước khi chạy bất kỳ lệnh nào:
chcp 65001

# UTF-8 sẽ áp dụng cho session hiện tại
```

---

## Kiểm Tra

Sau khi chạy, log sẽ hiển thị:

```
✅ TRƯỚC (SAI):
─É├ú kß║┐t nß╗æi WebSocket ─æß║┐n server

✅ SAU (ĐÚNG):
Đã kết nối WebSocket đến server
```

---

## Lưu Ý

- **Phải rebuild** để file `logback.xml` được đóng gói vào JAR
- **Chỉ cần `chcp 65001` một lần** mỗi khi mở terminal mới
- **Có thể thêm vào script** để tự động set encoding

---

## Script Tự Động (Optional)

Tạo file `run-client.bat`:
```batch
@echo off
chcp 65001
cd client
java -jar target/client-1.0.jar --server.url=http://192.168.1.196:8080
```

Tạo file `run-server.bat`:
```batch
@echo off
chcp 65001
cd server
mvn spring-boot:run
```

Chạy: Double-click file `.bat` để tự động set encoding và start.
