package com.vendo.retailer.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.entity.Order;
import com.vendo.order.entity.OrderItem;
import com.vendo.order.repository.OrderItemRepository;
import com.vendo.order.repository.OrderRepository;
import com.vendo.retailer.dto.response.CommissionReportResponse;
import com.vendo.retailer.dto.response.CommissionTrackingResponse;
import com.vendo.retailer.entity.CommissionTracking;
import com.vendo.retailer.entity.Retailer;
import com.vendo.retailer.repository.CommissionTrackingRepository;
import com.vendo.retailer.repository.RetailerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionService {
    
    private final CommissionTrackingRepository commissionTrackingRepository;
    private final RetailerRepository retailerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    
    /**
     * Calculate commission for an order item based on retailer's commission rate
     */
    @Transactional
    public BigDecimal calculateCommission(BigDecimal orderItemTotal, BigDecimal commissionRate) {
        if (orderItemTotal == null || orderItemTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        if (commissionRate == null || commissionRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Commission = (orderItemTotal * commissionRate) / 100
        BigDecimal commission = orderItemTotal
                .multiply(commissionRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        return commission;
    }
    
    /**
     * Create commission records for all order items in an order
     */
    @Transactional
    public List<CommissionTracking> createCommissionRecordsForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        List<CommissionTracking> commissionRecords = new ArrayList<>();
        
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            log.warn("Order {} has no order items", orderId);
            return commissionRecords;
        }
        
        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem.getRetailer() == null) {
                continue; // Skip items without retailer (platform-owned products)
            }
            
            Retailer retailer = orderItem.getRetailer();
            BigDecimal commissionRate = retailer.getCommissionRate();
            
            if (commissionRate == null || commissionRate.compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("Retailer {} has no commission rate, skipping commission calculation", retailer.getId());
                continue;
            }
            
            BigDecimal commissionAmount = calculateCommission(orderItem.getTotalPrice(), commissionRate);
            
            if (commissionAmount.compareTo(BigDecimal.ZERO) > 0) {
                CommissionTracking commissionTracking = CommissionTracking.builder()
                        .retailer(retailer)
                        .order(order)
                        .orderItem(orderItem)
                        .commissionAmount(commissionAmount)
                        .commissionRate(commissionRate)
                        .status("PENDING")
                        .notes("Commission calculated for order " + order.getOrderNumber())
                        .build();
                
                commissionTracking = commissionTrackingRepository.save(commissionTracking);
                commissionRecords.add(commissionTracking);
                
                // Update order item commission amount
                orderItem.setCommissionAmount(commissionAmount);
                orderItemRepository.save(orderItem);
                
                log.info("Commission record created: retailerId={}, orderId={}, amount={}", 
                        retailer.getId(), orderId, commissionAmount);
            }
        }
        
        return commissionRecords;
    }
    
    /**
     * Create commission record for a single order item
     */
    @Transactional
    public CommissionTracking createCommissionRecordForOrderItem(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", orderItemId));
        
        if (orderItem.getRetailer() == null) {
            throw new BadRequestException("Order item does not have an associated retailer");
        }
        
        Retailer retailer = orderItem.getRetailer();
        BigDecimal commissionRate = retailer.getCommissionRate();
        
        if (commissionRate == null || commissionRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Retailer does not have a valid commission rate");
        }
        
        BigDecimal commissionAmount = calculateCommission(orderItem.getTotalPrice(), commissionRate);
        
        CommissionTracking commissionTracking = CommissionTracking.builder()
                .retailer(retailer)
                .order(orderItem.getOrder())
                .orderItem(orderItem)
                .commissionAmount(commissionAmount)
                .commissionRate(commissionRate)
                .status("PENDING")
                .notes("Commission calculated for order item " + orderItemId)
                .build();
        
        commissionTracking = commissionTrackingRepository.save(commissionTracking);
        
        // Update order item commission amount
        orderItem.setCommissionAmount(commissionAmount);
        orderItemRepository.save(orderItem);
        
        log.info("Commission record created: retailerId={}, orderItemId={}, amount={}", 
                retailer.getId(), orderItemId, commissionAmount);
        
        return commissionTracking;
    }
    
    /**
     * Get total commissions for a retailer
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalCommissionsByRetailer(Long retailerId) {
        BigDecimal total = commissionTrackingRepository.getTotalCommissionsByRetailerId(retailerId);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Get total commissions for a retailer by status
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalCommissionsByRetailerAndStatus(Long retailerId, String status) {
        BigDecimal total = commissionTrackingRepository.getTotalCommissionsByRetailerIdAndStatus(retailerId, status);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Get commission records for a retailer
     */
    @Transactional(readOnly = true)
    public Page<CommissionTrackingResponse> getCommissionsByRetailer(
            Long retailerId, String status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        
        // Verify retailer exists
        retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        Page<CommissionTracking> commissions = commissionTrackingRepository.findCommissionsByRetailerWithFilters(
                retailerId, status, startDate != null ? startDate.atStartOfDay().toLocalDate() : null,
                endDate != null ? endDate.atStartOfDay().toLocalDate() : null, pageable);
        
        return commissions.map(this::mapToResponse);
    }
    
    /**
     * Get commission records for an order
     */
    @Transactional(readOnly = true)
    public List<CommissionTrackingResponse> getCommissionsByOrder(Long orderId) {
        List<CommissionTracking> commissions = commissionTrackingRepository.findByOrderId(orderId);
        return commissions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update commission status
     */
    @Transactional
    public CommissionTrackingResponse updateCommissionStatus(Long commissionId, String status, 
                                                             LocalDate settlementDate, String paymentReference) {
        CommissionTracking commission = commissionTrackingRepository.findById(commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("CommissionTracking", "id", commissionId));
        
        if (!List.of("PENDING", "PAID", "SETTLED", "CANCELLED").contains(status)) {
            throw new BadRequestException("Invalid commission status: " + status);
        }
        
        commission.setStatus(status);
        
        if (settlementDate != null) {
            commission.setSettlementDate(settlementDate);
        }
        
        if (paymentReference != null) {
            commission.setPaymentReference(paymentReference);
        }
        
        CommissionTracking updated = commissionTrackingRepository.save(commission);
        log.info("Commission status updated: id={}, status={}", commissionId, status);
        
        return mapToResponse(updated);
    }
    
    /**
     * Generate commission report for a retailer
     */
    @Transactional(readOnly = true)
    public CommissionReportResponse generateCommissionReport(Long retailerId, LocalDate startDate, LocalDate endDate) {
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        // Create final copies for use in lambda
        final LocalDate finalStartDate = startDate;
        final LocalDate finalEndDate = endDate;
        
        BigDecimal totalCommissions = commissionTrackingRepository.getTotalCommissionsByRetailerAndDateRange(
                retailerId, startDate, endDate);
        totalCommissions = totalCommissions != null ? totalCommissions : BigDecimal.ZERO;
        
        BigDecimal pendingCommissions = getTotalCommissionsByRetailerAndStatus(retailerId, "PENDING");
        BigDecimal paidCommissions = getTotalCommissionsByRetailerAndStatus(retailerId, "PAID");
        BigDecimal settledCommissions = getTotalCommissionsByRetailerAndStatus(retailerId, "SETTLED");
        
        Long totalTransactions = commissionTrackingRepository.getCommissionCountByRetailerId(retailerId);
        
        // Get daily summaries
        List<CommissionTracking> allCommissions = commissionTrackingRepository.findByRetailerId(retailerId);
        List<CommissionReportResponse.CommissionSummary> summaries = allCommissions.stream()
                .filter(c -> c.getCreatedAt().toLocalDate().isAfter(finalStartDate.minusDays(1)) &&
                            c.getCreatedAt().toLocalDate().isBefore(finalEndDate.plusDays(1)))
                .collect(Collectors.groupingBy(
                        c -> c.getCreatedAt().toLocalDate(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    BigDecimal total = list.stream()
                                            .map(CommissionTracking::getCommissionAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    return CommissionReportResponse.CommissionSummary.builder()
                                            .date(list.get(0).getCreatedAt().toLocalDate())
                                            .amount(total)
                                            .transactionCount((long) list.size())
                                            .status(list.get(0).getStatus())
                                            .build();
                                }
                        )
                ))
                .values()
                .stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .collect(Collectors.toList());
        
        return CommissionReportResponse.builder()
                .retailerId(retailer.getId())
                .retailerName(retailer.getStoreName())
                .startDate(startDate)
                .endDate(endDate)
                .totalCommissions(totalCommissions)
                .pendingCommissions(pendingCommissions)
                .paidCommissions(paidCommissions)
                .settledCommissions(settledCommissions)
                .totalTransactions(totalTransactions)
                .summaries(summaries)
                .build();
    }
    
    private CommissionTrackingResponse mapToResponse(CommissionTracking commission) {
        CommissionTrackingResponse.CommissionTrackingResponseBuilder builder = CommissionTrackingResponse.builder()
                .id(commission.getId())
                .commissionAmount(commission.getCommissionAmount())
                .commissionRate(commission.getCommissionRate())
                .status(commission.getStatus())
                .settlementDate(commission.getSettlementDate())
                .paymentReference(commission.getPaymentReference())
                .notes(commission.getNotes())
                .createdAt(commission.getCreatedAt())
                .updatedAt(commission.getUpdatedAt());
        
        if (commission.getRetailer() != null) {
            builder.retailerId(commission.getRetailer().getId());
            builder.retailer(CommissionTrackingResponse.RetailerInfo.builder()
                    .id(commission.getRetailer().getId())
                    .storeName(commission.getRetailer().getStoreName())
                    .storeSlug(commission.getRetailer().getStoreSlug())
                    .build());
        }
        
        if (commission.getOrder() != null) {
            builder.orderId(commission.getOrder().getId());
        }
        
        if (commission.getOrderItem() != null) {
            builder.orderItemId(commission.getOrderItem().getId());
        }
        
        return builder.build();
    }
}

