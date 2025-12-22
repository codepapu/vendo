package com.vendo.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private Long id;
    private String chargeId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private BigDecimal paymentFee;
    private BigDecimal refundedAmount;
    private String currency;
    private String paymentChannel;
    private String status;
    private String paymentType;
    private String description;
    private String refundNote;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
