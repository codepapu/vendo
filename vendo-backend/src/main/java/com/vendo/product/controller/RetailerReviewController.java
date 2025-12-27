package com.vendo.product.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.product.dto.request.RetailerReviewRequest;
import com.vendo.product.dto.response.RetailerReviewResponse;
import com.vendo.product.service.RetailerReviewService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/retailer-reviews")
@RequiredArgsConstructor
@Tag(name = "Retailer Reviews", description = "Retailer review management endpoints for customers to review retailers and view retailer ratings")
@SecurityRequirement(name = "bearer-jwt")
public class RetailerReviewController {
    
    private final RetailerReviewService retailerReviewService;
    
    @Operation(
            summary = "Get reviews by retailer",
            description = "Retrieves a paginated list of reviews for a retailer. Public endpoint, no authentication required.",
            tags = {"Retailer Reviews"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Reviews retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Retailer not found")
    })
    @GetMapping("/retailer/{retailerId}")
    public ResponseEntity<ApiResponse<Page<RetailerReviewResponse>>> getReviewsByRetailer(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Filter by approved status", example = "true")
            @RequestParam(required = false) Boolean isApproved,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RetailerReviewResponse> reviews = retailerReviewService.getReviewsByRetailer(retailerId, isApproved, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }
    
    @Operation(
            summary = "Get approved reviews by retailer",
            description = "Retrieves all approved reviews for a retailer. Public endpoint, no authentication required.",
            tags = {"Retailer Reviews"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Approved reviews retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Retailer not found")
    })
    @GetMapping("/retailer/{retailerId}/approved")
    public ResponseEntity<ApiResponse<List<RetailerReviewResponse>>> getApprovedReviews(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId) {
        List<RetailerReviewResponse> reviews = retailerReviewService.getApprovedReviewsByRetailer(retailerId);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }
    
    @Operation(
            summary = "Get average rating for retailer",
            description = "Retrieves the average rating for a retailer based on approved reviews. " +
                    "Public endpoint, no authentication required.",
            tags = {"Retailer Reviews"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Average rating retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Double.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Retailer not found")
    })
    @GetMapping("/retailer/{retailerId}/rating")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId) {
        Double rating = retailerReviewService.getAverageRating(retailerId);
        return ResponseEntity.ok(ApiResponse.success(rating));
    }
    
    @Operation(
            summary = "Get review count for retailer",
            description = "Retrieves the total count of approved reviews for a retailer. " +
                    "Public endpoint, no authentication required.",
            tags = {"Retailer Reviews"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Review count retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Long.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Retailer not found")
    })
    @GetMapping("/retailer/{retailerId}/count")
    public ResponseEntity<ApiResponse<Long>> getReviewCount(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId) {
        Long count = retailerReviewService.getReviewCount(retailerId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    @Operation(
            summary = "Get review by ID",
            description = "Retrieves a retailer review by its ID. Public endpoint, no authentication required.",
            tags = {"Retailer Reviews"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Review retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RetailerReviewResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RetailerReviewResponse>> getReviewById(
            @Parameter(description = "Review ID", required = true, example = "1")
            @PathVariable Long id) {
        RetailerReviewResponse review = retailerReviewService.getReviewById(id);
        return ResponseEntity.ok(ApiResponse.success(review));
    }
    
    @Operation(
            summary = "Create retailer review",
            description = "Creates a new review for a retailer. The review will be in PENDING status until approved by an admin. " +
                    "Requires USER or CUSTOMER role.",
            tags = {"Retailer Reviews"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Review created successfully",
                    content = @Content(schema = @Schema(implementation = RetailerReviewResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data or user already reviewed this retailer"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Retailer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<RetailerReviewResponse>> createReview(
            @Parameter(description = "Review details", required = true)
            @Valid @RequestBody RetailerReviewRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        RetailerReviewResponse review = retailerReviewService.createReview(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review created successfully", review));
    }
    
    @Operation(
            summary = "Approve retailer review",
            description = "Approves a pending retailer review, making it visible to the public. Only accessible by ADMIN role.",
            tags = {"Retailer Reviews"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Review approved successfully",
                    content = @Content(schema = @Schema(implementation = RetailerReviewResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetailerReviewResponse>> approveReview(
            @Parameter(description = "Review ID", required = true, example = "1")
            @PathVariable Long id) {
        RetailerReviewResponse review = retailerReviewService.approveReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review approved successfully", review));
    }
    
    @Operation(
            summary = "Delete retailer review",
            description = "Deletes a retailer review. Only accessible by ADMIN role.",
            tags = {"Retailer Reviews"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @Parameter(description = "Review ID", required = true, example = "1")
            @PathVariable Long id) {
        retailerReviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully"));
    }
}

