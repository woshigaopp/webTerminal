package com.keqing.webterminal.service;

import lombok.Data;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * @author keqing@date 2025/02/05
 */
@Data
public class WebSocketClient {

    private final HttpClient client;

    private String url;

    private WebSocket webSocket;

    /**
     * 初始化webSocket client 创建一个http Client用于创建websocket client
     */
    public WebSocketClient() {
        this.client = HttpClient.newHttpClient();
    }

    /**
     * 连接TTyd
     * @param url TTyd 的启动地址
     */
    public void initConnection(String url) {
        this.url = url;
        this.webSocket = this.client.newWebSocketBuilder()
                // TTyd自定义的子协议
                .subprotocols("tty")
                .buildAsync(URI.create(url), new WebSocket.Listener() {

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        if (onMessageCallback != null) {
                            onMessageCallback.accept(data.toString());
                        }
                        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "ok").thenRun(() -> System.out.println("Sent close"));
                        return WebSocket.Listener.super.onText(webSocket, data, last);
                    }

                    @Override
                    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
                        // 此处处理二进制数据
                        // ByteBuffer -> byte[]

                        // 如果你有一个处理二进制数据的回调函数，例如 onBinaryMessageCallback
                        // 那么你可以在这里调用该函数
                        // onBinaryMessageCallback.accept(byteArray);

                        byte[] byteArray = new byte[data.remaining()];
                        data.get(byteArray);

                        if (onBinaryMessageCallback != null) {
                            onBinaryMessageCallback.accept(byteArray);
                        }

                        return WebSocket.Listener.super.onBinary(webSocket, data, last);
                    }
                }).join();
    }

    /**
     * 发送消息
     * @param message 发送消息到TTyd
     */
    public void send(ByteBuffer message) {
        if (this.webSocket == null) {
            throw new NullPointerException("connection not established");
        }

        this.webSocket.sendBinary(message, true);
    }

    private Consumer<String> onMessageCallback;

    private Consumer<byte[]> onBinaryMessageCallback;

    /**
     * 接受到TTyd的消息后，定义的回调接口
     * @param callback 回调接口
     */
    public void registerOnBinaryMessageCallback(Consumer<byte[]> callback) {
        this.onBinaryMessageCallback = callback;
    }
}


