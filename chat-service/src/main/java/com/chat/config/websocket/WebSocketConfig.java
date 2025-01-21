package com.chat.config.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.chat.application.messageUsecase.MessageDeliveryCoordinator;
import com.chat.domain.entity.user.UserId;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketSessionManager sessionManager;
    @Lazy private final MessageDeliveryCoordinator deliveryCoordinator;
    private final WebSocketHandshakeInterceptor websocketHandshakeInterceptor;

    public WebSocketConfig(WebSocketSessionManager sessionManager,
                           MessageDeliveryCoordinator messageDeliveryCoordinator,
                           WebSocketHandshakeInterceptor websocketHandshakeInterceptor) {
        this.sessionManager = sessionManager;
        this.deliveryCoordinator = messageDeliveryCoordinator;
        this.websocketHandshakeInterceptor = websocketHandshakeInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("Registering STOMP endpoints");

        DefaultHandshakeHandler handshakeHandler = new DefaultHandshakeHandler() {
            @Override
            protected Principal determineUser(ServerHttpRequest request,
                                              WebSocketHandler wsHandler,
                                              Map<String, Object> attributes) {
                Object userIdObj = attributes.get("userId");
                if (userIdObj instanceof UserId) {
                    return () -> ((UserId) userIdObj).asString();
                }
                return null;
            }
        };

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(websocketHandshakeInterceptor)
                .setHandshakeHandler(handshakeHandler)
                .withSockJS()
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js")
                .setWebSocketEnabled(true)
                .setSessionCookieNeeded(false);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(taskScheduler());

        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("websocket-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        try {
            Principal principal = accessor.getUser();
            if (principal == null) {
                log.error("No Principal found for session: {}", sessionId);
                return;
            }

            UserId userId = UserId.fromString(principal.getName());
            log.info("User connected - Session ID: {}, User ID: {}", sessionId, userId);

            sessionManager.addSession(userId, sessionId);
            try {
                deliveryCoordinator.deliverPendingMessage(userId);
            } catch (Exception e) {
                log.error("Failed to deliver pending messages for userId: {}", userId, e);
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket connection for sessionId: {}", sessionId, e);
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal principal = accessor.getUser();

        if (principal != null) {
            UserId userId = UserId.fromString(principal.getName());
            log.info("User disconnected - Session ID: {}, User ID: {}", sessionId, userId);
            sessionManager.removeSession(userId);
        } else {
            log.warn("Disconnect event received but no Principal found for session: {}", sessionId);
        }
    }
}