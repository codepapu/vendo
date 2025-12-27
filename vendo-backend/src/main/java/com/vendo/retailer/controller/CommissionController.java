package com.vendo.retailer.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.retailer.dto.response.CommissionReportResponse;
import com.vendo.retailer.dto.response.CommissionTrackingResponse;
import com.vendo.retailer.service.CommissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/commissions")
@RequiredArgsConstructor
public class CommissionController {
    
    private final CommissionService commissionService;
    
    @GetMapping("/retailer/{retailerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<Page<CommissionTrackingResponse>>> getCommissionsByRetailer(
            @PathVariable Long retailerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<CommissionTrackingResponse> commissions = commissionService.getCommissionsByRetailer(
                retailerId, status, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(commissions));
    }
    
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CommissionTrackingResponse>>> getCommissionsByOrder(
            @PathVariable Long orderId) {
        List<CommissionTrackingResponse> commissions = commissionService.getCommissionsByOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(commissions));
    }
    
    @GetMapping("/retailer/{retailerId}/total")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalCommissions(
            @PathVariable Long retailerId) {
        BigDecimal total = commissionService.getTotalCommissionsByRetailer(retailerId);
        return ResponseEntity.ok(ApiResponse.success(total));
    }
    
    @GetMapping("/retailer/{retailerId}/total/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalCommissionsByStatus(
            @PathVariable Long retailerId,
            @PathVariable String status) {
        BigDecimal total = commissionService.getTotalCommissionsByRetailerAndStatus(retailerId, status);
        return ResponseEntity.ok(ApiResponse.success(total));
    }
    
    @PutMapping("/{commissionId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CommissionTrackingResponse>> updateCommissionStatus(
            @PathVariable Long commissionId,
            @RequestParam String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate settlementDate,
            @RequestParam(required = false) String paymentReference) {
        CommissionTrackingResponse commission = commissionService.updateCommissionStatus(
                commissionId, status, settlementDate, paymentReference);
        return ResponseEntity.ok(ApiResponse.success("Commission status updated successfully", commission));
    }
    
    @GetMapping("/retailer/{retailerId}/report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<CommissionReportResponse>> generateCommissionReport(
            @PathVariable Long retailerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        CommissionReportResponse report = commissionService.generateCommissionReport(retailerId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
    
    @PostMapping("/order/{orderId}/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<com.vendo.retailer.entity.CommissionTracking>>> calculateCommissionsForOrder(
            @PathVariable Long orderId) {
        List<com.vendo.retailer.entity.CommissionTracking> commissions = 
                commissionService.createCommissionRecordsForOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Commissions calculated successfully", commissions));
    }
}

