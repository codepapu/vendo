package com.vendo.b2b.service;

import com.vendo.b2b.dto.request.CreatePriceListRequest;
import com.vendo.b2b.dto.response.PriceListResponse;
import com.vendo.b2b.entity.BusinessAccount;
import com.vendo.b2b.entity.PriceList;
import com.vendo.b2b.entity.PriceListAssignment;
import com.vendo.b2b.entity.PriceListItem;
import com.vendo.b2b.repository.BusinessAccountRepository;
import com.vendo.b2b.repository.PriceListAssignmentRepository;
import com.vendo.b2b.repository.PriceListRepository;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.entity.Product;
import com.vendo.product.repository.ProductRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceListService {
    
    private final PriceListRepository priceListRepository;
    private final PriceListAssignmentRepository priceListAssignmentRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    /**
     * Create a new price list
     */
    @Transactional
    public PriceListResponse createPriceList(Long userId, CreatePriceListRequest request) {
        log.info("Creating price list for user: {}", userId);
        
        BusinessAccount businessAccount = businessAccountRepository.findById(request.getBusinessAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("BusinessAccount", "id", request.getBusinessAccountId()));
        
        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // If this is set as default, unset other defaults
        if (request.getIsDefault() != null && request.getIsDefault()) {
            Optional<PriceList> existingDefault = priceListRepository.findDefaultPriceListForBusinessAccount(request.getBusinessAccountId());
            existingDefault.ifPresent(pl -> {
                pl.setIsDefault(false);
                priceListRepository.save(pl);
            });
        }
        
        PriceList priceList = PriceList.builder()
                .businessAccount(businessAccount)
                .name(request.getName())
                .description(request.getDescription())
                .version(1)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveUntil(request.getEffectiveUntil())
                .isActive(true)
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .createdBy(createdBy)
                .priceListItems(new ArrayList<>())
                .build();
        
        // Create price list items
        for (CreatePriceListRequest.PriceListItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));
            
            PriceListItem item = PriceListItem.builder()
                    .priceList(priceList)
                    .product(product)
                    .price(itemRequest.getPrice())
                    .minQuantity(itemRequest.getMinQuantity() != null ? itemRequest.getMinQuantity() : 1)
                    .maxQuantity(itemRequest.getMaxQuantity())
                    .discountPercentage(itemRequest.getDiscountPercentage() != null ? 
                            itemRequest.getDiscountPercentage() : BigDecimal.ZERO)
                    .isActive(true)
                    .build();
            priceList.getPriceListItems().add(item);
        }
        
        PriceList saved = priceListRepository.save(priceList);
        log.info("Price list created with ID: {}", saved.getId());
        
        return mapToResponse(saved);
    }
    
    /**
     * Update price list
     */
    @Transactional
    public PriceListResponse updatePriceList(Long id, CreatePriceListRequest request) {
        log.info("Updating price list: {}", id);
        
        PriceList priceList = priceListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PriceList", "id", id));
        
        if (request.getName() != null) priceList.setName(request.getName());
        if (request.getDescription() != null) priceList.setDescription(request.getDescription());
        if (request.getEffectiveFrom() != null) priceList.setEffectiveFrom(request.getEffectiveFrom());
        if (request.getEffectiveUntil() != null) priceList.setEffectiveUntil(request.getEffectiveUntil());
        
        // Update default status
        if (request.getIsDefault() != null && request.getIsDefault()) {
            Optional<PriceList> existingDefault = priceListRepository.findDefaultPriceListForBusinessAccount(
                    priceList.getBusinessAccount().getId());
            existingDefault.ifPresent(pl -> {
                if (!pl.getId().equals(id)) {
                    pl.setIsDefault(false);
                    priceListRepository.save(pl);
                }
            });
            priceList.setIsDefault(true);
        }
        
        // Update items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            priceList.getPriceListItems().clear();
            for (CreatePriceListRequest.PriceListItemRequest itemRequest : request.getItems()) {
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));
                
                PriceListItem item = PriceListItem.builder()
                        .priceList(priceList)
                        .product(product)
                        .price(itemRequest.getPrice())
                        .minQuantity(itemRequest.getMinQuantity() != null ? itemRequest.getMinQuantity() : 1)
                        .maxQuantity(itemRequest.getMaxQuantity())
                        .discountPercentage(itemRequest.getDiscountPercentage() != null ? 
                                itemRequest.getDiscountPercentage() : BigDecimal.ZERO)
                        .isActive(true)
                        .build();
                priceList.getPriceListItems().add(item);
            }
            priceList.setVersion(priceList.getVersion() + 1);
        }
        
        PriceList saved = priceListRepository.save(priceList);
        return mapToResponse(saved);
    }
    
    /**
     * Assign price list to business account
     */
    @Transactional
    public PriceListResponse assignPriceList(Long priceListId, Long businessAccountId, Long assignedByUserId) {
        log.info("Assigning price list {} to business account {}", priceListId, businessAccountId);
        
        PriceList priceList = priceListRepository.findById(priceListId)
                .orElseThrow(() -> new ResourceNotFoundException("PriceList", "id", priceListId));
        
        BusinessAccount businessAccount = businessAccountRepository.findById(businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessAccount", "id", businessAccountId));
        
        User assignedBy = userRepository.findById(assignedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", assignedByUserId));
        
        // Check if assignment already exists
        Optional<PriceListAssignment> existing = priceListAssignmentRepository
                .findByPriceListIdAndBusinessAccountId(priceListId, businessAccountId);
        
        if (existing.isPresent()) {
            PriceListAssignment assignment = existing.get();
            assignment.setIsActive(true);
            priceListAssignmentRepository.save(assignment);
        } else {
            PriceListAssignment assignment = PriceListAssignment.builder()
                    .priceList(priceList)
                    .businessAccount(businessAccount)
                    .isActive(true)
                    .assignedBy(assignedBy)
                    .build();
            priceListAssignmentRepository.save(assignment);
        }
        
        return mapToResponse(priceList);
    }
    
    /**
     * Get price list for business account
     */
    @Transactional(readOnly = true)
    public PriceListResponse getPriceListForAccount(Long businessAccountId) {
        log.debug("Getting price list for business account: {}", businessAccountId);
        
        LocalDate today = LocalDate.now();
        List<PriceList> activePriceLists = priceListRepository.findActivePriceListsForBusinessAccount(businessAccountId, today);
        
        // Prefer default price list
        Optional<PriceList> defaultPriceList = priceListRepository.findDefaultPriceListForBusinessAccount(businessAccountId);
        if (defaultPriceList.isPresent() && activePriceLists.contains(defaultPriceList.get())) {
            return mapToResponse(defaultPriceList.get());
        }
        
        // Return first active price list
        if (!activePriceLists.isEmpty()) {
            return mapToResponse(activePriceLists.get(0));
        }
        
        throw new ResourceNotFoundException("PriceList", "businessAccountId", businessAccountId);
    }
    
    /**
     * Get price lists
     */
    @Transactional(readOnly = true)
    public Page<PriceListResponse> getPriceLists(Long businessAccountId, Pageable pageable) {
        Page<PriceList> priceLists;
        
        if (businessAccountId != null) {
            priceLists = priceListRepository.findByBusinessAccountId(businessAccountId, pageable);
        } else {
            priceLists = priceListRepository.findAll(pageable);
        }
        
        return priceLists.map(this::mapToResponse);
    }
    
    /**
     * Map PriceList entity to PriceListResponse
     */
    private PriceListResponse mapToResponse(PriceList priceList) {
        List<PriceListResponse.PriceListItemResponse> items = priceList.getPriceListItems().stream()
                .map(item -> PriceListResponse.PriceListItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                        .productSku(item.getProduct() != null ? item.getProduct().getSku() : null)
                        .price(item.getPrice())
                        .minQuantity(item.getMinQuantity())
                        .maxQuantity(item.getMaxQuantity())
                        .discountPercentage(item.getDiscountPercentage())
                        .isActive(item.getIsActive())
                        .build())
                .collect(Collectors.toList());
        
        return PriceListResponse.builder()
                .id(priceList.getId())
                .businessAccountId(priceList.getBusinessAccount() != null ? priceList.getBusinessAccount().getId() : null)
                .businessAccountName(priceList.getBusinessAccount() != null ? 
                        priceList.getBusinessAccount().getCompanyName() : null)
                .name(priceList.getName())
                .description(priceList.getDescription())
                .version(priceList.getVersion())
                .effectiveFrom(priceList.getEffectiveFrom())
                .effectiveUntil(priceList.getEffectiveUntil())
                .isActive(priceList.getIsActive())
                .isDefault(priceList.getIsDefault())
                .createdBy(priceList.getCreatedBy() != null ? priceList.getCreatedBy().getId() : null)
                .createdByName(priceList.getCreatedBy() != null ? 
                        (priceList.getCreatedBy().getFirstName() + " " + priceList.getCreatedBy().getLastName()).trim() : null)
                .createdAt(priceList.getCreatedAt())
                .updatedAt(priceList.getUpdatedAt())
                .items(items)
                .build();
    }
}

