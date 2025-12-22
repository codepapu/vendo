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
    private String status;
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
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal total;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private String name;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
