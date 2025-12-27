import { apiClient, handleApiError, showSuccessToast } from '@/lib/api';
import type {
  ReportRequest,
  ReportResponse,
  ReportType,
  ReportFormat,
  ScheduledReportConfig,
  ScheduledReportResponse,
  ApiResponse,
} from '@/types';

/**
 * Reporting API service
 * Handles report generation and export
 */
class ReportingApiService {
  /**
   * Generate sales report
   */
  async generateSalesReport(params: {
    startDate?: string;
    endDate?: string;
    format?: ReportFormat;
    includeOrderDetails?: boolean;
    includeProductBreakdown?: boolean;
    groupBy?: 'DAY' | 'WEEK' | 'MONTH' | 'YEAR';
    orderStatus?: string[];
    paymentStatus?: string[];
  }): Promise<ReportResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<ReportResponse>>(
        '/v1/reports/sales',
        {
          reportType: 'SALES',
          ...params,
        }
      );

      if (response.data.success && response.data.data) {
        showSuccessToast('Sales report generated successfully');
        return response.data.data;
      }

      return null;
    } catch (error) {
      handleApiError(error, 'Failed to generate sales report');
      return null;
    }
  }

  /**
   * Generate product report
   */
  async generateProductReport(params: {
    startDate?: string;
    endDate?: string;
    format?: ReportFormat;
    categoryId?: number;
    brandId?: number;
    includeInventory?: boolean;
    includeSales?: boolean;
    sortBy?: 'SALES' | 'REVENUE' | 'VIEWS' | 'RATING';
  }): Promise<ReportResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<ReportResponse>>(
        '/v1/reports/products',
        {
          reportType: 'PRODUCT',
          ...params,
        }
      );

      if (response.data.success && response.data.data) {
        showSuccessToast('Product report generated successfully');
        return response.data.data;
      }

      return null;
    } catch (error) {
      handleApiError(error, 'Failed to generate product report');
      return null;
    }
  }

  /**
   * Generate customer report
   */
  async generateCustomerReport(params: {
    startDate?: string;
    endDate?: string;
    format?: ReportFormat;
    userType?: 'CUSTOMER' | 'BUSINESS' | 'RETAILER';
    includeOrderHistory?: boolean;
    includePurchaseBehavior?: boolean;
    segmentBy?: 'REGISTRATION_DATE' | 'PURCHASE_FREQUENCY' | 'TOTAL_SPENT';
  }): Promise<ReportResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<ReportResponse>>(
        '/v1/reports/customers',
        {
          reportType: 'CUSTOMER',
          ...params,
        }
      );

      if (response.data.success && response.data.data) {
        showSuccessToast('Customer report generated successfully');
        return response.data.data;
      }

      return null;
    } catch (error) {
      handleApiError(error, 'Failed to generate customer report');
      return null;
    }
  }

  /**
   * Export report
   * Downloads report in the specified format
   */
  async exportReport(
    reportId: string,
    format: ReportFormat = 'PDF'
  ): Promise<Blob | null> {
    try {
      const response = await apiClient.get(
        `/v1/reports/${reportId}/export`,
        {
          params: { format },
          responseType: 'blob',
        }
      );

      return response.data;
    } catch (error) {
      handleApiError(error, 'Failed to export report');
      return null;
    }
  }

  /**
   * Download report file
   */
  async downloadReport(reportId: string, format: ReportFormat = 'PDF', filename?: string): Promise<boolean> {
    try {
      const blob = await this.exportReport(reportId, format);
      
      if (!blob) {
        return false;
      }

      // Determine file extension
      let extension = format.toLowerCase();
      if (format === 'EXCEL') {
        extension = 'xlsx'; // Excel files use .xlsx extension
      }
      const defaultFilename = `report-${reportId}.${extension}`;
      
      // Create download link
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename || defaultFilename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      return true;
    } catch (error) {
      handleApiError(error, 'Failed to download report');
      return false;
    }
  }

  /**
   * Generate and download report directly
   */
  async generateAndDownloadReport(
    request: ReportRequest,
    filename?: string
  ): Promise<boolean> {
    try {
      const response = await apiClient.post<ApiResponse<ReportResponse>>(
        '/v1/reports/generate',
        request
      );

      if (response.data.success && response.data.data) {
        const reportId = response.data.data.reportId;
        const format = response.data.data.format;
        
        // Wait a bit for report processing
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        return await this.downloadReport(reportId, format, filename);
      }

      return false;
    } catch (error) {
      handleApiError(error, 'Failed to generate and download report');
      return false;
    }
  }

  /**
   * Schedule automated report
   */
  async scheduleReport(config: ScheduledReportConfig): Promise<ScheduledReportResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<ScheduledReportResponse>>(
        '/v1/reports/schedule',
        config
      );

      if (response.data.success && response.data.data) {
        showSuccessToast('Report scheduled successfully');
        return response.data.data;
      }

      return null;
    } catch (error) {
      handleApiError(error, 'Failed to schedule report');
      return null;
    }
  }

  /**
   * Get scheduled reports
   */
  async getScheduledReports(): Promise<ScheduledReportResponse[] | null> {
    try {
      const response = await apiClient.get<ApiResponse<ScheduledReportResponse[]>>(
        '/v1/reports/scheduled'
      );

      if (response.data.success && response.data.data) {
        return response.data.data;
      }

      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch scheduled reports');
      return null;
    }
  }

  /**
   * Cancel scheduled report
   */
  async cancelScheduledReport(reportId: number): Promise<boolean> {
    try {
      const response = await apiClient.delete<ApiResponse<void>>(
        `/v1/reports/scheduled/${reportId}`
      );

      if (response.data.success) {
        showSuccessToast('Scheduled report cancelled successfully');
        return true;
      }

      return false;
    } catch (error) {
      handleApiError(error, 'Failed to cancel scheduled report');
      return false;
    }
  }

  /**
   * Get report by ID
   */
  async getReport(reportId: string): Promise<ReportResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<ReportResponse>>(
        `/v1/reports/${reportId}`
      );

      if (response.data.success && response.data.data) {
        return response.data.data;
      }

      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch report');
      return null;
    }
  }
}

export const reportingApi = new ReportingApiService();
export default reportingApi;

