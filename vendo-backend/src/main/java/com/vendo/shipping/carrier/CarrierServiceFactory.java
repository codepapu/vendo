package com.vendo.shipping.carrier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CarrierServiceFactory {
    
    private final Map<String, CarrierService> carrierServices;
    
    @Autowired(required = false)
    public CarrierServiceFactory(List<CarrierService> services) {
        this.carrierServices = services.stream()
                .collect(Collectors.toMap(
                        service -> service.getCarrierName().toUpperCase(),
                        Function.identity()
                ));
        log.info("Loaded {} carrier services: {}", carrierServices.size(), carrierServices.keySet());
    }
    
    public CarrierService getCarrierService(String carrierName) {
        if (carrierName == null || carrierName.isEmpty()) {
            return null;
        }
        
        return carrierServices.get(carrierName.toUpperCase());
    }
}

