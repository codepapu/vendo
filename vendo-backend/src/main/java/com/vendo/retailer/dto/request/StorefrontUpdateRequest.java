package com.vendo.retailer.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorefrontUpdateRequest {
    
    private String description;
    
    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;
    
    @Size(max = 500, message = "Banner URL must not exceed 500 characters")
    private String bannerUrl;
}

