package com.vendo.integration.service.provider;

import com.vendo.integration.entity.IntegrationConfiguration;
import com.vendo.integration.service.provider.impl.MicrosoftDynamicsProviderService;
import com.vendo.integration.service.provider.impl.OracleProviderService;
import com.vendo.integration.service.provider.impl.SAPProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ERPProviderFactory {
    
    private final SAPProviderService sapService;
    private final OracleProviderService oracleService;
    private final MicrosoftDynamicsProviderService microsoftDynamicsService;
    
    public ERPProviderService getProviderService(IntegrationConfiguration.IntegrationProvider provider) {
        return switch (provider) {
            case SAP -> sapService;
            case ORACLE -> oracleService;
            case MICROSOFT_DYNAMICS -> microsoftDynamicsService;
            default -> throw new IllegalArgumentException("Unsupported ERP provider: " + provider);
        };
    }
}

