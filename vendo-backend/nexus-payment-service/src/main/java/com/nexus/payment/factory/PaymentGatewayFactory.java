package com.nexus.payment.factory;

import com.nexus.payment.enums.PaymentGateway;
import com.nexus.payment.gateway.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Payment Gateway Factory
 * Returns the appropriate payment gateway service based on the gateway type
 */
@Component
@RequiredArgsConstructor
public class PaymentGatewayFactory {
    
    private final List<PaymentGatewayService> gatewayServices;
    
    private Map<PaymentGateway, PaymentGatewayService> gatewayMap;
    
    /**
     * Get payment gateway service by gateway type
     */
    public PaymentGatewayService getGatewayService(PaymentGateway gateway) {
        if (gatewayMap == null) {
            gatewayMap = gatewayServices.stream()
                    .collect(Collectors.toMap(
                            service -> PaymentGateway.valueOf(service.getGatewayName().toUpperCase()),
                            Function.identity()
                    ));
        }
        
        PaymentGatewayService service = gatewayMap.get(gateway);
        if (service == null) {
            throw new IllegalArgumentException("Payment gateway not supported: " + gateway);
        }
        
        return service;
    }
}
