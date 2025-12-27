package com.vendo.b2b.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderTemplateRequest {
    
    @NotNull(message = "Business account ID is required")
    private Long businessAccountId;
    
    @NotNull(message = "Name is required")
    private String name;
    
    private String description;
    
    private Boolean isShared; // shared within business account
    
    @NotEmpty(message = "Template items are required")
    @Valid
    private List<OrderTemplateItemRequest> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderTemplateItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
        
        private String notes;
        
        private Integer sortOrder;
    }
}

