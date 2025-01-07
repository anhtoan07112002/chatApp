package com.chat.infrastructure.user;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.repository.userReponsitory.IUserRepository;
import com.chat.domain.service.userservice.IUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService{
    
    private final IUserRepository userRepository;
    private final Set<UserId> onlineUsers = new HashSet<>();

    @Override
    public User createUser(String name, String email, String password) {
        return User.create(name, email, password);
    }

    @Override
    public User getUserById(UserId id) {
        return userRepository.findById(id);
    }

    @Override
    public boolean isOnline(UserId id) {
        return onlineUsers.contains(id);
    }
}
