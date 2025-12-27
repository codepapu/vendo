package com.vendo.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CDNResponse {
    
    private String originalUrl;
    
    private String cdnUrl;
    
    private String path;
    
    private Boolean cached;
    
    private LocalDateTime cachedAt;
    
    private Long cacheSize;
    
    private Map<String, Object> metadata;
}

