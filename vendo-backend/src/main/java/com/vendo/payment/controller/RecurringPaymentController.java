package com.vendo.payment.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.payment.dto.request.ProcessPaymentRequest;
import com.vendo.payment.dto.response.PaymentResponse;
import com.vendo.payment.service.RecurringPaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/recurring")
@RequiredArgsConstructor
public class RecurringPaymentController {
    
    private final RecurringPaymentService recurringPaymentService;
    
    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<PaymentResponse>> createRecurringPayment(
            @Valid @RequestBody ProcessPaymentRequest request,
            @RequestParam @NotNull String frequency,
            @RequestParam @NotNull Integer interval,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        request.setUserId(userId);
        PaymentResponse payment = recurringPaymentService.createRecurringPayment(request, frequency, interval);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recurring payment subscription created", payment));
    }
    
    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRecurringPayment(@PathVariable Long subscriptionId) {
        recurringPaymentService.cancelRecurringPayment(subscriptionId);
        return ResponseEntity.ok(ApiResponse.success("Recurring payment cancelled"));
    }
}
