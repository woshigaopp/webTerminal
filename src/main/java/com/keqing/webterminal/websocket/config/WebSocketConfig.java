package com.keqing.webterminal.websocket.config;


import com.keqing.webterminal.websocket.service.WebShellSocketHandler;
import com.keqing.webterminal.websocket.service.subprotocol.SampleProtocol;
import com.keqing.webterminal.websocket.service.subprotocol.TTyProtocolHandler;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;


/**
 * @author keqing@date 2025/02/05
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    /**
     * 注册websocket handler
     */
    @Resource
    private WebShellSocketHandler webSocketHandler;

    /**
     * @param webSocketHandlerRegistry websocketHandler 注册器
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        //指定处理器和路径
//        webSocketHandlerRegistry.addHandler(webSocketHandler, "/proxy/ws")
//                .addInterceptors(new WsHandshakeInterceptor())
//                .setAllowedOrigins("*");

        ExecutorSubscribableChannel clientInboundChannel = new ExecutorSubscribableChannel();
        ExecutorSubscribableChannel clientOutboundChannel = new ExecutorSubscribableChannel();
        SubProtocolWebSocketHandler subProtocolWebSocketHandler = new SubProtocolWebSocketHandler(clientInboundChannel, clientOutboundChannel);

        subProtocolWebSocketHandler.addProtocolHandler(new SampleProtocol());
        webSocketHandlerRegistry.addHandler(subProtocolWebSocketHandler, "/test")
                .setAllowedOrigins("*");
    }
}
