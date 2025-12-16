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
public class BlogTagRequest {
    
    @NotBlank(message = "Tag name is required")
    @Size(max = 191, message = "Tag name must not exceed 191 characters")
    private String name;
    
    @Size(max = 191, message = "Slug must not exceed 191 characters")
    private String slug;
}
