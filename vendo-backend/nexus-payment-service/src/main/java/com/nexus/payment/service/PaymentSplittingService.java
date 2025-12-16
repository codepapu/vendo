package com.nexus.payment.service;

import com.nexus.common.exception.BadRequestException;
import com.nexus.payment.dto.response.PaymentResponse;
import com.nexus.payment.entity.Payment;
import com.nexus.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Payment Splitting Service
 * Handles marketplace payment splitting (vendor commissions, platform fees)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSplittingService {
    
    private final PaymentRepository paymentRepository;
    
    /**
     * Split payment for marketplace order
     * @param paymentId Original payment ID
     * @param splits Map of vendor/store IDs to amounts
     * @param platformFee Platform commission percentage
     */
    @Transactional
    public List<PaymentResponse> splitPayment(Long paymentId, Map<Long, BigDecimal> splits, BigDecimal platformFee) {
        Payment originalPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new com.nexus.common.exception.ResourceNotFoundException("Payment", "id", paymentId));
        
        // Validate total splits don't exceed payment amount
        BigDecimal totalSplits = splits.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal platformFeeAmount = originalPayment.getAmount()
                .multiply(platformFee)
                .divide(BigDecimal.valueOf(100));
        
        BigDecimal totalWithFee = totalSplits.add(platformFeeAmount);
        
        if (totalWithFee.compareTo(originalPayment.getAmount()) > 0) {
            throw new BadRequestException("Total split amount exceeds payment amount");
        }
        
        // Create split payments for each vendor
        List<Payment> splitPayments = splits.entrySet().stream()
                .map(entry -> Payment.builder()
                        .orderId(originalPayment.getOrderId())
                        .userId(entry.getKey()) // Vendor/store ID
                        .amount(entry.getValue())
                        .currency(originalPayment.getCurrency())
                        .chargeId(originalPayment.getChargeId() + "_split_" + entry.getKey())
                        .paymentChannel(originalPayment.getPaymentChannel())
                        .status("completed")
                        .paymentType("split")
                        .description("Payment split for vendor: " + entry.getKey())
                        .build())
                .collect(Collectors.toList());
        
        // Save split payments
        List<Payment> savedPayments = paymentRepository.saveAll(splitPayments);
        
        log.info("Payment split completed: Original={}, Splits={}, PlatformFee={}", 
                paymentId, splits.size(), platformFeeAmount);
        
        return savedPayments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate platform commission
     */
    public BigDecimal calculatePlatformFee(BigDecimal amount, BigDecimal commissionRate) {
        return amount.multiply(commissionRate)
                .divide(BigDecimal.valueOf(100));
    }
    
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .chargeId(payment.getChargeId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentFee(payment.getPaymentFee())
                .refundedAmount(payment.getRefundedAmount())
                .currency(payment.getCurrency())
                .paymentChannel(payment.getPaymentChannel())
                .status(payment.getStatus())
                .paymentType(payment.getPaymentType())
                .description(payment.getDescription())
                .refundNote(payment.getRefundNote())
                .metadata(payment.getMetadata())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
