package com.vendo.shipping.carrier;

import com.vendo.order.entity.Order;
import com.vendo.order.entity.ShippingMethod;
import com.vendo.shipping.dto.request.CalculateShippingRequest;
import com.vendo.shipping.dto.response.ShippingResponse;
import com.vendo.shipping.dto.response.TrackingResponse;

/**
 * Interface for carrier service implementations (FedEx, UPS, DHL)
 */
public interface CarrierService {
    
    /**
     * Calculate shipping rate
     */
    java.math.BigDecimal calculateRate(java.math.BigDecimal weight, CalculateShippingRequest.AddressInfo destination);
    
    /**
     * Track shipment
     */
    TrackingResponse trackShipment(String trackingNumber);
    
    /**
     * Create shipping label
     */
    ShippingResponse createLabel(Order order, ShippingMethod shippingMethod);
    
    /**
     * Get carrier name
     */
    String getCarrierName();
}

