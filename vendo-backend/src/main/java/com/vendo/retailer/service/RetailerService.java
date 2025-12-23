package com.vendo.retailer.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.retailer.dto.request.RetailerRequest;
import com.vendo.retailer.dto.response.RetailerResponse;
import com.vendo.retailer.entity.Retailer;
import com.vendo.retailer.repository.RetailerRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetailerService {
    
    private final RetailerRepository retailerRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public Page<RetailerResponse> getAllRetailers(String status, Pageable pageable) {
        Page<Retailer> retailers;
        if (status != null && !status.isEmpty()) {
            retailers = retailerRepository.findAll(pageable); // Filter by status in service if needed
        } else {
            retailers = retailerRepository.findAll(pageable);
        }
        return retailers.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public RetailerResponse getRetailerById(Long id) {
        Retailer retailer = retailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", id));
        return mapToResponse(retailer);
    }
    
    @Transactional(readOnly = true)
    public RetailerResponse getRetailerBySlug(String slug) {
        Retailer retailer = retailerRepository.findByStoreSlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "storeSlug", slug));
        return mapToResponse(retailer);
    }
    
    @Transactional(readOnly = true)
    public RetailerResponse getRetailerByUserId(Long userId) {
        Retailer retailer = retailerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "userId", userId));
        return mapToResponse(retailer);
    }
    
    @Transactional
    public RetailerResponse createRetailer(Long userId, RetailerRequest request) {
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Check if user already has a retailer account
        if (retailerRepository.findByUserId(userId).isPresent()) {
            throw new BadRequestException("User already has a retailer account");
        }
        
        // Validate store slug uniqueness
        if (retailerRepository.existsByStoreSlug(request.getStoreSlug())) {
            throw new BadRequestException("Store slug already exists: " + request.getStoreSlug());
        }
        
        Retailer retailer = Retailer.builder()
                .user(user)
                .storeName(request.getStoreName())
                .storeSlug(request.getStoreSlug())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .bannerUrl(request.getBannerUrl())
                .commissionRate(request.getCommissionRate() != null ? request.getCommissionRate() : BigDecimal.ZERO)
                .status(request.getStatus() != null ? request.getStatus() : "PENDING")
                .rating(BigDecimal.ZERO)
                .totalSales(BigDecimal.ZERO)
                .totalOrders(0)
                .verified(false)
                .build();
        
        Retailer savedRetailer = retailerRepository.save(retailer);
        log.info("Retailer created with ID: {}", savedRetailer.getId());
        
        return mapToResponse(savedRetailer);
    }
    
    @Transactional
    public RetailerResponse updateRetailer(Long id, RetailerRequest request) {
        Retailer retailer = retailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", id));
        
        // Validate store slug uniqueness if changed
        if (request.getStoreSlug() != null && !request.getStoreSlug().equals(retailer.getStoreSlug())) {
            if (retailerRepository.existsByStoreSlug(request.getStoreSlug())) {
                throw new BadRequestException("Store slug already exists: " + request.getStoreSlug());
            }
            retailer.setStoreSlug(request.getStoreSlug());
        }
        
        if (request.getStoreName() != null) {
            retailer.setStoreName(request.getStoreName());
        }
        if (request.getDescription() != null) {
            retailer.setDescription(request.getDescription());
        }
        if (request.getLogoUrl() != null) {
            retailer.setLogoUrl(request.getLogoUrl());
        }
        if (request.getBannerUrl() != null) {
            retailer.setBannerUrl(request.getBannerUrl());
        }
        if (request.getCommissionRate() != null) {
            retailer.setCommissionRate(request.getCommissionRate());
        }
        if (request.getStatus() != null) {
            retailer.setStatus(request.getStatus());
        }
        
        Retailer updatedRetailer = retailerRepository.save(retailer);
        log.info("Retailer updated with ID: {}", updatedRetailer.getId());
        
        return mapToResponse(updatedRetailer);
    }
    
    @Transactional
    public RetailerResponse updateRetailerStatus(Long id, String status) {
        Retailer retailer = retailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", id));
        
        if (!List.of("PENDING", "ACTIVE", "SUSPENDED", "REJECTED").contains(status)) {
            throw new BadRequestException("Invalid status: " + status);
        }
        
        retailer.setStatus(status);
        Retailer updatedRetailer = retailerRepository.save(retailer);
        log.info("Retailer status updated: ID={}, Status={}", updatedRetailer.getId(), status);
        
        return mapToResponse(updatedRetailer);
    }
    
    @Transactional
    public void deleteRetailer(Long id) {
        Retailer retailer = retailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", id));
        
        retailerRepository.delete(retailer);
        log.info("Retailer deleted with ID: {}", id);
    }
    
    private RetailerResponse mapToResponse(Retailer retailer) {
        RetailerResponse.RetailerResponseBuilder builder = RetailerResponse.builder()
                .id(retailer.getId())
                .storeName(retailer.getStoreName())
                .storeSlug(retailer.getStoreSlug())
                .description(retailer.getDescription())
                .logoUrl(retailer.getLogoUrl())
                .bannerUrl(retailer.getBannerUrl())
                .commissionRate(retailer.getCommissionRate())
                .status(retailer.getStatus())
                .rating(retailer.getRating())
                .totalSales(retailer.getTotalSales())
                .totalOrders(retailer.getTotalOrders())
                .verified(retailer.getVerified())
                .createdAt(retailer.getCreatedAt())
                .updatedAt(retailer.getUpdatedAt());
        
        if (retailer.getUser() != null) {
            builder.userId(retailer.getUser().getId());
            User user = retailer.getUser();
            builder.user(RetailerResponse.UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build());
        }
        
        return builder.build();
    }
}

