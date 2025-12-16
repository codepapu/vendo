package com.nexus.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 191, message = "Title must not exceed 191 characters")
    private String title;
    
    @NotBlank(message = "Slug is required")
    @Size(max = 191, message = "Slug must not exceed 191 characters")
    private String slug;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private String template;
    
    @Size(max = 60, message = "Status must not exceed 60 characters")
    private String status; // draft, published
    
    private String metaTitle;
    
    private String metaDescription;
    
    private String metaKeywords;
}
