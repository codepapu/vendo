package com.vendo.tax.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Tax API Configuration
 * Configuration for external tax calculation APIs (e.g., Avalara, TaxJar)
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tax.api")
public class TaxApiConfig {
    
    /**
     * Enable external tax API integration
     */
    private Boolean enabled = false;
    
    /**
     * Tax API provider (AVALARA, TAXJAR, CUSTOM)
     */
    private String provider;
    
    /**
     * API base URL
     */
    private String baseUrl;
    
    /**
     * API key
     */
    private String apiKey;
    
    /**
     * API secret (if required)
     */
    private String apiSecret;
    
    /**
     * Company ID (for Avalara)
     */
    private String companyId;
    
    /**
     * Timeout in milliseconds
     */
    private Integer timeout = 5000;
    
    /**
     * Enable caching
     */
    private Boolean cacheEnabled = true;
    
    /**
     * Cache TTL in seconds
     */
    private Integer cacheTtl = 3600;
}

