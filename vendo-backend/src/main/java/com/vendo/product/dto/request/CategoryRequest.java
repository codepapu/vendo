package com.vendo.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    
    @NotBlank(message = "Category name is required")
    @Size(max = 191, message = "Category name must not exceed 191 characters")
    private String name;
    
    private Long parentId;
    
    private String description;
    
    @Pattern(regexp = "^(published|draft|pending)$", message = "Status must be published, draft, or pending")
    @Default
    private String status = "published";
    
    @Min(value = 0, message = "Order must be non-negative")
    @Default
    private Integer order = 0;
    
    @Default
    private Boolean isFeatured = false;
    
    private String image;
}
