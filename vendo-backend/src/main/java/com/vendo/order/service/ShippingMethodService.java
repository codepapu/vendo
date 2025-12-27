package com.vendo.order.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.dto.request.SelectShippingMethodRequest;
import com.vendo.order.dto.request.ShippingEstimateRequest;
import com.vendo.order.dto.response.ShippingEstimateResponse;
import com.vendo.order.dto.response.ShippingMethodResponse;
import com.vendo.order.entity.ShippingMethod;
import com.vendo.order.repository.ShippingMethodRepository;
import com.vendo.product.entity.Product;
import com.vendo.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingMethodService {
    
    private final ShippingMethodRepository shippingMethodRepository;
    private final ProductRepository productRepository;
    
    /**
     * Get all available shipping methods
     * Returns active shipping methods ordered by base cost
     */
    @Transactional(readOnly = true)
    public List<ShippingMethodResponse> getAvailableShippingMethods() {
        List<ShippingMethod> methods = shippingMethodRepository.findActiveShippingMethodsOrderedByCost();
        return methods.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get available shipping methods based on address and order weight
     * Returns methods with calculated costs
     */
    @Transactional(readOnly = true)
    public List<ShippingMethodResponse> getAvailableShippingMethods(
            ShippingEstimateRequest.AddressInfo address,
            BigDecimal totalWeight) {
        
        if (address == null) {
            throw new BadRequestException("Address is required");
        }
        
        if (totalWeight == null || totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Total weight must be greater than zero");
        }
        
        // Get all active shipping methods
        List<ShippingMethod> methods = shippingMethodRepository.findActiveShippingMethodsOrderedByCost();
        
        // Calculate costs for each method based on weight
        return methods.stream()
                .map(method -> calculateShippingCostForMethod(method, totalWeight))
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate shipping cost for a specific method and weight
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateShippingCost(Long shippingMethodId, BigDecimal totalWeight) {
        if (totalWeight == null || totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Total weight must be greater than zero");
        }
        
        ShippingMethod method = shippingMethodRepository.findById(shippingMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("ShippingMethod", "id", shippingMethodId));
        
        if (!method.getIsActive()) {
            throw new BadRequestException("Shipping method is not active");
        }
        
        return calculateCost(method, totalWeight);
    }
    
    /**
     * Calculate shipping cost for a specific method code and weight
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateShippingCost(String shippingMethodCode, BigDecimal totalWeight) {
        if (totalWeight == null || totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Total weight must be greater than zero");
        }
        
        ShippingMethod method = shippingMethodRepository.findActiveByCode(shippingMethodCode)
                .orElseThrow(() -> new ResourceNotFoundException("ShippingMethod", "code", shippingMethodCode));
        
        return calculateCost(method, totalWeight);
    }
    
    /**
     * Select shipping method (validation only - actual selection happens during order creation)
     * Returns the selected shipping method with calculated cost
     */
    @Transactional(readOnly = true)
    public ShippingMethodResponse selectShippingMethod(SelectShippingMethodRequest request, BigDecimal totalWeight) {
        if (request.getShippingMethodId() == null) {
            throw new BadRequestException("Shipping method ID is required");
        }
        
        ShippingMethod method = shippingMethodRepository.findById(request.getShippingMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("ShippingMethod", "id", request.getShippingMethodId()));
        
        if (!method.getIsActive()) {
            throw new BadRequestException("Shipping method is not active");
        }
        
        BigDecimal calculatedCost = totalWeight != null && totalWeight.compareTo(BigDecimal.ZERO) > 0
                ? calculateCost(method, totalWeight)
                : method.getBaseCost();
        
        ShippingMethodResponse response = mapToResponse(method);
        response.setCalculatedCost(calculatedCost);
        response.setTotalWeight(totalWeight);
        
        return response;
    }
    
    /**
     * Get shipping estimates for address and weight
     * Supports single address or multiple addresses per order
     */
    @Transactional(readOnly = true)
    public ShippingEstimateResponse getShippingEstimates(ShippingEstimateRequest request) {
        if (request.getAddress() == null && 
            (request.getAddressWeights() == null || request.getAddressWeights().isEmpty())) {
            throw new BadRequestException("At least one address is required");
        }
        
        // Single address scenario
        if (request.getAddress() != null && request.getTotalWeight() != null) {
            List<ShippingMethodResponse> methods = getAvailableShippingMethods(
                    request.getAddress(), 
                    request.getTotalWeight());
            
            return ShippingEstimateResponse.builder()
                    .availableMethods(methods)
                    .totalWeight(request.getTotalWeight())
                    .shippingAddress(ShippingEstimateResponse.AddressInfo.builder()
                            .city(request.getAddress().getCity())
                            .state(request.getAddress().getState())
                            .postalCode(request.getAddress().getPostalCode())
                            .country(request.getAddress().getCountry())
                            .build())
                    .build();
        }
        
        // Multiple addresses scenario
        if (request.getAddressWeights() != null && !request.getAddressWeights().isEmpty()) {
            List<ShippingEstimateResponse.AddressShippingEstimate> addressEstimates = new ArrayList<>();
            
            for (ShippingEstimateRequest.AddressWeightInfo addressWeight : request.getAddressWeights()) {
                List<ShippingMethodResponse> methods = getAvailableShippingMethods(
                        addressWeight.getAddress(),
                        addressWeight.getWeight());
                
                addressEstimates.add(ShippingEstimateResponse.AddressShippingEstimate.builder()
                        .address(ShippingEstimateResponse.AddressInfo.builder()
                                .city(addressWeight.getAddress().getCity())
                                .state(addressWeight.getAddress().getState())
                                .postalCode(addressWeight.getAddress().getPostalCode())
                                .country(addressWeight.getAddress().getCountry())
                                .build())
                        .weight(addressWeight.getWeight())
                        .availableMethods(methods)
                        .build());
            }
            
            // Calculate total weight
            BigDecimal totalWeight = request.getAddressWeights().stream()
                    .map(ShippingEstimateRequest.AddressWeightInfo::getWeight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            return ShippingEstimateResponse.builder()
                    .addressEstimates(addressEstimates)
                    .totalWeight(totalWeight)
                    .build();
        }
        
        throw new BadRequestException("Invalid request: provide either single address or multiple addresses");
    }
    
    /**
     * Calculate total weight from cart items or order items
     * Helper method to calculate weight from product IDs and quantities
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateOrderWeight(List<OrderItemWeightInfo> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalWeight = BigDecimal.ZERO;
        
        for (OrderItemWeightInfo item : items) {
            if (item.getProductId() != null) {
                Product product = productRepository.findById(item.getProductId())
                        .orElse(null);
                
                if (product != null && product.getWeight() != null) {
                    BigDecimal itemWeight = product.getWeight()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    totalWeight = totalWeight.add(itemWeight);
                }
            } else if (item.getWeight() != null) {
                // Direct weight provided
                totalWeight = totalWeight.add(
                        item.getWeight().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        
        return totalWeight;
    }
    
    /**
     * Helper method to calculate shipping cost for a method
     */
    private BigDecimal calculateCost(ShippingMethod method, BigDecimal weight) {
        BigDecimal cost = method.getBaseCost();
        
        // Add weight-based cost if costPerKg is set
        if (method.getCostPerKg() != null && weight != null && weight.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal weightCost = method.getCostPerKg()
                    .multiply(weight)
                    .setScale(2, RoundingMode.HALF_UP);
            cost = cost.add(weightCost);
        }
        
        return cost.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate shipping cost for a method and map to response
     */
    private ShippingMethodResponse calculateShippingCostForMethod(ShippingMethod method, BigDecimal weight) {
        ShippingMethodResponse response = mapToResponse(method);
        BigDecimal calculatedCost = calculateCost(method, weight);
        response.setCalculatedCost(calculatedCost);
        response.setTotalWeight(weight);
        return response;
    }
    
    /**
     * Map ShippingMethod entity to ShippingMethodResponse DTO
     */
    private ShippingMethodResponse mapToResponse(ShippingMethod method) {
        return ShippingMethodResponse.builder()
                .id(method.getId())
                .name(method.getName())
                .code(method.getCode())
                .description(method.getDescription())
                .carrier(method.getCarrier())
                .baseCost(method.getBaseCost())
                .costPerKg(method.getCostPerKg())
                .estimatedDays(method.getEstimatedDays())
                .isActive(method.getIsActive())
                .createdAt(method.getCreatedAt())
                .build();
    }
    
    /**
     * Helper class for order item weight calculation
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @lombok.Builder
    public static class OrderItemWeightInfo {
        private Long productId;
        private Integer quantity;
        private BigDecimal weight; // Direct weight if productId is not available
    }
}

