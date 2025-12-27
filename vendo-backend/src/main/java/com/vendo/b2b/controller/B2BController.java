package com.vendo.b2b.controller;

import com.vendo.b2b.dto.request.ApproveBusinessAccountRequest;
import com.vendo.b2b.dto.request.ApprovePurchaseOrderRequest;
import com.vendo.b2b.dto.request.BusinessAccountRequest;
import com.vendo.b2b.dto.request.CreatePurchaseOrderRequest;
import com.vendo.b2b.dto.request.UpdateCreditLimitRequest;
import com.vendo.b2b.dto.response.BusinessAccountResponse;
import com.vendo.b2b.dto.response.InvoiceResponse;
import com.vendo.b2b.dto.response.PricingResponse;
import com.vendo.b2b.dto.response.PurchaseOrderResponse;
import com.vendo.b2b.service.B2BService;
import com.vendo.b2b.service.InvoiceService;
import com.vendo.b2b.service.PurchaseOrderService;
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

