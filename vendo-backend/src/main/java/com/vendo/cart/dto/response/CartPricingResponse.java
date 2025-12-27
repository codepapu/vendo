package com.vendo.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartPricingResponse {
    
    private Long cartId;
    private String cartType;
    private List<CartItemPricing> items;
    private Map<Long, BigDecimal> retailerShipping; // retailerId -> shipping cost
    private Map<Long, BigDecimal> retailerSubtotals; // retailerId -> subtotal
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shipping;
    private BigDecimal total;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemPricing {
        private Long itemId;
        private Long productId;
        private String productName;
        private Long retailerId;
        private String retailerName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal itemTotal;
        private Boolean isB2BPricing;
    }
}

