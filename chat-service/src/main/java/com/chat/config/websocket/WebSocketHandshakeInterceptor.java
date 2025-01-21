package com.chat.config.websocket;

import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.repository.userReponsitory.IUserRepository;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    private final IUserRepository userRepository;

    public WebSocketHandshakeInterceptor(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // Extract token from request headers or parameters
        String userIdStr = extractUserId(request);
        log.debug("Attempting handshake with userId: {}", userIdStr);
        if (userIdStr != null) {
            User user = userRepository.findById(UUID.fromString(userIdStr));
            if (user != null) {
                attributes.put("userId", UserId.fromString(userIdStr));
                log.debug("Successfully added userId to attributes: {}", userIdStr);
                return true;
            }
        }
        log.debug("Handshake failed for userId: {}", userIdStr);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        // No implementation needed
    }

    private String extractUserId(ServerHttpRequest request) {
        // Lấy userId từ query parameter
        String query = request.getURI().getQuery();
        if (query != null && query.contains("userId=")) {
            log.debug("Extracted userId: {}", query.split("userId=")[1].split("&")[0]);
            return query.split("userId=")[1].split("&")[0];
        }
        
        // Hoặc từ header
        List<String> userIdHeader = request.getHeaders().get("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            log.debug("Extracted userId: {}", userIdHeader.get(0));
            return userIdHeader.get(0);
        }

        return null;
    }
}