package com.vendo.order.dto.response;

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
public class OrderResponse {
    
    private Long id;
    private String code;
    private Long userId;
    private Long businessAccountId;
    private String orderType; // B2C, B2B, RETAILER
    private String status;
    private String paymentStatus; // PENDING, PAID, PARTIAL, REFUNDED, FAILED
    private String paymentMethod;
    private BigDecimal amount;
    private BigDecimal subTotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal discountAmount;
    private BigDecimal paymentFee;
    private String shippingMethod;
    private String couponCode;
    private String description;
    private Boolean isConfirmed;
    private Boolean isFinished;
    private LocalDateTime completedAt;
    private List<OrderItemResponse> items;
    private AddressResponse shippingAddress;
    private AddressResponse billingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Alias for code field
    public String getOrderNumber() {
        return code;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private Long retailerId; // For marketplace orders
        private String productName;
        private String productSku;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal total;
        private BigDecimal commissionAmount; // Platform commission for marketplace orders
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressResponse {
        private String name;
        private String phone;
        private String email;
        private String address;
        private String city;
        private String state;
        private String zipCode;
        private String country;
    }
}
