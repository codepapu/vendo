package com.vendo.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for guest checkout
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestOrderResponse {
    
    private Long orderId;
    private String orderNumber;
    private String email;
    private String status;
    private String sessionToken; // Temporary token for guest order access
    private LocalDateTime sessionTokenExpiresAt;
    private Boolean accountCreated;
    private String message;
    private OrderResponse orderDetails;
}
