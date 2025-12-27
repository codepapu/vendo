package com.vendo.b2b.service;

import com.vendo.b2b.dto.request.ApproveBusinessAccountRequest;
import com.vendo.b2b.dto.request.BusinessAccountRequest;
import com.vendo.b2b.dto.request.UpdateCreditLimitRequest;
import com.vendo.b2b.dto.response.BusinessAccountResponse;
import com.vendo.b2b.entity.BusinessAccount;
import com.vendo.b2b.repository.BusinessAccountRepository;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class B2BService {
    
    private final BusinessAccountRepository businessAccountRepository;
    private final UserRepository userRepository;
    private final B2BPricingService b2BPricingService;
    
    /**
     * Create a new business account
     */
    @Transactional
    public BusinessAccountResponse createBusinessAccount(Long userId, BusinessAccountRequest request) {
        log.info("Creating business account for user: {}", userId);
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Check if user already has a business account
        if (businessAccountRepository.existsByUserId(userId)) {
            throw new BadRequestException("User already has a business account");
        }
        
        // Check if company name already exists
        if (request.getCompanyName() != null && businessAccountRepository.existsByCompanyName(request.getCompanyName())) {
            throw new BadRequestException("Company name already exists");
        }
        
        // Build business account entity
        BusinessAccount businessAccount = BusinessAccount.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .businessRegistrationNumber(request.getBusinessRegistrationNumber())
                .taxId(request.getTaxId())
                .businessType(request.getBusinessType())
                .creditLimit(request.getCreditLimit() != null ? request.getCreditLimit() : BigDecimal.ZERO)
                .creditTermsDays(request.getCreditTermsDays() != null ? request.getCreditTermsDays() : 30)
                .paymentTerms(request.getPaymentTerms())
                .billingAddressId(request.getBillingAddressId())
                .status("PENDING")
                .build();
        
        BusinessAccount savedAccount = businessAccountRepository.save(businessAccount);
        log.info("Business account created with ID: {}", savedAccount.getId());
        
        return mapToResponse(savedAccount);
    }
    
    /**
     * Approve or reject a business account
     */
    @Transactional
    public BusinessAccountResponse approveBusinessAccount(Long accountId, Long approverUserId, ApproveBusinessAccountRequest request) {
        log.info("Approving business account: {} by user: {}", accountId, approverUserId);
        
        BusinessAccount businessAccount = businessAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessAccount", "id", accountId));
        
        // Validate approver user exists
        User approver = userRepository.findById(approverUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", approverUserId));
        
        // Validate status
        if (!"APPROVED".equals(request.getStatus()) && !"REJECTED".equals(request.getStatus())) {
            throw new BadRequestException("Status must be either APPROVED or REJECTED");
        }
        
        // Validate current status
        if (!"PENDING".equals(businessAccount.getStatus())) {
            throw new BadRequestException("Only PENDING accounts can be approved or rejected");
        }
        
        // Update business account
        businessAccount.setStatus(request.getStatus());
        if ("APPROVED".equals(request.getStatus())) {
            businessAccount.setApprovedAt(LocalDateTime.now());
            businessAccount.setApprovedBy(approver);
        }
        
        BusinessAccount updatedAccount = businessAccountRepository.save(businessAccount);
        log.info("Business account {} status updated to: {}", accountId, request.getStatus());
        
        // Evict pricing cache if business type or status changed
        b2BPricingService.evictPricingCacheForBusinessAccount(accountId);
        
        return mapToResponse(updatedAccount);
    }
    
    /**
     * Get business account by ID
     */
    @Transactional(readOnly = true)
    public BusinessAccountResponse getBusinessAccount(Long accountId) {
        log.debug("Getting business account: {}", accountId);
        
        BusinessAccount businessAccount = businessAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessAccount", "id", accountId));
        
        return mapToResponse(businessAccount);
    }
    
    /**
     * Get business account by user ID
     */
    @Transactional(readOnly = true)
    public BusinessAccountResponse getBusinessAccountByUserId(Long userId) {
        log.debug("Getting business account for user: {}", userId);
        
        BusinessAccount businessAccount = businessAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessAccount", "userId", userId));
        
        return mapToResponse(businessAccount);
    }
    
    /**
     * Update credit limit for a business account
     */
    @Transactional
    public BusinessAccountResponse updateCreditLimit(Long accountId, UpdateCreditLimitRequest request) {
        log.info("Updating credit limit for business account: {}", accountId);
        
        BusinessAccount businessAccount = businessAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessAccount", "id", accountId));
        
        // Validate account is approved
        if (!"APPROVED".equals(businessAccount.getStatus())) {
            throw new BadRequestException("Only APPROVED accounts can have their credit limit updated");
        }
        
        // Update credit limit
        businessAccount.setCreditLimit(request.getCreditLimit());
        
        BusinessAccount updatedAccount = businessAccountRepository.save(businessAccount);
        log.info("Credit limit updated for business account: {} to {}", accountId, request.getCreditLimit());
        
        // Note: Credit limit changes don't affect pricing, so no cache eviction needed
        
        return mapToResponse(updatedAccount);
    }
    
    /**
     * Get B2B pricing for a product
     */
    @Transactional(readOnly = true)
    public com.vendo.b2b.dto.response.PricingResponse getB2BPricing(Long productId, Long businessAccountId, Integer quantity) {
        log.debug("Getting B2B pricing for product: {}, account: {}, quantity: {}", productId, businessAccountId, quantity);
        
        // Delegate to B2BPricingService (which handles business account lookup and caching)
        return b2BPricingService.getB2BPricing(productId, businessAccountId, quantity);
    }
    
    /**
     * Map BusinessAccount entity to BusinessAccountResponse
     */
    private BusinessAccountResponse mapToResponse(BusinessAccount businessAccount) {
        return BusinessAccountResponse.builder()
                .id(businessAccount.getId())
                .userId(businessAccount.getUser() != null ? businessAccount.getUser().getId() : null)
                .companyName(businessAccount.getCompanyName())
                .businessRegistrationNumber(businessAccount.getBusinessRegistrationNumber())
                .taxId(businessAccount.getTaxId())
                .businessType(businessAccount.getBusinessType())
                .creditLimit(businessAccount.getCreditLimit())
                .creditTermsDays(businessAccount.getCreditTermsDays())
                .paymentTerms(businessAccount.getPaymentTerms())
                .billingAddressId(businessAccount.getBillingAddressId())
                .status(businessAccount.getStatus())
                .approvedAt(businessAccount.getApprovedAt())
                .approvedBy(businessAccount.getApprovedBy() != null ? businessAccount.getApprovedBy().getId() : null)
                .createdAt(businessAccount.getCreatedAt())
                .updatedAt(businessAccount.getUpdatedAt())
                .build();
    }
}

