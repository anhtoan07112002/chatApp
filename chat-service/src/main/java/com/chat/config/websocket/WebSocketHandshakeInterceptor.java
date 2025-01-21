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
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        String userIdStr = extractUserId(request);
        log.debug("Attempting handshake with userId: {}", userIdStr);

        if (userIdStr == null) {
            log.error("No userId provided in request");
            return false;
        }

        try {
            UUID uuid = UUID.fromString(userIdStr);
            User user = userRepository.findById(uuid);

            if (user == null) {
                log.error("User not found for userId: {}", userIdStr);
                return false;
            }

            // Set both the UserId object and the string representation
            UserId userId = UserId.fromString(userIdStr);
            attributes.put("userId", userId);
            attributes.put("userIdString", userIdStr);

            // Store additional user information if needed
            attributes.put("userAuthenticated", true);

            log.debug("Successfully added userId to attributes: {}", userIdStr);
            return true;

        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for userId: {}", userIdStr);
            return false;
        } catch (Exception e) {
            log.error("Error during handshake for userId: {}", userIdStr, e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // Add any post-handshake logging if needed
        if (exception != null) {
            log.error("Exception occurred during handshake", exception);
        }
    }

    private String extractUserId(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (query != null && query.contains("userId=")) {
            String userIdFromQuery = query.split("userId=")[1].split("&")[0];
            log.debug("Extracted userId from query: {}", userIdFromQuery);
            return userIdFromQuery;
        }

        List<String> userIdHeader = request.getHeaders().get("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            String userIdFromHeader = userIdHeader.get(0);
            log.debug("Extracted userId from header: {}", userIdFromHeader);
            return userIdFromHeader;
        }

        log.debug("No userId found in request");
        return null;
    }
}