package com.vendo.retailer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetailerRequest {
    
    @NotBlank(message = "Store name is required")
    @Size(max = 255, message = "Store name must not exceed 255 characters")
    private String storeName;
    
    @NotBlank(message = "Store slug is required")
    @Size(max = 255, message = "Store slug must not exceed 255 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Store slug must contain only lowercase letters, numbers, and hyphens")
    private String storeSlug;
    
    private String description;
    
    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;
    
    @Size(max = 500, message = "Banner URL must not exceed 500 characters")
    private String bannerUrl;
    
    private BigDecimal commissionRate;
    
    @Pattern(regexp = "^(PENDING|ACTIVE|SUSPENDED|REJECTED)$", message = "Status must be PENDING, ACTIVE, SUSPENDED, or REJECTED")
    private String status;
}

