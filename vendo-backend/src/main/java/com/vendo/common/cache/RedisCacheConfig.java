package com.vendo.common.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration
 * Configures Redis as the primary cache provider with different TTLs for different cache names
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {
    
    /**
     * Configure Redis Cache Manager with different TTLs for different caches
     */
    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();
        
        // Cache-specific configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // B2B Pricing cache - 30 minutes TTL as per requirements
        cacheConfigurations.put("b2bPricing", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Products cache - 1 hour TTL
        cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Orders cache - 5 minutes TTL
        cacheConfigurations.put("orders", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Categories cache - 1 hour TTL
        cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Users cache - 30 minutes TTL
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Retailer dashboard cache - 5 minutes TTL as per requirements
        cacheConfigurations.put("retailerDashboard", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Recommendations cache - 30 minutes TTL
        cacheConfigurations.put("recommendations", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}

