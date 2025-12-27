package com.vendo.retailer.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.retailer.dto.response.CommissionReportResponse;
import com.vendo.retailer.dto.response.CommissionTrackingResponse;
import com.vendo.retailer.service.CommissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Commissions", description = "Commission tracking and reporting endpoints for retailers")
@SecurityRequirement(name = "bearer-jwt")
public class CommissionController {
    
    private final CommissionService commissionService;
    
    @Operation(
            summary = "Get commissions by retailer",
            description = "Retrieves a paginated list of commissions for a retailer with optional filtering by status and date range. " +
                    "Requires ADMIN or RETAILER role.",
            tags = {"Commissions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Commissions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/retailer/{retailerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<Page<CommissionTrackingResponse>>> getCommissionsByRetailer(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Filter by status (PENDING, PAID, CANCELLED, etc.)", example = "PENDING")
            @RequestParam(required = false) String status,
            @Parameter(description = "Start date for filtering (ISO format)", example = "2024-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for filtering (ISO format)", example = "2024-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "DESC")
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<CommissionTrackingResponse> commissions = commissionService.getCommissionsByRetailer(
                retailerId, status, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(commissions));
    }
    
    @Operation(
            summary = "Get commissions by order",
            description = "Retrieves all commissions associated with a specific order. Only accessible by ADMIN role.",
            tags = {"Commissions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Commissions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CommissionTrackingResponse>>> getCommissionsByOrder(
            @Parameter(description = "Order ID", required = true, example = "1")
            @PathVariable Long orderId) {
        List<CommissionTrackingResponse> commissions = commissionService.getCommissionsByOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(commissions));
    }
    
    @Operation(
            summary = "Get total commissions for retailer",
            description = "Retrieves the total commission amount for a retailer across all statuses. " +
                    "Requires ADMIN or RETAILER role.",
            tags = {"Commissions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Total commissions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BigDecimal.class))
            ),
            @ApiResponse(responseCode = "404", description = "Retailer not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/retailer/{retailerId}/total")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalCommissions(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId) {
        BigDecimal total = commissionService.getTotalCommissionsByRetailer(retailerId);
        return ResponseEntity.ok(ApiResponse.success(total));
    }
    
    @Operation(
            summary = "Get total commissions by status",
            description = "Retrieves the total commission amount for a retailer filtered by status. " +
                    "Requires ADMIN or RETAILER role.",
            tags = {"Commissions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Total commissions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BigDecimal.class))
            ),
            @ApiResponse(responseCode = "404", description = "Retailer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/retailer/{retailerId}/total/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalCommissionsByStatus(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Commission status (PENDING, PAID, CANCELLED, etc.)", required = true, example = "PENDING")
            @PathVariable String status) {
        BigDecimal total = commissionService.getTotalCommissionsByRetailerAndStatus(retailerId, status);
        return ResponseEntity.ok(ApiResponse.success(total));
    }
    
    @Operation(
            summary = "Update commission status",
            description = "Updates the status of a commission (e.g., mark as PAID). Only accessible by ADMIN role.",
            tags = {"Commissions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Commission status updated successfully",
                    content = @Content(schema = @Schema(implementation = CommissionTrackingResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Commission not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status or data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PutMapping("/{commissionId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CommissionTrackingResponse>> updateCommissionStatus(
            @Parameter(description = "Commission ID", required = true, example = "1")
            @PathVariable Long commissionId,
            @Parameter(description = "New status (PENDING, PAID, CANCELLED, etc.)", required = true, example = "PAID")
            @RequestParam String status,
            @Parameter(description = "Settlement date (ISO format)", example = "2024-01-15")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate settlementDate,
            @Parameter(description = "Payment reference number", example = "PAY-12345")
            @RequestParam(required = false) String paymentReference) {
        CommissionTrackingResponse commission = commissionService.updateCommissionStatus(
                commissionId, status, settlementDate, paymentReference);
        return ResponseEntity.ok(ApiResponse.success("Commission status updated successfully", commission));
    }
    
    @Operation(
            summary = "Generate commission report",
            description = "Generates a comprehensive commission report for a retailer with summary statistics. " +
                    "Requires ADMIN or RETAILER role.",
            tags = {"Commissions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Commission report generated successfully",
                    content = @Content(schema = @Schema(implementation = CommissionReportResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Retailer not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/retailer/{retailerId}/report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<CommissionReportResponse>> generateCommissionReport(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Start date for report (ISO format)", example = "2024-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for report (ISO format)", example = "2024-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        CommissionReportResponse report = commissionService.generateCommissionReport(retailerId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
    
    @Operation(
            summary = "Calculate commissions for order",
            description = "Calculates and creates commission records for all retailers associated with an order. " +
                    "Only accessible by ADMIN role.",
            tags = {"Commissions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Commissions calculated successfully",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "400", description = "Order not completed or commissions already calculated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PostMapping("/order/{orderId}/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<com.vendo.retailer.entity.CommissionTracking>>> calculateCommissionsForOrder(
            @Parameter(description = "Order ID", required = true, example = "1")
            @PathVariable Long orderId) {
        List<com.vendo.retailer.entity.CommissionTracking> commissions = 
                commissionService.createCommissionRecordsForOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Commissions calculated successfully", commissions));
    }
}

