package com.vendo.shipping.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.entity.Order;
import com.vendo.order.entity.Shipment;
import com.vendo.order.entity.ShippingMethod;
import com.vendo.order.repository.OrderRepository;
import com.vendo.order.repository.ShipmentRepository;
import com.vendo.order.repository.ShippingMethodRepository;
import com.vendo.shipping.carrier.CarrierService;
import com.vendo.shipping.carrier.CarrierServiceFactory;
import com.vendo.shipping.dto.request.CalculateShippingRequest;
import com.vendo.shipping.dto.request.CreateShipmentLabelRequest;
import com.vendo.shipping.dto.response.ShippingResponse;
import com.vendo.shipping.dto.response.TrackingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {
    
    private final ShippingMethodRepository shippingMethodRepository;
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final CarrierServiceFactory carrierServiceFactory;
    
    /**
     * Calculate shipping costs based on weight, dimensions, and destination
     */
    @Transactional(readOnly = true)
    public ShippingResponse calculateShipping(CalculateShippingRequest request) {
        if (request.getWeight() == null || request.getWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Weight must be greater than zero");
        }
        
        // Get all active shipping methods
        List<ShippingMethod> methods = shippingMethodRepository.findActiveShippingMethodsOrderedByCost();
        
        List<ShippingResponse.ShippingMethod> availableMethods = new ArrayList<>();
        
        for (ShippingMethod method : methods) {
            BigDecimal cost = calculateCost(method, request.getWeight());
            LocalDateTime estimatedDelivery = calculateEstimatedDelivery(method);
            
            availableMethods.add(ShippingResponse.ShippingMethod.builder()
                    .id(method.getId())
                    .name(method.getName())
                    .code(method.getCode())
                    .carrier(method.getCarrier())
                    .cost(cost)
                    .estimatedDays(method.getEstimatedDays())
                    .estimatedDelivery(estimatedDelivery)
                    .build());
        }
        
        // If carrier is specified, try to get real-time rates
        if (request.getDestination() != null && request.getDestination().getCountry() != null) {
            // Try to get rates from carrier APIs if configured
            String country = request.getDestination().getCountry();
            if (isCarrierAvailable(country)) {
                // Use carrier service for real-time rates
                try {
                    CarrierService carrierService = carrierServiceFactory.getCarrierService("FEDEX");
                    if (carrierService != null) {
                        // Get real-time rates from carrier
                        BigDecimal carrierCost = carrierService.calculateRate(
                                request.getWeight(),
                                request.getDestination()
                        );
                        if (carrierCost != null) {
                            // Update cost if carrier provides better rate
                            log.info("Carrier rate calculated: {}", carrierCost);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to get carrier rates: {}", e.getMessage());
                }
            }
        }
        
        return ShippingResponse.builder()
                .cost(availableMethods.isEmpty() ? BigDecimal.ZERO : availableMethods.get(0).getCost())
                .estimatedDelivery(availableMethods.isEmpty() ? null : availableMethods.get(0).getEstimatedDelivery())
                .availableMethods(availableMethods)
                .build();
    }
    
    /**
     * Get available shipping methods
     */
    @Transactional(readOnly = true)
    public List<ShippingResponse.ShippingMethod> getShippingMethods() {
        List<ShippingMethod> methods = shippingMethodRepository.findActiveShippingMethodsOrderedByCost();
        
        return methods.stream()
                .map(method -> ShippingResponse.ShippingMethod.builder()
                        .id(method.getId())
                        .name(method.getName())
                        .code(method.getCode())
                        .carrier(method.getCarrier())
                        .cost(method.getBaseCost())
                        .estimatedDays(method.getEstimatedDays())
                        .estimatedDelivery(calculateEstimatedDelivery(method))
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Track shipment by tracking number
     */
    @Transactional(readOnly = true)
    public TrackingResponse trackShipment(String trackingNumber, String carrier) {
        Shipment shipment;
        
        if (carrier != null) {
            shipment = shipmentRepository.findByTrackingNumberAndCarrier(trackingNumber, carrier)
                    .orElseThrow(() -> new ResourceNotFoundException("Shipment", 
                            "trackingNumber", trackingNumber + ", carrier=" + carrier));
        } else {
            shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Shipment", "trackingNumber", trackingNumber));
        }
        
        // Try to get real-time tracking from carrier
        List<TrackingResponse.TrackingEvent> events = new ArrayList<>();
        
        if (shipment.getCarrier() != null) {
            try {
                CarrierService carrierService = carrierServiceFactory.getCarrierService(shipment.getCarrier());
                if (carrierService != null) {
                    TrackingResponse carrierTracking = carrierService.trackShipment(trackingNumber);
                    if (carrierTracking != null && carrierTracking.getEvents() != null) {
                        events = carrierTracking.getEvents();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to get carrier tracking: {}", e.getMessage());
            }
        }
        
        // If no carrier events, create basic events from shipment history
        if (events.isEmpty()) {
            events.add(TrackingResponse.TrackingEvent.builder()
                    .timestamp(shipment.getCreatedAt())
                    .description("Shipment created")
                    .status("PENDING")
                    .build());
            
            if (shipment.getShippedAt() != null) {
                events.add(TrackingResponse.TrackingEvent.builder()
                        .timestamp(shipment.getShippedAt())
                        .description("Shipment shipped")
                        .status("IN_TRANSIT")
                        .build());
            }
            
            if (shipment.getDeliveredAt() != null) {
                events.add(TrackingResponse.TrackingEvent.builder()
                        .timestamp(shipment.getDeliveredAt())
                        .description("Shipment delivered")
                        .status("DELIVERED")
                        .build());
            }
        }
        
        return TrackingResponse.builder()
                .trackingNumber(shipment.getTrackingNumber())
                .carrier(shipment.getCarrier())
                .status(shipment.getStatus())
                .shippedAt(shipment.getShippedAt())
                .deliveredAt(shipment.getDeliveredAt())
                .events(events)
                .build();
    }
    
    /**
     * Create shipment label
     */
    @Transactional
    public ShippingResponse createShipmentLabel(CreateShipmentLabelRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));
        
        ShippingMethod shippingMethod = shippingMethodRepository.findById(request.getShippingMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("ShippingMethod", "id", request.getShippingMethodId()));
        
        String carrier = request.getCarrier() != null ? request.getCarrier() : shippingMethod.getCarrier();
        
        // Try to create label via carrier API
        String trackingNumber = null;
        String labelUrl = null;
        
        if (carrier != null && !carrier.isEmpty()) {
            try {
                CarrierService carrierService = carrierServiceFactory.getCarrierService(carrier);
                if (carrierService != null) {
                    ShippingResponse carrierResponse = carrierService.createLabel(order, shippingMethod);
                    if (carrierResponse != null) {
                        trackingNumber = carrierResponse.getTrackingNumber();
                        labelUrl = carrierResponse.getLabelUrl();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to create carrier label: {}", e.getMessage());
            }
        }
        
        // Generate tracking number if carrier didn't provide one
        if (trackingNumber == null) {
            trackingNumber = generateTrackingNumber(carrier);
        }
        
        // Create or update shipment
        Shipment shipment = shipmentRepository.findByOrderId(order.getId())
                .stream()
                .findFirst()
                .orElse(Shipment.builder()
                        .order(order)
                        .shippingMethod(shippingMethod)
                        .carrier(carrier)
                        .status("PENDING")
                        .build());
        
        shipment.setTrackingNumber(trackingNumber);
        shipment.setCarrier(carrier);
        
        Shipment savedShipment = shipmentRepository.save(shipment);
        
        return ShippingResponse.builder()
                .cost(shippingMethod.getBaseCost())
                .carrier(carrier)
                .trackingNumber(trackingNumber)
                .labelUrl(labelUrl)
                .estimatedDelivery(calculateEstimatedDelivery(shippingMethod))
                .build();
    }
    
    private BigDecimal calculateCost(ShippingMethod method, BigDecimal weight) {
        BigDecimal cost = method.getBaseCost();
        
        if (method.getCostPerKg() != null && weight != null && weight.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal weightCost = method.getCostPerKg().multiply(weight);
            cost = cost.add(weightCost);
        }
        
        return cost.setScale(2, java.math.RoundingMode.HALF_UP);
    }
    
    private LocalDateTime calculateEstimatedDelivery(ShippingMethod method) {
        if (method.getEstimatedDays() != null) {
            return LocalDateTime.now().plusDays(method.getEstimatedDays());
        }
        return LocalDateTime.now().plusDays(5); // Default 5 days
    }
    
    private boolean isCarrierAvailable(String country) {
        // Check if carrier services are available for the country
        // This would typically check configuration or carrier service availability
        return true; // Simplified
    }
    
    private String generateTrackingNumber(String carrier) {
        String prefix = carrier != null ? carrier.substring(0, Math.min(3, carrier.length())).toUpperCase() : "TRK";
        return prefix + "-" + System.currentTimeMillis() + "-" + 
               String.valueOf((int)(Math.random() * 10000));
    }
}

