package com.vendo.content.dto.request;

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
public class BlogCategoryRequest {
    
    @NotBlank(message = "Category name is required")
    @Size(max = 191, message = "Category name must not exceed 191 characters")
    private String name;
    
    @Size(max = 191, message = "Slug must not exceed 191 characters")
    private String slug;
    
    private String description;
    
    private Long parentId;
    
    private String image;
}
