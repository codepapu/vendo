package com.vendo.integration.repository;

import com.vendo.integration.entity.IntegrationSyncLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntegrationSyncLogRepository extends JpaRepository<IntegrationSyncLog, Long> {
    
    List<IntegrationSyncLog> findByIntegrationConfigId(Long configId);
    
    Page<IntegrationSyncLog> findByIntegrationConfigId(Long configId, Pageable pageable);
    
    List<IntegrationSyncLog> findByIntegrationConfigIdAndSyncType(
            Long configId, 
            IntegrationSyncLog.SyncType syncType);
    
    List<IntegrationSyncLog> findByStatus(IntegrationSyncLog.SyncStatus status);
}

