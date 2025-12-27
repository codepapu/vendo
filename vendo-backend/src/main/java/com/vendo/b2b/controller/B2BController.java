package com.vendo.b2b.controller;

import com.vendo.b2b.dto.request.*;
import com.vendo.b2b.dto.response.*;
import com.vendo.b2b.service.BulkOrderFileParserService;
import com.vendo.b2b.service.*;
import com.vendo.common.dto.ApiResponse;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/b2b")
@RequiredArgsConstructor
public class B2BController {
    
    private final B2BService b2BService;
    private final PurchaseOrderService purchaseOrderService;
    private final InvoiceService invoiceService;
    private final QuoteService quoteService;
    private final RecurringOrderService recurringOrderService;
    private final OrderTemplateService orderTemplateService;
    private final PriceListService priceListService;
    private final BulkOrderService bulkOrderService;
    private final BulkOrderFileParserService bulkOrderFileParserService;
    
    /**
     * Create a new business account
     * Accessible by authenticated users
     */
    @PostMapping("/accounts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BusinessAccountResponse>> createAccount(
            @Valid @RequestBody BusinessAccountRequest request,
            Authentication authentication) {
        log.info("Creating business account for user: {}", authentication.getName());
        Long userId = Long.parseLong(authentication.getName());
        BusinessAccountResponse account = b2BService.createBusinessAccount(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business account created successfully", account));
    }
    
    /**
     * Get business account by ID
     * Accessible by BUSINESS role users or ADMIN
     */
    @GetMapping("/accounts/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusinessAccountResponse>> getAccount(@PathVariable Long id) {
        BusinessAccountResponse account = b2BService.getBusinessAccount(id);
        return ResponseEntity.ok(ApiResponse.success(account));
    }
    
    /**
     * Get business account for current user
     * Accessible by BUSINESS role users
     */
    @GetMapping("/accounts/me")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusinessAccountResponse>> getMyAccount(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        BusinessAccountResponse account = b2BService.getBusinessAccountByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(account));
    }
    
    /**
     * Approve or reject a business account
     * Only accessible by ADMIN
     */
    @PostMapping("/accounts/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusinessAccountResponse>> approveAccount(
            @PathVariable Long id,
            @Valid @RequestBody ApproveBusinessAccountRequest request,
            Authentication authentication) {
        log.info("Approving business account: {} by admin: {}", id, authentication.getName());
        Long approverUserId = Long.parseLong(authentication.getName());
        BusinessAccountResponse account = b2BService.approveBusinessAccount(id, approverUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Business account " + request.getStatus().toLowerCase(), account));
    }
    
    /**
     * Update credit limit for a business account
     * Only accessible by ADMIN
     */
    @PutMapping("/accounts/{id}/credit-limit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusinessAccountResponse>> updateCreditLimit(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCreditLimitRequest request) {
        log.info("Updating credit limit for business account: {}", id);
        BusinessAccountResponse account = b2BService.updateCreditLimit(id, request);
        return ResponseEntity.ok(ApiResponse.success("Credit limit updated successfully", account));
    }
    
    /**
     * Get B2B pricing for a product
     * Accessible by BUSINESS role users or ADMIN
     */
    @GetMapping("/pricing/{productId}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PricingResponse>> getPricing(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) Long businessAccountId,
            Authentication authentication) {
        
        // If businessAccountId not provided, try to get from authenticated user
        if (businessAccountId == null && authentication != null) {
            try {
                Long userId = Long.parseLong(authentication.getName());
                BusinessAccountResponse account = b2BService.getBusinessAccountByUserId(userId);
                businessAccountId = account.getId();
            } catch (ResourceNotFoundException e) {
                // User doesn't have a business account, continue without it
                log.debug("User {} does not have a business account, getting pricing without account", authentication.getName());
            }
        }
        
        PricingResponse pricing = b2BService.getB2BPricing(productId, businessAccountId, quantity);
        return ResponseEntity.ok(ApiResponse.success(pricing));
    }
    
    /**
     * Create a new purchase order
     * Accessible by BUSINESS role users
     */
    @PostMapping("/purchase-orders")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> createPurchaseOrder(
            @Valid @RequestBody CreatePurchaseOrderRequest request,
            Authentication authentication) {
        log.info("Creating purchase order for user: {}", authentication.getName());
        Long userId = Long.parseLong(authentication.getName());
        PurchaseOrderResponse po = purchaseOrderService.createPurchaseOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase order created successfully", po));
    }
    
    /**
     * Get purchase orders
     * Accessible by BUSINESS role users or ADMIN
     */
    @GetMapping("/purchase-orders")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PurchaseOrderResponse>>> getPurchaseOrders(
            @RequestParam(required = false) Long businessAccountId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        // If businessAccountId not provided, try to get from authenticated user
        if (businessAccountId == null && authentication != null) {
            try {
                Long userId = Long.parseLong(authentication.getName());
                BusinessAccountResponse account = b2BService.getBusinessAccountByUserId(userId);
                businessAccountId = account.getId();
            } catch (ResourceNotFoundException e) {
                // User doesn't have a business account, continue without it
                log.debug("User {} does not have a business account", authentication.getName());
            }
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseOrderResponse> purchaseOrders = purchaseOrderService.getPurchaseOrders(businessAccountId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(purchaseOrders));
    }
    
    /**
     * Get purchase order by ID
     * Accessible by BUSINESS role users or ADMIN
     */
    @GetMapping("/purchase-orders/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getPurchaseOrder(@PathVariable Long id) {
        PurchaseOrderResponse po = purchaseOrderService.getPurchaseOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(po));
    }
    
    /**
     * Approve a purchase order
     * Only accessible by ADMIN
     */
    @PostMapping("/purchase-orders/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> approvePurchaseOrder(
            @PathVariable Long id,
            @Valid @RequestBody ApprovePurchaseOrderRequest request,
            Authentication authentication) {
        log.info("Approving purchase order: {} by admin: {}", id, authentication.getName());
        Long approverUserId = Long.parseLong(authentication.getName());
        PurchaseOrderResponse po = purchaseOrderService.approvePurchaseOrder(id, approverUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Purchase order approved", po));
    }
    
    /**
     * Reject a purchase order
     * Only accessible by ADMIN
     */
    @PostMapping("/purchase-orders/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> rejectPurchaseOrder(
            @PathVariable Long id,
            @Valid @RequestBody ApprovePurchaseOrderRequest request,
            Authentication authentication) {
        log.info("Rejecting purchase order: {} by admin: {}", id, authentication.getName());
        Long approverUserId = Long.parseLong(authentication.getName());
        PurchaseOrderResponse po = purchaseOrderService.rejectPurchaseOrder(id, approverUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Purchase order rejected", po));
    }
    
    /**
     * Generate invoice for an order
     * Accessible by BUSINESS role users or ADMIN
     */
    @PostMapping("/invoices/generate/{orderId}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generateInvoice(@PathVariable Long orderId) {
        log.info("Generating invoice for order: {}", orderId);
        InvoiceResponse invoice = invoiceService.generateInvoice(orderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice generated successfully", invoice));
    }
    
    /**
     * Get invoices
     * Accessible by BUSINESS role users or ADMIN
     */
    @GetMapping("/invoices")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getInvoices(
            @RequestParam(required = false) Long businessAccountId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        // If businessAccountId not provided, try to get from authenticated user
        if (businessAccountId == null && authentication != null) {
            try {
                Long userId = Long.parseLong(authentication.getName());
                BusinessAccountResponse account = b2BService.getBusinessAccountByUserId(userId);
                businessAccountId = account.getId();
            } catch (ResourceNotFoundException e) {
                // User doesn't have a business account, continue without it
                log.debug("User {} does not have a business account", authentication.getName());
            }
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponse> invoices = invoiceService.getInvoices(businessAccountId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }
    
    /**
     * Get invoice by ID
     * Accessible by BUSINESS role users or ADMIN
     */
    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(@PathVariable Long id) {
        InvoiceResponse invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }
    
    /**
     * Mark invoice as paid
     * Accessible by BUSINESS role users or ADMIN
     */
    @PostMapping("/invoices/{id}/mark-paid")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> markInvoiceAsPaid(
            @PathVariable Long id,
            @RequestParam BigDecimal paidAmount) {
        log.info("Marking invoice {} as paid with amount: {}", id, paidAmount);
        InvoiceResponse invoice = invoiceService.markInvoiceAsPaid(id, paidAmount);
        return ResponseEntity.ok(ApiResponse.success("Invoice marked as paid", invoice));
    }
    
    /**
     * Get overdue invoices
     * Accessible by BUSINESS role users or ADMIN
     */
    @GetMapping("/invoices/overdue")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getOverdueInvoices(
            @RequestParam(required = false) Long businessAccountId,
            Authentication authentication) {
        
        // If businessAccountId not provided, try to get from authenticated user
        if (businessAccountId == null && authentication != null) {
            try {
                Long userId = Long.parseLong(authentication.getName());
                BusinessAccountResponse account = b2BService.getBusinessAccountByUserId(userId);
                businessAccountId = account.getId();
            } catch (ResourceNotFoundException e) {
                // User doesn't have a business account, continue without it
                log.debug("User {} does not have a business account", authentication.getName());
            }
        }
        
        List<InvoiceResponse> invoices = invoiceService.getOverdueInvoices(businessAccountId);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }
    
    /**
     * Generate PDF invoice
     * Accessible by BUSINESS role users or ADMIN
     */
    @GetMapping("/invoices/{id}/pdf")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> generateInvoicePdf(@PathVariable Long id) {
        log.info("Generating PDF for invoice: {}", id);
        byte[] pdfBytes = invoiceService.generateInvoicePdf(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice-" + id + ".pdf");
        headers.setContentLength(pdfBytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    // ============================================
    // Quote Endpoints
    // ============================================
    
    /**
     * Generate a new quote
     * Accessible by BUSINESS role users
     */
    @PostMapping("/quotes")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuoteResponse>> generateQuote(
            @Valid @RequestBody CreateQuoteRequest request,
            Authentication authentication) {
        log.info("Generating quote for user: {}", authentication.getName());
        Long userId = Long.parseLong(authentication.getName());
        QuoteResponse quote = quoteService.generateQuote(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quote generated successfully", quote));
    }
    
    /**
     * Get quotes
     * Accessible by BUSINESS role users or ADMIN
     */
    @GetMapping("/quotes")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<QuoteResponse>>> getQuotes(
            @RequestParam(required = false) Long businessAccountId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        if (businessAccountId == null && authentication != null) {
            try {
                Long userId = Long.parseLong(authentication.getName());
                BusinessAccountResponse account = b2BService.getBusinessAccountByUserId(userId);
                businessAccountId = account.getId();
            } catch (ResourceNotFoundException e) {
                log.debug("User {} does not have a business account", authentication.getName());
            }
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<QuoteResponse> quotes = quoteService.getQuotes(businessAccountId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }
    
    /**
     * Get quote by ID
     * Accessible by BUSINESS role users or ADMIN
     */
    @GetMapping("/quotes/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuoteResponse>> getQuote(@PathVariable Long id) {
        QuoteResponse quote = quoteService.getQuoteById(id);
        return ResponseEntity.ok(ApiResponse.success(quote));
    }
    
    /**
     * Update quote
     * Accessible by BUSINESS role users or ADMIN
     */
    @PutMapping("/quotes/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuoteResponse>> updateQuote(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuoteRequest request) {
        log.info("Updating quote: {}", id);
        QuoteResponse quote = quoteService.updateQuote(id, request);
        return ResponseEntity.ok(ApiResponse.success("Quote updated successfully", quote));
    }
    
    /**
     * Convert quote to order
     * Accessible by BUSINESS role users or ADMIN
     */
    @PostMapping("/quotes/{id}/convert")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.vendo.order.dto.response.OrderResponse>> convertQuoteToOrder(
            @PathVariable Long id,
            @Valid @RequestBody ConvertQuoteToOrderRequest request,
            Authentication authentication) {
        log.info("Converting quote {} to order for user: {}", id, authentication.getName());
        Long userId = Long.parseLong(authentication.getName());
        request.setQuoteId(id);
        com.vendo.order.dto.response.OrderResponse order = quoteService.convertQuoteToOrder(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Quote converted to order successfully", order));
    }
    
    // ============================================
    // Recurring Order Endpoints
    // ============================================
    
    /**
     * Create a new recurring order
     * Accessible by BUSINESS role users
     */
    @PostMapping("/recurring-orders")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RecurringOrderResponse>> createRecurringOrder(
            @Valid @RequestBody CreateRecurringOrderRequest request,
            Authentication authentication) {
        log.info("Creating recurring order for user: {}", authentication.getName());
        Long userId = Long.parseLong(authentication.getName());
        RecurringOrderResponse recurringOrder = recurringOrderService.createRecurringOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recurring order created successfully", recurringOrder));
    }
    
    /**
     * Get recurring orders
     * Accessible by BUSINESS role users or ADMIN
     */
    @GetMapping("/recurring-orders")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RecurringOrderResponse>>> getRecurringOrders(
            @RequestParam(required = false) Long businessAccountId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        if (businessAccountId == null && authentication != null) {
            try {
                Long userId = Long.parseLong(authentication.getName());
                BusinessAccountResponse account = b2BService.getBusinessAccountByUserId(userId);
                businessAccountId = account.getId();
            } catch (ResourceNotFoundException e) {
                log.debug("User {} does not have a business account", authentication.getName());
            }
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<RecurringOrderResponse> recurringOrders = recurringOrderService.getRecurringOrders(businessAccountId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(recurringOrders));
    }
    
    /**
     * Update recurring order
     * Accessible by BUSINESS role users or ADMIN
     */
    @PutMapping("/recurring-orders/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RecurringOrderResponse>> updateRecurringOrder(
            @PathVariable Long id,
            @Valid @RequestBody CreateRecurringOrderRequest request) {
        log.info("Updating recurring order: {}", id);
        RecurringOrderResponse recurringOrder = recurringOrderService.updateRecurringOrder(id, request);
        return ResponseEntity.ok(ApiResponse.success("Recurring order updated successfully", recurringOrder));
    }
    
    /**
     * Cancel recurring order
     * Accessible by BUSINESS role users or ADMIN
     */
    @PostMapping("/recurring-orders/{id}/cancel")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RecurringOrderResponse>> cancelRecurringOrder(@PathVariable Long id) {
        log.info("Cancelling recurring order: {}", id);
        RecurringOrderResponse recurringOrder = recurringOrderService.cancelRecurringOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Recurring order cancelled successfully", recurringOrder));
    }
    
    // ============================================
    // Order Template Endpoints
    // ============================================
    
    /**
     * Create a new order template
     * Accessible by BUSINESS role users
     */
    @PostMapping("/order-templates")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderTemplateResponse>> createTemplate(
            @Valid @RequestBody CreateOrderTemplateRequest request,
            Authentication authentication) {
        log.info("Creating order template for user: {}", authentication.getName());
        Long userId = Long.parseLong(authentication.getName());
        OrderTemplateResponse template = orderTemplateService.createTemplate(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order template created successfully", template));
    }
    
    /**
     * Get order templates
     * Accessible by BUSINESS role users or ADMIN
     */
    @GetMapping("/order-templates")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderTemplateResponse>>> getTemplates(
            @RequestParam(required = false) Long businessAccountId,
            @RequestParam(required = false) Boolean isShared,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        if (businessAccountId == null && authentication != null) {
            try {
                Long userId = Long.parseLong(authentication.getName());
                BusinessAccountResponse account = b2BService.getBusinessAccountByUserId(userId);
                businessAccountId = account.getId();
            } catch (ResourceNotFoundException e) {
                log.debug("User {} does not have a business account", authentication.getName());
            }
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderTemplateResponse> templates = orderTemplateService.getTemplates(businessAccountId, isShared, pageable);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }
    
    /**
     * Update order template
     * Accessible by BUSINESS role users or ADMIN
     */
    @PutMapping("/order-templates/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderTemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody CreateOrderTemplateRequest request) {
        log.info("Updating order template: {}", id);
        OrderTemplateResponse template = orderTemplateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order template updated successfully", template));
    }
    
    /**
     * Delete order template
     * Accessible by BUSINESS role users or ADMIN
     */
    @DeleteMapping("/order-templates/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        log.info("Deleting order template: {}", id);
        orderTemplateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Order template deleted successfully", null));
    }
    
    /**
     * Apply template to create new order
     * Accessible by BUSINESS role users or ADMIN
     */
    @PostMapping("/order-templates/{id}/apply")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.vendo.order.dto.response.OrderResponse>> applyTemplate(
            @PathVariable Long id,
            @RequestParam(required = false) String notes,
            Authentication authentication) {
        log.info("Applying template {} to create order for user: {}", id, authentication.getName());
        Long userId = Long.parseLong(authentication.getName());
        com.vendo.order.dto.response.OrderResponse order = orderTemplateService.applyTemplate(userId, id, notes);
        return ResponseEntity.ok(ApiResponse.success("Order created from template successfully", order));
    }
    
    // ============================================
    // Price List Endpoints
    // ============================================
    
    /**
     * Create a new price list
     * Accessible by BUSINESS role users or ADMIN
     */
    @PostMapping("/price-lists")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PriceListResponse>> createPriceList(
            @Valid @RequestBody CreatePriceListRequest request,
            Authentication authentication) {
        log.info("Creating price list for user: {}", authentication.getName());
        Long userId = Long.parseLong(authentication.getName());
        PriceListResponse priceList = priceListService.createPriceList(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Price list created successfully", priceList));
    }
    
    /**
     * Get price lists
     * Accessible by BUSINESS role users or ADMIN
     */
    @GetMapping("/price-lists")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PriceListResponse>>> getPriceLists(
            @RequestParam(required = false) Long businessAccountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        if (businessAccountId == null && authentication != null) {
            try {
                Long userId = Long.parseLong(authentication.getName());
                BusinessAccountResponse account = b2BService.getBusinessAccountByUserId(userId);
                businessAccountId = account.getId();
            } catch (ResourceNotFoundException e) {
                log.debug("User {} does not have a business account", authentication.getName());
            }
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PriceListResponse> priceLists = priceListService.getPriceLists(businessAccountId, pageable);
        return ResponseEntity.ok(ApiResponse.success(priceLists));
    }
    
    /**
     * Get price list for business account
     * Accessible by BUSINESS role users or ADMIN
     */
    @GetMapping("/price-lists/account/{businessAccountId}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PriceListResponse>> getPriceListForAccount(@PathVariable Long businessAccountId) {
        PriceListResponse priceList = priceListService.getPriceListForAccount(businessAccountId);
        return ResponseEntity.ok(ApiResponse.success(priceList));
    }
    
    /**
     * Update price list
     * Accessible by BUSINESS role users or ADMIN
     */
    @PutMapping("/price-lists/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PriceListResponse>> updatePriceList(
            @PathVariable Long id,
            @Valid @RequestBody CreatePriceListRequest request) {
        log.info("Updating price list: {}", id);
        PriceListResponse priceList = priceListService.updatePriceList(id, request);
        return ResponseEntity.ok(ApiResponse.success("Price list updated successfully", priceList));
    }
    
    /**
     * Assign price list to business account
     * Accessible by BUSINESS role users or ADMIN
     */
    @PostMapping("/price-lists/{id}/assign")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PriceListResponse>> assignPriceList(
            @PathVariable Long id,
            @RequestParam Long businessAccountId,
            Authentication authentication) {
        log.info("Assigning price list {} to business account {}", id, businessAccountId);
        Long assignedByUserId = Long.parseLong(authentication.getName());
        PriceListResponse priceList = priceListService.assignPriceList(id, businessAccountId, assignedByUserId);
        return ResponseEntity.ok(ApiResponse.success("Price list assigned successfully", priceList));
    }
    
    // ============================================
    // Bulk Order Endpoints
    // ============================================
    
    /**
     * Create a new bulk order from file upload (CSV/Excel)
     * Accessible by BUSINESS role users
     */
    @PostMapping(value = "/bulk-orders/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BulkOrderResponse>> uploadBulkOrderFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("businessAccountId") Long businessAccountId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            Authentication authentication) {
        log.info("Uploading bulk order file: {} for user: {}", file.getOriginalFilename(), authentication.getName());
        Long userId = Long.parseLong(authentication.getName());
        
        // Parse file and create bulk order request
        CreateBulkOrderRequest request = bulkOrderFileParserService.parseBulkOrderFile(
                file, businessAccountId, name, description);
        
        // Create bulk order from parsed data
        BulkOrderResponse bulkOrder = bulkOrderService.createBulkOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bulk order created from file successfully", bulkOrder));
    }
    
    /**
     * Create a new bulk order from JSON request
     * Accessible by BUSINESS role users
     */
    @PostMapping("/bulk-orders")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BulkOrderResponse>> createBulkOrder(
            @Valid @RequestBody CreateBulkOrderRequest request,
            Authentication authentication) {
        log.info("Creating bulk order for user: {}", authentication.getName());
        Long userId = Long.parseLong(authentication.getName());
        BulkOrderResponse bulkOrder = bulkOrderService.createBulkOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bulk order created successfully", bulkOrder));
    }
    
    /**
     * Validate bulk order
     * Accessible by BUSINESS role users or ADMIN
     */
    @PostMapping("/bulk-orders/{id}/validate")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BulkOrderResponse>> validateBulkOrder(@PathVariable Long id) {
        log.info("Validating bulk order: {}", id);
        BulkOrderResponse bulkOrder = bulkOrderService.validateBulkOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Bulk order validated", bulkOrder));
    }
    
    /**
     * Process bulk order items
     * Accessible by BUSINESS role users or ADMIN
     */
    @PostMapping("/bulk-orders/{id}/process")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BulkOrderResponse>> processBulkOrderItems(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("Processing bulk order items for bulk order: {}", id);
        Long userId = Long.parseLong(authentication.getName());
        BulkOrderResponse bulkOrder = bulkOrderService.processBulkOrderItems(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Bulk order processed", bulkOrder));
    }
    
    /**
     * Get bulk orders
     * Accessible by BUSINESS role users or ADMIN
     */
    @GetMapping("/bulk-orders")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BulkOrderResponse>>> getBulkOrders(
            @RequestParam(required = false) Long businessAccountId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        if (businessAccountId == null && authentication != null) {
            try {
                Long userId = Long.parseLong(authentication.getName());
                BusinessAccountResponse account = b2BService.getBusinessAccountByUserId(userId);
                businessAccountId = account.getId();
            } catch (ResourceNotFoundException e) {
                log.debug("User {} does not have a business account", authentication.getName());
            }
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BulkOrderResponse> bulkOrders = bulkOrderService.getBulkOrders(businessAccountId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(bulkOrders));
    }
    
    /**
     * Exception handler for BadRequestException
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex) {
        log.error("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    /**
     * Exception handler for ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }
}

