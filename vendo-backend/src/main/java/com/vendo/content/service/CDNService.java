package com.vendo.content.service;

import com.vendo.content.dto.request.InvalidateCacheRequest;
import com.vendo.content.dto.response.CDNResponse;
import com.vendo.content.dto.response.CDNStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CDNService {
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${app.cdn.enabled:false}")
    private boolean cdnEnabled;
    
    @Value("${app.cdn.base-url:}")
    private String cdnBaseUrl;
    
    @Value("${app.cdn.provider:cloudflare}") // cloudflare, aws-cloudfront, fastly, etc.
    private String cdnProvider;
    
    @Value("${app.cdn.api-key:}")
    private String apiKey;
    
    @Value("${app.cdn.api-secret:}")
    private String apiSecret;
    
    @Value("${app.cdn.zone-id:}")
    private String zoneId;
    
    @Value("${app.cdn.cache-ttl:86400}") // 24 hours default
    private long cacheTtl;
    
    private static final String CDN_STATS_KEY = "cdn:stats";
    private static final String CDN_CACHE_KEY_PREFIX = "cdn:cache:";
    
    /**
     * Get CDN URL for a given path
     */
    @Transactional(readOnly = true)
    public CDNResponse getCDNUrl(String originalUrl, String path) {
        if (!cdnEnabled || cdnBaseUrl == null || cdnBaseUrl.isEmpty()) {
            return CDNResponse.builder()
                    .originalUrl(originalUrl)
                    .cdnUrl(originalUrl)
                    .path(path)
                    .cached(false)
                    .build();
        }
        
        // Generate CDN URL
        String cdnUrl = cdnBaseUrl + "/" + path;
        
        // Check if cached
        String cacheKey = CDN_CACHE_KEY_PREFIX + path;
        Boolean cached = redisTemplate != null && Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
        
        // Update stats
        updateStats("request", path, 0L);
        
        return CDNResponse.builder()
                .originalUrl(originalUrl)
                .cdnUrl(cdnUrl)
                .path(path)
                .cached(cached != null && cached)
                .cachedAt(cached != null && cached ? LocalDateTime.now() : null)
                .build();
    }
    
    /**
     * Invalidate CDN cache
     */
    @Transactional
    public void invalidateCache(InvalidateCacheRequest request) {
        if (!cdnEnabled) {
            log.warn("CDN is not enabled, cache invalidation skipped");
            return;
        }
        
        if (Boolean.TRUE.equals(request.getPurgeAll())) {
            // Purge entire cache
            purgeAllCache();
            log.info("All CDN cache purged");
            return;
        }
        
        if (request.getPaths() != null && !request.getPaths().isEmpty()) {
            // Invalidate specific paths
            for (String path : request.getPaths()) {
                invalidatePath(path);
            }
            log.info("CDN cache invalidated for {} paths", request.getPaths().size());
        }
        
        if (request.getPattern() != null && !request.getPattern().isEmpty()) {
            // Invalidate by pattern
            invalidateByPattern(request.getPattern());
            log.info("CDN cache invalidated for pattern: {}", request.getPattern());
        }
        
        // Call CDN provider API if configured
        if (apiKey != null && !apiKey.isEmpty()) {
            callCDNInvalidationAPI(request);
        }
    }
    
    /**
     * Get CDN statistics
     */
    @Transactional(readOnly = true)
    public CDNStatsResponse getCDNStats() {
        if (redisTemplate == null) {
            return CDNStatsResponse.builder()
                    .totalRequests(0L)
                    .totalBandwidth(0L)
                    .cacheHits(0L)
                    .cacheMisses(0L)
                    .cacheHitRatio(0.0)
                    .build();
        }
        Map<Object, Object> stats = redisTemplate.opsForHash().entries(CDN_STATS_KEY);
        
        long totalRequests = getLongValue(stats, "totalRequests", 0L);
        long totalBandwidth = getLongValue(stats, "totalBandwidth", 0L);
        long cacheHits = getLongValue(stats, "cacheHits", 0L);
        long cacheMisses = getLongValue(stats, "cacheMisses", 0L);
        
        double cacheHitRatio = totalRequests > 0 
                ? (double) cacheHits / totalRequests * 100 
                : 0.0;
        
        // Parse requests by type
        Map<String, Long> requestsByType = parseMapValue(stats, "requestsByType");
        Map<String, Long> bandwidthByType = parseMapValue(stats, "bandwidthByType");
        
        double averageResponseTime = getDoubleValue(stats, "averageResponseTime", 0.0);
        long totalCost = getLongValue(stats, "totalCost", 0L);
        
        return CDNStatsResponse.builder()
                .totalRequests(totalRequests)
                .totalBandwidth(totalBandwidth)
                .cacheHits(cacheHits)
                .cacheMisses(cacheMisses)
                .cacheHitRatio(cacheHitRatio)
                .requestsByType(requestsByType)
                .bandwidthByType(bandwidthByType)
                .averageResponseTime(averageResponseTime)
                .totalCost(totalCost)
                .build();
    }
    
    /**
     * Monitor CDN performance
     */
    public void recordCDNRequest(String path, String type, long size, long responseTime) {
        if (!cdnEnabled) {
            return;
        }
        
        updateStats("request", type, 1L);
        updateStats("bandwidth", type, size);
        updateResponseTime(responseTime);
    }
    
    // Private helper methods
    
    private void invalidatePath(String path) {
        if (redisTemplate == null) return;
        String cacheKey = CDN_CACHE_KEY_PREFIX + path;
        redisTemplate.delete(cacheKey);
        updateStats("cacheInvalidation", path, 1L);
    }
    
    private void invalidateByPattern(String pattern) {
        if (redisTemplate == null) return;
        Set<String> keys = redisTemplate.keys(CDN_CACHE_KEY_PREFIX + pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            updateStats("cacheInvalidation", "pattern", (long) keys.size());
        }
    }
    
    private void purgeAllCache() {
        if (redisTemplate == null) return;
        Set<String> keys = redisTemplate.keys(CDN_CACHE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        // Reset stats
        redisTemplate.delete(CDN_STATS_KEY);
    }
    
    private void updateStats(String metric, String type, Long value) {
        if (redisTemplate == null) return;
        String key = CDN_STATS_KEY;
        String field = metric + ":" + type;
        
        redisTemplate.opsForHash().increment(key, field, value != null ? value : 1L);
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
        
        // Update total
        if ("request".equals(metric)) {
            redisTemplate.opsForHash().increment(key, "totalRequests", value != null ? value : 1L);
        } else if ("bandwidth".equals(metric)) {
            redisTemplate.opsForHash().increment(key, "totalBandwidth", value != null ? value : 0L);
        }
    }
    
    private void updateResponseTime(long responseTime) {
        if (redisTemplate == null) return;
        String key = CDN_STATS_KEY;
        
        // Calculate running average
        Double currentAvg = getDoubleValue(
                redisTemplate.opsForHash().entries(key),
                "averageResponseTime",
                0.0
        );
        Long requestCount = getLongValue(
                redisTemplate.opsForHash().entries(key),
                "totalRequests",
                1L
        );
        
        double newAvg = ((currentAvg * (requestCount - 1)) + responseTime) / requestCount;
        redisTemplate.opsForHash().put(key, "averageResponseTime", newAvg);
    }
    
    private void callCDNInvalidationAPI(InvalidateCacheRequest request) {
        // This would call the actual CDN provider API
        // Implementation depends on the CDN provider (Cloudflare, CloudFront, Fastly, etc.)
        log.info("Calling CDN invalidation API for provider: {}", cdnProvider);
        
        // Example for Cloudflare:
        // if ("cloudflare".equals(cdnProvider)) {
        //     cloudflareAPI.purgeCache(zoneId, request.getPaths());
        // }
        
        // Example for AWS CloudFront:
        // if ("cloudfront".equals(cdnProvider)) {
        //     cloudFrontAPI.createInvalidation(distributionId, request.getPaths());
        // }
    }
    
    private long getLongValue(Map<Object, Object> map, String key, long defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private double getDoubleValue(Map<Object, Object> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Long> parseMapValue(Map<Object, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Map) {
            Map<String, Long> result = new HashMap<>();
            ((Map<?, ?>) value).forEach((k, v) -> {
                if (v instanceof Number) {
                    result.put(k.toString(), ((Number) v).longValue());
                }
            });
            return result;
        }
        return new HashMap<>();
    }
}

