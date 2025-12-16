package com.nexus.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogCategoryResponse {
    
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long parentId;
    private String image;
    private Long postCount;
    private List<BlogCategoryResponse> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
