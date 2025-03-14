package com.chat.infrastructure.user;

import com.chat.application.DTO.UserRedisDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserStatus;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisUserService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    @Qualifier("customStringRedisTemplate")
    private final RedisTemplate<String, String> stringRedisTemplate; // Thêm RedisTemplate cho String

    private static final String USER_KEY_PREFIX = "user:";
    private static final String USER_STATUS_KEY_PREFIX = "user:status:";
    private static final long USER_CACHE_TTL = 30; // 30 phút

    public RedisUserService(RedisTemplate<String, Object> redisTemplate, 
                            @Qualifier("customStringRedisTemplate") RedisTemplate<String, String> stringRedisTemplate, 
                            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public void cacheUser(String key, User user) {
        try {
            UserRedisDTO redisDTO = UserRedisDTO.fromDomain(user);
            redisTemplate.opsForValue().set(key, redisDTO, USER_CACHE_TTL, TimeUnit.MINUTES);
            log.debug("User cached successfully: {}", key);
        } catch (Exception e) {
            log.error("Error caching user: {}", key, e);
        }
    }

    public User getCachedUser(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            UserRedisDTO redisDTO = objectMapper.convertValue(value, UserRedisDTO.class);
            return redisDTO.toDomain();
        } catch (Exception e) {
            log.error("Error getting cached user: {}", key, e);
            return null;
        }
    }

    public void updateUserStatus(String userId, UserStatus status) {
        String statusKey = USER_STATUS_KEY_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(statusKey, status.name(), USER_CACHE_TTL, TimeUnit.MINUTES);

        User cachedUser = getCachedUser(userId);
        if (cachedUser != null) {
            cachedUser.setStatus(status);
            cacheUser(USER_KEY_PREFIX + userId, cachedUser);
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
        redisTemplate.delete(key);
        stringRedisTemplate.delete(statusKey);
    }
}
