package com.vendo.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for monitoring cache hit rates and performance.
 * Tracks cache statistics and logs them periodically.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheMonitoringService {
    
    private final CacheManager cacheManager;
    
    private final Map<String, AtomicLong> cacheHits = new HashMap<>();
    private final Map<String, AtomicLong> cacheMisses = new HashMap<>();
    
    /**
     * Record a cache hit for a specific cache name
     */
    public void recordCacheHit(String cacheName) {
        cacheHits.computeIfAbsent(cacheName, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    /**
     * Record a cache miss for a specific cache name
     */
    public void recordCacheMiss(String cacheName) {
        cacheMisses.computeIfAbsent(cacheName, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    /**
     * Get cache hit rate for a specific cache
     * @return Hit rate as a percentage (0-100)
     */
    public double getCacheHitRate(String cacheName) {
        long hits = cacheHits.getOrDefault(cacheName, new AtomicLong(0)).get();
        long misses = cacheMisses.getOrDefault(cacheName, new AtomicLong(0)).get();
        long total = hits + misses;
        
        if (total == 0) {
            return 0.0;
        }
        
        return (double) hits / total * 100.0;
    }
    
    /**
     * Get cache statistics for all caches
     */
    public Map<String, CacheStatistics> getAllCacheStatistics() {
        Map<String, CacheStatistics> stats = new HashMap<>();
        
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                long hits = cacheHits.getOrDefault(cacheName, new AtomicLong(0)).get();
                long misses = cacheMisses.getOrDefault(cacheName, new AtomicLong(0)).get();
                long total = hits + misses;
                double hitRate = total > 0 ? (double) hits / total * 100.0 : 0.0;
                
                stats.put(cacheName, new CacheStatistics(cacheName, hits, misses, total, hitRate));
            }
        });
        
        return stats;
    }
    
    /**
     * Log cache statistics every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void logCacheStatistics() {
        log.info("=== Cache Statistics ===");
        getAllCacheStatistics().forEach((cacheName, stats) -> {
            log.info("Cache: {} | Hits: {} | Misses: {} | Total: {} | Hit Rate: {:.2f}%",
                    cacheName, stats.getHits(), stats.getMisses(), stats.getTotal(), stats.getHitRate());
        });
        log.info("========================");
    }
    
    /**
     * Reset cache statistics
     */
    public void resetStatistics() {
        cacheHits.clear();
        cacheMisses.clear();
        log.info("Cache statistics reset");
    }
    
    /**
     * Cache statistics data class
     */
    public static class CacheStatistics {
        private final String cacheName;
        private final long hits;
        private final long misses;
        private final long total;
        private final double hitRate;
        
        public CacheStatistics(String cacheName, long hits, long misses, long total, double hitRate) {
            this.cacheName = cacheName;
            this.hits = hits;
            this.misses = misses;
            this.total = total;
            this.hitRate = hitRate;
        }
        
        public String getCacheName() {
            return cacheName;
        }
        
        public long getHits() {
            return hits;
        }
        
        public long getMisses() {
            return misses;
        }
        
        public long getTotal() {
            return total;
        }
        
        public double getHitRate() {
            return hitRate;
        }
    }
}

