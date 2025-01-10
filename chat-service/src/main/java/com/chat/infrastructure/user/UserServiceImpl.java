package com.chat.infrastructure.user;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.chat.config.websocket.WebSocketSessionManager;
import com.chat.domain.entity.user.User;
import com.chat.domain.repository.userReponsitory.IUserRepository;
import com.chat.domain.service.userservice.IUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService{
    
    private final IUserRepository userRepository;
    private final WebSocketSessionManager sessionManager;
    @Override
    public User createUser(String name, String email, String password) {
        return User.create(name, email, password);
    }

    @Override
    public User getUserById(String id) {
        // TODO Auto-generated method stub
        UUID userId = UUID.fromString(id);
        return userRepository.findById(userId);
    }

    @Override
    public boolean isOnline(String id) {
        // TODO Auto-generated method stub
        return sessionManager.isUserOnline(id);
    }
}
