package com.vendo.b2b.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {
    
    private Long id;
    private String poNumber;
    private Long businessAccountId;
    private String businessAccountName;
    private Long orderId;
    private String orderNumber;
    private String status;
    private Long requestedBy;
    private String requestedByName;
    private Long approvedBy;
    private String approvedByName;
    private String approvalNotes;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}

