package com.vendo.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for order cancellation operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationResponse {
    
    private Long orderId;
    private String orderNumber;
    private String status;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private Boolean refundProcessed;
    private BigDecimal refundAmount;
    private String refundStatus;
    private String refundTransactionId;
    private Boolean inventoryUpdated;
    private String message;
}
