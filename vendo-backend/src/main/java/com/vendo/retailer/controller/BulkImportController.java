package com.vendo.retailer.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.retailer.dto.response.ImportResponse;
import com.vendo.retailer.service.BulkImportService;
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
public class BulkImportController {
    
    private final BulkImportService bulkImportService;
    
    /**
     * Import products from CSV/Excel file
     */
    @PostMapping
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ImportResponse>> importProducts(
            @PathVariable Long retailerId,
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
    @PostMapping("/validate")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ImportResponse>> validateImportFile(
            @PathVariable Long retailerId,
            @RequestParam("file") MultipartFile file) {
        ImportResponse response = bulkImportService.validateImportFile(retailerId, file);
        return ResponseEntity.ok(ApiResponse.success("File validated", response));
    }
    
    /**
     * Get import template (CSV format)
     */
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
    @GetMapping
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ImportResponse>>> getImportHistory(
            @PathVariable Long retailerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<ImportResponse> history = bulkImportService.getImportHistory(retailerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}

