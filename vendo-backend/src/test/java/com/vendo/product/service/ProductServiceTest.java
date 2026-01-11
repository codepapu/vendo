package com.vendo.product.service;

import com.vendo.b2b.service.B2BPricingService;
import com.vendo.common.event.EventPublisher;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.dto.request.ProductRequest;
import com.vendo.product.dto.response.ProductResponse;
import com.vendo.product.entity.*;
import com.vendo.product.repository.*;
import com.vendo.search.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private ProductTagRepository productTagRepository;

    @Mock
    private ProductCollectionRepository productCollectionRepository;

    @Mock
    private TaxRepository taxRepository;

    @Mock
    private ProductReviewRepository productReviewRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private B2BPricingService b2BPricingService;

    @Mock
    private SearchService searchService;

    @InjectMocks
    private ProductService productService;

    private ProductRequest validRequest;
    private Product testProduct;
    private Brand testBrand;
    private Category testCategory;
    private Tax testTax;

    @BeforeEach
    void setUp() {
        testBrand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Test Category")
                .build();

        testTax = Tax.builder()
                .id(1L)
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .slug("test-product")
                .description("Test Description")
                .sku("TEST-SKU-001")
                .price(new BigDecimal("99.99"))
                .salePrice(new BigDecimal("79.99"))
                .quantity(100)
                .status("published")
                .brand(testBrand)
                .categories(Arrays.asList(testCategory))
                .taxId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validRequest = ProductRequest.builder()
                .name("Test Product")
                .description("Test Description")
                .sku("TEST-SKU-001")
                .price(new BigDecimal("99.99"))
                .salePrice(new BigDecimal("79.99"))
                .quantity(100)
                .status("published")
                .brandId(1L)
                .taxId(1L)
                .categoryIds(Arrays.asList(1L))
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct_Success() {
        // Given
        when(productRepository.existsBySku(validRequest.getSku())).thenReturn(false);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(testBrand));
        when(taxRepository.findById(1L)).thenReturn(Optional.of(testTax));
        when(categoryRepository.findAllById(anyList())).thenReturn(Arrays.asList(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productReviewRepository.calculateAverageRating(anyLong())).thenReturn(null);
        when(productReviewRepository.countByProductId(anyLong())).thenReturn(0L);
        doNothing().when(searchService).indexProduct(anyLong());
        doNothing().when(eventPublisher).publish(any(), anyString());

        // When
        ProductResponse response = productService.createProduct(validRequest);

        // Then
        assertNotNull(response);
        assertEquals("Test Product", response.getName());
        assertEquals("TEST-SKU-001", response.getSku());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(searchService, times(1)).indexProduct(anyLong());
        verify(eventPublisher, times(1)).publish(any(), eq("product-created"));
    }

    @Test
    @DisplayName("Should throw exception when SKU already exists")
    void testCreateProduct_DuplicateSku() {
        // Given
        when(productRepository.existsBySku(validRequest.getSku())).thenReturn(true);

        // When/Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productService.createProduct(validRequest);
        });
        assertTrue(exception.getMessage().contains("SKU"));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when brand not found")
    void testCreateProduct_BrandNotFound() {
        // Given
        when(productRepository.existsBySku(validRequest.getSku())).thenReturn(false);
        when(brandRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(validRequest);
        });
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void testGetProductById_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productReviewRepository.calculateAverageRating(1L)).thenReturn(4.5);
        when(productReviewRepository.countByProductId(1L)).thenReturn(10L);

        // When
        ProductResponse response = productService.getProductById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Product", response.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when product not found by ID")
    void testGetProductById_NotFound() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(1L);
        });
    }

    @Test
    @DisplayName("Should get product by SKU successfully")
    void testGetProductBySku_Success() {
        // Given
        when(productRepository.findBySku("TEST-SKU-001")).thenReturn(Optional.of(testProduct));
        when(productReviewRepository.calculateAverageRating(1L)).thenReturn(4.5);
        when(productReviewRepository.countByProductId(1L)).thenReturn(10L);

        // When
        ProductResponse response = productService.getProductBySku("TEST-SKU-001");

        // Then
        assertNotNull(response);
        assertEquals("TEST-SKU-001", response.getSku());
        verify(productRepository, times(1)).findBySku("TEST-SKU-001");
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct_Success() {
        // Given
        ProductRequest updateRequest = ProductRequest.builder()
                .name("Updated Product")
                .price(new BigDecimal("149.99"))
                .build();

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Updated Product")
                .sku("TEST-SKU-001")
                .price(new BigDecimal("149.99"))
                .quantity(100)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productReviewRepository.calculateAverageRating(1L)).thenReturn(null);
        when(productReviewRepository.countByProductId(1L)).thenReturn(0L);
        doNothing().when(b2BPricingService).evictPricingCacheForProduct(anyLong());
        doNothing().when(searchService).indexProduct(anyLong());
        doNothing().when(eventPublisher).publish(any(), anyString());

        // When
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals("Updated Product", response.getName());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(b2BPricingService, times(1)).evictPricingCacheForProduct(1L);
        verify(searchService, times(1)).indexProduct(1L);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent product")
    void testUpdateProduct_NotFound() {
        // Given
        ProductRequest updateRequest = ProductRequest.builder()
                .name("Updated Product")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(1L, updateRequest);
        });
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating with duplicate SKU")
    void testUpdateProduct_DuplicateSku() {
        // Given
        ProductRequest updateRequest = ProductRequest.builder()
                .sku("DUPLICATE-SKU")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.existsBySku("DUPLICATE-SKU")).thenReturn(true);

        // When/Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productService.updateProduct(1L, updateRequest);
        });
        assertTrue(exception.getMessage().contains("SKU"));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(any(Product.class));
        doNothing().when(searchService).removeProduct(anyLong());

        // When
        productService.deleteProduct(1L);

        // Then
        verify(productRepository, times(1)).delete(testProduct);
        verify(searchService, times(1)).removeProduct(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void testDeleteProduct_NotFound() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.deleteProduct(1L);
        });
        verify(productRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should get all products with pagination")
    void testGetAllProducts_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productReviewRepository.calculateAverageRating(anyLong())).thenReturn(null);
        when(productReviewRepository.countByProductId(anyLong())).thenReturn(0L);

        // When
        Page<ProductResponse> response = productService.getAllProducts(null, pageable);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should get products by status with pagination")
    void testGetAllProducts_WithStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.findByStatusAndIsVariationFalse("published", pageable)).thenReturn(productPage);
        when(productReviewRepository.calculateAverageRating(anyLong())).thenReturn(null);
        when(productReviewRepository.countByProductId(anyLong())).thenReturn(0L);

        // When
        Page<ProductResponse> response = productService.getAllProducts("published", pageable);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(productRepository, times(1)).findByStatusAndIsVariationFalse("published", pageable);
    }

    @Test
    @DisplayName("Should search products successfully")
    void testSearchProducts_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.searchProducts("published", "test", pageable)).thenReturn(productPage);
        when(productReviewRepository.calculateAverageRating(anyLong())).thenReturn(null);
        when(productReviewRepository.countByProductId(anyLong())).thenReturn(0L);

        // When
        Page<ProductResponse> response = productService.searchProducts("test", "published", pageable);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(productRepository, times(1)).searchProducts("published", "test", pageable);
    }

    @Test
    @DisplayName("Should throw exception when categories not found during creation")
    void testCreateProduct_CategoriesNotFound() {
        // Given
        when(productRepository.existsBySku(validRequest.getSku())).thenReturn(false);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(testBrand));
        when(taxRepository.findById(1L)).thenReturn(Optional.of(testTax));
        when(categoryRepository.findAllById(anyList())).thenReturn(new ArrayList<>()); // Empty list

        // When/Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productService.createProduct(validRequest);
        });
        assertTrue(exception.getMessage().contains("categories"));
        verify(productRepository, never()).save(any());
    }
}

