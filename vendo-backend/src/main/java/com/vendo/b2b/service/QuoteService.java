package com.vendo.b2b.service;

import com.vendo.b2b.dto.request.ConvertQuoteToOrderRequest;
import com.vendo.b2b.dto.request.CreateQuoteRequest;
import com.vendo.b2b.dto.request.UpdateQuoteRequest;
import com.vendo.b2b.dto.response.QuoteResponse;
import com.vendo.b2b.entity.BusinessAccount;
import com.vendo.b2b.entity.Quote;
import com.vendo.b2b.entity.QuoteItem;
import com.vendo.b2b.entity.QuoteRevision;
import com.vendo.b2b.repository.BusinessAccountRepository;
import com.vendo.b2b.repository.QuoteItemRepository;
import com.vendo.b2b.repository.QuoteRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {
    
    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final NotificationService notificationService;
    
    /**
     * Generate a new quote
     */
    @Transactional
    public QuoteResponse generateQuote(Long userId, CreateQuoteRequest request) {
        log.info("Generating quote for user: {}, business account: {}", userId, request.getBusinessAccountId());
        
        // Validate business account exists and is approved
        BusinessAccount businessAccount = businessAccountRepository.findById(request.getBusinessAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("BusinessAccount", "id", request.getBusinessAccountId()));
        
        if (!"APPROVED".equals(businessAccount.getStatus())) {
            throw new BadRequestException("Business account must be APPROVED to generate quotes");
        }
        
        // Get creating user
        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Generate unique quote number
        String quoteNumber = "QT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        List<QuoteItem> quoteItems = new ArrayList<>();
        
        for (CreateQuoteRequest.QuoteItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));
            
            // Use custom price if provided, otherwise use product price
            BigDecimal unitPrice = itemRequest.getCustomPrice() != null 
                    ? itemRequest.getCustomPrice() 
                    : (product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
            
            // Apply discount if provided
            BigDecimal itemDiscount = BigDecimal.ZERO;
            if (itemRequest.getDiscountPercentage() != null && itemRequest.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
                itemDiscount = unitPrice.multiply(itemRequest.getDiscountPercentage()).divide(BigDecimal.valueOf(100));
                unitPrice = unitPrice.subtract(itemDiscount);
            }
            
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            subtotal = subtotal.add(itemTotal);
            discountAmount = discountAmount.add(itemDiscount.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            
            QuoteItem quoteItem = QuoteItem.builder()
                    .quote(null) // Will be set after quote is created
                    .product(product)
                    .productName(product.getName())
                    .sku(product.getSku())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .discountPercentage(itemRequest.getDiscountPercentage() != null ? itemRequest.getDiscountPercentage() : BigDecimal.ZERO)
                    .totalPrice(itemTotal)
                    .notes(itemRequest.getNotes())
                    .build();
            quoteItems.add(quoteItem);
        }
        
        BigDecimal taxAmount = BigDecimal.ZERO; // Tax calculated when converting to order
        BigDecimal totalAmount = subtotal.add(taxAmount).subtract(discountAmount);
        
        // Create quote
        Quote quote = Quote.builder()
                .quoteNumber(quoteNumber)
                .businessAccount(businessAccount)
                .status("DRAFT")
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .currency("USD")
                .expirationDate(request.getExpirationDate())
                .terms(request.getTerms())
                .notes(request.getNotes())
                .createdBy(createdBy)
                .quoteItems(new ArrayList<>())
                .build();
        
        Quote savedQuote = quoteRepository.save(quote);
        
        // Set quote reference in items and save
        for (QuoteItem item : quoteItems) {
            item.setQuote(savedQuote);
        }
        quoteItemRepository.saveAll(quoteItems);
        savedQuote.setQuoteItems(quoteItems);
        
        log.info("Quote generated with ID: {} and number: {}", savedQuote.getId(), savedQuote.getQuoteNumber());
        
        return mapToResponse(savedQuote);
    }
    
    /**
     * Get quotes with filtering
     */
    @Transactional(readOnly = true)
    public Page<QuoteResponse> getQuotes(Long businessAccountId, String status, Pageable pageable) {
        log.debug("Getting quotes - businessAccountId: {}, status: {}", businessAccountId, status);
        
        Page<Quote> quotes;
        
        if (businessAccountId != null && status != null) {
            List<Quote> quoteList = quoteRepository.findByBusinessAccountIdAndStatus(businessAccountId, status);
            quotes = quoteRepository.findByBusinessAccountId(businessAccountId, pageable);
        } else if (businessAccountId != null) {
            quotes = quoteRepository.findByBusinessAccountId(businessAccountId, pageable);
        } else if (status != null) {
            quotes = quoteRepository.findByStatus(status, pageable);
        } else {
            quotes = quoteRepository.findAll(pageable);
        }
        
        return quotes.map(this::mapToResponse);
    }
    
    /**
     * Get quote by ID
     */
    @Transactional(readOnly = true)
    public QuoteResponse getQuoteById(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", id));
        return mapToResponse(quote);
    }
    
    /**
     * Update quote
     */
    @Transactional
    public QuoteResponse updateQuote(Long quoteId, UpdateQuoteRequest request) {
        log.info("Updating quote: {}", quoteId);
        
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", quoteId));
        
        // Validate quote can be updated
        if ("CONVERTED".equals(quote.getStatus()) || "EXPIRED".equals(quote.getStatus())) {
            throw new BadRequestException("Cannot update a converted or expired quote");
        }
        
        // Create revision before updating
        createQuoteRevision(quote);
        
        // Update items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // Remove existing items
            quoteItemRepository.deleteByQuoteId(quoteId);
            
            // Calculate new totals
            BigDecimal subtotal = BigDecimal.ZERO;
            BigDecimal discountAmount = BigDecimal.ZERO;
            List<QuoteItem> quoteItems = new ArrayList<>();
            
            for (CreateQuoteRequest.QuoteItemRequest itemRequest : request.getItems()) {
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));
                
                BigDecimal unitPrice = itemRequest.getCustomPrice() != null 
                        ? itemRequest.getCustomPrice() 
                        : (product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
                
                BigDecimal itemDiscount = BigDecimal.ZERO;
                if (itemRequest.getDiscountPercentage() != null && itemRequest.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
                    itemDiscount = unitPrice.multiply(itemRequest.getDiscountPercentage()).divide(BigDecimal.valueOf(100));
                    unitPrice = unitPrice.subtract(itemDiscount);
                }
                
                BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
                subtotal = subtotal.add(itemTotal);
                discountAmount = discountAmount.add(itemDiscount.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
                
                QuoteItem quoteItem = QuoteItem.builder()
                        .quote(quote)
                        .product(product)
                        .productName(product.getName())
                        .sku(product.getSku())
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(unitPrice)
                        .discountPercentage(itemRequest.getDiscountPercentage() != null ? itemRequest.getDiscountPercentage() : BigDecimal.ZERO)
                        .totalPrice(itemTotal)
                        .notes(itemRequest.getNotes())
                        .build();
                quoteItems.add(quoteItem);
            }
            
            quote.setSubtotal(subtotal);
            quote.setDiscountAmount(discountAmount);
            quote.setTotalAmount(subtotal.add(quote.getTaxAmount()).subtract(discountAmount));
            quote.setQuoteItems(quoteItems);
            quoteItemRepository.saveAll(quoteItems);
        }
        
        // Update other fields
        if (request.getExpirationDate() != null) {
            quote.setExpirationDate(request.getExpirationDate());
        }
        if (request.getTerms() != null) {
            quote.setTerms(request.getTerms());
        }
        if (request.getNotes() != null) {
            quote.setNotes(request.getNotes());
        }
        
        Quote savedQuote = quoteRepository.save(quote);
        log.info("Quote {} updated", savedQuote.getQuoteNumber());
        
        return mapToResponse(savedQuote);
    }
    
    /**
     * Convert quote to order
     */
    @Transactional
    public com.vendo.order.dto.response.OrderResponse convertQuoteToOrder(Long userId, ConvertQuoteToOrderRequest request) {
        log.info("Converting quote {} to order for user: {}", request.getQuoteId(), userId);
        
        Quote quote = quoteRepository.findById(request.getQuoteId())
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", request.getQuoteId()));
        
        // Validate quote can be converted
        if (!"DRAFT".equals(quote.getStatus()) && !"SENT".equals(quote.getStatus()) && !"ACCEPTED".equals(quote.getStatus())) {
            throw new BadRequestException("Only DRAFT, SENT, or ACCEPTED quotes can be converted to orders");
        }
        
        if (quote.getExpirationDate() != null && quote.getExpirationDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot convert expired quote");
        }
        
        // Build order request from quote
        List<OrderItemRequest> orderItems = quote.getQuoteItems().stream()
                .map(item -> OrderItemRequest.builder()
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .quantity(item.getQuantity())
                        .retailerId(null) // B2B orders don't have retailers
                        .build())
                .collect(Collectors.toList());
        
        // Get business account address for shipping/billing
        com.vendo.address.entity.Address defaultAddress = null;
        // TODO: Get default address from business account
        
        CreateOrderRequest orderRequest = CreateOrderRequest.builder()
                .businessAccountId(quote.getBusinessAccount().getId())
                .orderType("B2B")
                .items(orderItems)
                .notes((request.getNotes() != null ? request.getNotes() + " | " : "") + 
                       "Converted from quote: " + quote.getQuoteNumber())
                .build();
        
        // Create order using OrderService
        com.vendo.order.dto.response.OrderResponse orderResponse = orderService.createOrder(userId, orderRequest);
        
        // Update quote status
        quote.setStatus("CONVERTED");
        Order order = orderRepository.findByOrderNumber(orderResponse.getOrderNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderResponse.getOrderNumber()));
        quote.setConvertedToOrder(order);
        quoteRepository.save(quote);
        
        log.info("Quote {} converted to order {}", quote.getQuoteNumber(), orderResponse.getOrderNumber());
        
        // Send notification
        sendQuoteNotification(quote, "Quote Converted to Order", 
                String.format("Quote %s has been converted to order %s", 
                        quote.getQuoteNumber(), orderResponse.getOrderNumber()));
        
        return orderResponse;
    }
    
    /**
     * Create a quote revision
     */
    private void createQuoteRevision(Quote quote) {
        // Get next revision number
        int nextRevisionNumber = quote.getQuoteRevisions().size() + 1;
        
        QuoteRevision revision = QuoteRevision.builder()
                .quote(quote)
                .revisionNumber(nextRevisionNumber)
                .status(quote.getStatus())
                .subtotal(quote.getSubtotal())
                .taxAmount(quote.getTaxAmount())
                .totalAmount(quote.getTotalAmount())
                .notes("Revision " + nextRevisionNumber)
                .createdBy(quote.getCreatedBy())
                .build();
        
        quote.getQuoteRevisions().add(revision);
    }
    
    /**
     * Send notification for quote events
     */
    private void sendQuoteNotification(Quote quote, String title, String message) {
        try {
            if (quote.getBusinessAccount() == null || quote.getBusinessAccount().getUser() == null) {
                return;
            }
            
            Long userId = quote.getBusinessAccount().getUser().getId();
            
            Map<String, Object> data = new HashMap<>();
            data.put("quoteId", quote.getId());
            data.put("quoteNumber", quote.getQuoteNumber());
            data.put("status", quote.getStatus());
            data.put("totalAmount", quote.getTotalAmount());
            
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(userId)
                    .notificationType(NotificationType.ORDER_CONFIRMED) // Using existing type
                    .channel("in_app")
                    .title(title)
                    .message(message)
                    .data(data)
                    .build();
            
            notificationService.sendNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Failed to send quote notification", e);
        }
    }
    
    /**
     * Map Quote entity to QuoteResponse
     */
    private QuoteResponse mapToResponse(Quote quote) {
        List<QuoteResponse.QuoteItemResponse> items = quote.getQuoteItems().stream()
                .map(item -> QuoteResponse.QuoteItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .productName(item.getProductName())
                        .sku(item.getSku())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .discountPercentage(item.getDiscountPercentage())
                        .totalPrice(item.getTotalPrice())
                        .notes(item.getNotes())
                        .build())
                .collect(Collectors.toList());
        
        return QuoteResponse.builder()
                .id(quote.getId())
                .quoteNumber(quote.getQuoteNumber())
                .businessAccountId(quote.getBusinessAccount() != null ? quote.getBusinessAccount().getId() : null)
                .businessAccountName(quote.getBusinessAccount() != null ? quote.getBusinessAccount().getCompanyName() : null)
                .status(quote.getStatus())
                .subtotal(quote.getSubtotal())
                .taxAmount(quote.getTaxAmount())
                .discountAmount(quote.getDiscountAmount())
                .totalAmount(quote.getTotalAmount())
                .currency(quote.getCurrency())
                .expirationDate(quote.getExpirationDate())
                .terms(quote.getTerms())
                .notes(quote.getNotes())
                .createdBy(quote.getCreatedBy() != null ? quote.getCreatedBy().getId() : null)
                .createdByName(quote.getCreatedBy() != null ? 
                        (quote.getCreatedBy().getFirstName() + " " + quote.getCreatedBy().getLastName()).trim() : null)
                .convertedToOrderId(quote.getConvertedToOrder() != null ? quote.getConvertedToOrder().getId() : null)
                .convertedToOrderNumber(quote.getConvertedToOrder() != null ? quote.getConvertedToOrder().getOrderNumber() : null)
                .createdAt(quote.getCreatedAt())
                .updatedAt(quote.getUpdatedAt())
                .items(items)
                .build();
    }
}

