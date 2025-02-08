package com.keqing.webterminal.websocket.service.subprotocol;

import com.keqing.webterminal.websocket.service.WebSocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.socket.*;
import org.springframework.web.socket.messaging.SubProtocolHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author keqing
 */
@Slf4j
public class TTyProtocolHandler implements SubProtocolHandler {

    /**
     * 保存websocket连接map 保存websocketClient和websocket session的对应关系
     */
    private static final Map<String, WebSocketClient> WEB_SOCKET_CLIENT_MAP = new HashMap<>();

    @Override
    public List<String> getSupportedProtocols() {
        return Collections.singletonList("tty");
    }

    @Override
    public void handleMessageFromClient(WebSocketSession session, WebSocketMessage<?> message, MessageChannel outputChannel) throws Exception {
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
            // 将从客户端接收到的数据发送给TTyd
            client.send((ByteBuffer) message.getPayload());

            ByteBuffer byteMsg = (ByteBuffer) message.getPayload();

            //进行解码后进行log采集和上云
            String decodeSendMsg = new String(byteMsg.array(), 1, byteMsg.array().length - 1, StandardCharsets.UTF_8);
            log.info("send message to TTyd: {}", decodeSendMsg);

            // Register a callback to forward the response back to this server's client
            client.registerOnBinaryMessageCallback((responseMsg) -> {
                try {
                    // 接收TTyd的返回结果，返回客户端
                    // Relay the response from the other server back to this server's client
                    session.sendMessage(new BinaryMessage(responseMsg));

                    //进行解码后进行log采集和上云
                    String decodeRecMsg = new String(responseMsg, 1, responseMsg.length - 1, StandardCharsets.UTF_8);
                    log.info("received message to TTyd: {}", decodeRecMsg);
                } catch (IOException e) {
                    log.error("Failed to send message to client", e);
                }
            });
        } else if (message.getPayload() instanceof PingMessage) {
            log.info("webShell received ping: {}", message.getPayload());
        }
    }

    @Override
    public void handleMessageToClient(WebSocketSession session, Message<?> message) throws Exception {
    }

    @Override
    public String resolveSessionId(Message<?> message) {
        return UUID.randomUUID().toString();
    }

    @Override
    public void afterSessionStarted(WebSocketSession session, MessageChannel outputChannel) throws Exception {
        WebSocketClient client = new WebSocketClient();
        client.initConnection("ws://localhost:7777/ws");
        WEB_SOCKET_CLIENT_MAP.put(session.getId(), client);
        log.info("connection established");
    }

    @Override
    public void afterSessionEnded(WebSocketSession session, CloseStatus closeStatus, MessageChannel outputChannel) throws Exception {
        WebSocketClient client = WEB_SOCKET_CLIENT_MAP.get(session.getId());
        client.getWebSocket().sendClose(closeStatus.getCode(), "aa");
        log.info("webShell closed status code: {}, reason:{}", closeStatus.getCode(), closeStatus.getReason());
    }
}
