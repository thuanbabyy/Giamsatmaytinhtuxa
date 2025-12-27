# Hướng Dẫn Chạy Dự Án

## Yêu Cầu
- Java 11 hoặc cao hơn
- Maven 3.6+
- SQL Server (đã cài đặt và chạy)

## Bước 1: Cấu Hình Database

1. Tạo database trong SQL Server:
```sql
CREATE DATABASE Giamsatmaytinhtuxa;
```

2. Chạy script tạo bảng:
   - Mở file: `server/src/main/resources/database/schema.sql`
   - Chạy script trong SQL Server Management Studio

3. Cấu hình kết nối database:
   - Mở file: `server/src/main/resources/application.yml`
   - Sửa thông tin kết nối:
     ```yaml
     spring:
       datasource:
         url: jdbc:sqlserver://localhost:1433;databaseName=Giamsatmaytinhtuxa;encrypt=true;trustServerCertificate=true;
         username: sa          # Thay bằng username của bạn
         password: 1            # Thay bằng password của bạn
     ```

## Bước 2: Chạy Server

### Cách 1: Dùng Maven (Khuyến nghị)
```bash
cd server
mvn clean package
mvn spring-boot:run
```

### Cách 2: Chạy JAR file
```bash
cd server
mvn clean package
java -jar target/server-1.0.jar
```

**Kiểm tra:** Mở trình duyệt truy cập: `http://localhost:8080/server`

## Bước 3: Chạy Client

### Lấy IP của Server
- **Windows:** Mở Command Prompt, gõ `ipconfig`, tìm IPv4 Address
- Ví dụ: `192.168.1.100`

### Build và Chạy Client
```bash
cd client
mvn clean package
java -jar target/client-1.0.jar --server.url=http://IP_SERVER:8080
```

**Ví dụ:**
```bash
java -jar target/client-1.0.jar --server.url=http://192.168.1.100:8080
```

## Lưu Ý

- Server phải chạy trước khi chạy client
- Đảm bảo firewall cho phép port 8080
- Client chỉ chạy được trên Windows
- Nếu lỗi kết nối, kiểm tra IP và port của server

## Truy Cập Giao Diện Web

Sau khi server chạy, mở trình duyệt:
```
http://IP_SERVER:8080/server
```

Ví dụ: `http://192.168.1.100:8080/server`

