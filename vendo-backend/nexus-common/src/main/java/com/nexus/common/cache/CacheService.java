package com.nexus.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.Callable;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {
    
    private final CacheManager cacheManager;
    
    /**
     * Get value from cache
     */
    public <T> Optional<T> get(String cacheName, String key, Class<T> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return Optional.empty();
        }
        
        Cache.ValueWrapper wrapper = cache.get(key);
        if (wrapper == null) {
            return Optional.empty();
        }
        
        Object value = wrapper.get();
        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        
        return Optional.empty();
    }
    
    /**
     * Put value in cache
     */
    public void put(String cacheName, String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
            log.debug("Cached value for key: {} in cache: {}", key, cacheName);
        }
    }
    
    /**
     * Get value from cache or compute if absent
     */
    public <T> T getOrCompute(String cacheName, String key, Callable<T> valueLoader, Class<T> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            try {
                return valueLoader.call();
            } catch (Exception e) {
                log.error("Error computing value for key: {}", key, e);
                throw new RuntimeException("Error computing cache value", e);
            }
        }
        
        return cache.get(key, () -> {
            try {
                return valueLoader.call();
            } catch (Exception e) {
                log.error("Error computing value for key: {}", key, e);
                throw new RuntimeException("Error computing cache value", e);
            }
        });
    }
    
    /**
     * Evict value from cache
     */
    public void evict(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.debug("Evicted key: {} from cache: {}", key, cacheName);
        }
    }
    
    /**
     * Clear entire cache
     */
    public void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.debug("Cleared cache: {}", cacheName);
        }
    }
}
