package com.vendo.retailer.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.product.dto.response.ProductRetailerResponse;
import com.vendo.retailer.dto.request.StorefrontUpdateRequest;
import com.vendo.retailer.dto.response.StorefrontResponse;
import com.vendo.retailer.service.RetailerStorefrontService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
// Removed ambiguous import
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/storefronts")
@RequiredArgsConstructor
@Tag(name = "Storefronts", description = "Public storefront endpoints for retailers to showcase their products")
@SecurityRequirement(name = "bearer-jwt")
public class RetailerStorefrontController {
    
    private final RetailerStorefrontService storefrontService;
    
    /**
     * Get public storefront by slug
     */
    @Operation(
            summary = "Get storefront by slug",
            description = "Retrieves a public storefront by its slug. Public endpoint, no authentication required.",
            tags = {"Storefronts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Storefront retrieved successfully",
                    content = @Content(schema = @Schema(implementation = StorefrontResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Storefront not found")
    })
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<StorefrontResponse>> getStorefront(
            @Parameter(description = "Storefront slug", required = true, example = "acme-store")
            @PathVariable String slug) {
        StorefrontResponse storefront = storefrontService.getStorefront(slug);
        return ResponseEntity.ok(ApiResponse.success(storefront));
    }
    
    /**
     * Get public storefront by retailer ID
     */
    @Operation(
            summary = "Get storefront by retailer ID",
            description = "Retrieves a public storefront by retailer ID. Public endpoint, no authentication required.",
            tags = {"Storefronts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Storefront retrieved successfully",
                    content = @Content(schema = @Schema(implementation = StorefrontResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Storefront not found")
    })
    @GetMapping("/retailer/{retailerId}")
    public ResponseEntity<ApiResponse<StorefrontResponse>> getStorefrontById(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId) {
        StorefrontResponse storefront = storefrontService.getStorefrontById(retailerId);
        return ResponseEntity.ok(ApiResponse.success(storefront));
    }
    
    /**
     * Update storefront (retailer owners only)
     */
    @Operation(
            summary = "Update storefront",
            description = "Updates storefront information. RETAILER role users can only update their own storefront. " +
                    "Requires RETAILER or ADMIN role.",
            tags = {"Storefronts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Storefront updated successfully",
                    content = @Content(schema = @Schema(implementation = StorefrontResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Storefront not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions or ownership")
    })
    @PutMapping("/{retailerId}")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StorefrontResponse>> updateStorefront(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Storefront update request", required = true)
            @Valid @RequestBody StorefrontUpdateRequest request,
            Authentication authentication) {
        // Validate ownership for RETAILER role
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RETAILER"))) {
            Long userId = Long.parseLong(authentication.getName());
            // Validate ownership through service
            storefrontService.validateRetailerOwnership(retailerId, userId);
        }
        StorefrontResponse storefront = storefrontService.updateStorefront(retailerId, request);
        return ResponseEntity.ok(ApiResponse.success("Storefront updated successfully", storefront));
    }
    
    /**
     * Get products for a storefront
     */
    @Operation(
            summary = "Get storefront products",
            description = "Retrieves a paginated list of products for a storefront. Public endpoint, no authentication required.",
            tags = {"Storefronts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Storefront not found")
    })
    @GetMapping("/{slug}/products")
    public ResponseEntity<ApiResponse<Page<ProductRetailerResponse>>> getStorefrontProducts(
            @Parameter(description = "Storefront slug", required = true, example = "acme-store")
            @PathVariable String slug,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "DESC")
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<ProductRetailerResponse> products = storefrontService.getStorefrontProducts(slug, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    /**
     * Search retailers
     */
    @Operation(
            summary = "Search retailers",
            description = "Searches for retailers by name or description. Public endpoint, no authentication required.",
            tags = {"Storefronts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Retailers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<StorefrontResponse>>> searchRetailers(
            @Parameter(description = "Search query", required = true, example = "electronics")
            @RequestParam String q,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "storeName")
            @RequestParam(defaultValue = "storeName") String sortBy,
            @Parameter(description = "Sort direction", example = "ASC")
            @RequestParam(defaultValue = "ASC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<StorefrontResponse> retailers = storefrontService.searchRetailers(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(retailers));
    }
}

