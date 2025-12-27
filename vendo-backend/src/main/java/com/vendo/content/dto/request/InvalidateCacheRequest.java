package com.vendo.content.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvalidateCacheRequest {
    
    private List<String> paths; // CDN paths to invalidate
    
    private String pattern; // Pattern to match paths (e.g., "images/*")
    
    private Boolean purgeAll; // Purge entire cache
}

