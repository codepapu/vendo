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
public class CreateQuoteRequest {
    
    @NotNull(message = "Business account ID is required")
    private Long businessAccountId;
    
    @NotEmpty(message = "Quote items are required")
    @Valid
    private List<QuoteItemRequest> items;
    
    private LocalDate expirationDate;
    
    private String terms;
    
    private String notes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuoteItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
        
        private java.math.BigDecimal customPrice; // Optional custom price override
        
        private java.math.BigDecimal discountPercentage;
        
        private String notes;
    }
}

