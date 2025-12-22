package com.vendo.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 191, message = "Title must not exceed 191 characters")
    private String title;
    
    @NotBlank(message = "Slug is required")
    @Size(max = 191, message = "Slug must not exceed 191 characters")
    private String slug;
    
    private String excerpt;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private String featuredImage;
    
    @Size(max = 60, message = "Status must not exceed 60 characters")
    private String status; // draft, published, archived
    
    private Long categoryId;
    
    private String metaTitle;
    
    private String metaDescription;
    
    private String metaKeywords;
    
    private List<Long> tagIds;
}
