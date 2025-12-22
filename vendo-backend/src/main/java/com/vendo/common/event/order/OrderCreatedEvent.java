package com.vendo.common.event.order;

import com.vendo.common.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Event published when a new order is created.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderCreatedEvent extends BaseEvent {
    
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
    private String paymentMethod;
    private List<OrderItem> items;
    private ShippingAddress shippingAddress;
    
    public OrderCreatedEvent(Long orderId, String orderNumber, Long userId, 
                            BigDecimal totalAmount, String status, 
                            String paymentMethod, List<OrderItem> items,
                            ShippingAddress shippingAddress) {
        super("order.created", "order-service", userId);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.items = items;
        this.shippingAddress = shippingAddress;
    }
    
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
    }
    
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddress {
        private String fullName;
        private String phone;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }
}
