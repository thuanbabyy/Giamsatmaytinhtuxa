## Fix Nhanh - Hiển Thị Processes

Thay vì dùng WebSocket command phức tạp, mình đã thay đổi để **gửi processes cùng metrics** (trong heartbeat).

### Client Changes
- `SystemMonitor.java`: Top 10 processes được gửi kèm metrics mỗi 5 giây

### Server Changes  
- `MetricService.java`: Cần thêm lưu topProcesses vào database (hoặc cache)

### Frontend Changes
File `server.html` - thay đổi hàm `refreshProcesses()`:

```javascript
async function refreshProcesses() {
    const content = document.getElementById('processesContent');
    content.innerHTML = '<div class="loading">Đang tải...</div>';
    
    try {
        // Lấy từ metrics thay vì gọi command
        const response = await fetch(`/api/machines/${currentMachineId}/metrics/latest`);
        const metric = await response.json();
        
        if (metric && metric.extraData) {
            const data = JSON.parse(metric.extraData);
            if (data.topProcesses) {
                const processes = data.topProcesses;
                let html = '<table style="width:100%; border-collapse: collapse;">';
                html += '<tr style="background: #f0f0f0; font-weight: bold;">';
                html += '<th style="padding:8px; border:1px solid #ddd;">PID</th>';
                html += '<th style="padding:8px; border:1px solid #ddd;">Tên</th>';
                html += '<th style="padding:8px; border:1px solid #ddd;">CPU %</th>';
                html += '<th style="padding:8px; border:1px solid #ddd;">RAM (MB)</th>';
                html += '</tr>';
                
                processes.forEach(proc => {
                    html += '<tr>';
                    html += `<td style="padding:8px; border:1px solid #ddd;">${proc.pid || 'N/A'}</td>`;
                    html += `<td style="padding:8px; border:1px solid #ddd;">${proc.name || 'N/A'}</td>`;
                    html += `<td style="padding:8px; border:1px solid #ddd;">${(proc.cpuUsage || 0).toFixed(2)}%</td>`;
                    html += `<td style="padding:8px; border:1px solid #ddd;">${((proc.memoryUsage || 0) / 1024 / 1024).toFixed(1)}</td>`;
                    html += '</tr>';
                });
                
                html += '</table>';
                content.innerHTML = html;
            } else {
                content.innerHTML = '<div>Chưa có dữ liệu processes</div>';
            }
        } else {
            content.innerHTML = '<div>Không tìm thấy metrics</div>';
        }
    } catch (error) {
        content.innerHTML = '<div style="color: red;">Lỗi: ' + error.message + '</div>';
    }
}
```

### Build & Run
```bash
# Build client
cd client
mvn clean package -DskipTests

# Chạy client
java -jar target/client-1.0.jar --server.url=http://192.168.0.107:8080
```

Không cần restart server, chỉ cần sửa frontend!
