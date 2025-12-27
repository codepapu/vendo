package com.vendo.common.ratelimit;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to register the rate limit filter.
 * Note: RateLimitFilter is already a @Component, but we register it explicitly
 * to ensure proper ordering and URL pattern matching.
 */
@Configuration
public class RateLimitFilterConfig {
    
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter rateLimitFilter) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(rateLimitFilter);
        registration.addUrlPatterns("/api/*"); // Apply to all API endpoints
        registration.setOrder(1); // High priority, run before other filters
        registration.setName("rateLimitFilter");
        return registration;
    }
}

