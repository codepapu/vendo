import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Loader2, AlertCircle, RefreshCw } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { useToast } from '@/hooks/use-toast';
import { PaymentResponse, RefundPaymentRequest } from '@/types';
import { paymentApi } from '@/services/payment-api';
import { formatPrice } from '@/lib/utils';

interface RefundPaymentFormProps {
  payment: PaymentResponse;
  onRefundSuccess?: (refundedPayment: PaymentResponse) => void;
  onCancel?: () => void;
}

export function RefundPaymentForm({
  payment,
  onRefundSuccess,
  onCancel,
}: RefundPaymentFormProps) {
  const { toast } = useToast();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [refundType, setRefundType] = useState<'full' | 'partial'>('full');
  const [refundAmount, setRefundAmount] = useState<string>('');
  const [reason, setReason] = useState('');
  const [note, setNote] = useState('');

  const availableAmount = payment.amount - (payment.refundedAmount || 0);
  const canRefund = availableAmount > 0;

  const handleRefund = async () => {
    if (!canRefund) {
      setError('No amount available for refund');
      return;
    }

    if (refundType === 'partial') {
      const amount = parseFloat(refundAmount);
      if (isNaN(amount) || amount <= 0) {
        setError('Please enter a valid refund amount');
        return;
      }
      if (amount > availableAmount) {
        setError(`Refund amount cannot exceed ${formatPrice(availableAmount, payment.currency)}`);
        return;
      }
    }

    setLoading(true);
    setError(null);

    try {
      const refundRequest: RefundPaymentRequest = {
        paymentId: payment.id,
        amount: refundType === 'partial' ? parseFloat(refundAmount) : undefined,
        reason: reason || undefined,
        note: note || undefined,
      };

      const refundedPayment = await paymentApi.processRefund(refundRequest);

      toast({
        title: 'Refund Processed',
        description: `Refund of ${formatPrice(
          refundedPayment.refundedAmount || availableAmount,
          payment.currency
        )} has been processed successfully.`,
      });

      if (onRefundSuccess) {
        onRefundSuccess(refundedPayment);
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to process refund';
      setError(errorMessage);
      toast({
        title: 'Refund Failed',
        description: errorMessage,
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  if (!canRefund) {
    return (
      <Card>
        <CardContent className="py-8">
          <Alert>
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              This payment has already been fully refunded.
            </AlertDescription>
          </Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <RefreshCw className="h-5 w-5" />
          Process Refund
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {error && (
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        <div className="space-y-2">
          <Label>Payment Details</Label>
          <div className="bg-muted p-4 rounded-lg space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Original Amount:</span>
              <span className="font-medium">
                {formatPrice(payment.amount, payment.currency)}
              </span>
            </div>
            {payment.refundedAmount && payment.refundedAmount > 0 && (
              <div className="flex justify-between text-red-600">
                <span className="text-muted-foreground">Already Refunded:</span>
                <span className="font-medium">
                  {formatPrice(payment.refundedAmount, payment.currency)}
                </span>
              </div>
            )}
            <div className="flex justify-between font-semibold pt-2 border-t">
              <span>Available for Refund:</span>
              <span className="text-green-600">
                {formatPrice(availableAmount, payment.currency)}
              </span>
            </div>
          </div>
        </div>

        <div className="space-y-2">
          <Label>Refund Type</Label>
          <div className="flex gap-4">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                name="refundType"
                value="full"
                checked={refundType === 'full'}
                onChange={() => {
                  setRefundType('full');
                  setRefundAmount('');
                }}
                className="w-4 h-4"
              />
              <span>Full Refund</span>
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                name="refundType"
                value="partial"
                checked={refundType === 'partial'}
                onChange={() => setRefundType('partial')}
                className="w-4 h-4"
              />
              <span>Partial Refund</span>
            </label>
          </div>
        </div>

        {refundType === 'partial' && (
          <div className="space-y-2">
            <Label htmlFor="refundAmount">
              Refund Amount ({payment.currency})
            </Label>
            <Input
              id="refundAmount"
              type="number"
              step="0.01"
              min="0.01"
              max={availableAmount}
              value={refundAmount}
              onChange={(e) => setRefundAmount(e.target.value)}
              placeholder={`Max: ${formatPrice(availableAmount, payment.currency)}`}
              disabled={loading}
            />
            <p className="text-xs text-muted-foreground">
              Maximum refundable: {formatPrice(availableAmount, payment.currency)}
            </p>
          </div>
        )}

        <div className="space-y-2">
          <Label htmlFor="reason">Reason for Refund</Label>
          <Input
            id="reason"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="e.g., Customer request, Product defect, etc."
            disabled={loading}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="note">Additional Notes (Optional)</Label>
          <Textarea
            id="note"
            value={note}
            onChange={(e) => setNote(e.target.value)}
            placeholder="Any additional information about this refund..."
            rows={3}
            disabled={loading}
          />
        </div>

        <div className="flex gap-3 pt-4">
          <Button
            onClick={handleRefund}
            disabled={loading || (refundType === 'partial' && !refundAmount)}
            className="flex-1"
          >
            {loading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Processing...
              </>
            ) : (
              <>
                <RefreshCw className="mr-2 h-4 w-4" />
                Process Refund
              </>
            )}
          </Button>
          {onCancel && (
            <Button onClick={onCancel} variant="outline" disabled={loading}>
              Cancel
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  );
}

