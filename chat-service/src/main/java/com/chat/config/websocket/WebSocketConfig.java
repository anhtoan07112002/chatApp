package com.chat.config.websocket;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
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
    private final WebSocketHandshakeInterceptor websocketHandshakeInterceptor;
    private final ObjectProvider<MessageDeliveryCoordinator> deliveryCoordinatorProvider;

    public WebSocketConfig(
            WebSocketSessionManager sessionManager,
            WebSocketHandshakeInterceptor websocketHandshakeInterceptor,
            ObjectProvider<MessageDeliveryCoordinator> deliveryCoordinatorProvider) {
        this.sessionManager = sessionManager;
        this.websocketHandshakeInterceptor = websocketHandshakeInterceptor;
        this.deliveryCoordinatorProvider = deliveryCoordinatorProvider;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue", "/topic");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/ws")
//                .setAllowedOriginPatterns("*")
//                .addInterceptors(websocketHandshakeInterceptor)
//                .setHandshakeHandler(new DefaultHandshakeHandler())
//                .withSockJS();
//    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("Registering STOMP endpoints");

        DefaultHandshakeHandler handshakeHandler = new DefaultHandshakeHandler() {
            @Override
            protected Principal determineUser(ServerHttpRequest request,
                                              WebSocketHandler wsHandler,
                                              Map<String, Object> attributes) {
                // Lấy userIdString từ attributes được set bởi interceptor
                Object userIdStr = attributes.get("userIdString");
                if (userIdStr instanceof String) {
                    log.debug("Creating StompPrincipal for user: {}", userIdStr);
                    return new StompPrincipal((String) userIdStr);
                }
                log.warn("No userIdString found in attributes");
                return null;
            }
        };

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(websocketHandshakeInterceptor)
                .setHandshakeHandler(handshakeHandler)
                .withSockJS();
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

            String userIdStr = principal.getName();
            log.debug("User connected - Session ID: {}, User ID: {}", sessionId, userIdStr);

            UserId userId = UserId.fromString(userIdStr);
            sessionManager.addSession(userId, sessionId);

            // Gửi tin nhắn pending
            MessageDeliveryCoordinator coordinator = deliveryCoordinatorProvider.getIfAvailable();
            if (coordinator != null) {
                try {
                    coordinator.deliverPendingMessages(userId);
                } catch (Exception e) {
                    log.error("Failed to deliver pending messages for userId: {}", userId, e);
                }
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

    private static class StompPrincipal implements Principal {
        private final String name;

        public StompPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}