package com.vendo.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse {
    
    private Long id;
    private String title;
    private String slug;
    private String content;
    private String template;
    private String status;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
