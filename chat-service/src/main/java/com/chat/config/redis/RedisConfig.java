package com.chat.config.redis;

import com.chat.application.DTO.UserRedisDTO;
import com.chat.config.redis.serializer.UserIdDeserializer;
import com.chat.config.redis.serializer.UserIdSerializer;
import com.chat.domain.entity.user.UserId;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.beans.factory.annotation.Value;
import com.chat.domain.entity.user.User;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
@EnableCaching
@Slf4j
//public class RedisConfig {
//
//    @Value("${spring.data.redis.host}")
//    private String redisHost;
//
//    @Value("${spring.data.redis.port}")
//    private int redisPort;
//
//    @Value("${spring.data.redis.password:}")
//    private String redisPassword;
//
//    @Value("${spring.data.redis.timeout:2000}")
//    private int timeout;
//
//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        try {
//            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
//
//            if (StringUtils.hasText(redisPassword)) {
//                config.setPassword(redisPassword);
//            }
//
//            LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
//            factory.setValidateConnection(true); // Kiểm tra kết nối trước khi sử dụng
//
//            log.info("Initializing Redis connection to {}:{}", redisHost, redisPort);
//            return factory;
//        } catch (Exception e) {
//            log.error("Failed to create Redis connection factory: {}", e.getMessage());
//            throw e;
//        }
//    }
//    // @Bean
//    // public RedisConnectionFactory redisConnectionFactory() {
//    //     RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
//    //     return new LettuceConnectionFactory(config);
//    // }
//
////    @Bean
////    public ObjectMapper redisObjectMapper() {
////        ObjectMapper mapper = new ObjectMapper();
////        mapper.registerModule(new JavaTimeModule());
////        return mapper;
////    }
//
//    @Bean
//    public ObjectMapper redisObjectMapper() {
//        ObjectMapper mapper = new ObjectMapper();
//
//        // Đăng ký các modules
//        mapper.registerModule(new JavaTimeModule());
//
//        // Cấu hình visibility
//        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//
//        // Cho phép deserialize các class không được đánh dấu
//        mapper.activateDefaultTyping(
//                LaissezFaireSubTypeValidator.instance,
//                ObjectMapper.DefaultTyping.NON_FINAL,
//                JsonTypeInfo.As.PROPERTY
//        );
//
//        // Đăng ký serializers cho UserId
//        SimpleModule module = new SimpleModule();
//        module.addSerializer(UserId.class, new UserIdSerializer());
//        module.addDeserializer(UserId.class, new UserIdDeserializer());
//        mapper.registerModule(module);
//
//        return mapper;
//    }
//
////    @Bean
////    public RedisTemplate<String, User> userRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
////        RedisTemplate<String, User> template = new RedisTemplate<>();
////        template.setConnectionFactory(connectionFactory);
////
////        // Tạo GenericJackson2JsonRedisSerializer với ObjectMapper tùy chỉnh
////        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
////
////        template.setKeySerializer(new StringRedisSerializer());
////        template.setValueSerializer(serializer);
////        template.setHashKeySerializer(new StringRedisSerializer());
////        template.setHashValueSerializer(serializer);
////
////        template.setEnableTransactionSupport(true);
////        template.afterPropertiesSet();
////
////        return template;
////    }
//
//    @Bean
//    public RedisTemplate<String, User> userRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
//        try {
//            RedisTemplate<String, User> template = new RedisTemplate<>();
//            template.setConnectionFactory(connectionFactory);
//
//            GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
//
//            template.setKeySerializer(new StringRedisSerializer());
//            template.setValueSerializer(serializer);
//            template.setHashKeySerializer(new StringRedisSerializer());
//            template.setHashValueSerializer(serializer);
//
//            template.afterPropertiesSet();
//
//            log.info("Successfully configured RedisTemplate for User");
//            return template;
//        } catch (Exception e) {
//            log.error("Error configuring RedisTemplate: {}", e.getMessage(), e);
//            throw e;
//        }
//    }
//
//    @Bean(name = "customStringRedisTemplate")
//    public RedisTemplate<String, String> stringRedisTemplate(RedisConnectionFactory connectionFactory) {
//        RedisTemplate<String, String> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setHashValueSerializer(new StringRedisSerializer());
//
//        template.setEnableTransactionSupport(true);
//        template.afterPropertiesSet();
//
//        return template;
//    }
//}
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }
        return new LettuceConnectionFactory(config);
    }

    @Bean(name = "redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Cấu hình cơ bản
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
        // Cấu hình type information
        mapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        
        // Đăng ký module cho UserId
        SimpleModule module = new SimpleModule();
        module.addSerializer(UserId.class, new UserIdSerializer());
        module.addDeserializer(UserId.class, new UserIdDeserializer());
        mapper.registerModule(module);
        
        return mapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            @Qualifier("redisObjectMapper") ObjectMapper objectMapper  // Thêm parameter này
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Sử dụng Jackson2JsonRedisSerializer với class cụ thể
        Jackson2JsonRedisSerializer<UserRedisDTO> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, UserRedisDTO.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = "customStringRedisTemplate")
    public RedisTemplate<String, String> stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();

        return template;
    }
}