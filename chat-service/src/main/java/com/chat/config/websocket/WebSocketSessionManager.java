package com.chat.config.websocket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
// import org.springframework.web.socket.WebSocketSession;

import com.chat.domain.entity.user.UserId;


@Component
@Slf4j
public class WebSocketSessionManager {
    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionTimestamps = new ConcurrentHashMap<>();

    public String getSession(UserId userId) {
        if (userId == null) {
            return null;
        }
        return sessions.get(userId.asString());
    }

    public void addSession(UserId userId, String sessionId) {
        if (userId == null || sessionId == null) {
            log.error("Cannot add session with null userId or sessionId");
            return;
        }

        String userIdStr = userId.asString();
        String existingSession = sessions.get(userIdStr);
        if (existingSession != null && !existingSession.equals(sessionId)) {
            log.warn("Replacing existing session {} with new session {} for user {}",
                    existingSession, sessionId, userIdStr);
        }

        sessions.put(userIdStr, sessionId);
        sessionTimestamps.put(sessionId, System.currentTimeMillis());
        log.debug("Added session {} for user {}", sessionId, userIdStr);
    }

    public void removeSession(UserId userId) {
        if (userId == null) {
            return;
        }

        String userIdStr = userId.asString();
        String removedSession = sessions.remove(userIdStr);
        if (removedSession != null) {
            sessionTimestamps.remove(removedSession);
            log.debug("Removed session {} for user {}", removedSession, userIdStr);
        }
    }

    public boolean isUserOnline(String userId) {
        if (userId == null) {
            return false;
        }

        String sessionId = sessions.get(userId);
        if (sessionId == null) {
            return false;
        }

        // Check if session is not too old (optional, adjust timeout as needed)
        Long timestamp = sessionTimestamps.get(sessionId);
        if (timestamp == null || System.currentTimeMillis() - timestamp > 300000) { // 5 minutes timeout
            sessions.remove(userId);
            sessionTimestamps.remove(sessionId);
            return false;
        }

        return true;
    }

    // Cleanup method to be called periodically (you can add @Scheduled annotation)
    public void cleanupStaleSessions() {
        long currentTime = System.currentTimeMillis();
        sessionTimestamps.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > 300000) { // 5 minutes timeout
                sessions.values().remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
}