package com.nexus.user.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@TestConfiguration
public class TestRedisConfig {
    
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        // Return a mock or embedded Redis connection factory
        // For now, we'll let Spring Boot auto-configure it
        // Tests should mock TokenBlacklistService instead
        return null;
    }
}
