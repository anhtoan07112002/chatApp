package com.chat.config.websocket;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.chat.infrastructure.security.JwtService;
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
    private final JwtService jwtService;

//    public WebSocketHandshakeInterceptor(IUserRepository userRepository) {
//        this.userRepository = userRepository;
//    }

    public WebSocketHandshakeInterceptor(
            IUserRepository userRepository,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }
//    @Override
//    public boolean beforeHandshake(ServerHttpRequest request,
//                                   ServerHttpResponse response,
//                                   WebSocketHandler wsHandler,
//                                   Map<String, Object> attributes) throws Exception {
//        String userIdStr = extractUserId(request);
//        log.debug("Attempting handshake with userId: {}", userIdStr);
//
//        if (userIdStr == null) {
//            log.error("No userId provided in request");
//            return false;
//        }
//
//        try {
//            UUID uuid = UUID.fromString(userIdStr);
//            User user = userRepository.findById(uuid);
//
//            if (user == null) {
//                log.error("User not found for userId: {}", userIdStr);
//                return false;
//            }
//
//            // Store both UserId object and string
//            attributes.put("userIdString", userIdStr);
//            attributes.put("userId", UserId.fromString(userIdStr));
//
//            log.debug("Added attributes - userIdString: {}, userId: {}",
//                    attributes.get("userIdString"),
//                    attributes.get("userId"));
//
//            return true;
//
//        } catch (IllegalArgumentException e) {
//            log.error("Invalid UUID format for userId: {}", userIdStr);
//            return false;
//        } catch (Exception e) {
//            log.error("Error during handshake for userId: {}", userIdStr, e);
//            return false;
//        }
//    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        // Extract token from query param
        String token = extractToken(request);
        String userIdStr = extractUserId(request);

        log.debug("Attempting handshake - Token: {}, UserId: {}", token, userIdStr);

        if (token == null || userIdStr == null) {
            log.error("Missing token or userId");
            return false;
        }

        try {
            // Validate token
            if (!jwtService.isTokenValid(token, new Date())) {
                log.error("Invalid token");
                return false;
            }

            UUID uuid = UUID.fromString(userIdStr);
            User user = userRepository.findById(uuid);

            if (user == null) {
                log.error("User not found for userId: {}", userIdStr);
                return false;
            }

            attributes.put("userIdString", userIdStr);
            attributes.put("userId", UserId.fromString(userIdStr));

            return true;
        } catch (Exception e) {
            log.error("Error during handshake", e);
            return false;
        }
    }

    private String extractToken(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            return query.split("token=")[1].split("&")[0];
        }
        return null;
    }

    private String extractUserId(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (query != null && query.contains("userId=")) {
            return query.split("userId=")[1].split("&")[0];
        }
        return null;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        if (exception != null) {
            log.error("Exception occurred during handshake", exception);
        }
    }

//    private String extractUserId(ServerHttpRequest request) {
//        String query = request.getURI().getQuery();
//        if (query != null && query.contains("userId=")) {
//            String userIdFromQuery = query.split("userId=")[1].split("&")[0];
//            log.debug("Extracted userId from query: {}", userIdFromQuery);
//            return userIdFromQuery;
//        }
//
//        List<String> userIdHeader = request.getHeaders().get("X-User-Id");
//        if (userIdHeader != null && !userIdHeader.isEmpty()) {
//            String userIdFromHeader = userIdHeader.get(0);
//            log.debug("Extracted userId from header: {}", userIdFromHeader);
//            return userIdFromHeader;
//        }
//
//        log.debug("No userId found in request");
//        return null;
//    }
}