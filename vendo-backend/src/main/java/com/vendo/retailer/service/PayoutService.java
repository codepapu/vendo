package com.vendo.retailer.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.entity.OrderItem;
import com.vendo.order.repository.OrderItemRepository;
import com.vendo.retailer.dto.response.PayoutResponse;
import com.vendo.retailer.entity.Retailer;
import com.vendo.retailer.entity.RetailerBankAccount;
import com.vendo.retailer.entity.RetailerPayout;
import com.vendo.retailer.repository.RetailerBankAccountRepository;
import com.vendo.retailer.repository.RetailerPayoutRepository;
import com.vendo.retailer.repository.RetailerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {
    
    private final RetailerRepository retailerRepository;
    private final RetailerPayoutRepository payoutRepository;
    private final RetailerBankAccountRepository bankAccountRepository;
    private final OrderItemRepository orderItemRepository;
    private final CommissionService commissionService;
    
    /**
     * Calculate payout for a retailer for a given period
     */
    @Transactional(readOnly = true)
    public PayoutResponse calculatePayout(Long retailerId, LocalDateTime startDate, LocalDateTime endDate) {
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        // Get all order items for this retailer in the period
        List<OrderItem> orderItems = orderItemRepository.findByRetailerId(retailerId).stream()
                .filter(item -> {
                    LocalDateTime createdAt = item.getCreatedAt();
                    if (createdAt == null) return false;
                    return !createdAt.isBefore(startDate) && !createdAt.isAfter(endDate);
                })
                .collect(Collectors.toList());
        
        // Calculate total sales
        BigDecimal totalSales = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate total commission
        BigDecimal totalCommission = orderItems.stream()
                .map(item -> item.getCommissionAmount() != null ? item.getCommissionAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Payout = Total Sales - Commission
        BigDecimal payoutAmount = totalSales.subtract(totalCommission);
        
        return PayoutResponse.builder()
                .retailerId(retailerId)
                .retailerName(retailer.getStoreName())
                .payoutAmount(payoutAmount)
                .totalSales(totalSales)
                .totalCommission(totalCommission)
                .periodStart(startDate)
                .periodEnd(endDate)
                .status("CALCULATED")
                .build();
    }
    
    /**
     * Process payout to retailer bank account
     */
    @Transactional
    public PayoutResponse processPayout(Long retailerId, LocalDateTime startDate, LocalDateTime endDate,
                                        Long bankAccountId, Long processedBy) {
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        // Validate bank account
        RetailerBankAccount bankAccount = null;
        if (bankAccountId != null) {
            bankAccount = bankAccountRepository.findById(bankAccountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Bank Account", "id", bankAccountId));
            
            if (!bankAccount.getRetailer().getId().equals(retailerId)) {
                throw new BadRequestException("Bank account does not belong to this retailer");
            }
        } else {
            // Get primary bank account
            List<RetailerBankAccount> accounts = bankAccountRepository.findByRetailerId(retailerId);
            bankAccount = accounts.stream()
                    .filter(RetailerBankAccount::getIsPrimary)
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("No primary bank account found for retailer"));
        }
        
        // Calculate payout
        PayoutResponse calculated = calculatePayout(retailerId, startDate, endDate);
        
        if (calculated.getPayoutAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payout amount must be greater than zero");
        }
        
        // Create payout record
        RetailerPayout payout = RetailerPayout.builder()
                .retailer(retailer)
                .payoutAmount(calculated.getPayoutAmount())
                .totalSales(calculated.getTotalSales())
                .totalCommission(calculated.getTotalCommission())
                .periodStart(startDate)
                .periodEnd(endDate)
                .status("PROCESSING")
                .payoutMethod("BANK_TRANSFER")
                .bankAccountId(bankAccount.getId())
                .processedBy(processedBy)
                .notes("Payout for period " + startDate + " to " + endDate)
                .build();
        
        payout = payoutRepository.save(payout);
        
        // TODO: Integrate with payment gateway (Stripe, PayPal, etc.) to process actual transfer
        // For now, we'll simulate the transfer
        try {
            // Simulate payment processing
            Thread.sleep(100); // Simulate API call
            
            // Update payout status
            payout.setStatus("COMPLETED");
            payout.setProcessedAt(LocalDateTime.now());
            payout.setTransactionId("TXN-" + System.currentTimeMillis());
            payout = payoutRepository.save(payout);
            
            log.info("Payout processed successfully: retailerId={}, amount={}", retailerId, calculated.getPayoutAmount());
        } catch (Exception e) {
            log.error("Error processing payout: {}", e.getMessage(), e);
            payout.setStatus("FAILED");
            payout.setNotes("Payout failed: " + e.getMessage());
            payout = payoutRepository.save(payout);
            throw new BadRequestException("Failed to process payout: " + e.getMessage());
        }
        
        return mapToResponse(payout);
    }
    
    /**
     * Get payout history for a retailer
     */
    @Transactional(readOnly = true)
    public Page<PayoutResponse> getPayoutHistory(Long retailerId, Pageable pageable) {
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        Page<RetailerPayout> payouts = payoutRepository.findByRetailerId(retailerId, pageable);
        
        return payouts.map(this::mapToResponse);
    }
    
    /**
     * Get pending payouts for a retailer
     */
    @Transactional(readOnly = true)
    public List<PayoutResponse> getPendingPayouts(Long retailerId) {
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        List<RetailerPayout> payouts = payoutRepository.findByRetailerIdAndStatus(retailerId, "PENDING");
        
        return payouts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all pending payouts (admin)
     */
    @Transactional(readOnly = true)
    public Page<PayoutResponse> getAllPendingPayouts(Pageable pageable) {
        Page<RetailerPayout> payouts = payoutRepository.findByStatus("PENDING", pageable);
        
        return payouts.map(this::mapToResponse);
    }
    
    /**
     * Scheduled job to process weekly payouts (runs every Monday at 2 AM)
     */
    @Scheduled(cron = "0 0 2 * * MON")
    @Transactional
    public void processScheduledWeeklyPayouts() {
        log.info("Processing scheduled weekly payouts");
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minus(7, ChronoUnit.DAYS);
        
        List<Retailer> activeRetailers = retailerRepository.findAll().stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()))
                .collect(Collectors.toList());
        
        for (Retailer retailer : activeRetailers) {
            try {
                PayoutResponse calculated = calculatePayout(retailer.getId(), startDate, endDate);
                if (calculated.getPayoutAmount().compareTo(BigDecimal.ZERO) > 0) {
                    processPayout(retailer.getId(), startDate, endDate, null, null);
                    log.info("Weekly payout processed for retailer: {}", retailer.getId());
                }
            } catch (Exception e) {
                log.error("Error processing weekly payout for retailer {}: {}", retailer.getId(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * Scheduled job to process monthly payouts (runs on 1st of every month at 2 AM)
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional
    public void processScheduledMonthlyPayouts() {
        log.info("Processing scheduled monthly payouts");
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minus(30, ChronoUnit.DAYS);
        
        List<Retailer> activeRetailers = retailerRepository.findAll().stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()))
                .collect(Collectors.toList());
        
        for (Retailer retailer : activeRetailers) {
            try {
                PayoutResponse calculated = calculatePayout(retailer.getId(), startDate, endDate);
                if (calculated.getPayoutAmount().compareTo(BigDecimal.ZERO) > 0) {
                    processPayout(retailer.getId(), startDate, endDate, null, null);
                    log.info("Monthly payout processed for retailer: {}", retailer.getId());
                }
            } catch (Exception e) {
                log.error("Error processing monthly payout for retailer {}: {}", retailer.getId(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * Map RetailerPayout entity to PayoutResponse DTO
     */
    private PayoutResponse mapToResponse(RetailerPayout payout) {
        return PayoutResponse.builder()
                .id(payout.getId())
                .retailerId(payout.getRetailer().getId())
                .retailerName(payout.getRetailer().getStoreName())
                .payoutAmount(payout.getPayoutAmount())
                .totalSales(payout.getTotalSales())
                .totalCommission(payout.getTotalCommission())
                .periodStart(payout.getPeriodStart())
                .periodEnd(payout.getPeriodEnd())
                .status(payout.getStatus())
                .payoutMethod(payout.getPayoutMethod())
                .transactionId(payout.getTransactionId())
                .bankAccountId(payout.getBankAccountId())
                .notes(payout.getNotes())
                .processedAt(payout.getProcessedAt())
                .processedBy(payout.getProcessedBy())
                .createdAt(payout.getCreatedAt())
                .updatedAt(payout.getUpdatedAt())
                .build();
    }
}

