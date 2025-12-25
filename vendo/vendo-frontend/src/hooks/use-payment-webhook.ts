import { useEffect, useRef } from 'react';
import { paymentApi } from '@/services/payment-api';
import { PaymentResponse } from '@/types';

/**
 * Hook for handling payment webhook updates
 * This can be used with Server-Sent Events (SSE) or WebSocket connections
 * For now, it provides a structure for webhook integration
 */
export function usePaymentWebhook(
  chargeId: string | null,
  onPaymentUpdate: (payment: PaymentResponse) => void,
  enabled: boolean = true
) {
  const eventSourceRef = useRef<EventSource | null>(null);
  const retryCountRef = useRef(0);
  const maxRetries = 3;

  useEffect(() => {
    if (!chargeId || !enabled) return;

    // For now, we'll use polling as fallback
    // In production, you would set up SSE or WebSocket connection here
    // Example SSE implementation:
    /*
    const eventSource = new EventSource(
      `/api/v1/payments/${chargeId}/stream`,
      { withCredentials: true }
    );

    eventSource.onmessage = (event) => {
      try {
        const payment: PaymentResponse = JSON.parse(event.data);
        onPaymentUpdate(payment);
        
        // Stop listening if payment is in final state
        if (payment.status === 'COMPLETED' || payment.status === 'FAILED') {
          eventSource.close();
        }
      } catch (error) {
        console.error('Failed to parse webhook event:', error);
      }
    };

    eventSource.onerror = (error) => {
      console.error('Webhook connection error:', error);
      retryCountRef.current++;
      
      if (retryCountRef.current >= maxRetries) {
        eventSource.close();
        // Fallback to polling
      }
    };

    eventSourceRef.current = eventSource;
    */

    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }
    };
  }, [chargeId, enabled, onPaymentUpdate]);

  /**
   * Manually trigger payment status check
   * Useful for webhook callback handlers
   */
  const refreshPaymentStatus = async () => {
    if (!chargeId) return;

    try {
      const payment = await paymentApi.getPaymentByChargeId(chargeId);
      onPaymentUpdate(payment);
    } catch (error) {
      console.error('Failed to refresh payment status:', error);
    }
  };

  return {
    refreshPaymentStatus,
  };
}

