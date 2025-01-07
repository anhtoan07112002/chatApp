package com.chat.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.chat.domain.entity.user.UserId;
import com.chat.domain.service.messageservice.IMessageQueueService;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketSessionManager sessionManager;
    private final IMessageQueueService messageQueueService; // Thêm service để gửi lại tin nhắn


    public WebSocketConfig(WebSocketSessionManager sessionManager, IMessageQueueService messageQueueService) {
        this.sessionManager = sessionManager;
        this.messageQueueService = messageQueueService;
    }

    @Override
    public void registerStompEndpoints(@SuppressWarnings("null") StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
               .setAllowedOrigins("*")
               .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        WebSocketSession session = event.getMessage().getHeaders().get("simpSessionId", WebSocketSession.class);
        UserId userId = (UserId) session.getAttributes().get("userId"); // Giả sử bạn đã lưu userId trong session attributes
        sessionManager.addSession(userId, session);

        messageQueueService.sendPendingMessages(userId);
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        String userId = (String) event.getSessionId(); // Lấy userId từ sessionId
        sessionManager.removeSession(userId);
    }
}

