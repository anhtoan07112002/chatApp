package com.chat.config.websocket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.chat.domain.entity.user.UserId;


@Component
public class WebSocketSessionManager {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void addSession(UserId userId, WebSocketSession session) {
        sessions.put(userId.asString(), session);
    }

    public void removeSession(String userId) {
        sessions.remove(userId);
    }

    public boolean isUserOnline(String userId) {
        return sessions.containsKey(userId);
    }
}
