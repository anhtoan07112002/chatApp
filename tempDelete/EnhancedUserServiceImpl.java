package com.chat.infrastructure.user;

import com.chat.config.websocket.WebSocketSessionManager;
import com.chat.domain.service.userservice.IUserService;
import org.springframework.stereotype.Service;
import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserStatus;
import com.chat.domain.repository.userReponsitory.IUserRepository;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnhancedUserServiceImpl implements IUserService {
    private final IUserRepository userRepository;
    private final RedisUserService redisUserService;
    private final WebSocketSessionManager sessionManager;

    @Override
    public User createUser(String name, String email, String password, String phoneNumber) {
        return null;
    }

    @Override
    public User getUserById(String id) {
        // Thử lấy từ cache trước
        User cachedUser = redisUserService.getCachedUser(id);
        if (cachedUser != null) {
            return cachedUser;
        }

        // Nếu không có trong cache, lấy từ database
        User user = userRepository.findById(UUID.fromString(id));
        if (user != null) {
            // Cache user để sử dụng lần sau
            redisUserService.cacheUser(user);
        }
        return user;
    }

    @Override
    public boolean isOnline(String id) {
        // Kiểm tra trạng thái trong Redis trước
        UserStatus status = redisUserService.getUserStatus(id);
        if (status != null) {
            return status == UserStatus.ONLINE;
        }

        // Nếu không có trong Redis, kiểm tra WebSocket session
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
        if (user != null) {
            user.setStatus(status);
            userRepository.save(user);
        }
    }

    @Override
    public User getUserByEmail(String email) {
        return null;
    }
}
