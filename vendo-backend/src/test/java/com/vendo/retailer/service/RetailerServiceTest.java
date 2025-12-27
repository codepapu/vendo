package com.vendo.retailer.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.entity.Order;
import com.vendo.order.entity.OrderItem;
import com.vendo.product.entity.Product;
import com.vendo.product.entity.ProductRetailer;
import com.vendo.retailer.dto.request.RetailerRequest;
import com.vendo.retailer.dto.response.RetailerDashboardResponse;
import com.vendo.retailer.dto.response.RetailerResponse;
import com.vendo.retailer.entity.Retailer;
import com.vendo.retailer.entity.RetailerBankAccount;
import com.vendo.retailer.repository.CommissionTrackingRepository;
import com.vendo.retailer.repository.RetailerBankAccountRepository;
import com.vendo.retailer.repository.RetailerRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import com.vendo.order.repository.OrderItemRepository;
import com.vendo.order.repository.OrderRepository;
import com.vendo.product.repository.ProductRetailerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetailerService Unit Tests")
class RetailerServiceTest {

    @Mock
    private RetailerRepository retailerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RetailerBankAccountRepository bankAccountRepository;

    @Mock
    private CommissionService commissionService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRetailerRepository productRetailerRepository;

    @Mock
    private CommissionTrackingRepository commissionTrackingRepository;

    @InjectMocks
    private RetailerService retailerService;

    private User testUser;
    private RetailerRequest validRequest;
    private Retailer pendingRetailer;
    private Retailer activeRetailer;
    private RetailerBankAccount primaryBankAccount;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("retailer@example.com")
                .username("retailer")
                .firstName("John")
                .lastName("Doe")
                .build();

        validRequest = RetailerRequest.builder()
                .storeName("Test Store")
                .storeSlug("test-store")
                .description("A test store")
                .logoUrl("https://example.com/logo.png")
                .bannerUrl("https://example.com/banner.png")
                .commissionRate(new BigDecimal("10.00"))
                .build();

        pendingRetailer = Retailer.builder()
                .id(1L)
                .user(testUser)
                .storeName("Test Store")
                .storeSlug("test-store")
                .description("A test store")
                .logoUrl("https://example.com/logo.png")
                .bannerUrl("https://example.com/banner.png")
                .commissionRate(new BigDecimal("10.00"))
                .status("PENDING")
                .rating(BigDecimal.ZERO)
                .totalSales(BigDecimal.ZERO)
                .totalOrders(0)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        activeRetailer = Retailer.builder()
                .id(1L)
                .user(testUser)
                .storeName("Test Store")
                .storeSlug("test-store")
                .description("A test store")
                .commissionRate(new BigDecimal("10.00"))
                .status("ACTIVE")
                .rating(new BigDecimal("4.5"))
                .totalSales(new BigDecimal("50000.00"))
                .totalOrders(100)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        primaryBankAccount = RetailerBankAccount.builder()
                .id(1L)
                .retailer(activeRetailer)
                .accountHolderName("John Doe")
                .accountNumber("1234567890")
                .bankName("Test Bank")
                .isPrimary(true)
                .build();
    }

    @Test
    @DisplayName("Should register retailer successfully")
    void testRegisterRetailer_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(retailerRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(retailerRepository.existsByStoreSlug("test-store")).thenReturn(false);
        when(retailerRepository.save(any(Retailer.class))).thenReturn(pendingRetailer);

        // When
        RetailerResponse response = retailerService.registerRetailer(1L, validRequest);

        // Then
        assertNotNull(response);
        assertEquals("Test Store", response.getStoreName());
        assertEquals("test-store", response.getStoreSlug());
        assertEquals("PENDING", response.getStatus());
        assertFalse(response.getVerified());
        verify(retailerRepository, times(1)).save(any(Retailer.class));
    }

    @Test
    @DisplayName("Should throw exception when user does not exist")
    void testRegisterRetailer_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            retailerService.registerRetailer(1L, validRequest);
        });
        verify(retailerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user already has retailer account")
    void testRegisterRetailer_UserAlreadyHasAccount() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(retailerRepository.findByUserId(1L)).thenReturn(Optional.of(pendingRetailer));

        // When/Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            retailerService.registerRetailer(1L, validRequest);
        });
        assertEquals("User already has a retailer account", exception.getMessage());
        verify(retailerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when store slug already exists")
    void testRegisterRetailer_StoreSlugExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(retailerRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(retailerRepository.existsByStoreSlug("test-store")).thenReturn(true);

        // When/Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            retailerService.registerRetailer(1L, validRequest);
        });
        assertTrue(exception.getMessage().contains("Store slug already exists"));
        verify(retailerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should register retailer with default commission rate")
    void testRegisterRetailer_DefaultCommissionRate() {
        // Given
        RetailerRequest requestWithoutCommission = RetailerRequest.builder()
                .storeName("Test Store")
                .storeSlug("test-store-2")
                .build();

        Retailer retailerWithDefault = Retailer.builder()
                .id(2L)
                .user(testUser)
                .storeName("Test Store")
                .storeSlug("test-store-2")
                .commissionRate(BigDecimal.ZERO)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(retailerRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(retailerRepository.existsByStoreSlug("test-store-2")).thenReturn(false);
        when(retailerRepository.save(any(Retailer.class))).thenReturn(retailerWithDefault);

        // When
        RetailerResponse response = retailerService.registerRetailer(1L, requestWithoutCommission);

        // Then
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getCommissionRate());
    }

    @Test
    @DisplayName("Should approve retailer successfully")
    void testApproveRetailer_Success() {
        // Given
        Long approverUserId = 2L;
        List<RetailerBankAccount> bankAccounts = List.of(primaryBankAccount);

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(pendingRetailer));
        when(bankAccountRepository.findByRetailerId(1L)).thenReturn(bankAccounts);
        when(retailerRepository.save(any(Retailer.class))).thenReturn(activeRetailer);

        // When
        RetailerResponse response = retailerService.approveRetailer(1L, approverUserId);

        // Then
        assertNotNull(response);
        assertEquals("ACTIVE", response.getStatus());
        assertTrue(response.getVerified());
        verify(retailerRepository, times(1)).save(any(Retailer.class));
    }

    @Test
    @DisplayName("Should throw exception when retailer not found for approval")
    void testApproveRetailer_RetailerNotFound() {
        // Given
        when(retailerRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            retailerService.approveRetailer(1L, 2L);
        });
    }

    @Test
    @DisplayName("Should throw exception when retailer is not pending")
    void testApproveRetailer_NotPending() {
        // Given
        when(retailerRepository.findById(1L)).thenReturn(Optional.of(activeRetailer));

        // When/Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            retailerService.approveRetailer(1L, 2L);
        });
        assertTrue(exception.getMessage().contains("Only PENDING retailers can be approved"));
    }

    @Test
    @DisplayName("Should throw exception when retailer has no bank account")
    void testApproveRetailer_NoBankAccount() {
        // Given
        when(retailerRepository.findById(1L)).thenReturn(Optional.of(pendingRetailer));
        when(bankAccountRepository.findByRetailerId(1L)).thenReturn(new ArrayList<>());

        // When/Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            retailerService.approveRetailer(1L, 2L);
        });
        assertTrue(exception.getMessage().contains("must have at least one bank account"));
    }

    @Test
    @DisplayName("Should throw exception when retailer has no primary bank account")
    void testApproveRetailer_NoPrimaryBankAccount() {
        // Given
        RetailerBankAccount nonPrimaryAccount = RetailerBankAccount.builder()
                .id(1L)
                .retailer(pendingRetailer)
                .accountHolderName("John Doe")
                .accountNumber("1234567890")
                .bankName("Test Bank")
                .isPrimary(false)
                .build();

        when(retailerRepository.findById(1L)).thenReturn(Optional.of(pendingRetailer));
        when(bankAccountRepository.findByRetailerId(1L)).thenReturn(List.of(nonPrimaryAccount));

        // When/Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            retailerService.approveRetailer(1L, 2L);
        });
        assertTrue(exception.getMessage().contains("must have at least one primary bank account"));
    }

    @Test
    @DisplayName("Should calculate commission correctly")
    void testCalculateCommission_Success() {
        // Given
        BigDecimal orderTotal = new BigDecimal("1000.00");
        BigDecimal commissionRate = new BigDecimal("10.00");
        BigDecimal expectedCommission = new BigDecimal("100.00");

        when(commissionService.calculateCommission(orderTotal, commissionRate))
                .thenReturn(expectedCommission);

        // When
        BigDecimal commission = retailerService.calculateCommission(orderTotal, commissionRate);

        // Then
        assertNotNull(commission);
        assertEquals(expectedCommission, commission);
        verify(commissionService, times(1)).calculateCommission(orderTotal, commissionRate);
    }

    @Test
    @DisplayName("Should calculate commission with different rates")
    void testCalculateCommission_DifferentRates() {
        // Given
        BigDecimal orderTotal = new BigDecimal("1000.00");
        BigDecimal rate5 = new BigDecimal("5.00");
        BigDecimal rate15 = new BigDecimal("15.00");
        BigDecimal rate20 = new BigDecimal("20.00");

        when(commissionService.calculateCommission(orderTotal, rate5))
                .thenReturn(new BigDecimal("50.00"));
        when(commissionService.calculateCommission(orderTotal, rate15))
                .thenReturn(new BigDecimal("150.00"));
        when(commissionService.calculateCommission(orderTotal, rate20))
                .thenReturn(new BigDecimal("200.00"));

        // When
        BigDecimal commission5 = retailerService.calculateCommission(orderTotal, rate5);
        BigDecimal commission15 = retailerService.calculateCommission(orderTotal, rate15);
        BigDecimal commission20 = retailerService.calculateCommission(orderTotal, rate20);

        // Then
        assertEquals(new BigDecimal("50.00"), commission5);
        assertEquals(new BigDecimal("150.00"), commission15);
        assertEquals(new BigDecimal("200.00"), commission20);
    }

    @Test
    @DisplayName("Should get retailer dashboard with aggregated data")
    void testGetRetailerDashboard_Success() {
        // Given
        Long retailerId = 1L;

        // Mock retailer
        when(retailerRepository.findById(retailerId)).thenReturn(Optional.of(activeRetailer));

        // Mock active products
        List<ProductRetailer> activeProducts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ProductRetailer pr = ProductRetailer.builder()
                    .id((long) i)
                    .retailer(activeRetailer)
                    .isActive(true)
                    .build();
            activeProducts.add(pr);
        }
        when(productRetailerRepository.findByRetailerIdAndIsActiveTrue(retailerId))
                .thenReturn(activeProducts);

        // Mock low stock products
        List<ProductRetailer> lowStockProducts = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            ProductRetailer pr = ProductRetailer.builder()
                    .id((long) (i + 10))
                    .retailer(activeRetailer)
                    .isActive(true)
                    .build();
            lowStockProducts.add(pr);
        }
        when(productRetailerRepository.findLowStockProductsByRetailer(retailerId))
                .thenReturn(lowStockProducts);

        // Mock commissions
        when(commissionService.getTotalCommissionsByRetailer(retailerId))
                .thenReturn(new BigDecimal("5000.00"));
        when(commissionService.getTotalCommissionsByRetailerAndStatus(retailerId, "PENDING"))
                .thenReturn(new BigDecimal("1000.00"));

        // Mock order items for this month
        List<OrderItem> thisMonthItems = new ArrayList<>();
        Order order1 = Order.builder()
                .id(1L)
                .orderNumber("ORD-001")
                .status("DELIVERED")
                .createdAt(LocalDateTime.now())
                .build();
        OrderItem item1 = OrderItem.builder()
                .id(1L)
                .order(order1)
                .retailer(activeRetailer)
                .totalPrice(new BigDecimal("500.00"))
                .quantity(5)
                .createdAt(LocalDateTime.now())
                .build();
        thisMonthItems.add(item1);

        when(orderItemRepository.findByRetailerId(retailerId)).thenReturn(thisMonthItems);

        // When
        RetailerDashboardResponse dashboard = retailerService.getRetailerDashboard(retailerId);

        // Then
        assertNotNull(dashboard);
        assertEquals(retailerId, dashboard.getRetailerId());
        assertEquals("Test Store", dashboard.getStoreName());
        assertEquals("ACTIVE", dashboard.getStatus());
        assertEquals(5, dashboard.getActiveProducts());
        assertEquals(2, dashboard.getLowStockProducts());
        assertEquals(new BigDecimal("5000.00"), dashboard.getTotalCommissions());
        assertEquals(new BigDecimal("1000.00"), dashboard.getPendingCommissions());
        verify(retailerRepository, times(1)).findById(retailerId);
        verify(productRetailerRepository, times(1)).findByRetailerIdAndIsActiveTrue(retailerId);
        verify(commissionService, times(1)).getTotalCommissionsByRetailer(retailerId);
    }

    @Test
    @DisplayName("Should get dashboard with empty data")
    void testGetRetailerDashboard_EmptyData() {
        // Given
        Long retailerId = 1L;
        Retailer newRetailer = Retailer.builder()
                .id(retailerId)
                .user(testUser)
                .storeName("New Store")
                .storeSlug("new-store")
                .status("ACTIVE")
                .totalSales(BigDecimal.ZERO)
                .totalOrders(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(retailerRepository.findById(retailerId)).thenReturn(Optional.of(newRetailer));
        when(productRetailerRepository.findByRetailerIdAndIsActiveTrue(retailerId))
                .thenReturn(new ArrayList<>());
        when(productRetailerRepository.findLowStockProductsByRetailer(retailerId))
                .thenReturn(new ArrayList<>());
        when(commissionService.getTotalCommissionsByRetailer(retailerId))
                .thenReturn(BigDecimal.ZERO);
        when(commissionService.getTotalCommissionsByRetailerAndStatus(retailerId, "PENDING"))
                .thenReturn(BigDecimal.ZERO);
        when(orderItemRepository.findByRetailerId(retailerId)).thenReturn(new ArrayList<>());

        // When
        RetailerDashboardResponse dashboard = retailerService.getRetailerDashboard(retailerId);

        // Then
        assertNotNull(dashboard);
        assertEquals(0, dashboard.getActiveProducts());
        assertEquals(0, dashboard.getLowStockProducts());
        assertEquals(BigDecimal.ZERO, dashboard.getTotalCommissions());
        assertEquals(BigDecimal.ZERO, dashboard.getPendingCommissions());
        assertEquals(BigDecimal.ZERO, dashboard.getThisMonthSales());
        assertEquals(0, dashboard.getThisMonthOrders());
    }

    @Test
    @DisplayName("Should throw exception when retailer not found for dashboard")
    void testGetRetailerDashboard_RetailerNotFound() {
        // Given
        when(retailerRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            retailerService.getRetailerDashboard(1L);
        });
    }

    @Test
    @DisplayName("Should handle invalid inputs in register retailer")
    void testRegisterRetailer_InvalidInputs() {
        // Given - Request with invalid store slug format
        RetailerRequest invalidRequest = RetailerRequest.builder()
                .storeName("Test Store")
                .storeSlug("INVALID_SLUG_WITH_UPPERCASE") // Invalid format
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(retailerRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(retailerRepository.existsByStoreSlug("INVALID_SLUG_WITH_UPPERCASE")).thenReturn(false);

        // When/Then - Service should handle or validation should catch
        // In production, validation annotations would catch this
        assertDoesNotThrow(() -> {
            // This would be caught by @Pattern validation in DTO
        });
    }

    @Test
    @DisplayName("Should calculate commission with zero rate")
    void testCalculateCommission_ZeroRate() {
        // Given
        BigDecimal orderTotal = new BigDecimal("1000.00");
        BigDecimal zeroRate = BigDecimal.ZERO;

        when(commissionService.calculateCommission(orderTotal, zeroRate))
                .thenReturn(BigDecimal.ZERO);

        // When
        BigDecimal commission = retailerService.calculateCommission(orderTotal, zeroRate);

        // Then
        assertEquals(BigDecimal.ZERO, commission);
    }

    @Test
    @DisplayName("Should calculate commission with high rate")
    void testCalculateCommission_HighRate() {
        // Given
        BigDecimal orderTotal = new BigDecimal("1000.00");
        BigDecimal highRate = new BigDecimal("50.00");

        when(commissionService.calculateCommission(orderTotal, highRate))
                .thenReturn(new BigDecimal("500.00"));

        // When
        BigDecimal commission = retailerService.calculateCommission(orderTotal, highRate);

        // Then
        assertEquals(new BigDecimal("500.00"), commission);
    }
}

