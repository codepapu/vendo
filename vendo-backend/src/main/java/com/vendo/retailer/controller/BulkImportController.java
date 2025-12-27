package com.vendo.retailer.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.retailer.dto.response.ImportResponse;
import com.vendo.retailer.service.BulkImportService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/retailers/{retailerId}/imports")
@RequiredArgsConstructor
@Tag(name = "Bulk Import", description = "Bulk product import endpoints for retailers to import products from CSV/Excel files")
@SecurityRequirement(name = "bearer-jwt")
public class BulkImportController {
    
    private final BulkImportService bulkImportService;
    
    /**
     * Import products from CSV/Excel file
     */
    @Operation(
            summary = "Import products from file",
            description = "Imports products from a CSV or Excel file. The file should contain product details. " +
                    "Requires RETAILER or ADMIN role.",
            tags = {"Bulk Import"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Import completed successfully",
                    content = @Content(schema = @Schema(implementation = ImportResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid file format or data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ImportResponse>> importProducts(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "CSV or Excel file with product data", required = true)
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        ImportResponse response = bulkImportService.importProducts(retailerId, file, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Import completed", response));
    }
    
    /**
     * Validate import file before processing
     */
    @Operation(
            summary = "Validate import file",
            description = "Validates an import file before processing to check for errors. " +
                    "Requires RETAILER or ADMIN role.",
            tags = {"Bulk Import"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "File validated successfully",
                    content = @Content(schema = @Schema(implementation = ImportResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid file format or data errors found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/validate")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ImportResponse>> validateImportFile(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "CSV or Excel file to validate", required = true)
            @RequestParam("file") MultipartFile file) {
        ImportResponse response = bulkImportService.validateImportFile(retailerId, file);
        return ResponseEntity.ok(ApiResponse.success("File validated", response));
    }
    
    /**
     * Get import template (CSV format)
     */
    @Operation(
            summary = "Get import template",
            description = "Downloads a CSV template file for product imports. " +
                    "Requires RETAILER or ADMIN role.",
            tags = {"Bulk Import"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Template downloaded successfully",
                    content = @Content(mediaType = "text/plain")
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/template")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<String> getImportTemplate() {
        String template = bulkImportService.getImportTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "product_import_template.csv");
        return ResponseEntity.ok()
                .headers(headers)
                .body(template);
    }
    
    /**
     * Get import history for a retailer
     */
    @Operation(
            summary = "Get import history",
            description = "Retrieves a paginated list of import history for a retailer. " +
                    "Requires RETAILER or ADMIN role.",
            tags = {"Bulk Import"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Import history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ImportResponse>>> getImportHistory(
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
        Page<ImportResponse> history = bulkImportService.getImportHistory(retailerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}

