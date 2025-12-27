package com.vendo.retailer.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.retailer.dto.request.RetailerBankAccountRequest;
import com.vendo.retailer.dto.response.RetailerBankAccountResponse;
import com.vendo.retailer.service.RetailerBankAccountService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/retailers/{retailerId}/bank-accounts")
@RequiredArgsConstructor
@Tag(name = "Retailer Bank Accounts", description = "Bank account management endpoints for retailers to manage payout accounts")
@SecurityRequirement(name = "bearer-jwt")
public class RetailerBankAccountController {
    
    private final RetailerBankAccountService bankAccountService;
    
    @Operation(
            summary = "Get bank accounts",
            description = "Retrieves all bank accounts for a retailer. Requires ADMIN or RETAILER role.",
            tags = {"Retailer Bank Accounts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Bank accounts retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<List<RetailerBankAccountResponse>>> getBankAccounts(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId) {
        List<RetailerBankAccountResponse> accounts = bankAccountService.getBankAccountsByRetailerId(retailerId);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }
    
    @Operation(
            summary = "Get primary bank account",
            description = "Retrieves the primary bank account for a retailer. Requires ADMIN or RETAILER role.",
            tags = {"Retailer Bank Accounts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Primary bank account retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RetailerBankAccountResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Primary bank account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/primary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<RetailerBankAccountResponse>> getPrimaryBankAccount(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId) {
        RetailerBankAccountResponse account = bankAccountService.getPrimaryBankAccount(retailerId);
        return ResponseEntity.ok(ApiResponse.success(account));
    }
    
    @Operation(
            summary = "Get bank account by ID",
            description = "Retrieves a specific bank account by ID. Requires ADMIN or RETAILER role.",
            tags = {"Retailer Bank Accounts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Bank account retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RetailerBankAccountResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bank account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<RetailerBankAccountResponse>> getBankAccountById(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Bank account ID", required = true, example = "1")
            @PathVariable Long id) {
        RetailerBankAccountResponse account = bankAccountService.getBankAccountById(id);
        return ResponseEntity.ok(ApiResponse.success(account));
    }
    
    @Operation(
            summary = "Create bank account",
            description = "Creates a new bank account for a retailer. Requires ADMIN or RETAILER role.",
            tags = {"Retailer Bank Accounts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Bank account created successfully",
                    content = @Content(schema = @Schema(implementation = RetailerBankAccountResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Retailer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<RetailerBankAccountResponse>> createBankAccount(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Bank account details", required = true)
            @Valid @RequestBody RetailerBankAccountRequest request) {
        RetailerBankAccountResponse account = bankAccountService.createBankAccount(retailerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bank account created successfully", account));
    }
    
    @Operation(
            summary = "Update bank account",
            description = "Updates an existing bank account. Requires ADMIN or RETAILER role.",
            tags = {"Retailer Bank Accounts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Bank account updated successfully",
                    content = @Content(schema = @Schema(implementation = RetailerBankAccountResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bank account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<RetailerBankAccountResponse>> updateBankAccount(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Bank account ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Bank account update request", required = true)
            @Valid @RequestBody RetailerBankAccountRequest request) {
        RetailerBankAccountResponse account = bankAccountService.updateBankAccount(id, request);
        return ResponseEntity.ok(ApiResponse.success("Bank account updated successfully", account));
    }
    
    @Operation(
            summary = "Delete bank account",
            description = "Deletes a bank account. Requires ADMIN or RETAILER role.",
            tags = {"Retailer Bank Accounts"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bank account deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bank account not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot delete primary bank account"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<Void>> deleteBankAccount(
            @Parameter(description = "Retailer ID", required = true, example = "1")
            @PathVariable Long retailerId,
            @Parameter(description = "Bank account ID", required = true, example = "1")
            @PathVariable Long id) {
        bankAccountService.deleteBankAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Bank account deleted successfully"));
    }
}

