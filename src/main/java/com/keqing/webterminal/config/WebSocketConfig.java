package com.keqing.webterminal.config;


import com.keqing.webterminal.service.WebShellSocketHandler;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


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
        webSocketHandlerRegistry.addHandler(webSocketHandler, "/webssh")
                .setAllowedOrigins("*");
    }
}
