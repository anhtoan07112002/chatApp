package com.chat.infrastructure.user;

//import java.util.UUID;
//
//import org.springframework.stereotype.Service;
//
//import com.chat.config.websocket.WebSocketSessionManager;
//import com.chat.domain.entity.user.User;
//import com.chat.domain.repository.userReponsitory.IUserRepository;
//import com.chat.domain.service.userservice.IUserService;
//
//import lombok.RequiredArgsConstructor;

//@Service
//@RequiredArgsConstructor
//public class UserServiceImpl implements IUserService{
//
//    private final IUserRepository userRepository;
//    private final WebSocketSessionManager sessionManager;
//    @Override
//    public User createUser(String name, String email, String password) {
//        return User.create(name, email, password);
//    }
//
//    @Override
//    public User getUserById(String id) {
//        UUID userId = UUID.fromString(id);
//        return userRepository.findById(userId);
//    }
//
//    @Override
//    public boolean isOnline(String id) {
//        return sessionManager.isUserOnline(id);
//    }
//}
import com.chat.config.websocket.WebSocketSessionManager;
import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.entity.user.UserStatus;
import com.chat.domain.exception.userException.UserNotFoundException;
import com.chat.domain.repository.userReponsitory.IUserRepository;
import com.chat.domain.service.userservice.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
//@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final IUserRepository userRepository;
    private final RedisUserService redisUserService;
    private final WebSocketSessionManager sessionManager;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            IUserRepository userRepository,
            RedisUserService redisUserService,
            WebSocketSessionManager sessionManager,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.redisUserService = redisUserService;
        this.sessionManager = sessionManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(String userName, String email, String password, String phoneNumber) {
        User user = User.builder()
                .id(new UserId(UUID.randomUUID()))
                .userName(userName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phoneNumber(phoneNumber)
                .status(UserStatus.OFFLINE)
                .build();

        userRepository.save(user);
        return user;
    }

    @Override
    public User getUserById(String id) {
        // Try cache first
        User cachedUser = redisUserService.getCachedUser(id);
        if (cachedUser != null) {
            return cachedUser;
        }

        // If not in cache, get from database
        User user = userRepository.findById(UUID.fromString(id));
        if (user != null) {
            redisUserService.cacheUser("user:" + id, user);
            return user;
        }

        throw new UserNotFoundException("User not found with id: " + id);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean isOnline(String id) {
        // Check Redis status first
        UserStatus status = redisUserService.getUserStatus(id);
        if (status != null) {
            return status == UserStatus.ONLINE;
        }

        // If not in Redis, check WebSocket session
        boolean isOnline = sessionManager.isUserOnline(id);
        if (isOnline) {
            redisUserService.updateUserStatus(id, UserStatus.ONLINE);
        }
        return isOnline;
    }

    @Override
    public void updateUserStatus(String userId, UserStatus status) {
        redisUserService.updateUserStatus(userId, status);
        User user = getUserById(userId);
        user.setStatus(status);
        userRepository.save(user);
    }

//    @Override
//    public void saveUser(User user) {
//        userRepository.save(user);
//        redisUserService.cacheUser(user);
//    }
}