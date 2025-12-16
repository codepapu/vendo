package com.nexus.user.config;

import com.nexus.user.service.TokenBlacklistService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@TestConfiguration
public class TestConfig {
    
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    
    @MockBean
    private RedisTemplate<String, String> redisTemplate;
}
