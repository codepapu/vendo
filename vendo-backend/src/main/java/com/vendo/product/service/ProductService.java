package com.vendo.product.service;

import com.vendo.b2b.service.B2BPricingService;
import com.vendo.common.event.EventPublisher;
import com.vendo.common.event.product.ProductCreatedEvent;
import com.vendo.common.event.product.ProductStockUpdatedEvent;
import com.vendo.common.event.product.ProductUpdatedEvent;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.dto.request.ProductRequest;
import com.vendo.product.dto.response.ProductResponse;
import com.vendo.product.entity.*;
import com.vendo.product.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductTagRepository productTagRepository;
    private final ProductCollectionRepository productCollectionRepository;
    private final TaxRepository taxRepository;
    private final ProductReviewRepository productReviewRepository;
    private final EventPublisher eventPublisher;
    private final B2BPricingService b2BPricingService;
    
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(String status, Pageable pageable) {
        Page<Product> products;
        if (status != null && !status.isEmpty()) {
            products = productRepository.findByStatusAndIsVariationFalse(status, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }
        return products.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToResponse(product);
    }
    
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
        return mapToResponse(product);
    }
    
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // Validate SKU uniqueness
        if (request.getSku() != null && productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Product with SKU " + request.getSku() + " already exists");
        }
        
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .content(request.getContent())
                .status(request.getStatus() != null ? request.getStatus() : "published")
                .images(request.getImages())
                .videoMedia(request.getVideoMedia())
                .sku(request.getSku())
                .order(request.getOrder() != null ? request.getOrder() : 0)
                .quantity(request.getQuantity())
                .allowCheckoutWhenOutOfStock(request.getAllowCheckoutWhenOutOfStock() != null ? 
                        request.getAllowCheckoutWhenOutOfStock() : false)
                .withStorehouseManagement(request.getWithStorehouseManagement() != null ? 
                        request.getWithStorehouseManagement() : false)
                .stockStatus(request.getStockStatus() != null ? request.getStockStatus() : "in_stock")
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .isVariation(request.getIsVariation() != null ? request.getIsVariation() : false)
                .saleType(request.getSaleType() != null ? request.getSaleType() : 0)
                .price(request.getPrice())
                .salePrice(request.getSalePrice())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .length(request.getLength())
                .wide(request.getWidth()) // Product entity uses "wide" not "width"
                .height(request.getHeight())
                .weight(request.getWeight())
                .build();
        
        // Set brand if provided
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", request.getBrandId()));
            product.setBrand(brand);
        }
        
        // Set taxId if provided
        if (request.getTaxId() != null) {
            Tax tax = taxRepository.findById(request.getTaxId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tax", "id", request.getTaxId()));
            product.setTaxId(request.getTaxId());
            product.getTaxes().add(tax);
        }
        
        // Set categories if provided
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            if (categories.size() != request.getCategoryIds().size()) {
                throw new BadRequestException("One or more categories not found");
            }
            product.setCategories(categories);
        }
        
        // Set tags if provided
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<ProductTag> tags = productTagRepository.findAllById(request.getTagIds());
            if (tags.size() != request.getTagIds().size()) {
                throw new BadRequestException("One or more tags not found");
            }
            product.setTags(tags);
        }
        
        // Set collections if provided
        if (request.getCollectionIds() != null && !request.getCollectionIds().isEmpty()) {
            List<ProductCollection> collections = productCollectionRepository.findAllById(request.getCollectionIds());
            if (collections.size() != request.getCollectionIds().size()) {
                throw new BadRequestException("One or more collections not found");
            }
            product.setCollections(collections);
        }
        
        Product savedProduct = productRepository.save(product);
        log.info("Product created with ID: {}", savedProduct.getId());
        
        // Evict products cache when new product is created
        evictProductsCache();
        
        // Publish ProductCreatedEvent
        Long categoryId = savedProduct.getCategories() != null && !savedProduct.getCategories().isEmpty() ?
                savedProduct.getCategories().get(0).getId() : null;
        
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getSku(),
                savedProduct.getPrice(),
                categoryId,
                savedProduct.getQuantity()
        );
        eventPublisher.publish(productCreatedEvent, "product-created");
        
        return mapToResponse(savedProduct);
    }
    
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        // Track old values for events
        Integer oldQuantity = product.getQuantity();
        String oldSku = product.getSku();
        Map<String, Object> updatedFields = new HashMap<>();
        
        // Validate SKU uniqueness if changed
        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            if (productRepository.existsBySku(request.getSku())) {
                throw new BadRequestException("Product with SKU " + request.getSku() + " already exists");
            }
            product.setSku(request.getSku());
            updatedFields.put("sku", request.getSku());
        }
        
        // Update fields
        if (request.getName() != null) {
            product.setName(request.getName());
            updatedFields.put("name", request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
            updatedFields.put("description", request.getDescription());
        }
        if (request.getContent() != null) {
            product.setContent(request.getContent());
            updatedFields.put("content", request.getContent());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
            updatedFields.put("status", request.getStatus());
        }
        if (request.getImages() != null) {
            product.setImages(request.getImages());
            updatedFields.put("images", request.getImages());
        }
        if (request.getVideoMedia() != null) {
            product.setVideoMedia(request.getVideoMedia());
            updatedFields.put("videoMedia", request.getVideoMedia());
        }
        if (request.getOrder() != null) {
            product.setOrder(request.getOrder());
            updatedFields.put("order", request.getOrder());
        }
        if (request.getQuantity() != null) {
            product.setQuantity(request.getQuantity());
            updatedFields.put("quantity", request.getQuantity());
        }
        if (request.getAllowCheckoutWhenOutOfStock() != null) 
            product.setAllowCheckoutWhenOutOfStock(request.getAllowCheckoutWhenOutOfStock());
        if (request.getWithStorehouseManagement() != null) 
            product.setWithStorehouseManagement(request.getWithStorehouseManagement());
        if (request.getStockStatus() != null) product.setStockStatus(request.getStockStatus());
        if (request.getIsFeatured() != null) product.setIsFeatured(request.getIsFeatured());
        if (request.getIsVariation() != null) product.setIsVariation(request.getIsVariation());
        if (request.getSaleType() != null) product.setSaleType(request.getSaleType());
        boolean priceChanged = false;
        if (request.getPrice() != null && !request.getPrice().equals(product.getPrice())) {
            product.setPrice(request.getPrice());
            priceChanged = true;
        }
        if (request.getSalePrice() != null) product.setSalePrice(request.getSalePrice());
        if (request.getStartDate() != null) product.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) product.setEndDate(request.getEndDate());
        if (request.getLength() != null) product.setLength(request.getLength());
        if (request.getWidth() != null) product.setWide(request.getWidth()); // Product entity uses "wide" not "width"
        if (request.getHeight() != null) product.setHeight(request.getHeight());
        if (request.getWeight() != null) product.setWeight(request.getWeight());
        
        // Update brand
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", request.getBrandId()));
            product.setBrand(brand);
        }
        
        // Update taxId
        if (request.getTaxId() != null) {
            taxRepository.findById(request.getTaxId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tax", "id", request.getTaxId()));
            product.setTaxId(request.getTaxId());
        }
        
        // Update categories
        if (request.getCategoryIds() != null) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            product.setCategories(categories);
        }
        
        // Update tags
        if (request.getTagIds() != null) {
            List<ProductTag> tags = productTagRepository.findAllById(request.getTagIds());
            product.setTags(tags);
        }
        
        // Update collections
        if (request.getCollectionIds() != null) {
            List<ProductCollection> collections = productCollectionRepository.findAllById(request.getCollectionIds());
            product.setCollections(collections);
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated with ID: {}", updatedProduct.getId());
        
        // Evict B2B pricing cache if product price changed
        if (priceChanged) {
            b2BPricingService.evictPricingCacheForProduct(updatedProduct.getId());
        }
        
        // Evict products cache when product is updated
        evictProductsCache();
        
        // Publish ProductUpdatedEvent
        if (!updatedFields.isEmpty()) {
            ProductUpdatedEvent productUpdatedEvent = new ProductUpdatedEvent(
                    updatedProduct.getId(),
                    updatedProduct.getName(),
                    updatedProduct.getSku(),
                    updatedFields
            );
            eventPublisher.publish(productUpdatedEvent, "product-updated");
        }
        
        // Publish ProductStockUpdatedEvent if quantity changed
        if (request.getQuantity() != null && !request.getQuantity().equals(oldQuantity)) {
            Integer quantityChange = request.getQuantity() - oldQuantity;
            ProductStockUpdatedEvent stockUpdatedEvent = new ProductStockUpdatedEvent(
                    updatedProduct.getId(),
                    updatedProduct.getSku(),
                    oldQuantity,
                    request.getQuantity(),
                    quantityChange,
                    "manual_update", // Reason for stock change
                    null // Order ID - would be set if stock change is due to order
            );
            eventPublisher.publish(stockUpdatedEvent, "product-stock-updated");
        }
        
        return mapToResponse(updatedProduct);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        productRepository.delete(product);
        log.info("Product deleted with ID: {}", id);
        
        // Evict products cache when product is deleted
        evictProductsCache();
    }
    
    /**
     * Evict all products cache
     * Called when products are created, updated, or deleted
     */
    @CacheEvict(value = "products", allEntries = true)
    public void evictProductsCache() {
        log.debug("Evicting products cache");
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, String status, Pageable pageable) {
        Page<Product> products = productRepository.searchProducts(
                status != null ? status : "published", 
                keyword, 
                pageable);
        return products.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, String status, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryIdAndStatus(
                categoryId, 
                status != null ? status : "published", 
                pageable);
        return products.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByBrand(Long brandId, String status, Pageable pageable) {
        Page<Product> products = productRepository.findByBrandIdAndStatus(
                brandId, 
                status != null ? status : "published", 
                pageable);
        return products.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, 
                                                         String status, Pageable pageable) {
        Page<Product> products = productRepository.findByPriceRange(
                status != null ? status : "published",
                minPrice,
                maxPrice,
                pageable);
        return products.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts(String status) {
        List<Product> products = productRepository.findFeaturedProducts(
                status != null ? status : "published");
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductVariations(Long productId) {
        // Get variations for a configurable product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        List<ProductVariation> variations = product.getConfigurableVariations();
        
        return variations.stream()
                .map(v -> mapToResponse(v.getProduct()))
                .collect(Collectors.toList());
    }
    
    private ProductResponse mapToResponse(Product product) {
        // Get rating information
        Double averageRating = productReviewRepository.calculateAverageRating(product.getId());
        Long reviewCount = productReviewRepository.countByProductId(product.getId());
        
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .content(product.getContent())
                .status(product.getStatus())
                .images(product.getImages())
                .videoMedia(product.getVideoMedia())
                .sku(product.getSku())
                .order(product.getOrder())
                .quantity(product.getQuantity())
                .allowCheckoutWhenOutOfStock(product.getAllowCheckoutWhenOutOfStock())
                .withStorehouseManagement(product.getWithStorehouseManagement())
                .stockStatus(product.getStockStatus())
                .isFeatured(product.getIsFeatured())
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .isVariation(product.getIsVariation())
                .saleType(product.getSaleType())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .startDate(product.getStartDate())
                .endDate(product.getEndDate())
                .length(product.getLength())
                .width(product.getWide()) // Product entity uses "wide" field
                .height(product.getHeight())
                .weight(product.getWeight())
                .taxId(product.getTaxId())
                .categoryIds(product.getCategories() != null ? 
                        product.getCategories().stream().map(Category::getId).collect(Collectors.toList()) : null)
                .categoryNames(product.getCategories() != null ? 
                        product.getCategories().stream().map(Category::getName).collect(Collectors.toList()) : null)
                .tagIds(product.getTags() != null ? 
                        product.getTags().stream().map(ProductTag::getId).collect(Collectors.toList()) : null)
                .tagNames(product.getTags() != null ? 
                        product.getTags().stream().map(ProductTag::getName).collect(Collectors.toList()) : null)
                .collectionIds(product.getCollections() != null ? 
                        product.getCollections().stream().map(ProductCollection::getId).collect(Collectors.toList()) : null)
                .collectionNames(product.getCollections() != null ? 
                        product.getCollections().stream().map(ProductCollection::getName).collect(Collectors.toList()) : null)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0L)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
