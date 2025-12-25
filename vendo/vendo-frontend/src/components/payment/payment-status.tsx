import { useEffect, useState, useRef } from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { Loader2, CheckCircle2, XCircle, Clock, AlertCircle } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { PaymentResponse } from '@/types';
import { paymentApi } from '@/services/payment-api';
import { usePaymentWebhook } from '@/hooks/use-payment-webhook';

interface PaymentStatusProps {
  chargeId: string;
  onStatusChange?: (payment: PaymentResponse) => void;
  pollingInterval?: number; // in milliseconds
}

export function PaymentStatus({
  chargeId,
  onStatusChange,
  pollingInterval = 3000, // Poll every 3 seconds
}: PaymentStatusProps) {
  const [payment, setPayment] = useState<PaymentResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchPaymentStatus = async () => {
    try {
      const paymentData = await paymentApi.getPaymentByChargeId(chargeId);
      setPayment(paymentData);
      setError(null);

      if (onStatusChange) {
        onStatusChange(paymentData);
      }

      // Stop polling if payment is completed or failed
      const status = paymentData.status?.toUpperCase();
      if (status === 'COMPLETED' || status === 'completed' || 
          status === 'FAILED' || status === 'failed') {
        return false; // Stop polling
      }
      return true; // Continue polling
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch payment status');
      return false; // Stop polling on error
    } finally {
      setLoading(false);
    }
  };

  // Try to use webhook if available, fallback to polling
  const { refreshPaymentStatus } = usePaymentWebhook(
    chargeId,
    (updatedPayment) => {
      setPayment(updatedPayment);
      if (onStatusChange) {
        onStatusChange(updatedPayment);
      }
    },
    true
  );

  useEffect(() => {
    if (!chargeId) return;

    // Initial fetch
    fetchPaymentStatus();

    // Set up polling as fallback (webhook may not be available)
    const interval = setInterval(async () => {
      const shouldContinue = await fetchPaymentStatus();
      if (!shouldContinue) {
        clearInterval(interval);
      }
    }, pollingInterval);

    return () => clearInterval(interval);
  }, [chargeId, pollingInterval]);

  if (loading && !payment) {
    return (
      <Card>
        <CardContent className="flex items-center justify-center py-8">
          <Loader2 className="h-6 w-6 animate-spin text-primary" />
          <span className="ml-2">Checking payment status...</span>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <CardContent className="py-8">
          <div className="flex items-center text-red-600">
            <XCircle className="h-5 w-5 mr-2" />
            <span>{error}</span>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (!payment) {
    return null;
  }

  const status = payment.status?.toUpperCase();
  const isCompleted = status === 'COMPLETED' || status === 'completed';
  const isFailed = status === 'FAILED' || status === 'failed';
  const isProcessing = status === 'PENDING' || status === 'pending' || 
                       status === 'PROCESSING' || status === 'processing';

  return (
    <Card>
      <CardContent className="py-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            {isCompleted && <CheckCircle2 className="h-5 w-5 text-green-500" />}
            {isFailed && <XCircle className="h-5 w-5 text-red-500" />}
            {isProcessing && <Clock className="h-5 w-5 text-yellow-500 animate-pulse" />}
            <div>
              <p className="font-medium">Payment Status</p>
              <p className="text-sm text-muted-foreground">
                {payment.status?.toUpperCase() || 'Unknown'}
              </p>
            </div>
          </div>
          {isProcessing && (
            <Loader2 className="h-4 w-4 animate-spin text-primary" />
          )}
        </div>

        {payment.errorMessage && (
          <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-sm text-red-700">{payment.errorMessage}</p>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

