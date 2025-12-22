package com.vendo.payment.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.payment.dto.response.PaymentResponse;
import com.vendo.payment.service.PaymentSplittingService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/splitting")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PaymentSplittingController {
    
    private final PaymentSplittingService paymentSplittingService;
    
    @PostMapping("/{paymentId}/split")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> splitPayment(
            @PathVariable Long paymentId,
            @RequestBody @NotNull Map<Long, BigDecimal> splits,
            @RequestParam(defaultValue = "10.0") BigDecimal platformFee) {
        List<PaymentResponse> splitPayments = paymentSplittingService.splitPayment(paymentId, splits, platformFee);
        return ResponseEntity.ok(ApiResponse.success("Payment split successfully", splitPayments));
    }
    
    @GetMapping("/calculate-fee")
    public ResponseEntity<ApiResponse<BigDecimal>> calculatePlatformFee(
            @RequestParam @NotNull BigDecimal amount,
            @RequestParam @NotNull BigDecimal commissionRate) {
        BigDecimal fee = paymentSplittingService.calculatePlatformFee(amount, commissionRate);
        return ResponseEntity.ok(ApiResponse.success(fee));
    }
}
