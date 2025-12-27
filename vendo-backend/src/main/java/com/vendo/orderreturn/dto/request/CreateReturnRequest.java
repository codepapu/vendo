package com.vendo.orderreturn.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReturnRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    @NotNull(message = "Items are required")
    private List<ReturnItem> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnItem {
        @NotNull(message = "Order item ID is required")
        private Long orderItemId;
        
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
        
        private String reason;
        private String condition; // NEW, USED, DAMAGED
    }
}

