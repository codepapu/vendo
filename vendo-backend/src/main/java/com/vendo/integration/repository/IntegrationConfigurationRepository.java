package com.vendo.integration.repository;

import com.vendo.integration.entity.IntegrationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationConfigurationRepository extends JpaRepository<IntegrationConfiguration, Long> {
    
    List<IntegrationConfiguration> findByIntegrationType(IntegrationConfiguration.IntegrationType type);
    
    Page<IntegrationConfiguration> findByIntegrationType(IntegrationConfiguration.IntegrationType type, Pageable pageable);
    
    List<IntegrationConfiguration> findByProvider(IntegrationConfiguration.IntegrationProvider provider);
    
    List<IntegrationConfiguration> findByIntegrationTypeAndProvider(
            IntegrationConfiguration.IntegrationType type,
            IntegrationConfiguration.IntegrationProvider provider);
    
    List<IntegrationConfiguration> findByIsActiveTrueAndIsEnabledTrue();
    
    @Query("SELECT ic FROM IntegrationConfiguration ic WHERE ic.isActive = true AND ic.isEnabled = true AND ic.autoSyncEnabled = true AND (ic.nextSyncAt IS NULL OR ic.nextSyncAt <= :now)")
    List<IntegrationConfiguration> findDueForSync(LocalDateTime now);
    
    Optional<IntegrationConfiguration> findByIntegrationTypeAndProviderAndName(
            IntegrationConfiguration.IntegrationType type,
            IntegrationConfiguration.IntegrationProvider provider,
            String name);
}

