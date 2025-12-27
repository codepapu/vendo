package com.vendo.retailer.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.retailer.dto.request.RetailerRequest;
import com.vendo.retailer.dto.response.RetailerResponse;
import com.vendo.retailer.service.RetailerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/retailers")
@RequiredArgsConstructor
@Tag(name = "Retailers", description = "Retailer management endpoints for registration, approval, dashboard, and order management")
@SecurityRequirement(name = "bearer-jwt")
public class RetailerController {
    
    private final RetailerService retailerService;
    
    @Operation(
            summary = "Get all retailers",
            description = "Retrieves a paginated list of all retailers. Public endpoint, no authentication required.",
            tags = {"Retailers"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Retailers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RetailerResponse>>> getAllRetailers(
            @Parameter(description = "Filter by status (PENDING, APPROVED, SUSPENDED, etc.)", example = "APPROVED")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "DESC")
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<RetailerResponse> retailers = retailerService.getAllRetailers(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(retailers));
    }
    
    @Operation(
            summary = "Get retailer by ID",
            description = "Retrieves a retailer by its ID. Public endpoint, no authentication required.",
            tags = {"Retailers"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Retailer retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RetailerResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Retailer not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RetailerResponse>> getRetailerById(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long id) {
        RetailerResponse retailer = retailerService.getRetailerById(id);
        return ResponseEntity.ok(ApiResponse.success(retailer));
    }
    
    @Operation(
            summary = "Get retailer by slug",
            description = "Retrieves a retailer by its slug (URL-friendly identifier). Public endpoint, no authentication required.",
            tags = {"Retailers"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Retailer retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RetailerResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Retailer not found")
    })
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<RetailerResponse>> getRetailerBySlug(
            @Parameter(description = "Retailer slug", required = true, example = "acme-retailer")
            @PathVariable String slug) {
        RetailerResponse retailer = retailerService.getRetailerBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(retailer));
    }
    
    @Operation(
            summary = "Get retailer by user ID",
            description = "Retrieves a retailer associated with a user ID. Requires ADMIN or RETAILER role.",
            tags = {"Retailers"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Retailer retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RetailerResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Retailer not found for user"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<RetailerResponse>> getRetailerByUserId(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        RetailerResponse retailer = retailerService.getRetailerByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(retailer));
    }
    
    @Operation(
            summary = "Register as a retailer",
            description = "Registers the authenticated user as a retailer. Requires USER or CUSTOMER role. " +
                    "The retailer account will be in PENDING status until approved by an admin.",
            tags = {"Retailers"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Retailer registered successfully",
                    content = @Content(schema = @Schema(implementation = RetailerResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data or user already registered"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/register")
    @PreAuthorize("hasRole('USER') or hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<RetailerResponse>> registerRetailer(
            @Parameter(description = "Retailer registration details", required = true)
            @Valid @RequestBody RetailerRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        RetailerResponse retailer = retailerService.registerRetailer(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Retailer registered successfully", retailer));
    }
    
    @Operation(
            summary = "Approve retailer",
            description = "Approves a pending retailer account. Only accessible by ADMIN role.",
            tags = {"Retailers"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Retailer approved successfully",
                    content = @Content(schema = @Schema(implementation = RetailerResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Retailer not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetailerResponse>> approveRetailer(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long id,
            Authentication authentication) {
        Long approvedBy = Long.parseLong(authentication.getName());
        RetailerResponse retailer = retailerService.approveRetailer(id, approvedBy);
        return ResponseEntity.ok(ApiResponse.success("Retailer approved successfully", retailer));
    }
    
    @Operation(
            summary = "Get retailer dashboard for current user",
            description = "Retrieves dashboard data for the authenticated retailer user. " +
                    "Includes sales statistics, orders, commissions, and other key metrics. " +
                    "Requires RETAILER or ADMIN role.",
            tags = {"Retailers - Dashboard"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dashboard data retrieved successfully",
                    content = @Content(schema = @Schema(implementation = com.vendo.retailer.dto.response.RetailerDashboardResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Retailer not found for user"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.vendo.retailer.dto.response.RetailerDashboardResponse>> getRetailerDashboard(
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        com.vendo.retailer.dto.response.RetailerDashboardResponse dashboard = retailerService.getRetailerDashboardByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
    
    @Operation(
            summary = "Get retailer dashboard by ID",
            description = "Retrieves dashboard data for a specific retailer. " +
                    "RETAILER role users can only access their own dashboard. " +
                    "Requires ADMIN or RETAILER role.",
            tags = {"Retailers - Dashboard"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dashboard data retrieved successfully",
                    content = @Content(schema = @Schema(implementation = com.vendo.retailer.dto.response.RetailerDashboardResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Retailer not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions or ownership")
    })
    @GetMapping("/{id}/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<com.vendo.retailer.dto.response.RetailerDashboardResponse>> getRetailerDashboardById(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long id,
            Authentication authentication) {
        // Validate ownership for RETAILER role
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RETAILER"))) {
            Long userId = Long.parseLong(authentication.getName());
            retailerService.validateRetailerOwnership(id, userId);
        }
        com.vendo.retailer.dto.response.RetailerDashboardResponse dashboard = retailerService.getRetailerDashboard(id);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
    
    @Operation(
            summary = "Calculate commission",
            description = "Calculates commission for a retailer based on order total and commission rate. " +
                    "If commission rate is not provided, uses the retailer's default rate. " +
                    "Requires ADMIN or RETAILER role.",
            tags = {"Retailers - Commission"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Commission calculated successfully",
                    content = @Content(schema = @Schema(implementation = java.math.BigDecimal.class))
            ),
            @ApiResponse(responseCode = "404", description = "Retailer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid order total or commission rate"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/{id}/commission/calculate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> calculateCommission(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Order total amount", required = true, example = "1000.00")
            @RequestParam java.math.BigDecimal orderTotal,
            @Parameter(description = "Commission rate (optional, uses retailer default if not provided)", example = "0.15")
            @RequestParam(required = false) java.math.BigDecimal commissionRate) {
        RetailerResponse retailerResponse = retailerService.getRetailerById(id);
        java.math.BigDecimal rate = commissionRate != null ? commissionRate : retailerResponse.getCommissionRate();
        java.math.BigDecimal commission = retailerService.calculateCommission(orderTotal, rate);
        return ResponseEntity.ok(ApiResponse.success(commission));
    }
    
    @Operation(
            summary = "Update retailer",
            description = "Updates retailer information. RETAILER role users can only update their own retailer account. " +
                    "Requires ADMIN or RETAILER role.",
            tags = {"Retailers"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Retailer updated successfully",
                    content = @Content(schema = @Schema(implementation = RetailerResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Retailer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions or ownership")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<RetailerResponse>> updateRetailer(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Retailer update request", required = true)
            @Valid @RequestBody RetailerRequest request,
            Authentication authentication) {
        // Validate ownership for RETAILER role
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RETAILER"))) {
            Long userId = Long.parseLong(authentication.getName());
            retailerService.validateRetailerOwnership(id, userId);
        }
        RetailerResponse retailer = retailerService.updateRetailer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Retailer updated successfully", retailer));
    }
    
    @Operation(
            summary = "Update retailer status",
            description = "Updates the status of a retailer (APPROVED, SUSPENDED, etc.). Only accessible by ADMIN role.",
            tags = {"Retailers"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Retailer status updated successfully",
                    content = @Content(schema = @Schema(implementation = RetailerResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Retailer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetailerResponse>> updateRetailerStatus(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "New status (APPROVED, SUSPENDED, etc.)", required = true, example = "APPROVED")
            @RequestParam String status) {
        RetailerResponse retailer = retailerService.updateRetailerStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Retailer status updated successfully", retailer));
    }
    
    @Operation(
            summary = "Delete retailer",
            description = "Deletes a retailer account. Only accessible by ADMIN role.",
            tags = {"Retailers"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retailer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Retailer not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRetailer(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long id) {
        retailerService.deleteRetailer(id);
        return ResponseEntity.ok(ApiResponse.success("Retailer deleted successfully"));
    }
    
    @Operation(
            summary = "Get retailer orders",
            description = "Retrieves a paginated list of orders for a retailer. " +
                    "RETAILER role users can only access their own orders. " +
                    "Requires ADMIN or RETAILER role.",
            tags = {"Retailers - Orders"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "404", description = "Retailer not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions or ownership")
    })
    @GetMapping("/{id}/orders")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<Page<com.vendo.order.dto.response.OrderResponse>>> getRetailerOrders(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Filter by order status (PENDING, CONFIRMED, SHIPPED, etc.)", example = "CONFIRMED")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "DESC")
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir,
            Authentication authentication) {
        // Validate ownership for RETAILER role
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RETAILER"))) {
            Long userId = Long.parseLong(authentication.getName());
            retailerService.validateRetailerOwnership(id, userId);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<com.vendo.order.dto.response.OrderResponse> orders = retailerService.getRetailerOrders(id, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
}

