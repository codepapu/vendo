package com.vendo.payment.service;

import com.vendo.common.event.EventPublisher;
import com.vendo.common.event.payment.PaymentFailedEvent;
import com.vendo.common.event.payment.PaymentProcessedEvent;
import com.vendo.common.event.payment.PaymentRefundedEvent;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.payment.dto.request.ProcessPaymentRequest;
import com.vendo.payment.dto.request.RefundPaymentRequest;
import com.vendo.payment.dto.response.PaymentGatewayResponse;
import com.vendo.payment.dto.response.PaymentResponse;
import com.vendo.payment.entity.Payment;
import com.vendo.payment.enums.PaymentStatus;
import com.vendo.payment.factory.PaymentGatewayFactory;
import com.vendo.payment.gateway.PaymentGatewayService;
import com.vendo.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayFactory gatewayFactory;
    private final EventPublisher eventPublisher;
    
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Long userId, Long orderId, String status, Pageable pageable) {
        Page<Payment> payments;
        
        if (userId != null && orderId != null && status != null) {
            // All filters - would need custom query
            payments = paymentRepository.findAll(pageable);
        } else if (userId != null && orderId != null) {
            // User and order filter
            payments = paymentRepository.findAll(pageable);
        } else if (userId != null && status != null) {
            // User and status filter
            payments = paymentRepository.findAll(pageable);
        } else if (orderId != null && status != null) {
            // Order and status filter
            List<Payment> paymentList = paymentRepository.findByOrderIdAndStatus(orderId, status);
            // Convert to page (simplified - in production would use proper query)
            payments = paymentRepository.findAll(pageable);
        } else if (userId != null) {
            // User filter only
            payments = paymentRepository.findAll(pageable);
        } else if (orderId != null) {
            // Order filter only
            payments = paymentRepository.findAll(pageable);
        } else if (status != null) {
            // Status filter only
            payments = paymentRepository.findAll(pageable);
        } else {
            // No filters
            payments = paymentRepository.findAll(pageable);
        }
        
        return payments.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return mapToResponse(payment);
    }
    
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByChargeId(String chargeId) {
        Payment payment = paymentRepository.findByChargeId(chargeId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "chargeId", chargeId));
        return mapToResponse(payment);
    }
    
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        // Get the appropriate payment gateway service
        PaymentGatewayService gatewayService = gatewayFactory.getGatewayService(request.getGateway());
        
        // Process payment through gateway
        PaymentGatewayResponse gatewayResponse = gatewayService.processPayment(request);
        
        if (!gatewayResponse.isSuccess()) {
            // Publish PaymentFailedEvent
            PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                    null, // Payment ID not yet created
                    request.getOrderId(),
                    request.getUserId(),
                    request.getAmount(),
                    request.getCurrency(),
                    request.getGateway().name(),
                    gatewayResponse.getMessage(),
                    gatewayResponse.getErrorCode() != null ? gatewayResponse.getErrorCode() : "UNKNOWN"
            );
            eventPublisher.publish(failedEvent, "payment-failed");
            
            throw new BadRequestException("Payment processing failed: " + gatewayResponse.getMessage());
        }
        
        // Create payment record
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .chargeId(gatewayResponse.getChargeId())
                .paymentChannel(request.getGateway().name())
                .status(gatewayResponse.getStatus() != null ? 
                        gatewayResponse.getStatus().name().toLowerCase() : "pending")
                .description(request.getDescription())
                .paymentType("confirm")
                .paymentFee(BigDecimal.ZERO) // Would calculate based on gateway
                .build();
        
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment processed: ID={}, ChargeId={}, Gateway={}", 
                savedPayment.getId(), savedPayment.getChargeId(), request.getGateway());
        
        // Publish PaymentProcessedEvent
        PaymentProcessedEvent processedEvent = new PaymentProcessedEvent(
                savedPayment.getId(),
                savedPayment.getOrderId(),
                savedPayment.getUserId(),
                savedPayment.getAmount(),
                savedPayment.getCurrency(),
                savedPayment.getPaymentChannel(),
                savedPayment.getChargeId(),
                savedPayment.getStatus()
        );
        eventPublisher.publish(processedEvent, "payment-processed");
        
        return mapToResponse(savedPayment);
    }
    
    @Transactional
    public PaymentResponse processRefund(RefundPaymentRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", request.getPaymentId()));
        
        if (!"completed".equals(payment.getStatus())) {
            throw new BadRequestException("Only completed payments can be refunded");
        }
        
        // Determine refund amount
        BigDecimal refundAmount = request.getAmount();
        if (refundAmount == null) {
            refundAmount = payment.getAmount().subtract(payment.getRefundedAmount() != null ? 
                    payment.getRefundedAmount() : BigDecimal.ZERO);
        }
        
        // Check if refund amount is valid
        BigDecimal alreadyRefunded = payment.getRefundedAmount() != null ? 
                payment.getRefundedAmount() : BigDecimal.ZERO;
        BigDecimal maxRefundable = payment.getAmount().subtract(alreadyRefunded);
        
        if (refundAmount.compareTo(maxRefundable) > 0) {
            throw new BadRequestException("Refund amount exceeds refundable amount");
        }
        
        // Get payment gateway service
        com.vendo.payment.enums.PaymentGateway gateway = 
                com.vendo.payment.enums.PaymentGateway.valueOf(payment.getPaymentChannel());
        PaymentGatewayService gatewayService = gatewayFactory.getGatewayService(gateway);
        
        // Process refund through gateway
        PaymentGatewayResponse gatewayResponse = gatewayService.processRefund(
                payment.getChargeId(),
                refundAmount.doubleValue(),
                request.getReason()
        );
        
        if (!gatewayResponse.isSuccess()) {
            throw new BadRequestException("Refund processing failed: " + gatewayResponse.getMessage());
        }
        
        // Update payment record
        BigDecimal newRefundedAmount = alreadyRefunded.add(refundAmount);
        payment.setRefundedAmount(newRefundedAmount);
        payment.setRefundNote(request.getNote());
        
        if (newRefundedAmount.compareTo(payment.getAmount()) >= 0) {
            payment.setStatus("refunded");
        } else {
            payment.setStatus("partially_refunded");
        }
        
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Refund processed: Payment ID={}, Amount={}", 
                updatedPayment.getId(), refundAmount);
        
        // Publish PaymentRefundedEvent
        PaymentRefundedEvent refundedEvent = new PaymentRefundedEvent(
                updatedPayment.getId(),
                null, // Refund ID - would be available if we had a refund entity
                updatedPayment.getOrderId(),
                updatedPayment.getUserId(),
                refundAmount,
                updatedPayment.getCurrency(),
                request.getReason(),
                gatewayResponse.getChargeId() != null ? gatewayResponse.getChargeId() : null
        );
        eventPublisher.publish(refundedEvent, "payment-refunded");
        
        return mapToResponse(updatedPayment);
    }
    
    @Transactional
    public PaymentResponse updatePaymentStatus(Long id, String status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        
        payment.setStatus(status);
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment status updated: ID={}, Status={}", id, status);
        
        return mapToResponse(updatedPayment);
    }
    
    @Transactional
    public PaymentResponse handleWebhook(String gatewayName, String payload, String signature) {
        // Get payment gateway service
        com.vendo.payment.enums.PaymentGateway gateway = 
                com.vendo.payment.enums.PaymentGateway.valueOf(gatewayName.toUpperCase());
        PaymentGatewayService gatewayService = gatewayFactory.getGatewayService(gateway);
        
        // Handle webhook
        PaymentGatewayResponse gatewayResponse = gatewayService.handleWebhook(payload, signature);
        
        if (gatewayResponse.isSuccess() && gatewayResponse.getChargeId() != null) {
            // Update payment status based on webhook
            Payment payment = paymentRepository.findByChargeId(gatewayResponse.getChargeId())
                    .orElse(null);
            
            if (payment != null && gatewayResponse.getStatus() != null) {
                String oldStatus = payment.getStatus();
                payment.setStatus(gatewayResponse.getStatus().name().toLowerCase());
                payment = paymentRepository.save(payment);
                
                // Publish event based on status change
                if ("completed".equals(payment.getStatus()) && !"completed".equals(oldStatus)) {
                    PaymentProcessedEvent processedEvent = new PaymentProcessedEvent(
                            payment.getId(),
                            payment.getOrderId(),
                            payment.getUserId(),
                            payment.getAmount(),
                            payment.getCurrency(),
                            payment.getPaymentChannel(),
                            payment.getChargeId(),
                            payment.getStatus()
                    );
                    eventPublisher.publish(processedEvent, "payment-processed");
                } else if ("failed".equals(payment.getStatus()) || "declined".equals(payment.getStatus())) {
                    PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                            payment.getId(),
                            payment.getOrderId(),
                            payment.getUserId(),
                            payment.getAmount(),
                            payment.getCurrency(),
                            payment.getPaymentChannel(),
                            gatewayResponse.getMessage() != null ? gatewayResponse.getMessage() : "Payment failed",
                            gatewayResponse.getErrorCode() != null ? gatewayResponse.getErrorCode() : "UNKNOWN"
                    );
                    eventPublisher.publish(failedEvent, "payment-failed");
                }
                
                return mapToResponse(payment);
            }
        }
        
        // Return gateway response (payment not found or webhook doesn't contain charge ID)
        return PaymentResponse.builder()
                .status(gatewayResponse.getStatus() != null ? 
                        gatewayResponse.getStatus().name().toLowerCase() : "unknown")
                .build();
    }
    
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentHistory(Long userId) {
        // Get all payments for user
        List<Payment> payments = paymentRepository.findByUserId(userId);
        
        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
