package com.chat.infrastructure.security;

import com.chat.domain.entity.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private final RedisTemplate<String, String> redisTemplate;

    public JwtService(@Qualifier("customStringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().getValue());
        claims.put("email", user.getEmail());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", String.class);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

//    public boolean isTokenValid(String token, Date now) {
//        try {
//            Claims claims = extractAllClaims(token);
//            Date expiration = claims.getExpiration();
//            log.debug("Token expiration: {}, current time: {}", expiration, now);
//            return !claims.getExpiration().before(now);
//        } catch (Exception e) {
//            log.error("Error validating token", e);
//            return false;
//        }
//    }

    public void invalidateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            long expirationTime = claims.getExpiration().getTime();
            long currentTime = System.currentTimeMillis();
            long ttl = (expirationTime - currentTime) / 1000; // Convert to seconds

            if (ttl > 0) {
                String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(blacklistKey, "blacklisted", ttl, TimeUnit.SECONDS);
                log.debug("Token blacklisted: {}", token);
            }
        } catch (Exception e) {
            log.error("Error invalidating token", e);
        }
    }

    public boolean isTokenValid(String token, Date now) {
        try {
            // Kiểm tra token có trong blacklist không
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            Boolean isBlacklisted = redisTemplate.hasKey(blacklistKey);

            if (Boolean.TRUE.equals(isBlacklisted)) {
                log.debug("Token is blacklisted: {}", token);
                return false;
            }

            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            log.debug("Token expiration: {}, current time: {}", expiration, now);
            return !claims.getExpiration().before(now);
        } catch (Exception e) {
            log.error("Error validating token", e);
            return false;
        }
    }
}