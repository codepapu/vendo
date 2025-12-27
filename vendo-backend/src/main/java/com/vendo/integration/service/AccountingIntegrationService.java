package com.vendo.integration.service;

import com.vendo.b2b.dto.response.InvoiceResponse;
import com.vendo.b2b.service.InvoiceService;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.integration.dto.response.AccountingIntegrationResponse;
import com.vendo.integration.dto.response.SyncStatusResponse;
import com.vendo.integration.entity.IntegrationConfiguration;
import com.vendo.integration.entity.IntegrationSyncLog;
import com.vendo.integration.repository.IntegrationConfigurationRepository;
import com.vendo.integration.repository.IntegrationSyncLogRepository;
import com.vendo.integration.service.provider.AccountingProviderFactory;
import com.vendo.integration.service.provider.AccountingProviderService;
import com.vendo.order.entity.Invoice;
import com.vendo.order.entity.Order;
import com.vendo.order.repository.InvoiceRepository;
import com.vendo.order.repository.OrderRepository;
import com.vendo.payment.entity.Payment;
import com.vendo.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountingIntegrationService {
    
    private final IntegrationConfigurationRepository integrationConfigRepository;
    private final IntegrationSyncLogRepository syncLogRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    private final AccountingProviderFactory providerFactory;
    
    /**
     * Sync orders, invoices, and payments to accounting software
     */
    @Transactional
    public AccountingIntegrationResponse syncToAccounting(Long integrationId) {
        log.info("Starting accounting sync for integration: {}", integrationId);
        
        IntegrationConfiguration config = integrationConfigRepository.findById(integrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Integration Configuration", "id", integrationId));
        
        if (!config.getIntegrationType().equals(IntegrationConfiguration.IntegrationType.ACCOUNTING)) {
            throw new BadRequestException("Integration is not an accounting integration");
        }
        
        if (!config.getIsActive() || !config.getIsEnabled()) {
            throw new BadRequestException("Integration is not active or enabled");
        }
        
        // Create sync log
        IntegrationSyncLog syncLog = IntegrationSyncLog.builder()
                .integrationConfig(config)
                .syncType(IntegrationSyncLog.SyncType.ORDER_SYNC)
                .syncDirection(IntegrationSyncLog.SyncDirection.OUTBOUND)
                .status(IntegrationSyncLog.SyncStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();
        syncLog = syncLogRepository.save(syncLog);
        
        AccountingProviderService providerService = providerFactory.getProviderService(config.getProvider());
        
        int invoicesSynced = 0;
        int paymentsSynced = 0;
        int ordersSynced = 0;
        List<String> errors = new ArrayList<>();
        
        try {
            // Sync invoices
            List<Invoice> pendingInvoices = invoiceRepository.findByStatus("PENDING");
            for (Invoice invoice : pendingInvoices) {
                try {
                    Map<String, Object> result = providerService.syncInvoice(invoice, config);
                    invoicesSynced++;
                    log.info("Synced invoice {} to {}", invoice.getInvoiceNumber(), config.getProvider());
                } catch (Exception e) {
                    log.error("Error syncing invoice {}: {}", invoice.getInvoiceNumber(), e.getMessage());
                    errors.add("Invoice " + invoice.getInvoiceNumber() + ": " + e.getMessage());
                }
            }
            
            // Sync payments
            List<Payment> pendingPayments = paymentRepository.findByStatus("PENDING");
            for (Payment payment : pendingPayments) {
                try {
                    Map<String, Object> result = providerService.syncPayment(payment, config);
                    paymentsSynced++;
                    log.info("Synced payment {} to {}", payment.getId(), config.getProvider());
                } catch (Exception e) {
                    log.error("Error syncing payment {}: {}", payment.getId(), e.getMessage());
                    errors.add("Payment " + payment.getId() + ": " + e.getMessage());
                }
            }
            
            // Sync orders (for order-based accounting)
            List<Order> pendingOrders = orderRepository.findByStatus("PENDING");
            for (Order order : pendingOrders) {
                try {
                    Map<String, Object> result = providerService.syncOrder(order, config);
                    ordersSynced++;
                    log.info("Synced order {} to {}", order.getCode(), config.getProvider());
                } catch (Exception e) {
                    log.error("Error syncing order {}: {}", order.getCode(), e.getMessage());
                    errors.add("Order " + order.getCode() + ": " + e.getMessage());
                }
            }
            
            // Update sync log
            syncLog.setStatus(errors.isEmpty() ? IntegrationSyncLog.SyncStatus.SUCCESS : IntegrationSyncLog.SyncStatus.PARTIAL);
            syncLog.setRecordsProcessed(invoicesSynced + paymentsSynced + ordersSynced);
            syncLog.setRecordsSuccessful(invoicesSynced + paymentsSynced + ordersSynced - errors.size());
            syncLog.setRecordsFailed(errors.size());
            syncLog.setCompletedAt(LocalDateTime.now());
            syncLog.setDurationMs(java.time.Duration.between(syncLog.getStartedAt(), syncLog.getCompletedAt()).toMillis());
            if (!errors.isEmpty()) {
                syncLog.setErrorMessage(String.join("; ", errors));
            }
            syncLogRepository.save(syncLog);
            
            // Update config last sync time
            config.setLastSyncAt(LocalDateTime.now());
            integrationConfigRepository.save(config);
            
            return AccountingIntegrationResponse.builder()
                    .integrationId(integrationId)
                    .integrationName(config.getName())
                    .syncStatus(mapToSyncStatusResponse(syncLog))
                    .invoicesSynced(invoicesSynced)
                    .paymentsSynced(paymentsSynced)
                    .ordersSynced(ordersSynced)
                    .lastSyncAt(config.getLastSyncAt())
                    .errors(errors)
                    .success(errors.isEmpty())
                    .message("Sync completed: " + invoicesSynced + " invoices, " + paymentsSynced + " payments, " + ordersSynced + " orders")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error during accounting sync: ", e);
            syncLog.setStatus(IntegrationSyncLog.SyncStatus.FAILED);
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setCompletedAt(LocalDateTime.now());
            syncLogRepository.save(syncLog);
            
            throw new BadRequestException("Accounting sync failed: " + e.getMessage());
        }
    }
    
    /**
     * Handle webhook from accounting system
     */
    @Transactional
    public AccountingIntegrationResponse handleWebhook(Long integrationId, String payload, String signature) {
        log.info("Handling accounting webhook for integration: {}", integrationId);
        
        IntegrationConfiguration config = integrationConfigRepository.findById(integrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Integration Configuration", "id", integrationId));
        
        AccountingProviderService providerService = providerFactory.getProviderService(config.getProvider());
        
        // Verify webhook signature
        if (!providerService.verifyWebhookSignature(payload, signature, config)) {
            throw new BadRequestException("Invalid webhook signature");
        }
        
        // Process webhook event
        Map<String, Object> event = providerService.parseWebhookEvent(payload);
        String eventType = (String) event.get("eventType");
        
        int processed = 0;
        List<String> errors = new ArrayList<>();
        
        try {
            switch (eventType) {
                case "invoice.created":
                case "invoice.updated":
                    providerService.handleInvoiceWebhook(event, config);
                    processed++;
                    break;
                case "payment.received":
                case "payment.updated":
                    providerService.handlePaymentWebhook(event, config);
                    processed++;
                    break;
                default:
                    log.warn("Unhandled webhook event type: {}", eventType);
            }
            
            return AccountingIntegrationResponse.builder()
                    .integrationId(integrationId)
                    .integrationName(config.getName())
                    .success(true)
                    .message("Webhook processed successfully")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error processing webhook: ", e);
            errors.add(e.getMessage());
            return AccountingIntegrationResponse.builder()
                    .integrationId(integrationId)
                    .integrationName(config.getName())
                    .success(false)
                    .errors(errors)
                    .message("Webhook processing failed")
                    .build();
        }
    }
    
    /**
     * Sync from accounting system (inbound)
     */
    @Transactional
    public AccountingIntegrationResponse syncFromAccounting(Long integrationId) {
        log.info("Starting inbound accounting sync for integration: {}", integrationId);
        
        IntegrationConfiguration config = integrationConfigRepository.findById(integrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Integration Configuration", "id", integrationId));
        
        AccountingProviderService providerService = providerFactory.getProviderService(config.getProvider());
        
        IntegrationSyncLog syncLog = IntegrationSyncLog.builder()
                .integrationConfig(config)
                .syncType(IntegrationSyncLog.SyncType.INVOICE_SYNC)
                .syncDirection(IntegrationSyncLog.SyncDirection.INBOUND)
                .status(IntegrationSyncLog.SyncStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();
        syncLog = syncLogRepository.save(syncLog);
        
        int invoicesSynced = 0;
        List<String> errors = new ArrayList<>();
        
        try {
            // Fetch invoices from accounting system
            List<Map<String, Object>> externalInvoices = providerService.fetchInvoices(config);
            
            for (Map<String, Object> externalInvoice : externalInvoices) {
                try {
                    providerService.importInvoice(externalInvoice, config);
                    invoicesSynced++;
                } catch (Exception e) {
                    log.error("Error importing invoice: {}", e.getMessage());
                    errors.add("Invoice import error: " + e.getMessage());
                }
            }
            
            syncLog.setStatus(errors.isEmpty() ? IntegrationSyncLog.SyncStatus.SUCCESS : IntegrationSyncLog.SyncStatus.PARTIAL);
            syncLog.setRecordsProcessed(externalInvoices.size());
            syncLog.setRecordsSuccessful(invoicesSynced);
            syncLog.setRecordsFailed(errors.size());
            syncLog.setCompletedAt(LocalDateTime.now());
            syncLogRepository.save(syncLog);
            
            config.setLastSyncAt(LocalDateTime.now());
            integrationConfigRepository.save(config);
            
            return AccountingIntegrationResponse.builder()
                    .integrationId(integrationId)
                    .integrationName(config.getName())
                    .syncStatus(mapToSyncStatusResponse(syncLog))
                    .invoicesSynced(invoicesSynced)
                    .lastSyncAt(config.getLastSyncAt())
                    .errors(errors)
                    .success(errors.isEmpty())
                    .message("Imported " + invoicesSynced + " invoices from accounting system")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error during inbound sync: ", e);
            syncLog.setStatus(IntegrationSyncLog.SyncStatus.FAILED);
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setCompletedAt(LocalDateTime.now());
            syncLogRepository.save(syncLog);
            
            throw new BadRequestException("Inbound sync failed: " + e.getMessage());
        }
    }
    
    private SyncStatusResponse mapToSyncStatusResponse(IntegrationSyncLog syncLog) {
        return SyncStatusResponse.builder()
                .syncLogId(syncLog.getId())
                .syncType(syncLog.getSyncType())
                .syncDirection(syncLog.getSyncDirection())
                .status(syncLog.getStatus())
                .recordsProcessed(syncLog.getRecordsProcessed())
                .recordsSuccessful(syncLog.getRecordsSuccessful())
                .recordsFailed(syncLog.getRecordsFailed())
                .errorMessage(syncLog.getErrorMessage())
                .startedAt(syncLog.getStartedAt())
                .completedAt(syncLog.getCompletedAt())
                .durationMs(syncLog.getDurationMs())
                .build();
    }
}

