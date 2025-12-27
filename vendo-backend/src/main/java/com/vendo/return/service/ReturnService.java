package com.vendo.return.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.entity.Order;
import com.vendo.order.entity.OrderItem;
import com.vendo.order.entity.OrderReturn;
import com.vendo.order.entity.OrderReturnHistory;
import com.vendo.order.entity.OrderReturnItem;
import com.vendo.order.repository.OrderRepository;
import com.vendo.order.repository.OrderItemRepository;
import com.vendo.order.repository.OrderReturnRepository;
import com.vendo.order.repository.OrderReturnItemRepository;
import com.vendo.order.repository.OrderReturnHistoryRepository;
import com.vendo.payment.service.PaymentService;
import com.vendo.payment.dto.request.RefundPaymentRequest;
import com.vendo.return.dto.request.CreateReturnRequest;
import com.vendo.return.dto.response.ReturnResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnService {
    
    private static final int RETURN_WINDOW_DAYS = 30;
    
    private final OrderReturnRepository returnRepository;
    private final OrderReturnItemRepository returnItemRepository;
    private final OrderReturnHistoryRepository returnHistoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentService paymentService;
    
    /**
     * Create return request
     */
    @Transactional
    public ReturnResponse createReturnRequest(Long userId, CreateReturnRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));
        
        // Validate return eligibility
        validateReturnEligibility(order, userId);
        
        // Generate return code
        String returnCode = "RET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Create return entity
        OrderReturn orderReturn = OrderReturn.builder()
                .code(returnCode)
                .order(order)
                .userId(userId)
                .reason(request.getReason())
                .orderStatus(order.getStatus())
                .returnStatus("PENDING")
                .build();
        
        // Create return items
        List<OrderReturnItem> returnItems = new ArrayList<>();
        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        
        for (CreateReturnRequest.ReturnItem itemRequest : request.getItems()) {
            OrderItem orderItem = orderItemRepository.findById(itemRequest.getOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", itemRequest.getOrderItemId()));
            
            // Validate item belongs to order
            if (!orderItem.getOrder().getId().equals(order.getId())) {
                throw new BadRequestException("Order item does not belong to the specified order");
            }
            
            // Validate quantity
            if (itemRequest.getQuantity() > orderItem.getQuantity()) {
                throw new BadRequestException("Return quantity cannot exceed ordered quantity");
            }
            
            BigDecimal refundAmount = orderItem.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            
            OrderReturnItem returnItem = OrderReturnItem.builder()
                    .orderReturn(orderReturn)
                    .orderProductId(orderItem.getId())
                    .productId(orderItem.getProduct() != null ? orderItem.getProduct().getId() : null)
                    .productName(orderItem.getProductName())
                    .qty(itemRequest.getQuantity())
                    .price(orderItem.getUnitPrice())
                    .refundAmount(refundAmount)
                    .reason(itemRequest.getReason())
                    .build();
            
            returnItems.add(returnItem);
            totalRefundAmount = totalRefundAmount.add(refundAmount);
        }
        
        orderReturn.setReturnItems(returnItems);
        
        // Create initial history
        OrderReturnHistory history = OrderReturnHistory.builder()
                .orderReturn(orderReturn)
                .action("RETURN_REQUESTED")
                .description("Return request created")
                .userId(userId)
                .build();
        
        orderReturn.setHistories(List.of(history));
        
        OrderReturn savedReturn = returnRepository.save(orderReturn);
        log.info("Return request created: ID={}, Code={}", savedReturn.getId(), savedReturn.getCode());
        
        return mapToResponse(savedReturn);
    }
    
    /**
     * Approve return request
     */
    @Transactional
    public ReturnResponse approveReturn(Long returnId, Long adminId, String notes) {
        OrderReturn orderReturn = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return", "id", returnId));
        
        if (!"PENDING".equals(orderReturn.getReturnStatus())) {
            throw new BadRequestException("Only pending returns can be approved");
        }
        
        orderReturn.setReturnStatus("APPROVED");
        
        // Create history
        OrderReturnHistory history = OrderReturnHistory.builder()
                .orderReturn(orderReturn)
                .action("RETURN_APPROVED")
                .description(notes != null ? notes : "Return approved")
                .userId(adminId)
                .userType("Admin")
                .build();
        
        orderReturn.getHistories().add(history);
        
        OrderReturn savedReturn = returnRepository.save(orderReturn);
        log.info("Return approved: ID={}", savedReturn.getId());
        
        return mapToResponse(savedReturn);
    }
    
    /**
     * Reject return request
     */
    @Transactional
    public ReturnResponse rejectReturn(Long returnId, Long adminId, String reason) {
        OrderReturn orderReturn = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return", "id", returnId));
        
        if (!"PENDING".equals(orderReturn.getReturnStatus())) {
            throw new BadRequestException("Only pending returns can be rejected");
        }
        
        orderReturn.setReturnStatus("REJECTED");
        
        // Create history
        OrderReturnHistory history = OrderReturnHistory.builder()
                .orderReturn(orderReturn)
                .action("RETURN_REJECTED")
                .description(reason != null ? reason : "Return rejected")
                .userId(adminId)
                .userType("Admin")
                .build();
        
        orderReturn.getHistories().add(history);
        
        OrderReturn savedReturn = returnRepository.save(orderReturn);
        log.info("Return rejected: ID={}, Reason={}", savedReturn.getId(), reason);
        
        return mapToResponse(savedReturn);
    }
    
    /**
     * Process refund
     */
    @Transactional
    public ReturnResponse processRefund(Long returnId, Long adminId) {
        OrderReturn orderReturn = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return", "id", returnId));
        
        if (!"APPROVED".equals(orderReturn.getReturnStatus())) {
            throw new BadRequestException("Only approved returns can be refunded");
        }
        
        // Calculate total refund amount
        BigDecimal totalRefundAmount = orderReturn.getReturnItems().stream()
                .map(OrderReturnItem::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Process refund through payment gateway
        try {
            Order order = orderReturn.getOrder();
            if (order.getPayments() != null && !order.getPayments().isEmpty()) {
                // Get the first payment (in production, would handle multiple payments)
                com.vendo.payment.entity.Payment payment = order.getPayments().get(0);
                
                RefundPaymentRequest refundRequest = RefundPaymentRequest.builder()
                        .paymentId(payment.getId())
                        .amount(totalRefundAmount)
                        .reason("Return refund for order: " + order.getOrderNumber())
                        .note("Return ID: " + orderReturn.getCode())
                        .build();
                
                paymentService.processRefund(refundRequest);
            }
            
            orderReturn.setReturnStatus("COMPLETED");
            
            // Create history
            OrderReturnHistory history = OrderReturnHistory.builder()
                    .orderReturn(orderReturn)
                    .action("REFUND_PROCESSED")
                    .description("Refund processed: " + totalRefundAmount)
                    .userId(adminId)
                    .userType("Admin")
                    .build();
            
            orderReturn.getHistories().add(history);
            
            OrderReturn savedReturn = returnRepository.save(orderReturn);
            log.info("Refund processed: Return ID={}, Amount={}", savedReturn.getId(), totalRefundAmount);
            
            return mapToResponse(savedReturn);
            
        } catch (Exception e) {
            log.error("Failed to process refund for return {}: {}", returnId, e.getMessage(), e);
            throw new BadRequestException("Failed to process refund: " + e.getMessage());
        }
    }
    
    /**
     * Get return history
     */
    @Transactional(readOnly = true)
    public List<ReturnResponse> getReturnHistory(Long userId, Pageable pageable) {
        List<OrderReturn> returns = returnRepository.findByUserId(userId);
        
        // Apply pagination manually (in production, would use proper query)
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), returns.size());
        List<OrderReturn> pagedReturns = start < returns.size() ? returns.subList(start, end) : List.of();
        
        return pagedReturns.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ReturnResponse getReturnById(Long id) {
        OrderReturn orderReturn = returnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return", "id", id));
        return mapToResponse(orderReturn);
    }
    
    @Transactional(readOnly = true)
    public ReturnResponse getReturnByCode(String code) {
        OrderReturn orderReturn = returnRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Return", "code", code));
        return mapToResponse(orderReturn);
    }
    
    @Transactional(readOnly = true)
    public List<ReturnResponse> getReturnsByOrderId(Long orderId) {
        List<OrderReturn> returns = returnRepository.findByOrderId(orderId);
        return returns.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ReturnResponse> getReturnsByStatus(String status) {
        List<OrderReturn> returns = returnRepository.findByReturnStatus(status);
        return returns.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private void validateReturnEligibility(Order order, Long userId) {
        // Validate user owns the order
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new BadRequestException("User does not own this order");
        }
        
        // Validate return window (30 days from order creation)
        LocalDateTime returnDeadline = order.getCreatedAt().plusDays(RETURN_WINDOW_DAYS);
        if (LocalDateTime.now().isAfter(returnDeadline)) {
            throw new BadRequestException("Return window has expired. Returns must be requested within " + 
                    RETURN_WINDOW_DAYS + " days of order creation");
        }
        
        // Validate order status
        if ("CANCELLED".equals(order.getStatus()) || "REFUNDED".equals(order.getStatus())) {
            throw new BadRequestException("Cannot return a cancelled or already refunded order");
        }
    }
    
    private ReturnResponse mapToResponse(OrderReturn orderReturn) {
        BigDecimal totalRefundAmount = orderReturn.getReturnItems().stream()
                .map(OrderReturnItem::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<ReturnResponse.ReturnItem> items = orderReturn.getReturnItems().stream()
                .map(item -> ReturnResponse.ReturnItem.builder()
                        .id(item.getId())
                        .orderItemId(item.getOrderProductId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQty())
                        .price(item.getPrice())
                        .refundAmount(item.getRefundAmount())
                        .reason(item.getReason())
                        .build())
                .collect(Collectors.toList());
        
        List<ReturnResponse.ReturnHistory> history = orderReturn.getHistories().stream()
                .map(h -> ReturnResponse.ReturnHistory.builder()
                        .id(h.getId())
                        .status(h.getAction())
                        .description(h.getDescription())
                        .createdAt(h.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return ReturnResponse.builder()
                .id(orderReturn.getId())
                .code(orderReturn.getCode())
                .orderId(orderReturn.getOrder().getId())
                .userId(orderReturn.getUserId())
                .reason(orderReturn.getReason())
                .status(orderReturn.getReturnStatus())
                .refundAmount(totalRefundAmount)
                .refundStatus("PENDING") // Would be calculated based on payment status
                .createdAt(orderReturn.getCreatedAt())
                .updatedAt(orderReturn.getUpdatedAt())
                .items(items)
                .history(history)
                .build();
    }
}

