package com.vendo.b2b.service;

import com.vendo.b2b.dto.request.CreateOrderTemplateRequest;
import com.vendo.b2b.dto.response.OrderTemplateResponse;
import com.vendo.b2b.entity.BusinessAccount;
import com.vendo.b2b.entity.OrderTemplate;
import com.vendo.b2b.entity.OrderTemplateItem;
import com.vendo.b2b.repository.BusinessAccountRepository;
import com.vendo.b2b.repository.OrderTemplateRepository;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.dto.request.CreateOrderRequest;
import com.vendo.order.dto.request.OrderItemRequest;
import com.vendo.order.dto.response.OrderResponse;
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
public class OrderTemplateService {
    
    private final OrderTemplateRepository orderTemplateRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;
    
    /**
     * Create a new order template
     */
    @Transactional
    public OrderTemplateResponse createTemplate(Long userId, CreateOrderTemplateRequest request) {
        log.info("Creating order template for user: {}", userId);
        
        BusinessAccount businessAccount = businessAccountRepository.findById(request.getBusinessAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("BusinessAccount", "id", request.getBusinessAccountId()));
        
        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        OrderTemplate template = OrderTemplate.builder()
                .businessAccount(businessAccount)
                .name(request.getName())
                .description(request.getDescription())
                .isShared(request.getIsShared() != null ? request.getIsShared() : false)
                .createdBy(createdBy)
                .usageCount(0)
                .templateItems(new ArrayList<>())
                .build();
        
        // Create template items
        int sortOrder = 0;
        for (CreateOrderTemplateRequest.OrderTemplateItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));
            
            BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
            
            OrderTemplateItem item = OrderTemplateItem.builder()
                    .template(template)
                    .product(product)
                    .productName(product.getName())
                    .sku(product.getSku())
                    .quantity(itemRequest.getQuantity() != null ? itemRequest.getQuantity() : 1)
                    .unitPrice(unitPrice)
                    .notes(itemRequest.getNotes())
                    .sortOrder(itemRequest.getSortOrder() != null ? itemRequest.getSortOrder() : sortOrder++)
                    .build();
            template.getTemplateItems().add(item);
        }
        
        OrderTemplate saved = orderTemplateRepository.save(template);
        log.info("Order template created with ID: {}", saved.getId());
        
        return mapToResponse(saved);
    }
    
    /**
     * Update order template
     */
    @Transactional
    public OrderTemplateResponse updateTemplate(Long id, CreateOrderTemplateRequest request) {
        log.info("Updating order template: {}", id);
        
        OrderTemplate template = orderTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderTemplate", "id", id));
        
        if (request.getName() != null) template.setName(request.getName());
        if (request.getDescription() != null) template.setDescription(request.getDescription());
        if (request.getIsShared() != null) template.setIsShared(request.getIsShared());
        
        // Update items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            template.getTemplateItems().clear();
            int sortOrder = 0;
            for (CreateOrderTemplateRequest.OrderTemplateItemRequest itemRequest : request.getItems()) {
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));
                
                BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
                
                OrderTemplateItem item = OrderTemplateItem.builder()
                        .template(template)
                        .product(product)
                        .productName(product.getName())
                        .sku(product.getSku())
                        .quantity(itemRequest.getQuantity() != null ? itemRequest.getQuantity() : 1)
                        .unitPrice(unitPrice)
                        .notes(itemRequest.getNotes())
                        .sortOrder(itemRequest.getSortOrder() != null ? itemRequest.getSortOrder() : sortOrder++)
                        .build();
                template.getTemplateItems().add(item);
            }
        }
        
        OrderTemplate saved = orderTemplateRepository.save(template);
        return mapToResponse(saved);
    }
    
    /**
     * Delete order template
     */
    @Transactional
    public void deleteTemplate(Long id) {
        log.info("Deleting order template: {}", id);
        
        OrderTemplate template = orderTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderTemplate", "id", id));
        
        orderTemplateRepository.delete(template);
    }
    
    /**
     * Get templates
     */
    @Transactional(readOnly = true)
    public Page<OrderTemplateResponse> getTemplates(Long businessAccountId, Boolean isShared, Pageable pageable) {
        Page<OrderTemplate> templates;
        
        if (businessAccountId != null) {
            if (isShared != null && isShared) {
                List<OrderTemplate> list = orderTemplateRepository.findSharedTemplatesByBusinessAccountId(businessAccountId);
                templates = orderTemplateRepository.findByBusinessAccountId(businessAccountId, pageable);
            } else {
                templates = orderTemplateRepository.findByBusinessAccountId(businessAccountId, pageable);
            }
        } else {
            templates = orderTemplateRepository.findAll(pageable);
        }
        
        return templates.map(this::mapToResponse);
    }
    
    /**
     * Apply template to create new order
     */
    @Transactional
    public OrderResponse applyTemplate(Long userId, Long templateId, String notes) {
        log.info("Applying template {} to create order for user: {}", templateId, userId);
        
        OrderTemplate template = orderTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderTemplate", "id", templateId));
        
        // Build order request from template
        List<OrderItemRequest> orderItems = template.getTemplateItems().stream()
                .map(item -> OrderItemRequest.builder()
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());
        
        CreateOrderRequest orderRequest = CreateOrderRequest.builder()
                .businessAccountId(template.getBusinessAccount().getId())
                .orderType("B2B")
                .items(orderItems)
                .notes((notes != null ? notes + " | " : "") + "Created from template: " + template.getName())
                .build();
        
        // Create order
        OrderResponse orderResponse = orderService.createOrder(userId, orderRequest);
        
        // Increment usage count
        template.setUsageCount(template.getUsageCount() + 1);
        orderTemplateRepository.save(template);
        
        log.info("Order {} created from template {}", orderResponse.getOrderNumber(), templateId);
        
        return orderResponse;
    }
    
    /**
     * Map OrderTemplate entity to OrderTemplateResponse
     */
    private OrderTemplateResponse mapToResponse(OrderTemplate template) {
        List<OrderTemplateResponse.OrderTemplateItemResponse> items = template.getTemplateItems().stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(item -> OrderTemplateResponse.OrderTemplateItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .productName(item.getProductName())
                        .sku(item.getSku())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .notes(item.getNotes())
                        .sortOrder(item.getSortOrder())
                        .build())
                .collect(Collectors.toList());
        
        return OrderTemplateResponse.builder()
                .id(template.getId())
                .businessAccountId(template.getBusinessAccount() != null ? template.getBusinessAccount().getId() : null)
                .businessAccountName(template.getBusinessAccount() != null ? 
                        template.getBusinessAccount().getCompanyName() : null)
                .name(template.getName())
                .description(template.getDescription())
                .isShared(template.getIsShared())
                .createdBy(template.getCreatedBy() != null ? template.getCreatedBy().getId() : null)
                .createdByName(template.getCreatedBy() != null ? 
                        (template.getCreatedBy().getFirstName() + " " + template.getCreatedBy().getLastName()).trim() : null)
                .usageCount(template.getUsageCount())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .items(items)
                .build();
    }
}

