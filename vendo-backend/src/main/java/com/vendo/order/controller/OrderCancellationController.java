package com.vendo.order.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.order.dto.request.CancelOrderRequest;
import com.vendo.order.dto.response.CancellationResponse;
import com.vendo.order.enums.CancellationReason;
import com.vendo.order.service.OrderCancellationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for order cancellation operations.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderCancellationController {
    
    private final OrderCancellationService cancellationService;
    
    /**
     * Get available cancellation reasons.
     * 
     * @return List of cancellation reasons
     */
    @GetMapping("/cancellation-reasons")
    public ResponseEntity<ApiResponse<List<CancellationReason>>> getCancellationReasons() {
        List<CancellationReason> reasons = cancellationService.getCancellationReasons();
        return ResponseEntity.ok(ApiResponse.success(reasons));
    }
    
    /**
     * Cancel an order.
     * 
     * @param orderId The ID of the order to cancel
     * @param request The cancellation request
     * @param authentication The authenticated user
     * @return CancellationResponse with cancellation details
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<CancellationResponse>> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequest request,
            Authentication authentication) {
        
        // Verify order belongs to user (unless admin)
        // This would be implemented in the service layer
        Long userId = Long.parseLong(authentication.getName());
        
        CancellationResponse response = cancellationService.cancelOrder(orderId, request);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", response));
    }
    
    /**
     * Process refund for a cancelled order.
     * Admin only operation.
     * 
     * @param orderId The ID of the order to refund
     * @param reason The reason for the refund
     * @return CancellationResponse with refund details
     */
    @PostMapping("/{orderId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CancellationResponse>> processRefund(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        
        String refundReason = reason != null ? reason : "Order cancellation refund";
        CancellationResponse response = cancellationService.processRefund(orderId, refundReason);
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", response));
    }
}
