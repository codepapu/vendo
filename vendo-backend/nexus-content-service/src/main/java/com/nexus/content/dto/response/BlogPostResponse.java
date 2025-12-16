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
public class BlogPostResponse {
    
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private String featuredImage;
    private String status;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private Long viewCount;
    private LocalDateTime publishedAt;
    private List<BlogTagResponse> tags;
    private List<BlogCommentResponse> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
