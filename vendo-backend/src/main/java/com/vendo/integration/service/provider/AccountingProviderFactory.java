package com.vendo.integration.service.provider;

import com.vendo.integration.entity.IntegrationConfiguration;
import com.vendo.integration.service.provider.impl.QuickBooksProviderService;
import com.vendo.integration.service.provider.impl.SageProviderService;
import com.vendo.integration.service.provider.impl.XeroProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountingProviderFactory {
    
    private final QuickBooksProviderService quickBooksService;
    private final XeroProviderService xeroService;
    private final SageProviderService sageService;
    
    public AccountingProviderService getProviderService(IntegrationConfiguration.IntegrationProvider provider) {
        return switch (provider) {
            case QUICKBOOKS -> quickBooksService;
            case XERO -> xeroService;
            case SAGE -> sageService;
            default -> throw new IllegalArgumentException("Unsupported accounting provider: " + provider);
        };
    }
}

