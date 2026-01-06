package com.monitor.client.test;

import com.monitor.client.command.CommandHandler;
import com.monitor.client.websocket.ClientWebSocket;

import java.net.URI;

public class ClientWebSocketTester {
    public static void main(String[] args) throws Exception {
        String serverUrl = "http://localhost:8080";
        String machineId = "MACHINE-Lilmon-Lilmon1767681678185";

        CommandHandler handler = new CommandHandler(serverUrl, machineId);
        ClientWebSocket client = new ClientWebSocket(new URI("ws://localhost:8080/ws"), machineId, handler);

        // Sample message from your logs (data as String)
        String sample = "{\"machineId\":\"MACHINE-Lilmon-Lilmon1767681678185\",\"data\":\"{\\\"title\\\":\\\"1\\\",\\\"message\\\":\\\"a\\\",\\\"type\\\":\\\"INFO\\\"}\",\"notificationId\":4,\"title\":\"1\",\"message\":\"a\",\"type\":\"INFO\",\"commandId\":20,\"command\":\"NOTIFICATION\",\"timestamp\":1767681702579}";

        System.out.println("Invoking onMessage with sample payload...");
        client.onMessage(sample);

        // Vì client chưa kết nối WebSocket trong test này, việc gửi response sẽ ném WebsocketNotConnectedException.
        // Để kiểm tra logic parsing và việc hiển thị thông báo, gọi trực tiếp CommandHandler.
        com.google.gson.Gson gson = new com.google.gson.Gson();
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> parsed = (java.util.Map<String, Object>) gson.fromJson(sample, java.util.Map.class);
        String cmd = (String) parsed.get("command");
        Object dataObj = parsed.get("data");
        java.util.Map<String, Object> data = null;
        if (dataObj instanceof String) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> tmp = (java.util.Map<String, Object>) gson.fromJson((String) dataObj, java.util.Map.class);
            data = tmp;
        } else if (dataObj instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> tmp = (java.util.Map<String, Object>) dataObj;
            data = tmp;
        }

        System.out.println("Calling handler directly to verify behavior...");
        java.util.Map<String, Object> result = handler.handleCommand(cmd, data);
        System.out.println("Direct handler result: " + result);

        System.out.println("Message delivered to handler. Waiting 2s for popup (if any)...");
        Thread.sleep(2000);

        System.out.println("Done.");
    }
}
