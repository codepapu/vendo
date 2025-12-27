package com.vendo.promotion.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.promotion.dto.request.FlashSaleRequest;
import com.vendo.promotion.dto.response.FlashSaleResponse;
import com.vendo.promotion.entity.FlashSale;
import com.vendo.promotion.repository.FlashSaleRepository;
import com.vendo.product.entity.Product;
import com.vendo.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlashSaleService {
    
    private final FlashSaleRepository flashSaleRepository;
    private final ProductRepository productRepository;
    
    @Transactional
    public FlashSaleResponse createFlashSale(FlashSaleRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));
        
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }
        
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Start time cannot be in the past");
        }
        
        BigDecimal discountPercentage = request.getOriginalPrice()
                .subtract(request.getSalePrice())
                .divide(request.getOriginalPrice(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        FlashSale flashSale = FlashSale.builder()
                .product(product)
                .salePrice(request.getSalePrice())
                .originalPrice(request.getOriginalPrice())
                .discountPercentage(discountPercentage)
                .quantityLimit(request.getQuantityLimit())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isActive(true)
                .build();
        
        FlashSale saved = flashSaleRepository.save(flashSale);
        log.info("Flash sale created: ID={}, Product={}", saved.getId(), product.getName());
        
        return mapToResponse(saved);
    }
    
    @Transactional
    public FlashSaleResponse updateFlashSale(Long id, FlashSaleRequest request) {
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FlashSale", "id", id));
        
        if (request.getSalePrice() != null) {
            flashSale.setSalePrice(request.getSalePrice());
            BigDecimal discountPercentage = flashSale.getOriginalPrice()
                    .subtract(request.getSalePrice())
                    .divide(flashSale.getOriginalPrice(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            flashSale.setDiscountPercentage(discountPercentage);
        }
        
        if (request.getQuantityLimit() != null) {
            flashSale.setQuantityLimit(request.getQuantityLimit());
        }
        
        if (request.getStartTime() != null) {
            flashSale.setStartTime(request.getStartTime());
        }
        
        if (request.getEndTime() != null) {
            flashSale.setEndTime(request.getEndTime());
        }
        
        FlashSale saved = flashSaleRepository.save(flashSale);
        return mapToResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public List<FlashSaleResponse> getActiveFlashSales() {
        LocalDateTime now = LocalDateTime.now();
        List<FlashSale> flashSales = flashSaleRepository.findActiveFlashSales(now);
        return flashSales.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public FlashSaleResponse applyFlashSaleDiscount(Long flashSaleId, Integer quantity) {
        FlashSale flashSale = flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> new ResourceNotFoundException("FlashSale", "id", flashSaleId));
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(flashSale.getStartTime()) || now.isAfter(flashSale.getEndTime())) {
            throw new BadRequestException("Flash sale is not active");
        }
        
        if (!flashSale.getIsActive()) {
            throw new BadRequestException("Flash sale is not active");
        }
        
        int availableQuantity = flashSale.getQuantityLimit() - flashSale.getQuantitySold();
        if (quantity > availableQuantity) {
            throw new BadRequestException("Insufficient quantity available. Available: " + availableQuantity);
        }
        
        // Update quantity sold
        flashSale.setQuantitySold(flashSale.getQuantitySold() + quantity);
        flashSaleRepository.save(flashSale);
        
        return mapToResponse(flashSale);
    }
    
    private FlashSaleResponse mapToResponse(FlashSale flashSale) {
        LocalDateTime now = LocalDateTime.now();
        boolean isActive = flashSale.getIsActive() && 
                          now.isAfter(flashSale.getStartTime()) && 
                          now.isBefore(flashSale.getEndTime());
        
        int availableQuantity = flashSale.getQuantityLimit() - flashSale.getQuantitySold();
        
        return FlashSaleResponse.builder()
                .id(flashSale.getId())
                .productId(flashSale.getProduct().getId())
                .productName(flashSale.getProduct().getName())
                .salePrice(flashSale.getSalePrice())
                .originalPrice(flashSale.getOriginalPrice())
                .discountPercentage(flashSale.getDiscountPercentage())
                .quantityLimit(flashSale.getQuantityLimit())
                .quantitySold(flashSale.getQuantitySold())
                .availableQuantity(availableQuantity)
                .startTime(flashSale.getStartTime())
                .endTime(flashSale.getEndTime())
                .isActive(isActive)
                .build();
    }
}

