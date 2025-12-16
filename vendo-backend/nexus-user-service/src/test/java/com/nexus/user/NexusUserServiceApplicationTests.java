package com.nexus.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.nexus.user.service.TokenBlacklistService;

@SpringBootTest
@ActiveProfiles("test")
class NexusUserServiceApplicationTests {

    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    
    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void contextLoads() {
        // Test that Spring context loads successfully
    }
}
