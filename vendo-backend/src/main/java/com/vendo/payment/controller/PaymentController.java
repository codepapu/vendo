package com.vendo.payment.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.payment.dto.request.ProcessPaymentRequest;
import com.vendo.payment.dto.request.RefundPaymentRequest;
import com.vendo.payment.dto.response.PaymentResponse;
import com.vendo.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getAllPayments(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        // If userId not provided, use authenticated user's ID
        if (userId == null && authentication != null) {
            userId = Long.parseLong(authentication.getName());
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponse> payments = paymentService.getAllPayments(userId, orderId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }
    
    @GetMapping("/charge/{chargeId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByChargeId(@PathVariable String chargeId) {
        PaymentResponse payment = paymentService.getPaymentByChargeId(chargeId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByOrderId(@PathVariable Long orderId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
    
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        request.setUserId(userId);
        PaymentResponse payment = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment processed successfully", payment));
    }
    
    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> processRefund(
            @Valid @RequestBody RefundPaymentRequest request) {
        PaymentResponse payment = paymentService.processRefund(request);
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", payment));
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        PaymentResponse payment = paymentService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Payment status updated", payment));
    }
    
    @PostMapping("/webhook/{gateway}")
    public ResponseEntity<ApiResponse<PaymentResponse>> handleWebhook(
            @PathVariable String gateway,
            @RequestBody String payload,
            @RequestHeader(value = "X-Signature", required = false) String signature) {
        PaymentResponse payment = paymentService.handleWebhook(gateway, payload, signature);
        return ResponseEntity.ok(ApiResponse.success("Webhook processed", payment));
    }
    
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentHistory(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<PaymentResponse> payments = paymentService.getPaymentHistory(userId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}
