package com.vendo.integration.repository;

import com.vendo.integration.entity.IntegrationSyncRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationSyncRecordRepository extends JpaRepository<IntegrationSyncRecord, Long> {
    
    List<IntegrationSyncRecord> findBySyncLogId(Long syncLogId);
    
    List<IntegrationSyncRecord> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    Optional<IntegrationSyncRecord> findByEntityTypeAndEntityIdAndExternalId(
            String entityType, 
            Long entityId, 
            String externalId);
    
    List<IntegrationSyncRecord> findByStatus(IntegrationSyncRecord.RecordStatus status);
    
    List<IntegrationSyncRecord> findBySyncLogIdAndStatus(
            Long syncLogId, 
            IntegrationSyncRecord.RecordStatus status);
}

