package com.vendo.b2b.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringOrderResponse {
    
    private Long id;
    private Long businessAccountId;
    private String businessAccountName;
    private String name;
    private String description;
    private String recurrencePattern;
    private Integer recurrenceValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextOrderDate;
    private String status;
    private Integer notificationDaysBefore;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RecurringOrderItemResponse> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecurringOrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String sku;
        private Integer quantity;
        private java.math.BigDecimal unitPrice;
    }
}

