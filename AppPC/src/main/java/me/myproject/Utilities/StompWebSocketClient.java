package me.myproject.Utilities;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class StompWebSocketClient implements Listener {
    private static final String END_OF_FRAME = "\u0000";
    private final Consumer<String> messageHandler;
    private final String destination;
    private WebSocket webSocket;
    private final StringBuilder buffer = new StringBuilder();

    public StompWebSocketClient(String destination, Consumer<String> messageHandler) {
        this.destination = destination;
        this.messageHandler = messageHandler;
    }

    public CompletableFuture<Void> connect(String wsUrl) {
        HttpClient client = HttpClient.newHttpClient();
        return client.newWebSocketBuilder()
                .buildAsync(URI.create(wsUrl), this)
                .thenAccept(socket -> this.webSocket = socket)
                .exceptionally(ex -> {
                    if (messageHandler != null) {
                        messageHandler.accept("ERROR: Không kết nối được WebSocket: " + ex.getMessage());
                    }
                    return null;
                });
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
        }
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("[WS OPEN] Connected to " + destination);
        String connectFrame = "CONNECT\naccept-version:1.2\nhost:localhost\n\n" + END_OF_FRAME;
        webSocket.sendText(connectFrame, true);
        String subscribeFrame = "SUBSCRIBE\nid:sub-0\ndestination:" + destination + "\n\n" + END_OF_FRAME;
        webSocket.sendText(subscribeFrame, true);
        Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        buffer.append(data);
        if (last) {
            String payload = buffer.toString();
            buffer.setLength(0);
            
            // Split theo ký tự kết thúc frame của STOMP
            String[] frames = payload.split(END_OF_FRAME);
            for (String frame : frames) {
                String trimmedFrame = frame.trim(); // Loại bỏ các ký tự xuống dòng thừa ở đầu/cuối
                if (trimmedFrame.contains("MESSAGE")) { // Dùng contains thay vì startsWith cho an toàn
                    String body = extractBody(trimmedFrame);
                    if (body != null) {
                        String msg = body.trim();
                        // LOG QUAN TRỌNG: Kiểm tra xem callback có thực sự được gọi không
                        System.out.println("[WS DEBUG] Chuan bi day sang View: " + msg);
                        
                        if (messageHandler != null) {
                            messageHandler.accept(msg); 
                        } else {
                            System.out.println("[WS ERROR] messageHandler dang bi NULL!");
                        }
                    }
                }
            }
        }
        return Listener.super.onText(webSocket, data, last);
    }

    private String extractBody(String frame) {
        // STOMP frame body bắt đầu sau hai dấu xuống dòng liên tiếp
        // Thử cả \n\n (Linux/Mac) và \r\n\r\n (Windows)
        int index = frame.indexOf("\n\n");
        if (index < 0) index = frame.indexOf("\r\n\r\n");
        
        if (index < 0) return null;
        return frame.substring(index).trim(); // Lấy từ vị trí đó đến hết
    }

    
   

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        if (messageHandler != null) {
            messageHandler.accept("ERROR:" + Objects.toString(error.getMessage(), "unknown"));
        }
        Listener.super.onError(webSocket, error);
    }
}
