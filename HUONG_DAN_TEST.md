# Hướng Dẫn Test & Chạy Các Tính Năng Mới

## 🚀 Bước 1: Build và Chạy Server

```powershell
# Dừng tất cả server cũ
Get-Process java | Stop-Process -Force

# Vào thư mục server
cd "e:\Lập trình mạng\Giamsatmaytinhtuxa\Giamsatmaytinhtuxa\server"

# Chạy server (Maven sẽ tự động compile)
mvn spring-boot:run
```

**Đợi đến khi thấy log:**
```
Started MonitorServerApplication in X.XXX seconds
```

---

## 🚀 Bước 2: Chạy Client (Optional)

```powershell
cd "e:\Lập trình mạng\Giamsatmaytinhtuxa\Giamsatmaytinhtuxa\client"
java -jar target/client-1.0.jar --server.url=http://192.168.0.107:8080
```

---

## 🧪 Bước 3: Test Các Tính Năng

### Test 1: Kiểm Tra Giao Diện Mới

1. Mở browser: `http://192.168.0.107:8080/server`

2. **Kiểm tra nút mới:**
   - ✅ Mỗi máy PHẢI CÓ nút **"🗑️ Xóa"** màu đỏ
   
3. **Click "🖼️ Màn Hình"** trên một máy:
   - ✅ CHỈ CÓ 1 nút: **"Chụp Màn Hình"**
   - ❌ KHÔNG CÓ nút "Bắt Đầu Quan Sát"
   - ❌ KHÔNG CÓ nút "Dừng Quan Sát"

---

### Test 2: Test Xóa Máy

**Chuẩn bị:**
- Tìm một máy OFFLINE (để không ảnh hưởng client đang chạy)
- Hoặc tạo máy test bằng cách chạy client rồi tắt đi

**Thực hiện:**

1. Click nút **"🗑️ Xóa"** trên máy muốn xóa

2. **Kiểm tra confirm dialog:**
   ```
   Bạn có chắc muốn xóa máy này?
   
   ID: MACHINE-xxx
   
   Tất cả dữ liệu (metrics, commands, ảnh màn hình) 
   sẽ bị XÓA VĨNH VIỄN!
   ```

3. Click **OK**

4. **Expected:**
   - Alert: "Đã xóa máy thành công!"
   - Máy biến mất khỏi danh sách

**Kiểm tra Database:**
```sql
-- Vào SQL Server Management Studio
USE Giamsatmaytinhtuxa;

-- Kiểm tra máy đã bị xóa chưa
SELECT * FROM machines WHERE machine_id = 'MACHINE-xxx-đã-xóa';
-- Kết quả: 0 rows

-- Kiểm tra metrics đã bị xóa chưa
SELECT * FROM metrics WHERE machine_id = 'MACHINE-xxx-đã-xóa';
-- Kết quả: 0 rows

-- Kiểm tra commands đã bị xóa chưa
SELECT * FROM commands WHERE machine_id = 'MACHINE-xxx-đã-xóa';
-- Kết quả: 0 rows
```

---

### Test 3: Test Chụp Màn Hình (Không Auto-Refresh)

1. Click **"🖼️ Màn Hình"** trên máy ONLINE

2. Click **"Chụp Màn Hình"**

3. Đợi 2-3 giây → Ảnh hiển thị

4. **Kiểm tra:**
   - ✅ Ảnh KHÔNG tự động refresh
   - ✅ Muốn chụp lại phải click "Chụp Màn Hình" lần nữa

---

### Test 4: Test API Trực Tiếp (PowerShell)

```powershell
# Test DELETE API
$machineId = "MACHINE-test-DELETE"

# Gọi API DELETE
Invoke-RestMethod -Uri "http://192.168.0.107:8080/api/machines/$machineId" -Method DELETE

# Expected output:
# success  : True
# message  : Đã xóa máy thành công
```

**Nếu máy không tồn tại:**
```
StatusCode        : 404
```

---

## ✅ Checklist Sau Khi Test

- [ ] Nút "🗑️ Xóa" hiển thị trên tất cả máy
- [ ] Confirm dialog hiện khi click xóa
- [ ] Máy biến mất sau khi xóa thành công
- [ ] Database đã xóa sạch (machines, metrics, commands, screen_data)
- [ ] Modal màn hình CHỈ CÓ 1 nút "Chụp Màn Hình"
- [ ] Không còn nút "Bắt Đầu Quan Sát" và "Dừng Quan Sát"
- [ ] Ảnh màn hình KHÔNG tự động refresh
- [ ] Các chức năng khác vẫn hoạt động (Thông báo, Processes)

---

## 🐛 Nếu Có Lỗi

### Lỗi: "Cannot delete or update a parent row"

**Nguyên nhân:** Database có foreign key constraints

**Giải pháp:** Đã được xử lý bằng cascade delete trong code. Nếu vẫn lỗi:

```sql
-- Kiểm tra foreign keys
SELECT 
    OBJECT_NAME(parent_object_id) AS TableName,
    name AS ForeignKeyName
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID('machines');

-- Nếu cần, tạm thời disable constraints (KHÔNG khuyến khích)
-- ALTER TABLE metrics NOCHECK CONSTRAINT ALL;
```

### Lỗi: Frontend không thấy thay đổi

**Giải pháp:**
```
1. Hard refresh: Ctrl + Shift + R
2. Clear cache: Ctrl + Shift + Delete
3. Restart browser
```

### Lỗi: Server không compile

```powershell
# Clean và build lại
cd server
mvn clean install -DskipTests
mvn spring-boot:run
```

---

## 📝 Ghi Chú

- **Xóa máy ONLINE**: Có thể xóa, nhưng client sẽ tự đăng ký lại khi gửi heartbeat tiếp theo
- **Không thể hoàn tác**: Khi đã xóa, dữ liệu mất vĩnh viễn
- **Performance**: Xóa máy có nhiều dữ liệu (>10k metrics) có thể mất vài giây

---

**Tác giả:** AI Assistant  
**Ngày tạo:** 2026-01-07
