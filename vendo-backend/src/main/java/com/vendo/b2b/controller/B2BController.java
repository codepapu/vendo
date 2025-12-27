package com.vendo.b2b.controller;

import com.vendo.b2b.dto.request.*;
import com.vendo.b2b.dto.response.*;
import com.vendo.b2b.service.BulkOrderFileParserService;
import com.vendo.b2b.service.*;
import com.vendo.common.dto.ApiResponse;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "B2B", description = "B2B business account, purchase orders, invoices, quotes, and bulk order management endpoints")
@SecurityRequirement(name = "bearer-jwt")
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
    @Operation(
            summary = "Create a new business account",
            description = "Creates a new B2B business account for the authenticated user. " +
                    "The account will be in PENDING status until approved by an admin.",
            tags = {"B2B - Business Accounts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Business account created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BusinessAccountResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"message\":\"Business account created successfully\",\"data\":{\"id\":1,\"userId\":1,\"companyName\":\"Acme Corp\",\"businessType\":\"CORPORATE\",\"status\":\"PENDING\"}}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    @PostMapping("/accounts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BusinessAccountResponse>> createAccount(
            @Parameter(description = "Business account details", required = true)
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
    @Operation(
            summary = "Get business account by ID",
            description = "Retrieves a business account by its ID. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Business Accounts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Business account retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BusinessAccountResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Business account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/accounts/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusinessAccountResponse>> getAccount(
            @Parameter(description = "Business account ID", required = true, example = "1")
            @PathVariable Long id) {
        BusinessAccountResponse account = b2BService.getBusinessAccount(id);
        return ResponseEntity.ok(ApiResponse.success(account));
    }
    
    /**
     * Get business account for current user
     * Accessible by BUSINESS role users
     */
    @Operation(
            summary = "Get current user's business account",
            description = "Retrieves the business account associated with the authenticated user. " +
                    "Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Business Accounts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Business account retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BusinessAccountResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Business account not found for user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
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
    @Operation(
            summary = "Approve or reject a business account",
            description = "Approves or rejects a pending business account. Only accessible by ADMIN role. " +
                    "Status can be APPROVED or REJECTED.",
            tags = {"B2B - Business Accounts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Business account status updated successfully",
                    content = @Content(schema = @Schema(implementation = BusinessAccountResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Business account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PostMapping("/accounts/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusinessAccountResponse>> approveAccount(
            @Parameter(description = "Business account ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Approval request with status and optional notes", required = true)
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
    @Operation(
            summary = "Update credit limit for a business account",
            description = "Updates the credit limit for a business account. Only accessible by ADMIN role.",
            tags = {"B2B - Business Accounts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Credit limit updated successfully",
                    content = @Content(schema = @Schema(implementation = BusinessAccountResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Business account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PutMapping("/accounts/{id}/credit-limit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusinessAccountResponse>> updateCreditLimit(
            @Parameter(description = "Business account ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Credit limit update request", required = true)
            @Valid @RequestBody UpdateCreditLimitRequest request) {
        log.info("Updating credit limit for business account: {}", id);
        BusinessAccountResponse account = b2BService.updateCreditLimit(id, request);
        return ResponseEntity.ok(ApiResponse.success("Credit limit updated successfully", account));
    }
    
    /**
     * Get B2B pricing for a product
     * Accessible by BUSINESS role users or ADMIN
     */
    @Operation(
            summary = "Get B2B pricing for a product",
            description = "Retrieves B2B pricing for a product based on quantity and business account. " +
                    "If businessAccountId is not provided, it will be retrieved from the authenticated user's account.",
            tags = {"B2B - Pricing"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pricing retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PricingResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product or business account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/pricing/{productId}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PricingResponse>> getPricing(
            @Parameter(description = "Product ID", required = true, example = "1")
            @PathVariable Long productId,
            @Parameter(description = "Quantity for pricing calculation", required = true, example = "100")
            @RequestParam Integer quantity,
            @Parameter(description = "Business account ID (optional, defaults to authenticated user's account)", example = "1")
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
    @Operation(
            summary = "Create a new purchase order",
            description = "Creates a new purchase order for the authenticated business user. " +
                    "The purchase order will be in PENDING status until approved by an admin.",
            tags = {"B2B - Purchase Orders"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Purchase order created successfully",
                    content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/purchase-orders")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> createPurchaseOrder(
            @Parameter(description = "Purchase order details", required = true)
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
    @Operation(
            summary = "Get purchase orders",
            description = "Retrieves a paginated list of purchase orders. " +
                    "If businessAccountId is not provided, it will be retrieved from the authenticated user's account.",
            tags = {"B2B - Purchase Orders"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Purchase orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/purchase-orders")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PurchaseOrderResponse>>> getPurchaseOrders(
            @Parameter(description = "Business account ID (optional)", example = "1")
            @RequestParam(required = false) Long businessAccountId,
            @Parameter(description = "Filter by status (PENDING, APPROVED, REJECTED, etc.)", example = "APPROVED")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
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
    @Operation(
            summary = "Get purchase order by ID",
            description = "Retrieves a purchase order by its ID. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Purchase Orders"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Purchase order retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Purchase order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/purchase-orders/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getPurchaseOrder(
            @Parameter(description = "Purchase order ID", required = true, example = "1")
            @PathVariable Long id) {
        PurchaseOrderResponse po = purchaseOrderService.getPurchaseOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(po));
    }
    
    /**
     * Approve a purchase order
     * Only accessible by ADMIN
     */
    @Operation(
            summary = "Approve a purchase order",
            description = "Approves a pending purchase order. Only accessible by ADMIN role.",
            tags = {"B2B - Purchase Orders"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Purchase order approved successfully",
                    content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Purchase order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PostMapping("/purchase-orders/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> approvePurchaseOrder(
            @Parameter(description = "Purchase order ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Approval request with optional notes", required = true)
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
    @Operation(
            summary = "Reject a purchase order",
            description = "Rejects a pending purchase order. Only accessible by ADMIN role.",
            tags = {"B2B - Purchase Orders"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Purchase order rejected successfully",
                    content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Purchase order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PostMapping("/purchase-orders/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> rejectPurchaseOrder(
            @Parameter(description = "Purchase order ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Rejection request with optional notes", required = true)
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
    @Operation(
            summary = "Generate invoice for an order",
            description = "Generates an invoice for a completed order. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Invoices"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Invoice generated successfully",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invoice already exists or order not completed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/invoices/generate/{orderId}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generateInvoice(
            @Parameter(description = "Order ID", required = true, example = "1")
            @PathVariable Long orderId) {
        log.info("Generating invoice for order: {}", orderId);
        InvoiceResponse invoice = invoiceService.generateInvoice(orderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice generated successfully", invoice));
    }
    
    /**
     * Get invoices
     * Accessible by BUSINESS role users or ADMIN
     */
    @Operation(
            summary = "Get invoices",
            description = "Retrieves a paginated list of invoices. " +
                    "If businessAccountId is not provided, it will be retrieved from the authenticated user's account.",
            tags = {"B2B - Invoices"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Invoices retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/invoices")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getInvoices(
            @Parameter(description = "Business account ID (optional)", example = "1")
            @RequestParam(required = false) Long businessAccountId,
            @Parameter(description = "Filter by status (PENDING, PAID, OVERDUE, etc.)", example = "PENDING")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
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
    @Operation(
            summary = "Get invoice by ID",
            description = "Retrieves an invoice by its ID. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Invoices"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Invoice retrieved successfully",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(
            @Parameter(description = "Invoice ID", required = true, example = "1")
            @PathVariable Long id) {
        InvoiceResponse invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }
    
    /**
     * Mark invoice as paid
     * Accessible by BUSINESS role users or ADMIN
     */
    @Operation(
            summary = "Mark invoice as paid",
            description = "Marks an invoice as paid with the specified amount. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Invoices"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Invoice marked as paid successfully",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payment amount"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/invoices/{id}/mark-paid")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> markInvoiceAsPaid(
            @Parameter(description = "Invoice ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Paid amount", required = true, example = "1000.00")
            @RequestParam BigDecimal paidAmount) {
        log.info("Marking invoice {} as paid with amount: {}", id, paidAmount);
        InvoiceResponse invoice = invoiceService.markInvoiceAsPaid(id, paidAmount);
        return ResponseEntity.ok(ApiResponse.success("Invoice marked as paid", invoice));
    }
    
    /**
     * Get overdue invoices
     * Accessible by BUSINESS role users or ADMIN
     */
    @Operation(
            summary = "Get overdue invoices",
            description = "Retrieves all overdue invoices. " +
                    "If businessAccountId is not provided, it will be retrieved from the authenticated user's account.",
            tags = {"B2B - Invoices"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Overdue invoices retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/invoices/overdue")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getOverdueInvoices(
            @Parameter(description = "Business account ID (optional)", example = "1")
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
    @Operation(
            summary = "Generate PDF invoice",
            description = "Generates a PDF document for an invoice. Returns the PDF file as a download. " +
                    "Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Invoices"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "PDF invoice generated successfully",
                    content = @Content(mediaType = "application/pdf")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/invoices/{id}/pdf")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> generateInvoicePdf(
            @Parameter(description = "Invoice ID", required = true, example = "1")
            @PathVariable Long id) {
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
    @Operation(
            summary = "Generate a new quote",
            description = "Generates a new quote for products. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Quotes"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Quote generated successfully",
                    content = @Content(schema = @Schema(implementation = QuoteResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/quotes")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuoteResponse>> generateQuote(
            @Parameter(description = "Quote creation request", required = true)
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
    @Operation(
            summary = "Get quotes",
            description = "Retrieves a paginated list of quotes. " +
                    "If businessAccountId is not provided, it will be retrieved from the authenticated user's account.",
            tags = {"B2B - Quotes"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Quotes retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/quotes")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<QuoteResponse>>> getQuotes(
            @Parameter(description = "Business account ID (optional)", example = "1")
            @RequestParam(required = false) Long businessAccountId,
            @Parameter(description = "Filter by status (PENDING, ACCEPTED, REJECTED, etc.)", example = "PENDING")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
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
    @Operation(
            summary = "Get quote by ID",
            description = "Retrieves a quote by its ID. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Quotes"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Quote retrieved successfully",
                    content = @Content(schema = @Schema(implementation = QuoteResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Quote not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/quotes/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuoteResponse>> getQuote(
            @Parameter(description = "Quote ID", required = true, example = "1")
            @PathVariable Long id) {
        QuoteResponse quote = quoteService.getQuoteById(id);
        return ResponseEntity.ok(ApiResponse.success(quote));
    }
    
    /**
     * Update quote
     * Accessible by BUSINESS role users or ADMIN
     */
    @Operation(
            summary = "Update quote",
            description = "Updates an existing quote. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Quotes"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Quote updated successfully",
                    content = @Content(schema = @Schema(implementation = QuoteResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Quote not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PutMapping("/quotes/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuoteResponse>> updateQuote(
            @Parameter(description = "Quote ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Quote update request", required = true)
            @Valid @RequestBody UpdateQuoteRequest request) {
        log.info("Updating quote: {}", id);
        QuoteResponse quote = quoteService.updateQuote(id, request);
        return ResponseEntity.ok(ApiResponse.success("Quote updated successfully", quote));
    }
    
    /**
     * Convert quote to order
     * Accessible by BUSINESS role users or ADMIN
     */
    @Operation(
            summary = "Convert quote to order",
            description = "Converts an accepted quote into an order. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Quotes"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Quote converted to order successfully",
                    content = @Content(schema = @Schema(implementation = com.vendo.order.dto.response.OrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Quote not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Quote not accepted or invalid state"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/quotes/{id}/convert")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.vendo.order.dto.response.OrderResponse>> convertQuoteToOrder(
            @Parameter(description = "Quote ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Conversion request with shipping details", required = true)
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
    @Operation(
            summary = "Create a new recurring order",
            description = "Creates a new recurring order that will be automatically processed at specified intervals. " +
                    "Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Recurring Orders"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Recurring order created successfully",
                    content = @Content(schema = @Schema(implementation = RecurringOrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/recurring-orders")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RecurringOrderResponse>> createRecurringOrder(
            @Parameter(description = "Recurring order details", required = true)
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
    @Operation(
            summary = "Get recurring orders",
            description = "Retrieves a paginated list of recurring orders. " +
                    "If businessAccountId is not provided, it will be retrieved from the authenticated user's account.",
            tags = {"B2B - Recurring Orders"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Recurring orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/recurring-orders")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RecurringOrderResponse>>> getRecurringOrders(
            @Parameter(description = "Business account ID (optional)", example = "1")
            @RequestParam(required = false) Long businessAccountId,
            @Parameter(description = "Filter by status (ACTIVE, CANCELLED, etc.)", example = "ACTIVE")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
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
    @Operation(
            summary = "Update recurring order",
            description = "Updates an existing recurring order. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Recurring Orders"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Recurring order updated successfully",
                    content = @Content(schema = @Schema(implementation = RecurringOrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurring order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PutMapping("/recurring-orders/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RecurringOrderResponse>> updateRecurringOrder(
            @Parameter(description = "Recurring order ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Recurring order update request", required = true)
            @Valid @RequestBody CreateRecurringOrderRequest request) {
        log.info("Updating recurring order: {}", id);
        RecurringOrderResponse recurringOrder = recurringOrderService.updateRecurringOrder(id, request);
        return ResponseEntity.ok(ApiResponse.success("Recurring order updated successfully", recurringOrder));
    }
    
    /**
     * Cancel recurring order
     * Accessible by BUSINESS role users or ADMIN
     */
    @Operation(
            summary = "Cancel recurring order",
            description = "Cancels an active recurring order. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Recurring Orders"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Recurring order cancelled successfully",
                    content = @Content(schema = @Schema(implementation = RecurringOrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurring order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/recurring-orders/{id}/cancel")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RecurringOrderResponse>> cancelRecurringOrder(
            @Parameter(description = "Recurring order ID", required = true, example = "1")
            @PathVariable Long id) {
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
    @Operation(
            summary = "Create a new order template",
            description = "Creates a reusable order template for quick order creation. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Order Templates"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Order template created successfully",
                    content = @Content(schema = @Schema(implementation = OrderTemplateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/order-templates")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderTemplateResponse>> createTemplate(
            @Parameter(description = "Order template details", required = true)
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
    @Operation(
            summary = "Get order templates",
            description = "Retrieves a paginated list of order templates. " +
                    "If businessAccountId is not provided, it will be retrieved from the authenticated user's account.",
            tags = {"B2B - Order Templates"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Order templates retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/order-templates")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderTemplateResponse>>> getTemplates(
            @Parameter(description = "Business account ID (optional)", example = "1")
            @RequestParam(required = false) Long businessAccountId,
            @Parameter(description = "Filter by shared status", example = "true")
            @RequestParam(required = false) Boolean isShared,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
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
    @Operation(
            summary = "Update order template",
            description = "Updates an existing order template. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Order Templates"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Order template updated successfully",
                    content = @Content(schema = @Schema(implementation = OrderTemplateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order template not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PutMapping("/order-templates/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderTemplateResponse>> updateTemplate(
            @Parameter(description = "Order template ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Order template update request", required = true)
            @Valid @RequestBody CreateOrderTemplateRequest request) {
        log.info("Updating order template: {}", id);
        OrderTemplateResponse template = orderTemplateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order template updated successfully", template));
    }
    
    /**
     * Delete order template
     * Accessible by BUSINESS role users or ADMIN
     */
    @Operation(
            summary = "Delete order template",
            description = "Deletes an order template. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Order Templates"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order template deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order template not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @DeleteMapping("/order-templates/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @Parameter(description = "Order template ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("Deleting order template: {}", id);
        orderTemplateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Order template deleted successfully", null));
    }
    
    /**
     * Apply template to create new order
     * Accessible by BUSINESS role users or ADMIN
     */
    @Operation(
            summary = "Apply template to create new order",
            description = "Creates a new order from an order template. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Order Templates"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Order created from template successfully",
                    content = @Content(schema = @Schema(implementation = com.vendo.order.dto.response.OrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order template not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid template or missing data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/order-templates/{id}/apply")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.vendo.order.dto.response.OrderResponse>> applyTemplate(
            @Parameter(description = "Order template ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Optional notes for the order", example = "Urgent order")
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
    @Operation(
            summary = "Create a new price list",
            description = "Creates a new price list with custom pricing for products. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Price Lists"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Price list created successfully",
                    content = @Content(schema = @Schema(implementation = PriceListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/price-lists")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PriceListResponse>> createPriceList(
            @Parameter(description = "Price list details", required = true)
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
    @Operation(
            summary = "Get price lists",
            description = "Retrieves a paginated list of price lists. " +
                    "If businessAccountId is not provided, it will be retrieved from the authenticated user's account.",
            tags = {"B2B - Price Lists"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Price lists retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/price-lists")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PriceListResponse>>> getPriceLists(
            @Parameter(description = "Business account ID (optional)", example = "1")
            @RequestParam(required = false) Long businessAccountId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
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
    @Operation(
            summary = "Get price list for business account",
            description = "Retrieves the active price list assigned to a business account. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Price Lists"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Price list retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PriceListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Price list not found for account"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/price-lists/account/{businessAccountId}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PriceListResponse>> getPriceListForAccount(
            @Parameter(description = "Business account ID", required = true, example = "1")
            @PathVariable Long businessAccountId) {
        PriceListResponse priceList = priceListService.getPriceListForAccount(businessAccountId);
        return ResponseEntity.ok(ApiResponse.success(priceList));
    }
    
    /**
     * Update price list
     * Accessible by BUSINESS role users or ADMIN
     */
    @Operation(
            summary = "Update price list",
            description = "Updates an existing price list. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Price Lists"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Price list updated successfully",
                    content = @Content(schema = @Schema(implementation = PriceListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Price list not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PutMapping("/price-lists/{id}")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PriceListResponse>> updatePriceList(
            @Parameter(description = "Price list ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Price list update request", required = true)
            @Valid @RequestBody CreatePriceListRequest request) {
        log.info("Updating price list: {}", id);
        PriceListResponse priceList = priceListService.updatePriceList(id, request);
        return ResponseEntity.ok(ApiResponse.success("Price list updated successfully", priceList));
    }
    
    /**
     * Assign price list to business account
     * Accessible by BUSINESS role users or ADMIN
     */
    @Operation(
            summary = "Assign price list to business account",
            description = "Assigns a price list to a business account. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Price Lists"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Price list assigned successfully",
                    content = @Content(schema = @Schema(implementation = PriceListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Price list or business account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/price-lists/{id}/assign")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PriceListResponse>> assignPriceList(
            @Parameter(description = "Price list ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Business account ID", required = true, example = "1")
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
    @Operation(
            summary = "Upload bulk order file",
            description = "Creates a bulk order from a CSV or Excel file upload. " +
                    "The file should contain product SKUs, quantities, and other order details. " +
                    "Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Bulk Orders"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Bulk order created from file successfully",
                    content = @Content(schema = @Schema(implementation = BulkOrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file format or data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping(value = "/bulk-orders/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BulkOrderResponse>> uploadBulkOrderFile(
            @Parameter(description = "CSV or Excel file with order data", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Business account ID", required = true, example = "1")
            @RequestParam("businessAccountId") Long businessAccountId,
            @Parameter(description = "Optional name for the bulk order", example = "Monthly Order")
            @RequestParam(required = false) String name,
            @Parameter(description = "Optional description", example = "Monthly bulk order for Q1")
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
    @Operation(
            summary = "Create a new bulk order",
            description = "Creates a new bulk order from JSON request. Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Bulk Orders"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Bulk order created successfully",
                    content = @Content(schema = @Schema(implementation = BulkOrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/bulk-orders")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BulkOrderResponse>> createBulkOrder(
            @Parameter(description = "Bulk order creation request", required = true)
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
    @Operation(
            summary = "Validate bulk order",
            description = "Validates a bulk order to check for errors, stock availability, and pricing. " +
                    "Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Bulk Orders"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Bulk order validated successfully",
                    content = @Content(schema = @Schema(implementation = BulkOrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bulk order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/bulk-orders/{id}/validate")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BulkOrderResponse>> validateBulkOrder(
            @Parameter(description = "Bulk order ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("Validating bulk order: {}", id);
        BulkOrderResponse bulkOrder = bulkOrderService.validateBulkOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Bulk order validated", bulkOrder));
    }
    
    /**
     * Process bulk order items
     * Accessible by BUSINESS role users or ADMIN
     */
    @Operation(
            summary = "Process bulk order items",
            description = "Processes bulk order items and creates individual orders. " +
                    "Requires BUSINESS or ADMIN role.",
            tags = {"B2B - Bulk Orders"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Bulk order processed successfully",
                    content = @Content(schema = @Schema(implementation = BulkOrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bulk order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bulk order not validated or invalid state"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/bulk-orders/{id}/process")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BulkOrderResponse>> processBulkOrderItems(
            @Parameter(description = "Bulk order ID", required = true, example = "1")
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
    @Operation(
            summary = "Get bulk orders",
            description = "Retrieves a paginated list of bulk orders. " +
                    "If businessAccountId is not provided, it will be retrieved from the authenticated user's account.",
            tags = {"B2B - Bulk Orders"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Bulk orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/bulk-orders")
    @PreAuthorize("hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BulkOrderResponse>>> getBulkOrders(
            @Parameter(description = "Business account ID (optional)", example = "1")
            @RequestParam(required = false) Long businessAccountId,
            @Parameter(description = "Filter by status (PENDING, VALIDATED, PROCESSED, etc.)", example = "PENDING")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
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

