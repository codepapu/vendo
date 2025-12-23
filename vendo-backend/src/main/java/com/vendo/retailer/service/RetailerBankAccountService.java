package com.vendo.retailer.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.retailer.dto.request.RetailerBankAccountRequest;
import com.vendo.retailer.dto.response.RetailerBankAccountResponse;
import com.vendo.retailer.entity.Retailer;
import com.vendo.retailer.entity.RetailerBankAccount;
import com.vendo.retailer.repository.RetailerBankAccountRepository;
import com.vendo.retailer.repository.RetailerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetailerBankAccountService {
    
    private final RetailerBankAccountRepository bankAccountRepository;
    private final RetailerRepository retailerRepository;
    
    @Transactional(readOnly = true)
    public List<RetailerBankAccountResponse> getBankAccountsByRetailerId(Long retailerId) {
        List<RetailerBankAccount> accounts = bankAccountRepository.findByRetailerId(retailerId);
        return accounts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public RetailerBankAccountResponse getBankAccountById(Long id) {
        RetailerBankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RetailerBankAccount", "id", id));
        return mapToResponse(account);
    }
    
    @Transactional(readOnly = true)
    public RetailerBankAccountResponse getPrimaryBankAccount(Long retailerId) {
        RetailerBankAccount account = bankAccountRepository.findByRetailerIdAndIsPrimaryTrue(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Primary bank account not found for retailer", "retailerId", retailerId));
        return mapToResponse(account);
    }
    
    @Transactional
    public RetailerBankAccountResponse createBankAccount(Long retailerId, RetailerBankAccountRequest request) {
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        // If this is set as primary, unset other primary accounts
        if (request.getIsPrimary() != null && request.getIsPrimary()) {
            List<RetailerBankAccount> primaryAccounts = bankAccountRepository.findByRetailerIdAndIsPrimary(retailerId, true);
            primaryAccounts.forEach(account -> account.setIsPrimary(false));
            bankAccountRepository.saveAll(primaryAccounts);
        }
        
        RetailerBankAccount account = RetailerBankAccount.builder()
                .retailer(retailer)
                .accountHolderName(request.getAccountHolderName())
                .accountNumber(request.getAccountNumber())
                .bankName(request.getBankName())
                .ifscCode(request.getIfscCode())
                .swiftCode(request.getSwiftCode())
                .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
                .build();
        
        RetailerBankAccount savedAccount = bankAccountRepository.save(account);
        log.info("Bank account created with ID: {} for retailer: {}", savedAccount.getId(), retailerId);
        
        return mapToResponse(savedAccount);
    }
    
    @Transactional
    public RetailerBankAccountResponse updateBankAccount(Long id, RetailerBankAccountRequest request) {
        RetailerBankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RetailerBankAccount", "id", id));
        
        if (request.getAccountHolderName() != null) {
            account.setAccountHolderName(request.getAccountHolderName());
        }
        if (request.getAccountNumber() != null) {
            account.setAccountNumber(request.getAccountNumber());
        }
        if (request.getBankName() != null) {
            account.setBankName(request.getBankName());
        }
        if (request.getIfscCode() != null) {
            account.setIfscCode(request.getIfscCode());
        }
        if (request.getSwiftCode() != null) {
            account.setSwiftCode(request.getSwiftCode());
        }
        
        // Handle primary account change
        if (request.getIsPrimary() != null && request.getIsPrimary() && !account.getIsPrimary()) {
            List<RetailerBankAccount> primaryAccounts = bankAccountRepository.findByRetailerIdAndIsPrimary(
                    account.getRetailer().getId(), true);
            primaryAccounts.forEach(acc -> acc.setIsPrimary(false));
            bankAccountRepository.saveAll(primaryAccounts);
            account.setIsPrimary(true);
        }
        
        RetailerBankAccount updatedAccount = bankAccountRepository.save(account);
        log.info("Bank account updated with ID: {}", updatedAccount.getId());
        
        return mapToResponse(updatedAccount);
    }
    
    @Transactional
    public void deleteBankAccount(Long id) {
        RetailerBankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RetailerBankAccount", "id", id));
        
        bankAccountRepository.delete(account);
        log.info("Bank account deleted with ID: {}", id);
    }
    
    private RetailerBankAccountResponse mapToResponse(RetailerBankAccount account) {
        return RetailerBankAccountResponse.builder()
                .id(account.getId())
                .retailerId(account.getRetailer().getId())
                .accountHolderName(account.getAccountHolderName())
                .accountNumber(account.getAccountNumber())
                .bankName(account.getBankName())
                .ifscCode(account.getIfscCode())
                .swiftCode(account.getSwiftCode())
                .isPrimary(account.getIsPrimary())
                .createdAt(account.getCreatedAt())
                .build();
    }
}

