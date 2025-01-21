package com.chat.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.chat.application.messageUsecase.MessageDeliveryCoordinator;
import com.chat.domain.entity.user.UserId;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.util.Map;
import java.util.Objects;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketSessionManager sessionManager;
    @Lazy private final MessageDeliveryCoordinator deliveryCoordinator;
    private final WebSocketHandshakeInterceptor WebsocketHandshakeInterceptor;


    public WebSocketConfig(WebSocketSessionManager sessionManager, MessageDeliveryCoordinator messageDeliveryCoordinator, WebSocketHandshakeInterceptor Web) {
        this.sessionManager = sessionManager;
        this.deliveryCoordinator = messageDeliveryCoordinator;
        this.WebsocketHandshakeInterceptor = Web;   
    }

    @Override
    public void registerStompEndpoints(@SuppressWarnings("null") StompEndpointRegistry registry) {
        log.info("Registering STOMP endpoints");
//        registry.addEndpoint("/ws")
//                .setAllowedOrigins("*")
//                .addInterceptors(WebsocketHandshakeInterceptor)
//                Thêm endpoint không sử dụng SockJS để test với Postman
//                .setHandshakeHandler(new DefaultHandshakeHandler());


        registry.addEndpoint("/ws")
            //    .setAllowedOrigins("http://localhost:8080", "http://127.0.0.1:8080", "http://localhost:63342")
                .setAllowedOriginPatterns("*")
//               .setAllowedOrigins("*")
               .addInterceptors(WebsocketHandshakeInterceptor)  // Add interceptor
               .withSockJS()
               .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js")
               .setWebSocketEnabled(true)
               .setSessionCookieNeeded(false);
            //    .setSupportsJsonp(false);
        log.info("Registering STOMP endpoints");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");
//                .setHeartbeatValue(new long[]{1000, 1000});
        registry.setUserDestinationPrefix("/user");
    }
    
    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes == null) {
            log.error("Session attributes are null!");
            return;
        }

        Object userIdObj = sessionAttributes.get("userId");
        if (userIdObj == null || !(userIdObj instanceof UserId)) {
            log.error("Invalid or missing userId in session attributes: {}", userIdObj);
            return;
        }

        UserId userId = (UserId) userIdObj;
        log.info("User connected - Session ID: {}, User ID: {}", sessionId, userId);

        sessionManager.addSession(userId, sessionId);
        deliveryCoordinator.deliverPendingMessage(userId);
//        UserId userId = (UserId) Objects.requireNonNull(accessor.getSessionAttributes()).get("userId");
//
//        log.info("User connected - Session ID: {}, User ID: {}", sessionId, userId);
//
//        if (userId != null && sessionId != null) {
//            log.info("User connected - Session ID: {}, User ID: {}", sessionId, userId);
//            sessionManager.addSession(userId, sessionId);
//            // Attempt to deliver pending messages when user connects
//            deliveryCoordinator.deliverPendingMessage(userId);
//        } else {
//            log.warn("Connected event received but userId or sessionId is null");
//        }
    }

    // @EventListener
    // public void handleWebSocketConnect(SessionConnectedEvent event) {
    //     WebSocketSession session = event.getMessage().getHeaders().get("simpSessionId", WebSocketSession.class);
    //     UserId userId = (UserId) session.getAttributes().get("userId"); // Giả sử bạn đã lưu userId trong session attributes
    //     sessionManager.addSession(userId, session);

    //     if (userId != null) {
    //         sessionManager.addSession(userId, session);
    //         messageQueueService.sendPendingMessages(userId);
    //     }
    // }

    // @EventListener
    // public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
    //     WebSocketSession session = event.getMessage().getHeaders().get("simpSessionId", WebSocketSession.class);
    //     UserId userId = (UserId) session.getAttributes().get("userId");
    //     if (userId != null) {
    //         sessionManager.removeSession(userId);
    //     }
    // }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        UserId userId = (UserId) Objects.requireNonNull(accessor.getSessionAttributes()).get("userId");

        if (userId != null) {
            log.info("User disconnected - Session ID: {}, User ID: {}", sessionId, userId);
            sessionManager.removeSession(userId);
        }
    }
}

