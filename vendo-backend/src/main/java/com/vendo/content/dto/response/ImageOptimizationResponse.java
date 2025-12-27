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
public class ImageOptimizationResponse {
    
    private Long originalId;
    
    private String originalUrl;
    
    private Long originalSize;
    
    private Integer originalWidth;
    
    private Integer originalHeight;
    
    private Map<String, OptimizedImage> optimizedImages;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizedImage {
        private String url;
        private String format; // webp, jpg, png
        private Integer width;
        private Integer height;
        private Long size;
        private String sizeLabel; // thumbnail, medium, large
    }
}

