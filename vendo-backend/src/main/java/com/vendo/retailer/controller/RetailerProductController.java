package com.vendo.retailer.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.product.dto.request.ProductRetailerRequest;
import com.vendo.product.dto.response.ProductRetailerResponse;
import com.vendo.retailer.service.RetailerProductService;
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

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/retailers/{retailerId}/products")
@RequiredArgsConstructor
@Tag(name = "Retailer Products", description = "Product management endpoints for retailers to add, update, and manage their products")
@SecurityRequirement(name = "bearer-jwt")
public class RetailerProductController {
    
    private final RetailerProductService retailerProductService;
    
    @Operation(
            summary = "Add product to retailer",
            description = "Adds a product to a retailer's catalog with custom pricing and stock. " +
                    "Requires RETAILER or ADMIN role.",
            tags = {"Retailer Products"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Product added successfully",
                    content = @Content(schema = @Schema(implementation = ProductRetailerResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Retailer or product not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> addProduct(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Product retailer details", required = true)
            @Valid @RequestBody ProductRetailerRequest request) {
        ProductRetailerResponse productRetailer = retailerProductService.addProduct(retailerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product added successfully", productRetailer));
    }
    
    @Operation(
            summary = "Update product price",
            description = "Updates the price of a product for a retailer. Requires RETAILER or ADMIN role.",
            tags = {"Retailer Products"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product price updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductRetailerResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Product retailer relationship not found"),
            @ApiResponse(responseCode = "400", description = "Invalid price"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PutMapping("/{productRetailerId}/price")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> updateProductPrice(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Product retailer ID", required = true, example = "1")
            @PathVariable Long productRetailerId,
            @Parameter(description = "New price", required = true, example = "99.99")
            @RequestParam BigDecimal newPrice) {
        ProductRetailerResponse productRetailer = retailerProductService.updateProductPrice(
                retailerId, productRetailerId, newPrice);
        return ResponseEntity.ok(ApiResponse.success("Product price updated successfully", productRetailer));
    }
    
    @Operation(
            summary = "Update product stock",
            description = "Updates the stock quantity of a product for a retailer. Requires RETAILER or ADMIN role.",
            tags = {"Retailer Products"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductRetailerResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Product retailer relationship not found"),
            @ApiResponse(responseCode = "400", description = "Invalid stock quantity"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PutMapping("/{productRetailerId}/stock")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> updateStock(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Product retailer ID", required = true, example = "1")
            @PathVariable Long productRetailerId,
            @Parameter(description = "New stock quantity", required = true, example = "100")
            @RequestParam Integer newStockQuantity) {
        ProductRetailerResponse productRetailer = retailerProductService.updateStock(
                retailerId, productRetailerId, newStockQuantity);
        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", productRetailer));
    }
    
    @Operation(
            summary = "Get retailer products",
            description = "Retrieves a paginated list of products for a retailer with optional filtering. " +
                    "Requires ADMIN or RETAILER role.",
            tags = {"Retailer Products"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<Page<ProductRetailerResponse>>> getRetailerProducts(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Filter by active status", example = "true")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Search term for product name or SKU", example = "laptop")
            @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "DESC")
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<ProductRetailerResponse> products = retailerProductService.getRetailerProducts(
                retailerId, isActive, searchTerm, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @Operation(
            summary = "Deactivate product",
            description = "Deactivates a product for a retailer, making it unavailable for sale. " +
                    "Requires RETAILER or ADMIN role.",
            tags = {"Retailer Products"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product deactivated successfully",
                    content = @Content(schema = @Schema(implementation = ProductRetailerResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Product retailer relationship not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PutMapping("/{productRetailerId}/deactivate")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> deactivateProduct(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Product retailer ID", required = true, example = "1")
            @PathVariable Long productRetailerId) {
        ProductRetailerResponse productRetailer = retailerProductService.deactivateProduct(
                retailerId, productRetailerId);
        return ResponseEntity.ok(ApiResponse.success("Product deactivated successfully", productRetailer));
    }
    
    @Operation(
            summary = "Activate product",
            description = "Activates a product for a retailer, making it available for sale. " +
                    "Requires RETAILER or ADMIN role.",
            tags = {"Retailer Products"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product activated successfully",
                    content = @Content(schema = @Schema(implementation = ProductRetailerResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Product retailer relationship not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PutMapping("/{productRetailerId}/activate")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> activateProduct(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Product retailer ID", required = true, example = "1")
            @PathVariable Long productRetailerId) {
        ProductRetailerResponse productRetailer = retailerProductService.activateProduct(
                retailerId, productRetailerId);
        return ResponseEntity.ok(ApiResponse.success("Product activated successfully", productRetailer));
    }
}

