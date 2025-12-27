package com.vendo.common.ratelimit;

import com.vendo.common.exception.RateLimitExceededException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting filter using Redis for distributed rate limiting.
 * Implements token bucket algorithm for rate limiting.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitConfig rateLimitConfig;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Skip rate limiting for public endpoints
        String path = request.getRequestURI();
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Get client identifier (user ID if authenticated, IP address otherwise)
        String clientId = getClientIdentifier(request);
        
        // Determine rate limit based on authentication status
        RateLimitConfig.LimitConfig limitConfig = isAuthenticated(request) 
                ? rateLimitConfig.getAuthenticated() 
                : rateLimitConfig.getUnauthenticated();
        
        // Check rate limit
        RateLimitResult result = checkRateLimit(clientId, limitConfig);
        
        // Add rate limit headers to response
        addRateLimitHeaders(response, result, limitConfig);
        
        // If rate limit exceeded, return 429 Too Many Requests
        if (!result.isAllowed()) {
            log.warn("Rate limit exceeded for client: {} on path: {}", clientId, path);
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                    "{\"error\":\"Rate limit exceeded\",\"message\":\"%s\",\"retryAfter\":%d}",
                    result.getMessage(),
                    result.getRetryAfterSeconds()
            ));
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Check if endpoint is public (no rate limiting)
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/v1/auth/") ||
               path.startsWith("/oauth2/") ||
               path.startsWith("/login/oauth2/") ||
               path.startsWith("/ws/") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/actuator/health");
    }
    
    /**
     * Get client identifier for rate limiting
     */
    private String getClientIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            // Use user ID if authenticated
            String username = authentication.getName();
            return "user:" + username;
        }
        
        // Use IP address for unauthenticated users
        String ipAddress = getClientIpAddress(request);
        return "ip:" + ipAddress;
    }
    
    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Check if request is authenticated
     */
    private boolean isAuthenticated(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal().toString());
    }
    
    /**
     * Check rate limit using Redis sliding window algorithm
     */
    private RateLimitResult checkRateLimit(String clientId, RateLimitConfig.LimitConfig limitConfig) {
        String key = "ratelimit:" + clientId;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        
        // Get current count
        String currentCountStr = ops.get(key);
        int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
        
        // Check if limit exceeded
        if (currentCount >= limitConfig.getLimit()) {
            // Get TTL to calculate retry after
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            long retryAfter = ttl != null && ttl > 0 ? ttl : limitConfig.getWindowSeconds();
            
            return RateLimitResult.exceeded(
                    String.format("Rate limit exceeded. Maximum %d requests per %d seconds allowed.", 
                            limitConfig.getLimit(), limitConfig.getWindowSeconds()),
                    retryAfter
            );
        }
        
        // Increment counter
        if (currentCount == 0) {
            // First request in window, set with expiration
            ops.set(key, "1", Duration.ofSeconds(limitConfig.getWindowSeconds()));
        } else {
            // Increment existing counter
            ops.increment(key);
        }
        
        int remaining = limitConfig.getLimit() - currentCount - 1;
        return RateLimitResult.allowed(remaining, limitConfig.getWindowSeconds());
    }
    
    /**
     * Add rate limit headers to response
     */
    private void addRateLimitHeaders(HttpServletResponse response, 
                                     RateLimitResult result, 
                                     RateLimitConfig.LimitConfig limitConfig) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(limitConfig.getLimit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
        response.setHeader("X-RateLimit-Window", String.valueOf(limitConfig.getWindowSeconds()));
        
        if (!result.isAllowed()) {
            response.setHeader("Retry-After", String.valueOf(result.getRetryAfterSeconds()));
        }
    }
    
    /**
     * Rate limit result
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final int remaining;
        private final String message;
        private final long retryAfterSeconds;
        
        private RateLimitResult(boolean allowed, int remaining, String message, long retryAfterSeconds) {
            this.allowed = allowed;
            this.remaining = remaining;
            this.message = message;
            this.retryAfterSeconds = retryAfterSeconds;
        }
        
        public static RateLimitResult allowed(int remaining, long windowSeconds) {
            return new RateLimitResult(true, remaining, null, windowSeconds);
        }
        
        public static RateLimitResult exceeded(String message, long retryAfterSeconds) {
            return new RateLimitResult(false, 0, message, retryAfterSeconds);
        }
        
        public boolean isAllowed() {
            return allowed;
        }
        
        public int getRemaining() {
            return remaining;
        }
        
        public String getMessage() {
            return message;
        }
        
        public long getRetryAfterSeconds() {
            return retryAfterSeconds;
        }
    }
}

