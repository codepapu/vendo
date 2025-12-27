package com.vendo.b2b.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovePurchaseOrderRequest {
    
    private String approvalNotes; // Optional notes for approval/rejection
}

