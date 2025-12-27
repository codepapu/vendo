package com.vendo.product.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.dto.request.ProductRetailerRequest;
import com.vendo.product.dto.response.ProductRetailerResponse;
import com.vendo.product.entity.Product;
import com.vendo.product.entity.ProductRetailer;
import com.vendo.product.repository.ProductRepository;
import com.vendo.product.repository.ProductRetailerRepository;
import com.vendo.retailer.entity.Retailer;
import com.vendo.retailer.repository.RetailerRepository;
import com.vendo.retailer.service.RetailerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRetailerService {
    
    private final ProductRetailerRepository productRetailerRepository;
    private final ProductRepository productRepository;
    private final RetailerRepository retailerRepository;
    private final RetailerService retailerService;
    
    @Transactional(readOnly = true)
    public Page<ProductRetailerResponse> getProductsByRetailer(Long retailerId, Boolean isActive, Pageable pageable) {
        Page<ProductRetailer> productRetailers;
        if (isActive != null) {
            productRetailers = productRetailerRepository.findByRetailerIdAndIsActive(retailerId, isActive, pageable);
        } else {
            productRetailers = productRetailerRepository.findByRetailerId(retailerId, pageable);
        }
        return productRetailers.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public List<ProductRetailerResponse> getRetailersByProduct(Long productId) {
        List<ProductRetailer> productRetailers = productRetailerRepository.findActiveRetailersByProduct(productId);
        return productRetailers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ProductRetailerResponse getProductRetailerById(Long id) {
        ProductRetailer productRetailer = productRetailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductRetailer", "id", id));
        return mapToResponse(productRetailer);
    }
    
    @Transactional(readOnly = true)
    public ProductRetailerResponse getProductRetailerByProductAndRetailer(Long productId, Long retailerId) {
        ProductRetailer productRetailer = productRetailerRepository.findByProductIdAndRetailerId(productId, retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductRetailer", 
                        "productId=" + productId + ", retailerId=" + retailerId));
        return mapToResponse(productRetailer);
    }
    
    @Transactional(readOnly = true)
    public List<ProductRetailerResponse> getLowStockProducts(Long retailerId) {
        List<ProductRetailer> productRetailers = productRetailerRepository.findLowStockProductsByRetailer(retailerId);
        return productRetailers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ProductRetailerResponse createProductRetailer(ProductRetailerRequest request) {
        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));
        
        // Validate retailer exists
        Retailer retailer = retailerRepository.findById(request.getRetailerId())
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", request.getRetailerId()));
        
        // Check if product-retailer relationship already exists
        if (productRetailerRepository.existsByProductIdAndRetailerId(request.getProductId(), request.getRetailerId())) {
            throw new BadRequestException("Product already listed by this retailer");
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
        log.info("ProductRetailer created with ID: {}", savedProductRetailer.getId());
        
        // Evict retailer dashboard cache when product is added
        if (savedProductRetailer.getRetailer() != null) {
            retailerService.evictDashboardCache(savedProductRetailer.getRetailer().getId());
        }
        
        return mapToResponse(savedProductRetailer);
    }
    
    @Transactional
    public ProductRetailerResponse updateProductRetailer(Long id, ProductRetailerRequest request) {
        ProductRetailer productRetailer = productRetailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductRetailer", "id", id));
        
        if (request.getPrice() != null) {
            productRetailer.setPrice(request.getPrice());
        }
        if (request.getCompareAtPrice() != null) {
            productRetailer.setCompareAtPrice(request.getCompareAtPrice());
        }
        if (request.getCostPrice() != null) {
            productRetailer.setCostPrice(request.getCostPrice());
        }
        if (request.getStockQuantity() != null) {
            productRetailer.setStockQuantity(request.getStockQuantity());
        }
        if (request.getLowStockThreshold() != null) {
            productRetailer.setLowStockThreshold(request.getLowStockThreshold());
        }
        if (request.getIsActive() != null) {
            productRetailer.setIsActive(request.getIsActive());
        }
        
        ProductRetailer updatedProductRetailer = productRetailerRepository.save(productRetailer);
        log.info("ProductRetailer updated with ID: {}", updatedProductRetailer.getId());
        
        // Evict retailer dashboard cache when product-retailer is updated (affects active products, low stock)
        if (updatedProductRetailer.getRetailer() != null) {
            retailerService.evictDashboardCache(updatedProductRetailer.getRetailer().getId());
        }
        
        return mapToResponse(updatedProductRetailer);
    }
    
    @Transactional
    public void deleteProductRetailer(Long id) {
        ProductRetailer productRetailer = productRetailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductRetailer", "id", id));
        
        Long retailerId = productRetailer.getRetailer() != null ? productRetailer.getRetailer().getId() : null;
        
        productRetailerRepository.delete(productRetailer);
        log.info("ProductRetailer deleted with ID: {}", id);
        
        // Evict retailer dashboard cache when product-retailer is deleted
        if (retailerId != null) {
            retailerService.evictDashboardCache(retailerId);
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

