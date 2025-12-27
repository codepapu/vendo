package com.vendo.return.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponse {
    
    private Long id;
    private String code;
    private Long orderId;
    private Long userId;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED, PROCESSING, COMPLETED, CANCELLED
    private BigDecimal refundAmount;
    private String refundStatus; // PENDING, PROCESSED, FAILED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReturnItem> items;
    private List<ReturnHistory> history;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnItem {
        private Long id;
        private Long orderItemId;
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal refundAmount;
        private String reason;
        private String condition;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnHistory {
        private Long id;
        private String status;
        private String description;
        private LocalDateTime createdAt;
    }
}

