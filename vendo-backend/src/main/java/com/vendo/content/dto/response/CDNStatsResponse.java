package com.vendo.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CDNStatsResponse {
    
    private Long totalRequests;
    
    private Long totalBandwidth; // in bytes
    
    private Long cacheHits;
    
    private Long cacheMisses;
    
    private Double cacheHitRatio;
    
    private Map<String, Long> requestsByType; // images, css, js, etc.
    
    private Map<String, Long> bandwidthByType;
    
    private Double averageResponseTime; // in milliseconds
    
    private Long totalCost; // in cents or smallest currency unit
}

