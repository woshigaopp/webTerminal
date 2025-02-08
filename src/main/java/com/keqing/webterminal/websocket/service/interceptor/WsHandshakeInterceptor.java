package com.keqing.webterminal.websocket.service.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

/**
 * @author keqing
 */
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    private List<String> subProtocols;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // tomcat ws实现不支持request中的header 中Sec-WebSocket-Protocol 返回，需要在握手前保存下来，握手结束后添加到response 的header中，否则客户端接收的返回中没有子协议按照websocket协议实现，客户端会主动断开连接
        subProtocols = request.getHeaders().get("Sec-WebSocket-Protocol");
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        response.getHeaders().addAll("Sec-WebSocket-Protocol", subProtocols);
    }
}