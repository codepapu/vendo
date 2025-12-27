/**
 * Reporting Type Definitions
 * 
 * TypeScript types for Report generation and export.
 * All types match the backend API DTOs.
 */

// ============================================================================
// Report Types
// ============================================================================

/**
 * Report type enum
 */
export type ReportType = 'SALES' | 'PRODUCT' | 'CUSTOMER' | 'INVENTORY' | 'FINANCIAL' | 'CUSTOM';

/**
 * Report format enum
 */
export type ReportFormat = 'PDF' | 'CSV' | 'EXCEL' | 'JSON';

/**
 * Report status enum
 */
export type ReportStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

// ============================================================================
// Report Request Types
// ============================================================================

/**
 * Base report request
 */
export interface BaseReportRequest {
  reportType: ReportType;
  startDate?: string; // ISO date string
  endDate?: string; // ISO date string
  format?: ReportFormat;
  includeCharts?: boolean;
}

/**
 * Sales report request
 */
export interface SalesReportRequest extends BaseReportRequest {
  reportType: 'SALES';
  includeOrderDetails?: boolean;
  includeProductBreakdown?: boolean;
  groupBy?: 'DAY' | 'WEEK' | 'MONTH' | 'YEAR';
  orderStatus?: string[];
  paymentStatus?: string[];
}

/**
 * Product report request
 */
export interface ProductReportRequest extends BaseReportRequest {
  reportType: 'PRODUCT';
  categoryId?: number;
  brandId?: number;
  includeInventory?: boolean;
  includeSales?: boolean;
  sortBy?: 'SALES' | 'REVENUE' | 'VIEWS' | 'RATING';
}

/**
 * Customer report request
 */
export interface CustomerReportRequest extends BaseReportRequest {
  reportType: 'CUSTOMER';
  userType?: 'CUSTOMER' | 'BUSINESS' | 'RETAILER';
  includeOrderHistory?: boolean;
  includePurchaseBehavior?: boolean;
  segmentBy?: 'REGISTRATION_DATE' | 'PURCHASE_FREQUENCY' | 'TOTAL_SPENT';
}

/**
 * Report request union type
 */
export type ReportRequest = SalesReportRequest | ProductReportRequest | CustomerReportRequest | BaseReportRequest;

// ============================================================================
// Report Response Types
// ============================================================================

/**
 * Report data structure
 */
export interface ReportData {
  summary: ReportSummary;
  details: ReportDetail[];
  charts?: ReportChart[];
  metadata: ReportMetadata;
}

/**
 * Report summary
 */
export interface ReportSummary {
  totalRecords: number;
  totalRevenue?: number;
  totalOrders?: number;
  totalCustomers?: number;
  totalProducts?: number;
  period: {
    startDate: string; // ISO date string
    endDate: string; // ISO date string
  };
  generatedAt: string; // ISO date string
}

/**
 * Report detail row
 */
export interface ReportDetail {
  [key: string]: string | number | boolean | null | undefined;
}

/**
 * Report chart data
 */
export interface ReportChart {
  type: 'LINE' | 'BAR' | 'PIE' | 'AREA';
  title: string;
  data: ChartDataPoint[];
  labels?: string[];
}

/**
 * Chart data point
 */
export interface ChartDataPoint {
  label: string;
  value: number;
  [key: string]: string | number | boolean | null | undefined;
}

/**
 * Report metadata
 */
export interface ReportMetadata {
  reportId: string;
  reportType: ReportType;
  format: ReportFormat;
  generatedBy?: number;
  generatedAt: string; // ISO date string
  fileSize?: number;
  rowCount?: number;
}

/**
 * Report response type
 */
export interface ReportResponse {
  success: boolean;
  message?: string;
  data: {
    reportId: string;
    reportType: ReportType;
    format: ReportFormat;
    status: ReportStatus;
    downloadUrl?: string;
    expiresAt?: string; // ISO date string
    data?: ReportData; // For JSON format
  };
  timestamp?: string;
}

/**
 * Scheduled report configuration
 */
export interface ScheduledReportConfig {
  reportType: ReportType;
  format: ReportFormat;
  schedule: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'CUSTOM';
  scheduleConfig?: {
    dayOfWeek?: number; // 0-6 for weekly
    dayOfMonth?: number; // 1-31 for monthly
    time?: string; // HH:mm format
    timezone?: string;
  };
  recipients: string[]; // Email addresses
  startDate?: string; // ISO date string
  endDate?: string; // ISO date string
  filters?: Record<string, any>;
}

/**
 * Scheduled report response
 */
export interface ScheduledReportResponse {
  id: number;
  reportType: ReportType;
  format: ReportFormat;
  schedule: string;
  status: 'ACTIVE' | 'PAUSED' | 'CANCELLED';
  nextRunAt?: string; // ISO date string
  lastRunAt?: string; // ISO date string
  recipients: string[];
  createdAt: string; // ISO date string
}

// ============================================================================
// All types are exported above using 'export interface' and 'export type'
// ============================================================================

