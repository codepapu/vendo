package com.vendo.integration.service.provider;

import com.vendo.integration.entity.IntegrationConfiguration;
import com.vendo.integration.service.provider.impl.HubSpotProviderService;
import com.vendo.integration.service.provider.impl.SalesforceProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CRMProviderFactory {
    
    private final SalesforceProviderService salesforceService;
    private final HubSpotProviderService hubSpotService;
    
    public CRMProviderService getProviderService(IntegrationConfiguration.IntegrationProvider provider) {
        return switch (provider) {
            case SALESFORCE -> salesforceService;
            case HUBSPOT -> hubSpotService;
            default -> throw new IllegalArgumentException("Unsupported CRM provider: " + provider);
        };
    }
}

