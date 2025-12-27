package com.vendo.b2b.service;

import com.vendo.b2b.dto.request.CreateRecurringOrderRequest;
import com.vendo.b2b.dto.response.RecurringOrderResponse;
import com.vendo.b2b.entity.BusinessAccount;
import com.vendo.b2b.entity.RecurringOrder;
import com.vendo.b2b.entity.RecurringOrderHistory;
import com.vendo.b2b.entity.RecurringOrderItem;
import com.vendo.b2b.repository.BusinessAccountRepository;
import com.vendo.b2b.repository.RecurringOrderRepository;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.notification.dto.request.SendNotificationRequest;
import com.vendo.notification.enums.NotificationType;
import com.vendo.notification.service.NotificationService;
import com.vendo.order.dto.request.CreateOrderRequest;
import com.vendo.order.dto.request.OrderItemRequest;
import com.vendo.order.entity.Order;
import com.vendo.order.repository.OrderRepository;
import com.vendo.order.service.OrderService;
import com.vendo.product.entity.Product;
import com.vendo.product.repository.ProductRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringOrderService {
    
    private final RecurringOrderRepository recurringOrderRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final NotificationService notificationService;
    
    /**
     * Create a new recurring order
     */
    @Transactional
    public RecurringOrderResponse createRecurringOrder(Long userId, CreateRecurringOrderRequest request) {
        log.info("Creating recurring order for user: {}", userId);
        
        BusinessAccount businessAccount = businessAccountRepository.findById(request.getBusinessAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("BusinessAccount", "id", request.getBusinessAccountId()));
        
        if (!"APPROVED".equals(businessAccount.getStatus())) {
            throw new BadRequestException("Business account must be APPROVED to create recurring orders");
        }
        
        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Calculate next order date based on recurrence pattern
        LocalDate nextOrderDate = calculateNextOrderDate(request.getStartDate(), 
                request.getRecurrencePattern(), request.getRecurrenceValue());
        
        // Create recurring order
        RecurringOrder recurringOrder = RecurringOrder.builder()
                .businessAccount(businessAccount)
                .name(request.getName())
                .description(request.getDescription())
                .recurrencePattern(request.getRecurrencePattern())
                .recurrenceValue(request.getRecurrenceValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .nextOrderDate(nextOrderDate)
                .status("ACTIVE")
                .notificationDaysBefore(request.getNotificationDaysBefore() != null ? 
                        request.getNotificationDaysBefore() : 3)
                .createdBy(createdBy)
                .recurringOrderItems(new ArrayList<>())
                .build();
        
        // Create recurring order items
        List<RecurringOrderItem> items = new ArrayList<>();
        for (CreateRecurringOrderRequest.RecurringOrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));
            
            BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
            
            RecurringOrderItem item = RecurringOrderItem.builder()
                    .recurringOrder(recurringOrder)
                    .product(product)
                    .productName(product.getName())
                    .sku(product.getSku())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .build();
            items.add(item);
        }
        
        recurringOrder.setRecurringOrderItems(items);
        RecurringOrder saved = recurringOrderRepository.save(recurringOrder);
        
        log.info("Recurring order created with ID: {}", saved.getId());
        
        return mapToResponse(saved);
    }
    
    /**
     * Update recurring order
     */
    @Transactional
    public RecurringOrderResponse updateRecurringOrder(Long id, CreateRecurringOrderRequest request) {
        log.info("Updating recurring order: {}", id);
        
        RecurringOrder recurringOrder = recurringOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringOrder", "id", id));
        
        if (!"ACTIVE".equals(recurringOrder.getStatus()) && !"PAUSED".equals(recurringOrder.getStatus())) {
            throw new BadRequestException("Only ACTIVE or PAUSED recurring orders can be updated");
        }
        
        // Update fields
        if (request.getName() != null) recurringOrder.setName(request.getName());
        if (request.getDescription() != null) recurringOrder.setDescription(request.getDescription());
        if (request.getRecurrencePattern() != null) {
            recurringOrder.setRecurrencePattern(request.getRecurrencePattern());
            recurringOrder.setNextOrderDate(calculateNextOrderDate(recurringOrder.getStartDate(), 
                    request.getRecurrencePattern(), request.getRecurrenceValue()));
        }
        if (request.getRecurrenceValue() != null) {
            recurringOrder.setRecurrenceValue(request.getRecurrenceValue());
            recurringOrder.setNextOrderDate(calculateNextOrderDate(recurringOrder.getStartDate(), 
                    recurringOrder.getRecurrencePattern(), request.getRecurrenceValue()));
        }
        if (request.getEndDate() != null) recurringOrder.setEndDate(request.getEndDate());
        if (request.getNotificationDaysBefore() != null) {
            recurringOrder.setNotificationDaysBefore(request.getNotificationDaysBefore());
        }
        
        // Update items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            recurringOrder.getRecurringOrderItems().clear();
            for (CreateRecurringOrderRequest.RecurringOrderItemRequest itemRequest : request.getItems()) {
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));
                
                BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
                
                RecurringOrderItem item = RecurringOrderItem.builder()
                        .recurringOrder(recurringOrder)
                        .product(product)
                        .productName(product.getName())
                        .sku(product.getSku())
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(unitPrice)
                        .build();
                recurringOrder.getRecurringOrderItems().add(item);
            }
        }
        
        RecurringOrder saved = recurringOrderRepository.save(recurringOrder);
        return mapToResponse(saved);
    }
    
    /**
     * Cancel recurring order
     */
    @Transactional
    public RecurringOrderResponse cancelRecurringOrder(Long id) {
        log.info("Cancelling recurring order: {}", id);
        
        RecurringOrder recurringOrder = recurringOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringOrder", "id", id));
        
        if ("CANCELLED".equals(recurringOrder.getStatus())) {
            throw new BadRequestException("Recurring order is already cancelled");
        }
        
        recurringOrder.setStatus("CANCELLED");
        RecurringOrder saved = recurringOrderRepository.save(recurringOrder);
        
        return mapToResponse(saved);
    }
    
    /**
     * Get recurring orders
     */
    @Transactional(readOnly = true)
    public Page<RecurringOrderResponse> getRecurringOrders(Long businessAccountId, String status, Pageable pageable) {
        Page<RecurringOrder> recurringOrders;
        
        if (businessAccountId != null && status != null) {
            List<RecurringOrder> list = recurringOrderRepository.findByBusinessAccountIdAndStatus(businessAccountId, status);
            recurringOrders = recurringOrderRepository.findByBusinessAccountId(businessAccountId, pageable);
        } else if (businessAccountId != null) {
            recurringOrders = recurringOrderRepository.findByBusinessAccountId(businessAccountId, pageable);
        } else if (status != null) {
            List<RecurringOrder> list = recurringOrderRepository.findByStatus(status);
            recurringOrders = recurringOrderRepository.findAll(pageable);
        } else {
            recurringOrders = recurringOrderRepository.findAll(pageable);
        }
        
        return recurringOrders.map(this::mapToResponse);
    }
    
    /**
     * Automatically generate orders based on schedule (scheduled task)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void generateRecurringOrders() {
        log.info("Running scheduled task to generate recurring orders");
        
        LocalDate today = LocalDate.now();
        List<RecurringOrder> dueOrders = recurringOrderRepository.findRecurringOrdersDueForGeneration(today);
        
        for (RecurringOrder recurringOrder : dueOrders) {
            try {
                generateOrderFromRecurringOrder(recurringOrder);
            } catch (Exception e) {
                log.error("Failed to generate order from recurring order: {}", recurringOrder.getId(), e);
            }
        }
    }
    
    /**
     * Send notifications before recurring orders (scheduled task)
     */
    @Scheduled(cron = "0 0 9 * * ?") // Run daily at 9 AM
    @Transactional
    public void sendRecurringOrderNotifications() {
        log.info("Running scheduled task to send recurring order notifications");
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7); // Notify for orders in next 7 days
        
        List<RecurringOrder> ordersToNotify = recurringOrderRepository.findRecurringOrdersForNotification(today, endDate);
        
        for (RecurringOrder recurringOrder : ordersToNotify) {
            try {
                int daysUntil = (int) java.time.temporal.ChronoUnit.DAYS.between(today, recurringOrder.getNextOrderDate());
                if (daysUntil <= recurringOrder.getNotificationDaysBefore()) {
                    sendRecurringOrderNotification(recurringOrder);
                }
            } catch (Exception e) {
                log.error("Failed to send notification for recurring order: {}", recurringOrder.getId(), e);
            }
        }
    }
    
    /**
     * Generate order from recurring order
     */
    private void generateOrderFromRecurringOrder(RecurringOrder recurringOrder) {
        log.info("Generating order from recurring order: {}", recurringOrder.getId());
        
        // Build order request
        List<OrderItemRequest> orderItems = recurringOrder.getRecurringOrderItems().stream()
                .map(item -> OrderItemRequest.builder()
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());
        
        CreateOrderRequest orderRequest = CreateOrderRequest.builder()
                .businessAccountId(recurringOrder.getBusinessAccount().getId())
                .orderType("B2B")
                .items(orderItems)
                .notes("Auto-generated from recurring order: " + recurringOrder.getName())
                .build();
        
        // Create order
        Long userId = recurringOrder.getBusinessAccount().getUser() != null ? 
                recurringOrder.getBusinessAccount().getUser().getId() : null;
        com.vendo.order.dto.response.OrderResponse orderResponse = orderService.createOrder(userId, orderRequest);
        
        // Get created order
        Order order = orderRepository.findByOrderNumber(orderResponse.getOrderNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderResponse.getOrderNumber()));
        
        // Create history entry
        RecurringOrderHistory history = RecurringOrderHistory.builder()
                .recurringOrder(recurringOrder)
                .order(order)
                .scheduledDate(recurringOrder.getNextOrderDate())
                .status("GENERATED")
                .build();
        recurringOrder.getRecurringOrderHistory().add(history);
        
        // Calculate next order date
        LocalDate nextOrderDate = calculateNextOrderDate(recurringOrder.getNextOrderDate(), 
                recurringOrder.getRecurrencePattern(), recurringOrder.getRecurrenceValue());
        
        // Check if end date reached
        if (recurringOrder.getEndDate() != null && nextOrderDate.isAfter(recurringOrder.getEndDate())) {
            recurringOrder.setStatus("COMPLETED");
        } else {
            recurringOrder.setNextOrderDate(nextOrderDate);
        }
        
        recurringOrderRepository.save(recurringOrder);
        log.info("Order {} generated from recurring order {}", orderResponse.getOrderNumber(), recurringOrder.getId());
    }
    
    /**
     * Calculate next order date based on recurrence pattern
     */
    private LocalDate calculateNextOrderDate(LocalDate startDate, String pattern, Integer value) {
        LocalDate nextDate = startDate;
        
        switch (pattern.toUpperCase()) {
            case "DAILY":
                nextDate = startDate.plusDays(value);
                break;
            case "WEEKLY":
                nextDate = startDate.plusWeeks(value);
                break;
            case "MONTHLY":
                nextDate = startDate.plusMonths(value);
                break;
            case "QUARTERLY":
                nextDate = startDate.plusMonths(value * 3);
                break;
            default:
                nextDate = startDate.plusDays(value);
        }
        
        return nextDate;
    }
    
    /**
     * Send notification for recurring order
     */
    private void sendRecurringOrderNotification(RecurringOrder recurringOrder) {
        try {
            if (recurringOrder.getBusinessAccount() == null || recurringOrder.getBusinessAccount().getUser() == null) {
                return;
            }
            
            Long userId = recurringOrder.getBusinessAccount().getUser().getId();
            int daysUntil = (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), recurringOrder.getNextOrderDate());
            
            Map<String, Object> data = new HashMap<>();
            data.put("recurringOrderId", recurringOrder.getId());
            data.put("name", recurringOrder.getName());
            data.put("nextOrderDate", recurringOrder.getNextOrderDate());
            data.put("daysUntil", daysUntil);
            
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(userId)
                    .notificationType(NotificationType.ORDER_CONFIRMED)
                    .channel("in_app")
                    .title("Upcoming Recurring Order")
                    .message(String.format("Your recurring order '%s' will be generated in %d day(s) on %s", 
                            recurringOrder.getName(), daysUntil, recurringOrder.getNextOrderDate()))
                    .data(data)
                    .build();
            
            notificationService.sendNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Failed to send recurring order notification", e);
        }
    }
    
    /**
     * Map RecurringOrder entity to RecurringOrderResponse
     */
    private RecurringOrderResponse mapToResponse(RecurringOrder recurringOrder) {
        List<RecurringOrderResponse.RecurringOrderItemResponse> items = recurringOrder.getRecurringOrderItems().stream()
                .map(item -> RecurringOrderResponse.RecurringOrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .productName(item.getProductName())
                        .sku(item.getSku())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());
        
        return RecurringOrderResponse.builder()
                .id(recurringOrder.getId())
                .businessAccountId(recurringOrder.getBusinessAccount() != null ? recurringOrder.getBusinessAccount().getId() : null)
                .businessAccountName(recurringOrder.getBusinessAccount() != null ? 
                        recurringOrder.getBusinessAccount().getCompanyName() : null)
                .name(recurringOrder.getName())
                .description(recurringOrder.getDescription())
                .recurrencePattern(recurringOrder.getRecurrencePattern())
                .recurrenceValue(recurringOrder.getRecurrenceValue())
                .startDate(recurringOrder.getStartDate())
                .endDate(recurringOrder.getEndDate())
                .nextOrderDate(recurringOrder.getNextOrderDate())
                .status(recurringOrder.getStatus())
                .notificationDaysBefore(recurringOrder.getNotificationDaysBefore())
                .createdBy(recurringOrder.getCreatedBy() != null ? recurringOrder.getCreatedBy().getId() : null)
                .createdByName(recurringOrder.getCreatedBy() != null ? 
                        (recurringOrder.getCreatedBy().getFirstName() + " " + recurringOrder.getCreatedBy().getLastName()).trim() : null)
                .createdAt(recurringOrder.getCreatedAt())
                .updatedAt(recurringOrder.getUpdatedAt())
                .items(items)
                .build();
    }
}

