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
public class CreateBulkOrderRequest {
    
    @NotNull(message = "Business account ID is required")
    private Long businessAccountId;
    
    @NotNull(message = "Name is required")
    private String name;
    
    private String description;
    
    private String sourceFileName;
    
    private String sourceFileType; // CSV, XLSX, XLS
    
    @NotEmpty(message = "Bulk order items are required")
    @Valid
    private List<BulkOrderItemRequest> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkOrderItemRequest {
        @NotNull(message = "Row number is required")
        private Integer rowNumber;
        
        private Long productId;
        
        private String productSku;
        
        private String productName;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
        
        private java.math.BigDecimal unitPrice;
    }
}

