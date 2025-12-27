package com.vendo.retailer.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.retailer.dto.response.PayoutResponse;
import com.vendo.retailer.service.PayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
// Removed ambiguous import
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/retailers/{retailerId}/payouts")
@RequiredArgsConstructor
@Tag(name = "Payouts", description = "Payout management endpoints for retailers to calculate and process commission payouts")
@SecurityRequirement(name = "bearer-jwt")
public class PayoutController {
    
    private final PayoutService payoutService;
    
    /**
     * Calculate payout for a period
     */
    @Operation(
            summary = "Calculate payout for a period",
            description = "Calculates the payout amount for a retailer for a specific date range. " +
                    "Requires RETAILER or ADMIN role.",
            tags = {"Payouts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payout calculated successfully",
                    content = @Content(schema = @Schema(implementation = PayoutResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Retailer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid date range"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/calculate")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PayoutResponse>> calculatePayout(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Start date and time (ISO format)", required = true, example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date and time (ISO format)", required = true, example = "2024-01-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        PayoutResponse payout = payoutService.calculatePayout(retailerId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(payout));
    }
    
    /**
     * Process payout to retailer bank account
     */
    @Operation(
            summary = "Process payout",
            description = "Processes a payout to the retailer's bank account for a specific date range. " +
                    "Requires RETAILER or ADMIN role.",
            tags = {"Payouts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Payout processed successfully",
                    content = @Content(schema = @Schema(implementation = PayoutResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Retailer or bank account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid date range or insufficient funds"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PayoutResponse>> processPayout(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Start date and time (ISO format)", required = true, example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date and time (ISO format)", required = true, example = "2024-01-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Bank account ID (optional, uses primary if not provided)", example = "1")
            @RequestParam(required = false) Long bankAccountId,
            Authentication authentication) {
        Long processedBy = Long.parseLong(authentication.getName());
        PayoutResponse payout = payoutService.processPayout(retailerId, startDate, endDate, bankAccountId, processedBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payout processed successfully", payout));
    }
    
    /**
     * Get payout history for a retailer
     */
    @Operation(
            summary = "Get payout history",
            description = "Retrieves a paginated list of payout history for a retailer. " +
                    "Requires RETAILER or ADMIN role.",
            tags = {"Payouts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payout history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PayoutResponse>>> getPayoutHistory(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "DESC")
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<PayoutResponse> payouts = payoutService.getPayoutHistory(retailerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(payouts));
    }
    
    /**
     * Get pending payouts for a retailer
     */
    @Operation(
            summary = "Get pending payouts",
            description = "Retrieves all pending payouts for a retailer. Requires RETAILER or ADMIN role.",
            tags = {"Payouts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pending payouts retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/pending")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PayoutResponse>>> getPendingPayouts(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId) {
        List<PayoutResponse> payouts = payoutService.getPendingPayouts(retailerId);
        return ResponseEntity.ok(ApiResponse.success(payouts));
    }
    
    /**
     * Get all pending payouts (admin only)
     */
    @Operation(
            summary = "Get all pending payouts (admin)",
            description = "Retrieves a paginated list of all pending payouts across all retailers. Only accessible by ADMIN role.",
            tags = {"Payouts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pending payouts retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PayoutResponse>>> getAllPendingPayouts(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "DESC")
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<PayoutResponse> payouts = payoutService.getAllPendingPayouts(pageable);
        return ResponseEntity.ok(ApiResponse.success(payouts));
    }
}

