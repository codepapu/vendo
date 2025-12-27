package com.vendo.common.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Rate limiting configuration loaded from application.yml
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitConfig {
    
    /**
     * Rate limit configuration for authenticated users
     */
    private LimitConfig authenticated = new LimitConfig(1000, 3600); // 1000 requests per hour
    
    /**
     * Rate limit configuration for unauthenticated users
     */
    private LimitConfig unauthenticated = new LimitConfig(100, 3600); // 100 requests per hour
    
    /**
     * Rate limit configuration data class
     */
    @Data
    public static class LimitConfig {
        /**
         * Maximum number of requests allowed in the time window
         */
        private int limit;
        
        /**
         * Time window in seconds
         */
        private int windowSeconds;
        
        public LimitConfig() {
        }
        
        public LimitConfig(int limit, int windowSeconds) {
            this.limit = limit;
            this.windowSeconds = windowSeconds;
        }
    }
}

