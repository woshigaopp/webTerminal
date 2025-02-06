package com.keqing.webterminal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author keqing@date 2025/02/05
 */
@Slf4j
@Component
public class WebShellSocketHandler implements WebSocketHandler {

    /**
     * 保存websocket连接map 保存websocketClient和websocket session的对应关系
     */
    private static final Map<String, WebSocketClient> WEB_SOCKET_CLIENT_MAP = new HashMap<>();

    /**
     * @param session websocket session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        WebSocketClient client = new WebSocketClient();
        client.initConnection("ws://localhost:7777/ws");
        WEB_SOCKET_CLIENT_MAP.put(session.getId(), client);
        log.info("connection established");
    }

    /**
     * @param session websocket session
     * @param message websocket message
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        if (message instanceof TextMessage) {
            try {
                log.info("webShell received text: {}", message.getPayload());
                if (!WEB_SOCKET_CLIENT_MAP.containsKey(session.getId())) {
                    log.error("webShell handleTextMessage error webClient not established");
                    throw new RuntimeException("webShell handleMessage error webClient not established");
                }
                WebSocketClient client = WEB_SOCKET_CLIENT_MAP.get(session.getId());
                // 获取文本消息
                String payload = (String) message.getPayload();
                if (payload.startsWith("{")) {
                    client.send(ByteBuffer.wrap(payload.getBytes(StandardCharsets.UTF_8)));
                } else {
                    // 转换为二进制数据
                    byte[] binaryData = payload.getBytes(StandardCharsets.UTF_8);
                    // 添加前缀
                    byte[] prefixedBinaryData = new byte[binaryData.length + 1];
                    // ASCII码的"0"字符
                    prefixedBinaryData[0] = '0';
                    System.arraycopy(binaryData, 0, prefixedBinaryData, 1, binaryData.length);

                    // Connect to the other server and send the received message
                    client.send(ByteBuffer.wrap(prefixedBinaryData));
                }

                // Register a callback to forward the response back to this server's client
                client.registerOnBinaryMessageCallback((responseMsg) -> {
                    try {
                        if (responseMsg[0] == '0') {
                            String msg = new String(responseMsg, 1, responseMsg.length - 1, StandardCharsets.UTF_8);
                            session.sendMessage(new TextMessage(msg));
                            return;
                        }
                        String msg = new String(responseMsg, StandardCharsets.UTF_8);
                        // Relay the response from the other server back to this server's client
                        session.sendMessage(new TextMessage(msg));
                    } catch (IOException e) {
                        log.error("Failed to send message to client", e);
                    }
                });
            } catch (Exception e) {
                log.error("webShell received error", e);
            }
        } else if (message instanceof BinaryMessage) {
            log.info("webShell received BinaryMessage: {}", message.getPayload());

            if (!WEB_SOCKET_CLIENT_MAP.containsKey(session.getId())) {
                log.error("webShell handleBinaryMessage error webClient not established");
                throw new RuntimeException("webShell handleMessage error webClient not established");
            }
            WebSocketClient client = WEB_SOCKET_CLIENT_MAP.get(session.getId());
            // Connect to the other server and send the received message
            client.send((ByteBuffer) message.getPayload());

            // Register a callback to forward the response back to this server's client
            client.registerOnBinaryMessageCallback((responseMsg) -> {
                try {
                    // Relay the response from the other server back to this server's client
                    session.sendMessage(new BinaryMessage(responseMsg));
                } catch (IOException e) {
                    log.error("Failed to send message to client", e);
                }
            });
        } else if (message.getPayload() instanceof PingMessage) {
            log.info("webShell received ping: {}", message.getPayload());
        }
    }

    /**
     * @param session websocket session
     * @param exception 捕获的异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("webShell handleTransportError error", exception);
    }

    /**
     * @param session websocket session
     * @param closeStatus websocket 连接关闭的错误码
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        log.info("webShell closed");
    }

    /**
     * @return boolean
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
