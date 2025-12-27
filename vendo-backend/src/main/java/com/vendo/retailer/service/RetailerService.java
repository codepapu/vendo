package com.vendo.retailer.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.dto.response.OrderResponse;
import com.vendo.order.entity.Order;
import com.vendo.order.entity.OrderItem;
import com.vendo.order.repository.OrderItemRepository;
import com.vendo.order.repository.OrderRepository;
import com.vendo.product.entity.ProductRetailer;
import com.vendo.product.repository.ProductRetailerRepository;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetailerService {
    
    private final RetailerRepository retailerRepository;
    private final UserRepository userRepository;
    private final RetailerBankAccountRepository bankAccountRepository;
    private final CommissionService commissionService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRetailerRepository productRetailerRepository;
    private final CommissionTrackingRepository commissionTrackingRepository;
    
    @Transactional(readOnly = true)
    public Page<RetailerResponse> getAllRetailers(String status, Pageable pageable) {
        Page<Retailer> retailers;
        if (status != null && !status.isEmpty()) {
            retailers = retailerRepository.findAll(pageable); // Filter by status in service if needed
        } else {
            retailers = retailerRepository.findAll(pageable);
        }
        return retailers.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public RetailerResponse getRetailerById(Long id) {
        Retailer retailer = retailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", id));
        return mapToResponse(retailer);
    }
    
    @Transactional(readOnly = true)
    public RetailerResponse getRetailerBySlug(String slug) {
        Retailer retailer = retailerRepository.findByStoreSlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "storeSlug", slug));
        return mapToResponse(retailer);
    }
    
    @Transactional(readOnly = true)
    public RetailerResponse getRetailerByUserId(Long userId) {
        Retailer retailer = retailerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "userId", userId));
        return mapToResponse(retailer);
    }
    
    /**
     * Register a new retailer with bank account validation
     */
    @Transactional
    public RetailerResponse registerRetailer(Long userId, RetailerRequest request) {
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Check if user already has a retailer account
        if (retailerRepository.findByUserId(userId).isPresent()) {
            throw new BadRequestException("User already has a retailer account");
        }
        
        // Validate store slug uniqueness
        if (retailerRepository.existsByStoreSlug(request.getStoreSlug())) {
            throw new BadRequestException("Store slug already exists: " + request.getStoreSlug());
        }
        
        Retailer retailer = Retailer.builder()
                .user(user)
                .storeName(request.getStoreName())
                .storeSlug(request.getStoreSlug())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .bannerUrl(request.getBannerUrl())
                .commissionRate(request.getCommissionRate() != null ? request.getCommissionRate() : BigDecimal.ZERO)
                .status("PENDING") // Always start as PENDING
                .rating(BigDecimal.ZERO)
                .totalSales(BigDecimal.ZERO)
                .totalOrders(0)
                .verified(false)
                .build();
        
        Retailer savedRetailer = retailerRepository.save(retailer);
        log.info("Retailer registered with ID: {}", savedRetailer.getId());
        
        return mapToResponse(savedRetailer);
    }
    
    /**
     * Create retailer (backward compatibility)
     */
    @Transactional
    public RetailerResponse createRetailer(Long userId, RetailerRequest request) {
        return registerRetailer(userId, request);
    }
    
    /**
     * Approve a retailer (change status from PENDING to ACTIVE)
     */
    @Transactional
    public RetailerResponse approveRetailer(Long id, Long approvedByUserId) {
        Retailer retailer = retailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", id));
        
        if (!"PENDING".equals(retailer.getStatus())) {
            throw new BadRequestException("Only PENDING retailers can be approved. Current status: " + retailer.getStatus());
        }
        
        // Validate bank account exists
        List<RetailerBankAccount> bankAccounts = bankAccountRepository.findByRetailerId(id);
        if (bankAccounts.isEmpty()) {
            throw new BadRequestException("Retailer must have at least one bank account before approval");
        }
        
        // Validate at least one primary bank account
        boolean hasPrimary = bankAccounts.stream().anyMatch(RetailerBankAccount::getIsPrimary);
        if (!hasPrimary) {
            throw new BadRequestException("Retailer must have at least one primary bank account before approval");
        }
        
        // Validate bank account details
        for (RetailerBankAccount account : bankAccounts) {
            if (account.getAccountNumber() == null || account.getAccountNumber().trim().isEmpty()) {
                throw new BadRequestException("Bank account number is required");
            }
            if (account.getBankName() == null || account.getBankName().trim().isEmpty()) {
                throw new BadRequestException("Bank name is required");
            }
            if (account.getAccountHolderName() == null || account.getAccountHolderName().trim().isEmpty()) {
                throw new BadRequestException("Account holder name is required");
            }
        }
        
        retailer.setStatus("ACTIVE");
        retailer.setVerified(true);
        
        Retailer updatedRetailer = retailerRepository.save(retailer);
        log.info("Retailer approved: ID={}, ApprovedBy={}", id, approvedByUserId);
        
        return mapToResponse(updatedRetailer);
    }
    
    @Transactional
    public RetailerResponse updateRetailer(Long id, RetailerRequest request) {
        Retailer retailer = retailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", id));
        
        // Validate store slug uniqueness if changed
        if (request.getStoreSlug() != null && !request.getStoreSlug().equals(retailer.getStoreSlug())) {
            if (retailerRepository.existsByStoreSlug(request.getStoreSlug())) {
                throw new BadRequestException("Store slug already exists: " + request.getStoreSlug());
            }
            retailer.setStoreSlug(request.getStoreSlug());
        }
        
        if (request.getStoreName() != null) {
            retailer.setStoreName(request.getStoreName());
        }
        if (request.getDescription() != null) {
            retailer.setDescription(request.getDescription());
        }
        if (request.getLogoUrl() != null) {
            retailer.setLogoUrl(request.getLogoUrl());
        }
        if (request.getBannerUrl() != null) {
            retailer.setBannerUrl(request.getBannerUrl());
        }
        if (request.getCommissionRate() != null) {
            retailer.setCommissionRate(request.getCommissionRate());
        }
        if (request.getStatus() != null) {
            retailer.setStatus(request.getStatus());
        }
        
        Retailer updatedRetailer = retailerRepository.save(retailer);
        log.info("Retailer updated with ID: {}", updatedRetailer.getId());
        
        // Evict dashboard cache when retailer is updated
        evictDashboardCache(updatedRetailer.getId());
        
        return mapToResponse(updatedRetailer);
    }
    
    /**
     * Update retailer status with proper state transitions
     */
    @Transactional
    public RetailerResponse updateRetailerStatus(Long id, String status) {
        Retailer retailer = retailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", id));
        
        if (!List.of("PENDING", "ACTIVE", "SUSPENDED", "REJECTED").contains(status)) {
            throw new BadRequestException("Invalid status: " + status);
        }
        
        String currentStatus = retailer.getStatus();
        
        // Validate state transitions
        if ("PENDING".equals(currentStatus) && !List.of("ACTIVE", "REJECTED").contains(status)) {
            throw new BadRequestException("PENDING retailers can only transition to ACTIVE or REJECTED");
        }
        
        if ("ACTIVE".equals(currentStatus) && !List.of("SUSPENDED", "ACTIVE").contains(status)) {
            throw new BadRequestException("ACTIVE retailers can only transition to SUSPENDED or remain ACTIVE");
        }
        
        if ("SUSPENDED".equals(currentStatus) && !List.of("ACTIVE", "SUSPENDED").contains(status)) {
            throw new BadRequestException("SUSPENDED retailers can only transition to ACTIVE or remain SUSPENDED");
        }
        
        if ("REJECTED".equals(currentStatus)) {
            throw new BadRequestException("REJECTED retailers cannot change status");
        }
        
        retailer.setStatus(status);
        
        // Update verified status based on new status
        if ("ACTIVE".equals(status)) {
            retailer.setVerified(true);
        } else if (List.of("SUSPENDED", "REJECTED").contains(status)) {
            retailer.setVerified(false);
        }
        
        Retailer updatedRetailer = retailerRepository.save(retailer);
        log.info("Retailer status updated: ID={}, OldStatus={}, NewStatus={}", 
                updatedRetailer.getId(), currentStatus, status);
        
        // Evict dashboard cache when retailer status is updated
        evictDashboardCache(updatedRetailer.getId());
        
        return mapToResponse(updatedRetailer);
    }
    
    /**
     * Calculate commission for a given order total and commission rate
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateCommission(BigDecimal orderTotal, BigDecimal commissionRate) {
        return commissionService.calculateCommission(orderTotal, commissionRate);
    }
    
    /**
     * Get retailer dashboard with aggregated data
     * Results are cached for 5 minutes using Redis.
     */
    @Cacheable(value = "retailerDashboard", key = "'dashboard_' + #retailerId")
    @Transactional(readOnly = true)
    public RetailerDashboardResponse getRetailerDashboard(Long retailerId) {
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        // Get total sales and orders
        BigDecimal totalSales = retailer.getTotalSales() != null ? retailer.getTotalSales() : BigDecimal.ZERO;
        Integer totalOrders = retailer.getTotalOrders() != null ? retailer.getTotalOrders() : 0;
        
        // Get active products count
        List<ProductRetailer> activeProducts = productRetailerRepository.findByRetailerIdAndIsActiveTrue(retailerId);
        Integer activeProductsCount = activeProducts.size();
        
        // Get low stock products count
        List<ProductRetailer> lowStockProducts = productRetailerRepository.findLowStockProductsByRetailer(retailerId);
        Integer lowStockProductsCount = lowStockProducts.size();
        
        // Get commission totals
        BigDecimal totalCommissions = commissionService.getTotalCommissionsByRetailer(retailerId);
        BigDecimal pendingCommissions = commissionService.getTotalCommissionsByRetailerAndStatus(retailerId, "PENDING");
        
        // Get this month's sales and orders
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now();
        
        List<OrderItem> thisMonthItems = orderItemRepository.findByRetailerId(retailerId).stream()
                .filter(item -> {
                    LocalDateTime createdAt = item.getCreatedAt();
                    if (createdAt == null) return false;
                    LocalDate itemDate = createdAt.toLocalDate();
                    return !itemDate.isBefore(startOfMonth) && !itemDate.isAfter(endOfMonth);
                })
                .collect(Collectors.toList());
        
        BigDecimal thisMonthSales = thisMonthItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer thisMonthOrders = (int) thisMonthItems.stream()
                .map(OrderItem::getOrder)
                .map(Order::getId)
                .distinct()
                .count();
        
        // Get recent orders (last 10)
        List<OrderItem> recentOrderItems = orderItemRepository.findByRetailerId(retailerId).stream()
                .sorted((a, b) -> {
                    LocalDateTime aTime = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN;
                    LocalDateTime bTime = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN;
                    return bTime.compareTo(aTime);
                })
                .limit(10)
                .collect(Collectors.toList());
        
        List<RetailerDashboardResponse.RecentOrder> recentOrders = recentOrderItems.stream()
                .map(item -> {
                    Order order = item.getOrder();
                    return RetailerDashboardResponse.RecentOrder.builder()
                            .orderId(order.getId())
                            .orderNumber(order.getOrderNumber())
                            .totalAmount(item.getTotalPrice())
                            .status(order.getStatus())
                            .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : "")
                            .build();
                })
                .collect(Collectors.toList());
        
        // Get top products (by quantity sold)
        List<RetailerDashboardResponse.TopProduct> topProducts = orderItemRepository.findByRetailerId(retailerId).stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getProduct,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                items -> {
                                    Integer totalQty = items.stream()
                                            .mapToInt(OrderItem::getQuantity)
                                            .sum();
                                    BigDecimal totalRev = items.stream()
                                            .map(OrderItem::getTotalPrice)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    return RetailerDashboardResponse.TopProduct.builder()
                                            .productId(items.get(0).getProduct() != null ? items.get(0).getProduct().getId() : null)
                                            .productName(items.get(0).getProductName())
                                            .quantitySold(totalQty)
                                            .totalRevenue(totalRev)
                                            .build();
                                }
                        )
                ))
                .values()
                .stream()
                .sorted((a, b) -> b.getQuantitySold().compareTo(a.getQuantitySold()))
                .limit(5)
                .collect(Collectors.toList());
        
        return RetailerDashboardResponse.builder()
                .retailerId(retailer.getId())
                .storeName(retailer.getStoreName())
                .status(retailer.getStatus())
                .totalSales(totalSales)
                .totalOrders(totalOrders)
                .activeProducts(activeProductsCount)
                .lowStockProducts(lowStockProductsCount)
                .totalCommissions(totalCommissions)
                .pendingCommissions(pendingCommissions)
                .thisMonthSales(thisMonthSales)
                .thisMonthOrders(thisMonthOrders)
                .recentOrders(recentOrders)
                .topProducts(topProducts)
                .build();
    }
    
    @Transactional
    public void deleteRetailer(Long id) {
        Retailer retailer = retailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", id));
        
        retailerRepository.delete(retailer);
        log.info("Retailer deleted with ID: {}", id);
    }
    
    /**
     * Validate that the retailer belongs to the authenticated user
     */
    @Transactional(readOnly = true)
    public void validateRetailerOwnership(Long retailerId, Long userId) {
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        if (retailer.getUser() == null || !retailer.getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have permission to access this retailer");
        }
    }
    
    /**
     * Get retailer dashboard by user ID
     * Results are cached for 5 minutes using Redis.
     */
    @Cacheable(value = "retailerDashboard", key = "'dashboard_user_' + #userId")
    @Transactional(readOnly = true)
    public RetailerDashboardResponse getRetailerDashboardByUserId(Long userId) {
        Retailer retailer = retailerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "userId", userId));
        return getRetailerDashboard(retailer.getId());
    }
    
    /**
     * Evict retailer dashboard cache for a specific retailer
     * Call this when retailer data, orders, or products are updated
     */
    @CacheEvict(value = "retailerDashboard", key = "'dashboard_' + #retailerId")
    public void evictDashboardCache(Long retailerId) {
        log.debug("Evicting retailer dashboard cache for retailer: {}", retailerId);
    }
    
    /**
     * Evict retailer dashboard cache by user ID
     */
    @CacheEvict(value = "retailerDashboard", key = "'dashboard_user_' + #userId")
    public void evictDashboardCacheByUserId(Long userId) {
        log.debug("Evicting retailer dashboard cache for user: {}", userId);
    }
    
    /**
     * Evict all retailer dashboard cache
     */
    @CacheEvict(value = "retailerDashboard", allEntries = true)
    public void evictAllDashboardCache() {
        log.debug("Evicting all retailer dashboard cache");
    }
    
    /**
     * Get orders for a retailer with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getRetailerOrders(Long retailerId, String status, Pageable pageable) {
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        // Get all order items for this retailer
        List<OrderItem> orderItems = orderItemRepository.findByRetailerId(retailerId);
        
        // Extract unique order IDs
        List<Long> orderIds = orderItems.stream()
                .map(item -> item.getOrder().getId())
                .distinct()
                .collect(Collectors.toList());
        
        if (orderIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // Get orders by IDs with pagination
        // Note: This is a simplified approach. In production, you'd want a more efficient query
        List<Order> allOrders = orderRepository.findAllById(orderIds);
        
        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            allOrders = allOrders.stream()
                    .filter(order -> status.equals(order.getStatus()))
                    .collect(Collectors.toList());
        }
        
        // Sort orders
        allOrders.sort((a, b) -> {
            LocalDateTime aTime = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN;
            LocalDateTime bTime = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN;
            return bTime.compareTo(aTime); // DESC by default
        });
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allOrders.size());
        List<Order> pagedOrders = start < allOrders.size() ? allOrders.subList(start, end) : new ArrayList<>();
        
        // Convert to OrderResponse
        List<OrderResponse> orderResponses = pagedOrders.stream()
                .map(this::mapOrderToResponse)
                .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(
                orderResponses,
                pageable,
                allOrders.size()
        );
    }
    
    /**
     * Map Order entity to OrderResponse DTO (only items for this retailer)
     */
    private OrderResponse mapOrderToResponse(Order order) {
        // Map order items - only include items from this retailer
        List<OrderResponse.OrderItemResponse> items = List.of();
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            items = order.getOrderItems().stream()
                    .map(oi -> OrderResponse.OrderItemResponse.builder()
                            .id(oi.getId())
                            .productId(oi.getProduct() != null ? oi.getProduct().getId() : null)
                            .retailerId(oi.getRetailer() != null ? oi.getRetailer().getId() : null)
                            .productName(oi.getProductName())
                            .productSku(oi.getSku())
                            .quantity(oi.getQuantity())
                            .price(oi.getUnitPrice())
                            .total(oi.getTotalPrice())
                            .commissionAmount(oi.getCommissionAmount() != null ? oi.getCommissionAmount() : BigDecimal.ZERO)
                            .build())
                    .collect(Collectors.toList());
        }
        
        return OrderResponse.builder()
                .id(order.getId())
                .code(order.getCode())
                .userId(order.getUserId())
                .businessAccountId(order.getBusinessAccount() != null ? order.getBusinessAccount().getId() : null)
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .amount(order.getAmount())
                .subTotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .shippingAmount(order.getShippingAmount())
                .discountAmount(order.getDiscountAmount())
                .paymentFee(BigDecimal.ZERO)
                .description(order.getNotes())
                .isConfirmed(false) // Order entity doesn't have this field
                .isFinished(false) // Order entity doesn't have this field
                .completedAt(null) // Order entity doesn't have this field
                .items(items)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    
    private RetailerResponse mapToResponse(Retailer retailer) {
        RetailerResponse.RetailerResponseBuilder builder = RetailerResponse.builder()
                .id(retailer.getId())
                .storeName(retailer.getStoreName())
                .storeSlug(retailer.getStoreSlug())
                .description(retailer.getDescription())
                .logoUrl(retailer.getLogoUrl())
                .bannerUrl(retailer.getBannerUrl())
                .commissionRate(retailer.getCommissionRate())
                .status(retailer.getStatus())
                .rating(retailer.getRating())
                .totalSales(retailer.getTotalSales())
                .totalOrders(retailer.getTotalOrders())
                .verified(retailer.getVerified())
                .createdAt(retailer.getCreatedAt())
                .updatedAt(retailer.getUpdatedAt());
        
        if (retailer.getUser() != null) {
            builder.userId(retailer.getUser().getId());
            User user = retailer.getUser();
            builder.user(RetailerResponse.UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build());
        }
        
        return builder.build();
    }
}

