package com.vendo.retailer.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.inventory.service.InventoryService;
import com.vendo.notification.dto.request.SendNotificationRequest;
import com.vendo.notification.enums.NotificationType;
import com.vendo.notification.service.NotificationService;
import com.vendo.product.dto.request.ProductRetailerRequest;
import com.vendo.product.dto.response.ProductRetailerResponse;
import com.vendo.product.entity.Product;
import com.vendo.product.entity.ProductRetailer;
import com.vendo.product.repository.ProductRepository;
import com.vendo.product.repository.ProductRetailerRepository;
import com.vendo.retailer.entity.Retailer;
import com.vendo.retailer.repository.RetailerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetailerProductService {
    
    private final ProductRetailerRepository productRetailerRepository;
    private final ProductRepository productRepository;
    private final RetailerRepository retailerRepository;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    
    /**
     * Add a product to a retailer's catalog
     */
    @Transactional
    public ProductRetailerResponse addProduct(Long retailerId, ProductRetailerRequest request) {
        // Validate retailer exists and is active
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        if (!"ACTIVE".equals(retailer.getStatus())) {
            throw new BadRequestException("Only ACTIVE retailers can add products. Current status: " + retailer.getStatus());
        }
        
        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));
        
        // Check if product-retailer relationship already exists
        if (productRetailerRepository.existsByProductIdAndRetailerId(request.getProductId(), retailerId)) {
            throw new BadRequestException("Product already listed by this retailer");
        }
        
        // Validate price
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Price must be greater than zero");
        }
        
        ProductRetailer productRetailer = ProductRetailer.builder()
                .product(product)
                .retailer(retailer)
                .price(request.getPrice())
                .compareAtPrice(request.getCompareAtPrice())
                .costPrice(request.getCostPrice())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .lowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        
        ProductRetailer savedProductRetailer = productRetailerRepository.save(productRetailer);
        
        // Update inventory if stock quantity is provided
        if (request.getStockQuantity() != null && request.getStockQuantity() > 0) {
            try {
                // Try to create or update inventory
                try {
                    var inventory = inventoryService.getInventoryByProductAndRetailerAndWarehouse(
                            request.getProductId(), retailerId, null);
                    // Inventory exists, adjust it
                    inventoryService.adjustInventory(
                            inventory.getId(),
                            request.getStockQuantity(),
                            "Initial stock from product listing",
                            retailer.getUser().getId()
                    );
                } catch (ResourceNotFoundException e) {
                    // Inventory doesn't exist, create it
                    var inventoryRequest = new com.vendo.inventory.dto.request.InventoryRequest();
                    inventoryRequest.setProductId(request.getProductId());
                    inventoryRequest.setRetailerId(retailerId);
                    inventoryRequest.setQuantity(request.getStockQuantity());
                    inventoryService.createInventory(inventoryRequest);
                }
            } catch (Exception e) {
                log.warn("Failed to update inventory for product {}: {}", request.getProductId(), e.getMessage());
            }
        }
        
        log.info("Product added to retailer: retailerId={}, productId={}, productRetailerId={}", 
                retailerId, request.getProductId(), savedProductRetailer.getId());
        
        return mapToResponse(savedProductRetailer);
    }
    
    /**
     * Update product price for a retailer
     */
    @Transactional
    public ProductRetailerResponse updateProductPrice(Long retailerId, Long productRetailerId, BigDecimal newPrice) {
        // Validate ownership
        ProductRetailer productRetailer = validateProductOwnership(retailerId, productRetailerId);
        
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Price must be greater than zero");
        }
        
        productRetailer.setPrice(newPrice);
        ProductRetailer updated = productRetailerRepository.save(productRetailer);
        
        log.info("Product price updated: retailerId={}, productRetailerId={}, newPrice={}", 
                retailerId, productRetailerId, newPrice);
        
        return mapToResponse(updated);
    }
    
    /**
     * Update stock quantity for a retailer product
     */
    @Transactional
    public ProductRetailerResponse updateStock(Long retailerId, Long productRetailerId, Integer newStockQuantity) {
        // Validate ownership
        ProductRetailer productRetailer = validateProductOwnership(retailerId, productRetailerId);
        
        if (newStockQuantity == null || newStockQuantity < 0) {
            throw new BadRequestException("Stock quantity must be non-negative");
        }
        
        Integer oldStockQuantity = productRetailer.getStockQuantity();
        productRetailer.setStockQuantity(newStockQuantity);
        ProductRetailer updated = productRetailerRepository.save(productRetailer);
        
        // Update inventory
        try {
            Integer quantityDifference = newStockQuantity - oldStockQuantity;
            if (quantityDifference != 0) {
                try {
                    var inventory = inventoryService.getInventoryByProductAndRetailerAndWarehouse(
                            productRetailer.getProduct().getId(), retailerId, null);
                    // Inventory exists, adjust it
                    inventoryService.adjustInventory(
                            inventory.getId(),
                            quantityDifference,
                            "Stock update from retailer product management",
                            productRetailer.getRetailer().getUser().getId()
                    );
                } catch (ResourceNotFoundException e) {
                    // Inventory doesn't exist, create it
                    var inventoryRequest = new com.vendo.inventory.dto.request.InventoryRequest();
                    inventoryRequest.setProductId(productRetailer.getProduct().getId());
                    inventoryRequest.setRetailerId(retailerId);
                    inventoryRequest.setQuantity(newStockQuantity);
                    inventoryService.createInventory(inventoryRequest);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to update inventory for product {}: {}", 
                    productRetailer.getProduct().getId(), e.getMessage());
        }
        
        // Check for low stock alert
        checkAndSendLowStockAlert(updated);
        
        log.info("Stock updated: retailerId={}, productRetailerId={}, oldStock={}, newStock={}", 
                retailerId, productRetailerId, oldStockQuantity, newStockQuantity);
        
        return mapToResponse(updated);
    }
    
    /**
     * Get retailer products with pagination and filters
     */
    @Transactional(readOnly = true)
    public Page<ProductRetailerResponse> getRetailerProducts(
            Long retailerId, Boolean isActive, String searchTerm, Pageable pageable) {
        
        // Verify retailer exists
        retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        Specification<ProductRetailer> spec = Specification.where(
                (root, query, cb) -> cb.equal(root.get("retailer").get("id"), retailerId)
        );
        
        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        }
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> 
                    cb.or(
                            cb.like(cb.lower(root.get("product").get("name")), searchPattern),
                            cb.like(cb.lower(root.get("product").get("sku")), searchPattern)
                    )
            );
        }
        
        Page<ProductRetailer> productRetailers = productRetailerRepository.findAll(spec, pageable);
        return productRetailers.map(this::mapToResponse);
    }
    
    /**
     * Deactivate a product for a retailer
     */
    @Transactional
    public ProductRetailerResponse deactivateProduct(Long retailerId, Long productRetailerId) {
        // Validate ownership
        ProductRetailer productRetailer = validateProductOwnership(retailerId, productRetailerId);
        
        productRetailer.setIsActive(false);
        ProductRetailer updated = productRetailerRepository.save(productRetailer);
        
        log.info("Product deactivated: retailerId={}, productRetailerId={}", retailerId, productRetailerId);
        
        return mapToResponse(updated);
    }
    
    /**
     * Activate a product for a retailer
     */
    @Transactional
    public ProductRetailerResponse activateProduct(Long retailerId, Long productRetailerId) {
        // Validate ownership
        ProductRetailer productRetailer = validateProductOwnership(retailerId, productRetailerId);
        
        productRetailer.setIsActive(true);
        ProductRetailer updated = productRetailerRepository.save(productRetailer);
        
        log.info("Product activated: retailerId={}, productRetailerId={}", retailerId, productRetailerId);
        
        return mapToResponse(updated);
    }
    
    /**
     * Validate that a product belongs to the retailer
     */
    private ProductRetailer validateProductOwnership(Long retailerId, Long productRetailerId) {
        ProductRetailer productRetailer = productRetailerRepository.findById(productRetailerId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductRetailer", "id", productRetailerId));
        
        if (!productRetailer.getRetailer().getId().equals(retailerId)) {
            throw new BadRequestException("Product does not belong to this retailer");
        }
        
        return productRetailer;
    }
    
    /**
     * Check and send low stock alert if threshold is reached
     */
    private void checkAndSendLowStockAlert(ProductRetailer productRetailer) {
        if (productRetailer.getStockQuantity() <= productRetailer.getLowStockThreshold()) {
            try {
                Retailer retailer = productRetailer.getRetailer();
                Product product = productRetailer.getProduct();
                
                SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                        .userId(retailer.getUser().getId())
                        .notificationType(NotificationType.LOW_STOCK_ALERT)
                        .title("Low Stock Alert")
                        .message(String.format("Product '%s' (SKU: %s) is running low on stock. Current quantity: %d, Threshold: %d",
                                product.getName(),
                                product.getSku() != null ? product.getSku() : "N/A",
                                productRetailer.getStockQuantity(),
                                productRetailer.getLowStockThreshold()))
                        .build();
                
                notificationService.sendNotification(notificationRequest);
                
                log.info("Low stock alert sent: retailerId={}, productId={}, stockQuantity={}", 
                        retailer.getId(), product.getId(), productRetailer.getStockQuantity());
            } catch (Exception e) {
                log.error("Failed to send low stock alert", e);
            }
        }
    }
    
    private ProductRetailerResponse mapToResponse(ProductRetailer productRetailer) {
        ProductRetailerResponse.ProductRetailerResponseBuilder builder = ProductRetailerResponse.builder()
                .id(productRetailer.getId())
                .price(productRetailer.getPrice())
                .compareAtPrice(productRetailer.getCompareAtPrice())
                .costPrice(productRetailer.getCostPrice())
                .stockQuantity(productRetailer.getStockQuantity())
                .lowStockThreshold(productRetailer.getLowStockThreshold())
                .isActive(productRetailer.getIsActive())
                .createdAt(productRetailer.getCreatedAt())
                .updatedAt(productRetailer.getUpdatedAt());
        
        if (productRetailer.getProduct() != null) {
            builder.productId(productRetailer.getProduct().getId());
            Product product = productRetailer.getProduct();
            builder.product(ProductRetailerResponse.ProductInfo.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .sku(product.getSku())
                    .build());
        }
        
        if (productRetailer.getRetailer() != null) {
            builder.retailerId(productRetailer.getRetailer().getId());
            Retailer retailer = productRetailer.getRetailer();
            builder.retailer(ProductRetailerResponse.RetailerInfo.builder()
                    .id(retailer.getId())
                    .storeName(retailer.getStoreName())
                    .storeSlug(retailer.getStoreSlug())
                    .build());
        }
        
        // Calculate low stock flag
        boolean isLowStock = productRetailer.getStockQuantity() <= productRetailer.getLowStockThreshold();
        builder.isLowStock(isLowStock);
        
        return builder.build();
    }
}

