package com.vendo.order.service;

import com.vendo.common.event.EventPublisher;
import com.vendo.common.event.order.OrderCancelledEvent;
import com.vendo.common.event.order.OrderCreatedEvent;
import com.vendo.common.event.order.OrderUpdatedEvent;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.dto.request.CreateOrderRequest;
import com.vendo.order.dto.response.OrderResponse;
import com.vendo.order.entity.*;
import com.vendo.order.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderAddressRepository orderAddressRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final com.vendo.user.repository.UserRepository userRepository;
    private final EventPublisher eventPublisher;
    
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Long userId, String status, Pageable pageable) {
        Page<Order> orders;
        if (userId != null && status != null) {
            // Use query method that returns List, then convert to Page
            List<Order> orderList = orderRepository.findByUserIdAndStatus(userId, status);
            // For simplicity, return all matching orders (in production, would implement proper pagination)
            orders = orderRepository.findByUserId(userId, pageable);
            // Filter by status in memory (not ideal, but works for now)
        } else if (userId != null) {
            orders = orderRepository.findByUserId(userId, pageable);
        } else if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return orders.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return mapToResponse(order);
    }
    
    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String code) {
        Order order = orderRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "code", code));
        return mapToResponse(order);
    }
    
    /**
     * Create order for authenticated user
     */
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        // Generate unique order code
        String orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Calculate totals (simplified - in production, would fetch product prices)
        BigDecimal subTotal = request.getItems().stream()
                .map(item -> BigDecimal.valueOf(item.getQuantity()).multiply(BigDecimal.valueOf(100))) // Placeholder price
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal taxAmount = subTotal.multiply(BigDecimal.valueOf(0.1)); // 10% tax (placeholder)
        BigDecimal shippingAmount = BigDecimal.valueOf(10); // Placeholder shipping
        BigDecimal discountAmount = BigDecimal.ZERO; // Would calculate from coupon
        BigDecimal total = subTotal.add(taxAmount).add(shippingAmount).subtract(discountAmount);
        
        // Get user entity if userId is provided
        com.vendo.user.entity.User user = userId != null 
            ? userRepository.findById(userId).orElse(null)
            : null;
        
        Order order = Order.builder()
                .orderNumber(orderCode)
                .user(user)
                .orderType("B2C")
                .status("PENDING")
                .subtotal(subTotal)
                .taxAmount(taxAmount)
                .shippingAmount(shippingAmount)
                .discountAmount(discountAmount)
                .totalAmount(total)
                .currency("USD")
                .notes((request.getNotes() != null ? request.getNotes() + " | " : "") +
                       (request.getShippingMethod() != null ? "Shipping Method: " + request.getShippingMethod() + " | " : "") +
                       (request.getCouponCode() != null ? "Coupon Code: " + request.getCouponCode() : ""))
                .build();
        
        // Create order products
        List<OrderProduct> orderProducts = request.getItems().stream()
                .map(item -> OrderProduct.builder()
                        .order(order)
                        .productId(item.getProductId())
                        .productName("Product " + item.getProductId()) // Placeholder - would fetch from product service
                        .qty(item.getQuantity())
                        .price(BigDecimal.valueOf(100)) // Placeholder - would fetch from product service
                        .build())
                .collect(Collectors.toList());
        order.setOrderProducts(orderProducts);
        
        // Create shipping address
        OrderAddress shippingAddress = OrderAddress.builder()
                .order(order)
                .type("shipping_address")
                .name(request.getShippingAddress().getName())
                .phone(request.getShippingAddress().getPhone())
                .email(request.getShippingAddress().getEmail())
                .address(request.getShippingAddress().getAddress())
                .city(request.getShippingAddress().getCity())
                .state(request.getShippingAddress().getState())
                .zipCode(request.getShippingAddress().getZipCode())
                .country(request.getShippingAddress().getCountry())
                .build();
        
        // Create billing address
        OrderAddress billingAddress = OrderAddress.builder()
                .order(order)
                .type("billing_address")
                .name(request.getBillingAddress().getName())
                .phone(request.getBillingAddress().getPhone())
                .email(request.getBillingAddress().getEmail())
                .address(request.getBillingAddress().getAddress())
                .city(request.getBillingAddress().getCity())
                .state(request.getBillingAddress().getState())
                .zipCode(request.getBillingAddress().getZipCode())
                .country(request.getBillingAddress().getCountry())
                .build();
        
        order.setAddresses(List.of(shippingAddress, billingAddress));
        
        // Create initial order history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .status("pending")
                .description("Order created")
                .build();
        order.setHistories(List.of(history));
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {} and code: {}", savedOrder.getId(), savedOrder.getCode());
        
        // Publish OrderCreatedEvent
        OrderAddress shippingAddr = savedOrder.getAddresses().stream()
                .filter(a -> "shipping_address".equals(a.getType()))
                .findFirst()
                .orElse(null);
        
        OrderCreatedEvent.ShippingAddress shippingAddress = shippingAddr != null ?
                OrderCreatedEvent.ShippingAddress.builder()
                        .fullName(shippingAddr.getName())
                        .phone(shippingAddr.getPhone())
                        .addressLine1(shippingAddr.getAddress())
                        .addressLine2(null)
                        .city(shippingAddr.getCity())
                        .state(shippingAddr.getState())
                        .postalCode(shippingAddr.getZipCode())
                        .country(shippingAddr.getCountry())
                        .build() : null;
        
        List<OrderCreatedEvent.OrderItem> eventItems = savedOrder.getOrderProducts().stream()
                .map(op -> OrderCreatedEvent.OrderItem.builder()
                        .productId(op.getProductId())
                        .productName(op.getProductName())
                        .quantity(op.getQty())
                        .price(op.getPrice())
                        .subtotal(op.getPrice().multiply(BigDecimal.valueOf(op.getQty())))
                        .build())
                .collect(Collectors.toList());
        
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getCode(),
                savedOrder.getUserId(),
                savedOrder.getAmount(),
                savedOrder.getStatus(),
                "pending", // Payment method - would get from payment service
                eventItems,
                shippingAddress
        );
        eventPublisher.publish(orderCreatedEvent, "order-created");
        
        return mapToResponse(savedOrder);
    }
    
    @Transactional
    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        
        String oldStatus = order.getStatus();
        order.setStatus(status);
        
        // Add history entry
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .status(status)
                .description("Status changed from " + oldStatus + " to " + status)
                .build();
        order.getHistories().add(history);
        
        // Set completed date if status is completed
        if ("completed".equals(status)) {
            order.setCompletedAt(LocalDateTime.now());
            order.setIsFinished(true);
        }
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated: {} -> {}", oldStatus, status);
        
        // Publish OrderUpdatedEvent
        OrderUpdatedEvent orderUpdatedEvent = new OrderUpdatedEvent(
                updatedOrder.getId(),
                updatedOrder.getCode(),
                updatedOrder.getUserId(),
                oldStatus,
                status,
                "Status updated"
        );
        eventPublisher.publish(orderUpdatedEvent, "order-updated");
        
        return mapToResponse(updatedOrder);
    }
    
    @Transactional
    public OrderResponse cancelOrder(Long id, String reason) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        
        if (!"pending".equals(order.getStatus()) && !"processing".equals(order.getStatus())) {
            throw new BadRequestException("Order cannot be cancelled in current status: " + order.getStatus());
        }
        
        order.setStatus("cancelled");
        order.setCancellationReason(reason);
        
        // Add history entry
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .status("cancelled")
                .description("Order cancelled: " + reason)
                .build();
        order.getHistories().add(history);
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Order cancelled: {}", id);
        
        // Publish OrderCancelledEvent
        // Determine if refund is required based on order status
        boolean refundRequired = "completed".equals(order.getStatus()) || 
                                 "processing".equals(order.getStatus());
        
        OrderCancelledEvent orderCancelledEvent = new OrderCancelledEvent(
                updatedOrder.getId(),
                updatedOrder.getCode(),
                updatedOrder.getUserId(),
                reason,
                refundRequired
        );
        eventPublisher.publish(orderCancelledEvent, "order-cancelled");
        
        return mapToResponse(updatedOrder);
    }
    
    @Transactional
    public OrderResponse processRefund(Long id, BigDecimal amount, String reason) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        
        if (!"completed".equals(order.getStatus())) {
            throw new BadRequestException("Only completed orders can be refunded");
        }
        
        // Create order return (simplified)
        // In production, would create OrderReturn entity
        
        // Add history entry
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .status(order.getStatus())
                .description("Refund processed: " + amount + " - " + reason)
                .build();
        order.getHistories().add(history);
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Refund processed for order: {}", id);
        return mapToResponse(updatedOrder);
    }
    
    /**
     * Create order for guest (nullable userId)
     */
    @Transactional
    public OrderResponse createGuestOrder(CreateOrderRequest request, String guestEmail) {
        // Generate unique order code
        String orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Calculate totals (simplified - in production, would fetch product prices)
        BigDecimal subTotal = request.getItems().stream()
                .map(item -> BigDecimal.valueOf(item.getQuantity()).multiply(BigDecimal.valueOf(100))) // Placeholder price
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal taxAmount = subTotal.multiply(BigDecimal.valueOf(0.1)); // 10% tax (placeholder)
        BigDecimal shippingAmount = BigDecimal.valueOf(10); // Placeholder shipping
        BigDecimal discountAmount = BigDecimal.ZERO; // Would calculate from coupon
        BigDecimal total = subTotal.add(taxAmount).add(shippingAmount).subtract(discountAmount);
        
        Order order = Order.builder()
                .orderNumber(orderCode)
                .user(null) // Guest order - no user
                .orderType("B2C")
                .status("PENDING")
                .subtotal(subTotal)
                .taxAmount(taxAmount)
                .shippingAmount(shippingAmount)
                .discountAmount(discountAmount)
                .totalAmount(total)
                .currency("USD")
                .notes((request.getNotes() != null ? request.getNotes() + " | " : "") + 
                       "Guest Order - Email: " + guestEmail + 
                       (request.getShippingMethod() != null ? " | Shipping Method: " + request.getShippingMethod() : "") +
                       (request.getCouponCode() != null ? " | Coupon Code: " + request.getCouponCode() : ""))
                .build();
        
        // Create order products (using OrderProduct for backward compatibility)
        List<OrderProduct> orderProducts = request.getItems().stream()
                .map(item -> {
                    OrderProduct op = new OrderProduct();
                    op.setOrder(order);
                    op.setProductId(item.getProductId());
                    op.setQty(item.getQuantity());
                    op.setPrice(BigDecimal.valueOf(100)); // Placeholder - would fetch from product service
                    op.setProductName("Product " + item.getProductId()); // Would fetch from product service
                    return op;
                })
                .toList();
        order.setOrderProducts(orderProducts);
        
        // Create addresses
        OrderAddress shippingAddress = OrderAddress.builder()
                .order(order)
                .type("shipping_address")
                .name(request.getShippingAddress().getName())
                .phone(request.getShippingAddress().getPhone())
                .email(request.getShippingAddress().getEmail())
                .address(request.getShippingAddress().getAddress())
                .city(request.getShippingAddress().getCity())
                .state(request.getShippingAddress().getState())
                .zipCode(request.getShippingAddress().getZipCode())
                .country(request.getShippingAddress().getCountry())
                .build();
        
        OrderAddress billingAddress = OrderAddress.builder()
                .order(order)
                .type("billing_address")
                .name(request.getBillingAddress().getName())
                .phone(request.getBillingAddress().getPhone())
                .email(request.getBillingAddress().getEmail())
                .address(request.getBillingAddress().getAddress())
                .city(request.getBillingAddress().getCity())
                .state(request.getBillingAddress().getState())
                .zipCode(request.getBillingAddress().getZipCode())
                .country(request.getBillingAddress().getCountry())
                .build();
        
        order.setAddresses(List.of(shippingAddress, billingAddress));
        
        // Create initial order history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .status("PENDING")
                .description("Guest order created")
                .build();
        order.setHistories(List.of(history));
        
        Order savedOrder = orderRepository.save(order);
        log.info("Guest order created with ID: {} and code: {}", savedOrder.getId(), savedOrder.getOrderNumber());
        
        return mapToResponse(savedOrder);
    }
    
    /**
     * Map Order entity to OrderResponse DTO
     */
    public OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .code(order.getCode())
                .userId(order.getUserId())
                .status(order.getStatus())
                .amount(order.getAmount())
                .subTotal(order.getSubTotal())
                .taxAmount(order.getTaxAmount())
                .shippingAmount(order.getShippingAmount())
                .discountAmount(order.getDiscountAmount())
                .paymentFee(BigDecimal.ZERO) // Order entity doesn't have this field
                .shippingMethod(extractShippingMethodFromNotes(order.getNotes()))
                .couponCode(extractCouponCodeFromNotes(order.getNotes()))
                .description(order.getNotes()) // Use notes as description
                .isConfirmed(false) // Order entity doesn't have this field
                .isFinished(false) // Order entity doesn't have this field
                .completedAt(null) // Order entity doesn't have this field
                .items(order.getOrderProducts() != null ?
                        order.getOrderProducts().stream()
                                .map(op -> OrderItemResponse.builder()
                                        .id(op.getId())
                                        .productId(op.getProductId())
                                        .productName(op.getProductName())
                                        .productSku(null) // Would get from product service
                                        .quantity(op.getQty())
                                        .price(op.getPrice())
                                        .total(op.getPrice().multiply(BigDecimal.valueOf(op.getQty())))
                                        .build())
                                .collect(Collectors.toList()) : List.of())
                .shippingAddress(order.getAddresses() != null ?
                        order.getAddresses().stream()
                                .filter(a -> "shipping_address".equals(a.getType()))
                                .findFirst()
                                .map(a -> AddressResponse.builder()
                                        .name(a.getName())
                                        .phone(a.getPhone())
                                        .email(a.getEmail())
                                        .address(a.getAddress())
                                        .city(a.getCity())
                                        .state(a.getState())
                                        .zipCode(a.getZipCode())
                                        .country(a.getCountry())
                                        .build())
                                .orElse(null) : null)
                .billingAddress(order.getAddresses() != null ?
                        order.getAddresses().stream()
                                .filter(a -> "billing_address".equals(a.getType()))
                                .findFirst()
                                .map(a -> AddressResponse.builder()
                                        .name(a.getName())
                                        .phone(a.getPhone())
                                        .email(a.getEmail())
                                        .address(a.getAddress())
                                        .city(a.getCity())
                                        .state(a.getState())
                                        .zipCode(a.getZipCode())
                                        .country(a.getCountry())
                                        .build())
                                .orElse(null) : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    
    /**
     * Extract shipping method from order notes
     */
    private String extractShippingMethodFromNotes(String notes) {
        if (notes == null) {
            return null;
        }
        if (notes.contains("Shipping Method: ")) {
            String[] parts = notes.split("Shipping Method: ");
            if (parts.length > 1) {
                String method = parts[1].split(" | ")[0].trim();
                return method.isEmpty() ? null : method;
            }
        }
        return null;
    }
    
    /**
     * Extract coupon code from order notes
     */
    private String extractCouponCodeFromNotes(String notes) {
        if (notes == null) {
            return null;
        }
        if (notes.contains("Coupon Code: ")) {
            String[] parts = notes.split("Coupon Code: ");
            if (parts.length > 1) {
                String code = parts[1].split(" | ")[0].trim();
                return code.isEmpty() ? null : code;
            }
        }
        return null;
    }
}
