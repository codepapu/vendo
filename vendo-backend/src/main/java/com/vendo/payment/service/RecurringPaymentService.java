package com.vendo.payment.service;

import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.payment.dto.request.ProcessPaymentRequest;
import com.vendo.payment.dto.response.PaymentResponse;
import com.vendo.payment.entity.Payment;
import com.vendo.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Recurring Payment Service
 * Handles subscription and recurring payment processing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringPaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    
    /**
     * Create a recurring payment subscription
     */
    @Transactional
    public PaymentResponse createRecurringPayment(ProcessPaymentRequest request, 
                                                   String frequency, // daily, weekly, monthly, yearly
                                                   Integer interval) {
        // In production, would create subscription in payment gateway
        // For now, create initial payment record with subscription metadata
        
        ProcessPaymentRequest initialRequest = ProcessPaymentRequest.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .gateway(request.getGateway())
                .paymentMethod(request.getPaymentMethod())
                .description("Recurring payment - " + frequency)
                .metadata(request.getMetadata())
                .build();
        
        // Add subscription info to metadata
        if (initialRequest.getMetadata() == null) {
            initialRequest.setMetadata(new java.util.HashMap<>());
        }
        initialRequest.getMetadata().put("subscription_frequency", frequency);
        initialRequest.getMetadata().put("subscription_interval", String.valueOf(interval));
        initialRequest.getMetadata().put("is_recurring", "true");
        
        return paymentService.processPayment(initialRequest);
    }
    
    /**
     * Process next recurring payment
     */
    @Transactional
    public PaymentResponse processNextRecurringPayment(Long subscriptionId) {
        // In production, would fetch subscription details and process next payment
        log.info("Processing next recurring payment for subscription: {}", subscriptionId);
        
        // This would be called by scheduler or webhook
        throw new UnsupportedOperationException("Recurring payment processing not fully implemented");
    }
    
    /**
     * Cancel recurring payment subscription
     */
    @Transactional
    public void cancelRecurringPayment(Long subscriptionId) {
        log.info("Cancelling recurring payment subscription: {}", subscriptionId);
        // In production, would cancel subscription in payment gateway
    }
    
    /**
     * Scheduled job to process recurring payments
     * Runs daily to check for due recurring payments
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void processDueRecurringPayments() {
        log.info("Processing due recurring payments");
        // In production, would query for due subscriptions and process them
    }
}
