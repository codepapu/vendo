package com.vendo.order.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.dto.request.CreateOrderRequest;
import com.vendo.order.dto.response.OrderResponse;
import com.vendo.order.entity.Order;
import com.vendo.order.entity.OrderItem;
import com.vendo.order.entity.Shipment;
import com.vendo.order.repository.OrderRepository;
import com.vendo.order.repository.ShipmentRepository;
import com.vendo.retailer.entity.Retailer;
import com.vendo.retailer.repository.RetailerRepository;
import com.vendo.retailer.service.CommissionService;
import com.vendo.shipping.service.ShippingService;
import com.vendo.shipping.dto.request.CalculateShippingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MultiVendorOrderService {
    
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final RetailerRepository retailerRepository;
    private final OrderService orderService;
    private final ShippingService shippingService;
    private final CommissionService commissionService;
    
    /**
     * Process marketplace order - split into retailer-specific sub-orders
     */
    @Transactional
    public List<OrderResponse> processMultiVendorOrder(Long userId, CreateOrderRequest request) {
        // Group items by retailer
        Map<Long, List<CreateOrderRequest.OrderItemRequest>> itemsByRetailer = new HashMap<>();
        List<CreateOrderRequest.OrderItemRequest> platformItems = new ArrayList<>();
        
        for (CreateOrderRequest.OrderItemRequest item : request.getItems()) {
            if (item.getRetailerId() != null) {
                itemsByRetailer.computeIfAbsent(item.getRetailerId(), k -> new ArrayList<>()).add(item);
            } else {
                platformItems.add(item);
            }
        }
        
        if (itemsByRetailer.isEmpty() && platformItems.isEmpty()) {
            throw new BadRequestException("No items to process");
        }
        
        List<OrderResponse> subOrders = new ArrayList<>();
        BigDecimal totalCommission = BigDecimal.ZERO;
        
        // Create sub-order for each retailer
        for (Map.Entry<Long, List<CreateOrderRequest.OrderItemRequest>> entry : itemsByRetailer.entrySet()) {
            Long retailerId = entry.getKey();
            List<CreateOrderRequest.OrderItemRequest> retailerItems = entry.getValue();
            
            Retailer retailer = retailerRepository.findById(retailerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
            
            // Create sub-order request
            CreateOrderRequest subOrderRequest = createSubOrderRequest(request, retailerItems, retailerId);
            
            // Create order for this retailer
            OrderResponse subOrder = orderService.createOrder(userId, subOrderRequest);
            subOrders.add(subOrder);
            
            // Calculate commission
            BigDecimal retailerSubtotal = calculateSubtotal(retailerItems);
            BigDecimal commissionRate = retailer.getCommissionRate() != null ? 
                    retailer.getCommissionRate() : BigDecimal.valueOf(0.10); // Default 10%
            BigDecimal commission = retailerSubtotal.multiply(commissionRate);
            totalCommission = totalCommission.add(commission);
            
            // Create shipment for this retailer's items
            createRetailerShipment(subOrder.getId(), retailerId, retailerItems, request);
            
            log.info("Created sub-order for retailer {}: Order ID={}, Commission={}", 
                    retailerId, subOrder.getId(), commission);
        }
        
        // Create platform order if there are platform items
        if (!platformItems.isEmpty()) {
            CreateOrderRequest platformOrderRequest = createSubOrderRequest(request, platformItems, null);
            OrderResponse platformOrder = orderService.createOrder(userId, platformOrderRequest);
            subOrders.add(platformOrder);
        }
        
        // Process payment splitting
        processPaymentSplitting(request, subOrders, totalCommission);
        
        log.info("Processed multi-vendor order: {} sub-orders created, Total commission: {}", 
                subOrders.size(), totalCommission);
        
        return subOrders;
    }
    
    /**
     * Create sub-order request for a retailer
     */
    private CreateOrderRequest createSubOrderRequest(
            CreateOrderRequest originalRequest,
            List<CreateOrderRequest.OrderItemRequest> items,
            Long retailerId) {
        
        return CreateOrderRequest.builder()
                .items(items)
                .orderType(originalRequest.getOrderType())
                .businessAccountId(originalRequest.getBusinessAccountId())
                .shippingAddress(originalRequest.getShippingAddress())
                .billingAddress(originalRequest.getBillingAddress())
                .shippingMethod(originalRequest.getShippingMethod())
                .couponCode(originalRequest.getCouponCode())
                .notes(originalRequest.getNotes() + (retailerId != null ? " | Retailer ID: " + retailerId : ""))
                .build();
    }
    
    /**
     * Calculate subtotal for items
     */
    private BigDecimal calculateSubtotal(List<CreateOrderRequest.OrderItemRequest> items) {
        // This would fetch actual prices from product service
        // For now, return placeholder
        return BigDecimal.valueOf(items.size() * 100);
    }
    
    /**
     * Create shipment for retailer items
     */
    private void createRetailerShipment(
            Long orderId,
            Long retailerId,
            List<CreateOrderRequest.OrderItemRequest> items,
            CreateOrderRequest originalRequest) {
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        // Calculate shipping for this retailer's items
        BigDecimal totalWeight = calculateItemsWeight(items);
        
        CalculateShippingRequest shippingRequest = CalculateShippingRequest.builder()
                .weight(totalWeight)
                .destination(CalculateShippingRequest.AddressInfo.builder()
                        .addressLine1(originalRequest.getShippingAddress().getAddress())
                        .city(originalRequest.getShippingAddress().getCity())
                        .state(originalRequest.getShippingAddress().getState())
                        .postalCode(originalRequest.getShippingAddress().getZipCode())
                        .country(originalRequest.getShippingAddress().getCountry())
                        .build())
                .build();
        
        // Create shipment
        Shipment shipment = Shipment.builder()
                .order(order)
                .carrier("DEFAULT")
                .status("PENDING")
                .build();
        
        shipmentRepository.save(shipment);
    }
    
    /**
     * Process payment splitting across retailers
     */
    private void processPaymentSplitting(
            CreateOrderRequest originalRequest,
            List<OrderResponse> subOrders,
            BigDecimal totalCommission) {
        
        // Calculate total amount
        BigDecimal totalAmount = subOrders.stream()
                .map(OrderResponse::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Deduct commission from total
        BigDecimal amountAfterCommission = totalAmount.subtract(totalCommission);
        
        // Split payment across retailers (would integrate with PaymentSplittingService)
        log.info("Payment splitting: Total={}, Commission={}, After Commission={}", 
                totalAmount, totalCommission, amountAfterCommission);
        
        // In production, would:
        // 1. Process main payment
        // 2. Split amount to each retailer (minus commission)
        // 3. Hold commission for platform
        // 4. Create payout records
    }
    
    private BigDecimal calculateItemsWeight(List<CreateOrderRequest.OrderItemRequest> items) {
        // Placeholder - would calculate from product weights
        return BigDecimal.valueOf(items.size() * 0.5); // 0.5kg per item estimate
    }
}

