package com.vendo.product.dto.response;

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
public class CategoryResponse {
    
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
    private List<CategoryResponse> children;
    private String description;
    private String status;
    private Integer order;
    private Boolean isFeatured;
    private String image;
    private Long productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
