package com.vendo.b2b.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {
    
    private Long id;
    private String quoteNumber;
    private Long businessAccountId;
    private String businessAccountName;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDate expirationDate;
    private String terms;
    private String notes;
    private Long createdBy;
    private String createdByName;
    private Long convertedToOrderId;
    private String convertedToOrderNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuoteItemResponse> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuoteItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String sku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discountPercentage;
        private BigDecimal totalPrice;
        private String notes;
    }
}

