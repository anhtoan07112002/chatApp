package com.chat.config.websocket;

import java.util.Map;
import java.util.UUID;

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
        
        if (userIdStr != null) {
            User user = userRepository.findById(UUID.fromString(userIdStr));
            if (user != null) {
                attributes.put("userId", UserId.fromString(userIdStr));
                return true;
            }
        }
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
            return query.split("userId=")[1].split("&")[0];
        }
        
        // Hoặc từ header
        List<String> userIdHeader = request.getHeaders().get("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            return userIdHeader.get(0);
        }
        return null;
    }
}