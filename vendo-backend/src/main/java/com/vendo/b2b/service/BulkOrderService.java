package com.vendo.b2b.service;

import com.vendo.b2b.dto.request.CreateBulkOrderRequest;
import com.vendo.b2b.dto.response.BulkOrderResponse;
import com.vendo.b2b.entity.BulkOrder;
import com.vendo.b2b.entity.BulkOrderItem;
import com.vendo.b2b.entity.BusinessAccount;
import com.vendo.b2b.repository.BulkOrderRepository;
import com.vendo.b2b.repository.BusinessAccountRepository;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.dto.request.CreateOrderRequest;
import com.vendo.order.dto.request.CreateOrderRequest;
import com.vendo.order.dto.response.OrderResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkOrderService {
    
    private final BulkOrderRepository bulkOrderRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    
    /**
     * Create a new bulk order
     */
    @Transactional
    public BulkOrderResponse createBulkOrder(Long userId, CreateBulkOrderRequest request) {
        log.info("Creating bulk order for user: {}", userId);
        
        BusinessAccount businessAccount = businessAccountRepository.findById(request.getBusinessAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("BusinessAccount", "id", request.getBusinessAccountId()));
        
        if (!"APPROVED".equals(businessAccount.getStatus())) {
            throw new BadRequestException("Business account must be APPROVED to create bulk orders");
        }
        
        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        BulkOrder bulkOrder = BulkOrder.builder()
                .businessAccount(businessAccount)
                .name(request.getName())
                .description(request.getDescription())
                .sourceFileName(request.getSourceFileName())
                .sourceFileType(request.getSourceFileType())
                .totalItems(request.getItems().size())
                .processedItems(0)
                .failedItems(0)
                .status("PENDING")
                .createdBy(createdBy)
                .bulkOrderItems(new ArrayList<>())
                .build();
        
        // Create bulk order items
        for (CreateBulkOrderRequest.BulkOrderItemRequest itemRequest : request.getItems()) {
            Product product = null;
            if (itemRequest.getProductId() != null) {
                product = productRepository.findById(itemRequest.getProductId()).orElse(null);
            } else if (itemRequest.getProductSku() != null) {
                product = productRepository.findBySku(itemRequest.getProductSku()).orElse(null);
            }
            
            BigDecimal unitPrice = itemRequest.getUnitPrice() != null ? itemRequest.getUnitPrice() : 
                    (product != null && product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            
            BulkOrderItem item = BulkOrderItem.builder()
                    .bulkOrder(bulkOrder)
                    .rowNumber(itemRequest.getRowNumber())
                    .product(product)
                    .productSku(itemRequest.getProductSku())
                    .productName(itemRequest.getProductName() != null ? itemRequest.getProductName() : 
                            (product != null ? product.getName() : null))
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .status("PENDING")
                    .build();
            bulkOrder.getBulkOrderItems().add(item);
        }
        
        BulkOrder saved = bulkOrderRepository.save(bulkOrder);
        log.info("Bulk order created with ID: {} and {} items", saved.getId(), saved.getTotalItems());
        
        return mapToResponse(saved);
    }
    
    /**
     * Validate bulk order
     */
    @Transactional
    public BulkOrderResponse validateBulkOrder(Long bulkOrderId) {
        log.info("Validating bulk order: {}", bulkOrderId);
        
        BulkOrder bulkOrder = bulkOrderRepository.findById(bulkOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("BulkOrder", "id", bulkOrderId));
        
        if (!"PENDING".equals(bulkOrder.getStatus())) {
            throw new BadRequestException("Only PENDING bulk orders can be validated");
        }
        
        bulkOrder.setStatus("VALIDATING");
        bulkOrderRepository.save(bulkOrder);
        
        List<String> validationErrors = new ArrayList<>();
        int validatedCount = 0;
        int failedCount = 0;
        
        for (BulkOrderItem item : bulkOrder.getBulkOrderItems()) {
            List<String> itemErrors = new ArrayList<>();
            
            // Validate product exists
            if (item.getProduct() == null) {
                if (item.getProductSku() != null) {
                    itemErrors.add("Product with SKU '" + item.getProductSku() + "' not found");
                } else {
                    itemErrors.add("Product ID or SKU is required");
                }
            }
            
            // Validate quantity
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                itemErrors.add("Quantity must be greater than 0");
            }
            
            // Validate price
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                itemErrors.add("Unit price must be greater than 0");
            }
            
            if (itemErrors.isEmpty()) {
                item.setStatus("VALIDATED");
                validatedCount++;
            } else {
                item.setStatus("FAILED");
                item.setValidationErrors(String.join("; ", itemErrors));
                failedCount++;
                validationErrors.add("Row " + item.getRowNumber() + ": " + String.join("; ", itemErrors));
            }
        }
        
        bulkOrder.setStatus(validatedCount > 0 && failedCount == 0 ? "VALIDATED" : 
                (validatedCount > 0 ? "PARTIAL" : "FAILED"));
        bulkOrder.setValidationErrors(String.join("\n", validationErrors));
        bulkOrderRepository.save(bulkOrder);
        
        log.info("Bulk order {} validated: {} valid, {} failed", bulkOrderId, validatedCount, failedCount);
        
        return mapToResponse(bulkOrder);
    }
    
    /**
     * Process bulk order items and create orders
     */
    @Transactional
    public BulkOrderResponse processBulkOrderItems(Long bulkOrderId, Long userId) {
        log.info("Processing bulk order items for bulk order: {}", bulkOrderId);
        
        BulkOrder bulkOrder = bulkOrderRepository.findById(bulkOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("BulkOrder", "id", bulkOrderId));
        
        if (!"VALIDATED".equals(bulkOrder.getStatus()) && !"PARTIAL".equals(bulkOrder.getStatus())) {
            throw new BadRequestException("Only VALIDATED or PARTIAL bulk orders can be processed");
        }
        
        bulkOrder.setStatus("PROCESSING");
        bulkOrderRepository.save(bulkOrder);
        
        List<Long> createdOrderIds = new ArrayList<>();
        int processedCount = 0;
        int failedCount = 0;
        
        // Group items by row or process individually
        for (BulkOrderItem item : bulkOrder.getBulkOrderItems()) {
            if (!"VALIDATED".equals(item.getStatus())) {
                continue; // Skip failed items
            }
            
            try {
                // Create order for this item
                List<CreateOrderRequest.OrderItemRequest> orderItems = List.of(
                        CreateOrderRequest.OrderItemRequest.builder()
                                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                                .quantity(item.getQuantity())
                                .build()
                );
                
                CreateOrderRequest orderRequest = CreateOrderRequest.builder()
                        .businessAccountId(bulkOrder.getBusinessAccount().getId())
                        .orderType("B2B")
                        .items(orderItems)
                        .notes("Bulk order item from row " + item.getRowNumber() + 
                                (bulkOrder.getName() != null ? " - " + bulkOrder.getName() : ""))
                        .build();
                
                OrderResponse orderResponse = orderService.createOrder(userId, orderRequest);
                Order order = orderRepository.findByOrderNumber(orderResponse.getOrderNumber())
                        .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderResponse.getOrderNumber()));
                
                item.setOrder(order);
                item.setStatus("PROCESSED");
                processedCount++;
                createdOrderIds.add(order.getId());
                
            } catch (Exception e) {
                log.error("Failed to process bulk order item: {}", item.getId(), e);
                item.setStatus("FAILED");
                item.setProcessingErrors(e.getMessage());
                failedCount++;
            }
        }
        
        bulkOrder.setProcessedItems(processedCount);
        bulkOrder.setFailedItems(failedCount);
        bulkOrder.setStatus(processedCount > 0 && failedCount == 0 ? "COMPLETED" : 
                (processedCount > 0 ? "PARTIAL" : "FAILED"));
        bulkOrderRepository.save(bulkOrder);
        
        log.info("Bulk order {} processed: {} successful, {} failed", bulkOrderId, processedCount, failedCount);
        
        BulkOrderResponse response = mapToResponse(bulkOrder);
        response.setCreatedOrderIds(createdOrderIds);
        return response;
    }
    
    /**
     * Get bulk orders
     */
    @Transactional(readOnly = true)
    public Page<BulkOrderResponse> getBulkOrders(Long businessAccountId, String status, Pageable pageable) {
        Page<BulkOrder> bulkOrders;
        
        if (businessAccountId != null && status != null) {
            List<BulkOrder> list = bulkOrderRepository.findByBusinessAccountIdAndStatus(businessAccountId, status);
            bulkOrders = bulkOrderRepository.findByBusinessAccountId(businessAccountId, pageable);
        } else if (businessAccountId != null) {
            bulkOrders = bulkOrderRepository.findByBusinessAccountId(businessAccountId, pageable);
        } else if (status != null) {
            List<BulkOrder> list = bulkOrderRepository.findByStatus(status);
            bulkOrders = bulkOrderRepository.findAll(pageable);
        } else {
            bulkOrders = bulkOrderRepository.findAll(pageable);
        }
        
        return bulkOrders.map(this::mapToResponse);
    }
    
    /**
     * Map BulkOrder entity to BulkOrderResponse
     */
    private BulkOrderResponse mapToResponse(BulkOrder bulkOrder) {
        List<BulkOrderResponse.BulkOrderItemResponse> items = bulkOrder.getBulkOrderItems().stream()
                .map(item -> BulkOrderResponse.BulkOrderItemResponse.builder()
                        .id(item.getId())
                        .rowNumber(item.getRowNumber())
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .productSku(item.getProductSku())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .orderId(item.getOrder() != null ? item.getOrder().getId() : null)
                        .orderNumber(item.getOrder() != null ? item.getOrder().getOrderNumber() : null)
                        .status(item.getStatus())
                        .validationErrors(item.getValidationErrors())
                        .processingErrors(item.getProcessingErrors())
                        .build())
                .collect(Collectors.toList());
        
        return BulkOrderResponse.builder()
                .id(bulkOrder.getId())
                .businessAccountId(bulkOrder.getBusinessAccount() != null ? bulkOrder.getBusinessAccount().getId() : null)
                .businessAccountName(bulkOrder.getBusinessAccount() != null ? 
                        bulkOrder.getBusinessAccount().getCompanyName() : null)
                .name(bulkOrder.getName())
                .description(bulkOrder.getDescription())
                .sourceFileName(bulkOrder.getSourceFileName())
                .sourceFileType(bulkOrder.getSourceFileType())
                .totalItems(bulkOrder.getTotalItems())
                .processedItems(bulkOrder.getProcessedItems())
                .failedItems(bulkOrder.getFailedItems())
                .status(bulkOrder.getStatus())
                .validationErrors(bulkOrder.getValidationErrors())
                .createdBy(bulkOrder.getCreatedBy() != null ? bulkOrder.getCreatedBy().getId() : null)
                .createdByName(bulkOrder.getCreatedBy() != null ? 
                        (bulkOrder.getCreatedBy().getFirstName() + " " + bulkOrder.getCreatedBy().getLastName()).trim() : null)
                .createdAt(bulkOrder.getCreatedAt())
                .updatedAt(bulkOrder.getUpdatedAt())
                .items(items)
                .build();
    }
}

