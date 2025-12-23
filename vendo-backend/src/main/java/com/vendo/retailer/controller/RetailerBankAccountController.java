package com.vendo.retailer.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.retailer.dto.request.RetailerBankAccountRequest;
import com.vendo.retailer.dto.response.RetailerBankAccountResponse;
import com.vendo.retailer.service.RetailerBankAccountService;
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
public class RetailerBankAccountController {
    
    private final RetailerBankAccountService bankAccountService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<List<RetailerBankAccountResponse>>> getBankAccounts(
            @PathVariable Long retailerId) {
        List<RetailerBankAccountResponse> accounts = bankAccountService.getBankAccountsByRetailerId(retailerId);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }
    
    @GetMapping("/primary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<RetailerBankAccountResponse>> getPrimaryBankAccount(
            @PathVariable Long retailerId) {
        RetailerBankAccountResponse account = bankAccountService.getPrimaryBankAccount(retailerId);
        return ResponseEntity.ok(ApiResponse.success(account));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<RetailerBankAccountResponse>> getBankAccountById(
            @PathVariable Long retailerId,
            @PathVariable Long id) {
        RetailerBankAccountResponse account = bankAccountService.getBankAccountById(id);
        return ResponseEntity.ok(ApiResponse.success(account));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<RetailerBankAccountResponse>> createBankAccount(
            @PathVariable Long retailerId,
            @Valid @RequestBody RetailerBankAccountRequest request) {
        RetailerBankAccountResponse account = bankAccountService.createBankAccount(retailerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bank account created successfully", account));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<RetailerBankAccountResponse>> updateBankAccount(
            @PathVariable Long retailerId,
            @PathVariable Long id,
            @Valid @RequestBody RetailerBankAccountRequest request) {
        RetailerBankAccountResponse account = bankAccountService.updateBankAccount(id, request);
        return ResponseEntity.ok(ApiResponse.success("Bank account updated successfully", account));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<Void>> deleteBankAccount(
            @PathVariable Long retailerId,
            @PathVariable Long id) {
        bankAccountService.deleteBankAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Bank account deleted successfully"));
    }
}

