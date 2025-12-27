package com.vendo.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentResponse {
    
    private Long id;
    private Long inventoryId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Integer previousQuantity;
    private Integer newQuantity;
    private String reason;
    private String status;
    private Long createdById;
    private String createdByName;
    private Long approvedById;
    private String approvedByName;
    private String approvalNotes;
    private Boolean requiresApproval;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
}

