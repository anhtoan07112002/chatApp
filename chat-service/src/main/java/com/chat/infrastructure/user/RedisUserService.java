package com.chat.infrastructure.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserStatus;
import lombok.RequiredArgsConstructor;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisUserService {
    private final RedisTemplate<String, User> userRedisTemplate;
    @Qualifier("customStringRedisTemplate")
    private final RedisTemplate<String, String> stringRedisTemplate; // Thêm RedisTemplate cho String

    private static final String USER_KEY_PREFIX = "user:";
    private static final String USER_STATUS_KEY_PREFIX = "user:status:";
    private static final long USER_CACHE_TTL = 30; // 30 minutes

    public void cacheUser(User user) {
        if (user != null && user.getId() != null) {
            String key = USER_KEY_PREFIX + user.getId().asString();
            userRedisTemplate.opsForValue().set(key, user, USER_CACHE_TTL, TimeUnit.MINUTES);
        }
    }

    public User getCachedUser(String userId) {
        String key = USER_KEY_PREFIX + userId;
        return userRedisTemplate.opsForValue().get(key);
    }

    public void updateUserStatus(String userId, UserStatus status) {
        String statusKey = USER_STATUS_KEY_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(statusKey, status.name(), USER_CACHE_TTL, TimeUnit.MINUTES);

        User cachedUser = getCachedUser(userId);
        if (cachedUser != null) {
            cachedUser.setStatus(status);
            cacheUser(cachedUser);
        }
    }

    public UserStatus getUserStatus(String userId) {
        String statusKey = USER_STATUS_KEY_PREFIX + userId;
        String status = stringRedisTemplate.opsForValue().get(statusKey);
        return status != null ? UserStatus.valueOf(status) : null;
    }

    public void removeUserCache(String userId) {
        String key = USER_KEY_PREFIX + userId;
        String statusKey = USER_STATUS_KEY_PREFIX + userId;
        userRedisTemplate.delete(key);
        stringRedisTemplate.delete(statusKey);
    }
}
