package com.vendo.common.exception;

/**
 * Exception thrown when API rate limit is exceeded.
 */
public class RateLimitExceededException extends RuntimeException {
    
    private final long retryAfterSeconds;
    
    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

