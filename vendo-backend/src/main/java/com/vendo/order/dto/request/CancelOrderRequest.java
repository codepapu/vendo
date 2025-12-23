package com.vendo.order.dto.request;

import com.vendo.order.enums.CancellationReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for cancelling an order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {
    
    @NotNull(message = "Cancellation reason is required")
    private CancellationReason reason;
    
    private String customReason; // For "OTHER" reason type
}
