package com.vendo.product.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.product.dto.request.ProductRetailerRequest;
import com.vendo.product.dto.response.ProductRetailerResponse;
import com.vendo.product.service.ProductRetailerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-retailers")
@RequiredArgsConstructor
@Tag(name = "Product Retailers", description = "Product-retailer relationship management endpoints for managing which products are available through which retailers")
@SecurityRequirement(name = "bearer-jwt")
public class ProductRetailerController {
    
    private final ProductRetailerService productRetailerService;
    
    @Operation(
            summary = "Get products by retailer",
            description = "Retrieves a paginated list of products available through a retailer. " +
                    "Requires ADMIN or RETAILER role.",
            tags = {"Product Retailers"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/retailer/{retailerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<Page<ProductRetailerResponse>>> getProductsByRetailer(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Filter by active status", example = "true")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductRetailerResponse> products = productRetailerService.getProductsByRetailer(retailerId, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @Operation(
            summary = "Get retailers by product",
            description = "Retrieves all retailers that offer a specific product. Public endpoint, no authentication required.",
            tags = {"Product Retailers"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Retailers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ProductRetailerResponse>>> getRetailersByProduct(
            @Parameter(description = "Product ID", required = true, example = "1")
            @PathVariable Long productId) {
        List<ProductRetailerResponse> retailers = productRetailerService.getRetailersByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(retailers));
    }
    
    @Operation(
            summary = "Get low stock products for retailer",
            description = "Retrieves all products with low stock levels for a retailer. " +
                    "Requires ADMIN or RETAILER role.",
            tags = {"Product Retailers"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Low stock products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/retailer/{retailerId}/low-stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<List<ProductRetailerResponse>>> getLowStockProducts(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId) {
        List<ProductRetailerResponse> products = productRetailerService.getLowStockProducts(retailerId);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @Operation(
            summary = "Get product-retailer by ID",
            description = "Retrieves a product-retailer relationship by its ID. Public endpoint, no authentication required.",
            tags = {"Product Retailers"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Product-retailer relationship retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductRetailerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product-retailer relationship not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> getProductRetailerById(
            @Parameter(description = "Product-retailer ID", required = true, example = "1")
            @PathVariable Long id) {
        ProductRetailerResponse productRetailer = productRetailerService.getProductRetailerById(id);
        return ResponseEntity.ok(ApiResponse.success(productRetailer));
    }
    
    @Operation(
            summary = "Get product-retailer by product and retailer",
            description = "Retrieves a product-retailer relationship by product ID and retailer ID. " +
                    "Public endpoint, no authentication required.",
            tags = {"Product Retailers"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Product-retailer relationship retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductRetailerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product-retailer relationship not found")
    })
    @GetMapping("/product/{productId}/retailer/{retailerId}")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> getProductRetailerByProductAndRetailer(
            @Parameter(description = "Product ID", required = true, example = "1")
            @PathVariable Long productId,
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId) {
        ProductRetailerResponse productRetailer = productRetailerService.getProductRetailerByProductAndRetailer(productId, retailerId);
        return ResponseEntity.ok(ApiResponse.success(productRetailer));
    }
    
    @Operation(
            summary = "Create product-retailer relationship",
            description = "Creates a new product-retailer relationship, making a product available through a retailer. " +
                    "Requires ADMIN or RETAILER role.",
            tags = {"Product Retailers"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Product-retailer relationship created successfully",
                    content = @Content(schema = @Schema(implementation = ProductRetailerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product or retailer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> createProductRetailer(
            @Parameter(description = "Product-retailer relationship details", required = true)
            @Valid @RequestBody ProductRetailerRequest request) {
        ProductRetailerResponse productRetailer = productRetailerService.createProductRetailer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product retailer created successfully", productRetailer));
    }
    
    @Operation(
            summary = "Update product-retailer relationship",
            description = "Updates an existing product-retailer relationship (e.g., price, stock). " +
                    "Requires ADMIN or RETAILER role.",
            tags = {"Product Retailers"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Product-retailer relationship updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductRetailerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product-retailer relationship not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> updateProductRetailer(
            @Parameter(description = "Product-retailer ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Product-retailer update request", required = true)
            @Valid @RequestBody ProductRetailerRequest request) {
        ProductRetailerResponse productRetailer = productRetailerService.updateProductRetailer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product retailer updated successfully", productRetailer));
    }
    
    @Operation(
            summary = "Delete product-retailer relationship",
            description = "Deletes a product-retailer relationship, making the product unavailable through that retailer. " +
                    "Requires ADMIN or RETAILER role.",
            tags = {"Product Retailers"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product-retailer relationship deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product-retailer relationship not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<Void>> deleteProductRetailer(
            @Parameter(description = "Product-retailer ID", required = true, example = "1")
            @PathVariable Long id) {
        productRetailerService.deleteProductRetailer(id);
        return ResponseEntity.ok(ApiResponse.success("Product retailer deleted successfully"));
    }
}

