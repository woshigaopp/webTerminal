package com.keqing.webterminal.websocket.service.subprotocol;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SubProtocolHandler;

import java.util.List;
import java.util.UUID;


/**
 * @author keqing
 */
@Slf4j
public class SampleProtocol implements SubProtocolHandler {

    @Override
    public List<String> getSupportedProtocols() {
        return List.of("sample");
    }

    @Override
    public void handleMessageFromClient(WebSocketSession session, WebSocketMessage<?> message, MessageChannel outputChannel) throws Exception {
        log.info("handleMessageFromClient receive from client:{}", session.getId());

        String payload = message.getPayload().toString();
        Message message1 = MessageBuilder.withPayload(payload + "test").build();
        outputChannel.send(message1);
    }

    @Override
    public void handleMessageToClient(WebSocketSession session, Message<?> message) throws Exception {
        log.info("handleMessageToClient receive to client:{}", session.getId());
        session.sendMessage(new TextMessage(message.getPayload().toString()));
    }

    @Override
    public String resolveSessionId(Message<?> message) {
        String id = UUID.randomUUID().toString();
        log.info("resolveSessionId id {}", id);
        return id;
    }

    @Override
    public void afterSessionStarted(WebSocketSession session, MessageChannel outputChannel) throws Exception {
        log.info("afterSessionStarted receive from client:{}", session.getId());
    }

    @Override
    public void afterSessionEnded(WebSocketSession session, CloseStatus closeStatus, MessageChannel outputChannel) throws Exception {
        log.info("afterSessionEnded receive from client:{}", session.getId());
    }
}
