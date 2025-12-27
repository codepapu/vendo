package com.vendo.common.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Interceptor to monitor query performance and log slow queries.
 * Tracks query execution times and provides statistics.
 */
@Slf4j
@Component
public class QueryPerformanceInterceptor implements StatementInspector {
    
    private static final long SLOW_QUERY_THRESHOLD_MS = 1000; // 1 second
    private final ConcurrentHashMap<String, QueryStats> queryStats = new ConcurrentHashMap<>();
    
    @Override
    public String inspect(String sql) {
        long startTime = System.currentTimeMillis();
        
        // Store start time in thread local or return a marker
        // Note: This is a simplified version. For production, use ThreadLocal or AOP
        return sql;
    }
    
    /**
     * Record query execution time
     */
    public void recordQueryExecution(String sql, long executionTimeMs) {
        if (executionTimeMs > SLOW_QUERY_THRESHOLD_MS) {
            log.warn("Slow query detected ({}ms): {}", executionTimeMs, 
                    sql.length() > 200 ? sql.substring(0, 200) + "..." : sql);
        }
        
        QueryStats stats = queryStats.computeIfAbsent(
                normalizeSql(sql), 
                k -> new QueryStats()
        );
        stats.recordExecution(executionTimeMs);
    }
    
    /**
     * Get query statistics
     */
    public QueryStats getQueryStats(String sql) {
        return queryStats.get(normalizeSql(sql));
    }
    
    /**
     * Get all query statistics
     */
    public ConcurrentHashMap<String, QueryStats> getAllQueryStats() {
        return queryStats;
    }
    
    /**
     * Normalize SQL for statistics (remove parameters, whitespace)
     */
    private String normalizeSql(String sql) {
        if (sql == null) {
            return "";
        }
        // Remove extra whitespace and normalize
        return sql.replaceAll("\\s+", " ").trim();
    }
    
    /**
     * Query statistics data class
     */
    public static class QueryStats {
        private final AtomicLong executionCount = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private final AtomicLong maxExecutionTime = new AtomicLong(0);
        private final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);
        
        public void recordExecution(long executionTimeMs) {
            executionCount.incrementAndGet();
            totalExecutionTime.addAndGet(executionTimeMs);
            maxExecutionTime.updateAndGet(current -> Math.max(current, executionTimeMs));
            minExecutionTime.updateAndGet(current -> Math.min(current, executionTimeMs));
        }
        
        public long getExecutionCount() {
            return executionCount.get();
        }
        
        public long getTotalExecutionTime() {
            return totalExecutionTime.get();
        }
        
        public double getAverageExecutionTime() {
            long count = executionCount.get();
            return count > 0 ? (double) totalExecutionTime.get() / count : 0.0;
        }
        
        public long getMaxExecutionTime() {
            return maxExecutionTime.get();
        }
        
        public long getMinExecutionTime() {
            return minExecutionTime.get() == Long.MAX_VALUE ? 0 : minExecutionTime.get();
        }
    }
}

