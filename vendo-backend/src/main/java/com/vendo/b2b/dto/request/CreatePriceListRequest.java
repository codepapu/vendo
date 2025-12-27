package com.vendo.b2b.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePriceListRequest {
    
    @NotNull(message = "Business account ID is required")
    private Long businessAccountId;
    
    @NotNull(message = "Name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;
    
    private LocalDate effectiveUntil;
    
    private Boolean isDefault;
    
    @NotEmpty(message = "Price list items are required")
    @Valid
    private List<PriceListItemRequest> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceListItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Price is required")
        private java.math.BigDecimal price;
        
        @NotNull(message = "Min quantity is required")
        private Integer minQuantity;
        
        private Integer maxQuantity;
        
        private java.math.BigDecimal discountPercentage;
    }
}

