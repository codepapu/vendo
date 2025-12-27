import { api } from '../lib/api';

export interface IntegrationConfig {
  id: number;
  integrationType: 'ACCOUNTING' | 'CRM' | 'ERP';
  provider: string;
  name: string;
  description?: string;
  isActive: boolean;
  isEnabled: boolean;
  syncDirection: 'INBOUND' | 'OUTBOUND' | 'BIDIRECTIONAL';
  autoSyncEnabled: boolean;
  syncFrequency?: 'HOURLY' | 'DAILY' | 'WEEKLY' | 'MANUAL';
  lastSyncAt?: string;
  nextSyncAt?: string;
  webhookEnabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface IntegrationConfigRequest {
  integrationType: 'ACCOUNTING' | 'CRM' | 'ERP';
  provider: string;
  name: string;
  description?: string;
  isActive?: boolean;
  isEnabled?: boolean;
  connectionConfig: Record<string, any>;
  syncDirection?: 'INBOUND' | 'OUTBOUND' | 'BIDIRECTIONAL';
  autoSyncEnabled?: boolean;
  syncFrequency?: 'HOURLY' | 'DAILY' | 'WEEKLY' | 'MANUAL';
  webhookUrl?: string;
  webhookSecret?: string;
  webhookEnabled?: boolean;
  oauthClientId?: string;
  oauthClientSecret?: string;
}

export interface SyncStatus {
  syncLogId: number;
  syncType: string;
  syncDirection: 'INBOUND' | 'OUTBOUND';
  status: 'PENDING' | 'IN_PROGRESS' | 'SUCCESS' | 'FAILED' | 'PARTIAL';
  recordsProcessed: number;
  recordsSuccessful: number;
  recordsFailed: number;
  errorMessage?: string;
  startedAt: string;
  completedAt?: string;
  durationMs?: number;
}

export interface AccountingIntegrationResponse {
  integrationId: number;
  integrationName: string;
  syncStatus: SyncStatus;
  invoicesSynced: number;
  paymentsSynced: number;
  ordersSynced: number;
  lastSyncAt?: string;
  errors: string[];
  success: boolean;
  message: string;
}

export interface CRMIntegrationResponse {
  integrationId: number;
  integrationName: string;
  syncStatus: SyncStatus;
  contactsSynced: number;
  interactionsSynced: number;
  lastSyncAt?: string;
  errors: string[];
  success: boolean;
  message: string;
}

export interface ERPIntegrationResponse {
  integrationId: number;
  integrationName: string;
  syncStatus: SyncStatus;
  productsSynced: number;
  inventorySynced: number;
  ordersSynced: number;
  lastSyncAt?: string;
  errors: string[];
  success: boolean;
  message: string;
}

export const integrationApi = {
  // Configuration Management
  getAllConfigurations: async (page = 0, size = 20) => {
    const response = await api.get(`/integrations?page=${page}&size=${size}`);
    return response.data;
  },

  getConfigurationById: async (id: number) => {
    const response = await api.get(`/integrations/${id}`);
    return response.data;
  },

  getConfigurationsByType: async (type: 'ACCOUNTING' | 'CRM' | 'ERP', page = 0, size = 20) => {
    const response = await api.get(`/integrations/type/${type}?page=${page}&size=${size}`);
    return response.data;
  },

  createConfiguration: async (config: IntegrationConfigRequest) => {
    const response = await api.post('/integrations', config);
    return response.data;
  },

  updateConfiguration: async (id: number, config: IntegrationConfigRequest) => {
    const response = await api.put(`/integrations/${id}`, config);
    return response.data;
  },

  deleteConfiguration: async (id: number) => {
    const response = await api.delete(`/integrations/${id}`);
    return response.data;
  },

  enableConfiguration: async (id: number) => {
    const response = await api.post(`/integrations/${id}/enable`);
    return response.data;
  },

  disableConfiguration: async (id: number) => {
    const response = await api.post(`/integrations/${id}/disable`);
    return response.data;
  },

  // Accounting Integration
  syncToAccounting: async (id: number): Promise<AccountingIntegrationResponse> => {
    const response = await api.post(`/integrations/accounting/${id}/sync`);
    return response.data.data;
  },

  syncFromAccounting: async (id: number): Promise<AccountingIntegrationResponse> => {
    const response = await api.post(`/integrations/accounting/${id}/sync/inbound`);
    return response.data.data;
  },

  // CRM Integration
  syncToCRM: async (id: number): Promise<CRMIntegrationResponse> => {
    const response = await api.post(`/integrations/crm/${id}/sync`);
    return response.data.data;
  },

  syncFromCRM: async (id: number): Promise<CRMIntegrationResponse> => {
    const response = await api.post(`/integrations/crm/${id}/sync/inbound`);
    return response.data.data;
  },

  // ERP Integration
  syncToERP: async (id: number): Promise<ERPIntegrationResponse> => {
    const response = await api.post(`/integrations/erp/${id}/sync`);
    return response.data.data;
  },

  syncFromERP: async (id: number): Promise<ERPIntegrationResponse> => {
    const response = await api.post(`/integrations/erp/${id}/sync/inbound`);
    return response.data.data;
  },
};

