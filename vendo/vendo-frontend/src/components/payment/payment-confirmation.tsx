import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { CheckCircle2, XCircle, Clock, RefreshCw, Copy } from 'lucide-react';
import { PaymentResponse } from '@/types';
import { useNavigate } from 'react-router-dom';
import { RefundPaymentForm } from './refund-payment-form';
import { useToast } from '@/hooks/use-toast';
import { formatPrice } from '@/lib/utils';

interface PaymentConfirmationProps {
  payment: PaymentResponse;
  onRetry?: () => void;
}

export function PaymentConfirmation({ payment, onRetry }: PaymentConfirmationProps) {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [showRefundForm, setShowRefundForm] = useState(false);
  const [currentPayment, setCurrentPayment] = useState<PaymentResponse>(payment);

  const getStatusIcon = () => {
    switch (payment.status?.toUpperCase()) {
      case 'COMPLETED':
      case 'completed':
        return <CheckCircle2 className="h-12 w-12 text-green-500" />;
      case 'FAILED':
      case 'failed':
        return <XCircle className="h-12 w-12 text-red-500" />;
      case 'PENDING':
      case 'pending':
      case 'PROCESSING':
      case 'processing':
        return <Clock className="h-12 w-12 text-yellow-500" />;
      default:
        return <Clock className="h-12 w-12 text-gray-500" />;
    }
  };

  const getStatusMessage = () => {
    switch (payment.status?.toUpperCase()) {
      case 'COMPLETED':
      case 'completed':
        return {
          title: 'Payment Successful!',
          description: 'Your payment has been processed successfully.',
          color: 'text-green-600',
        };
      case 'FAILED':
      case 'failed':
        return {
          title: 'Payment Failed',
          description: payment.errorMessage || 'Your payment could not be processed.',
          color: 'text-red-600',
        };
      case 'PENDING':
      case 'pending':
      case 'PROCESSING':
      case 'processing':
        return {
          title: 'Payment Processing',
          description: 'Your payment is being processed. Please wait...',
          color: 'text-yellow-600',
        };
      default:
        return {
          title: 'Payment Status Unknown',
          description: 'Unable to determine payment status.',
          color: 'text-gray-600',
        };
    }
  };

  const statusInfo = getStatusMessage();
  const isSuccess = payment.status?.toUpperCase() === 'COMPLETED' || payment.status === 'completed';

  return (
    <div className="container max-w-2xl py-8">
      <Card>
        <CardHeader>
          <div className="flex flex-col items-center text-center space-y-4">
            {getStatusIcon()}
            <CardTitle className={`text-2xl ${statusInfo.color}`}>
              {statusInfo.title}
            </CardTitle>
            <p className="text-muted-foreground">{statusInfo.description}</p>
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Transaction Summary */}
          <div className="space-y-4">
            <h3 className="font-semibold text-lg">Transaction Summary</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Transaction ID:</span>
                <span className="font-mono">{payment.transactionId || payment.chargeId || 'N/A'}</span>
              </div>
              {payment.chargeId && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Charge ID:</span>
                  <span className="font-mono">{payment.chargeId}</span>
                </div>
              )}
              <div className="flex justify-between">
                <span className="text-muted-foreground">Order ID:</span>
                <span>#{payment.orderId}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Payment Gateway:</span>
                <span>{payment.paymentGateway || payment.paymentChannel || 'N/A'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Payment Method:</span>
                <span>{payment.paymentMethod || 'N/A'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Amount:</span>
                <span className="font-semibold">
                  {formatPrice(payment.amount, payment.currency || 'USD')}
                </span>
              </div>
              {payment.amount - (payment.refundedAmount || 0) > 0 && (
                <div className="flex justify-between text-green-600">
                  <span className="text-muted-foreground">Refundable Amount:</span>
                  <span className="font-semibold">
                    {formatPrice(
                      payment.amount - (payment.refundedAmount || 0),
                      payment.currency || 'USD'
                    )}
                  </span>
                </div>
              )}
              {payment.paymentFee && payment.paymentFee > 0 && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Processing Fee:</span>
                  <span>
                    {new Intl.NumberFormat('en-US', {
                      style: 'currency',
                      currency: payment.currency || 'USD',
                    }).format(payment.paymentFee)}
                  </span>
                </div>
              )}
              {payment.refundedAmount && payment.refundedAmount > 0 && (
                <div className="flex justify-between text-red-600">
                  <span className="text-muted-foreground">Refunded Amount:</span>
                  <span>
                    {new Intl.NumberFormat('en-US', {
                      style: 'currency',
                      currency: payment.currency || 'USD',
                    }).format(payment.refundedAmount)}
                  </span>
                </div>
              )}
              <div className="flex justify-between">
                <span className="text-muted-foreground">Status:</span>
                <span className={`font-semibold ${statusInfo.color}`}>
                  {payment.status?.toUpperCase()}
                </span>
              </div>
              {payment.createdAt && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Date:</span>
                  <span>{new Date(payment.createdAt).toLocaleString()}</span>
                </div>
              )}
            </div>
          </div>

          {/* Error Details */}
          {payment.errorCode && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4">
              <h4 className="font-semibold text-red-800 mb-2">Error Details</h4>
              <p className="text-sm text-red-700">
                <span className="font-medium">Code:</span> {payment.errorCode}
              </p>
              {payment.errorMessage && (
                <p className="text-sm text-red-700 mt-1">
                  <span className="font-medium">Message:</span> {payment.errorMessage}
                </p>
              )}
            </div>
          )}

          {/* Gateway Data Details */}
          {payment.gatewayData && Object.keys(payment.gatewayData).length > 0 && (
            <div className="space-y-4">
              <h3 className="font-semibold text-lg">Transaction Details</h3>
              <div className="bg-muted p-4 rounded-lg space-y-2 text-sm">
                {Object.entries(payment.gatewayData).map(([key, value]) => (
                  <div key={key} className="flex justify-between">
                    <span className="text-muted-foreground capitalize">
                      {key.replace(/_/g, ' ')}:
                    </span>
                    <span className="font-mono text-xs break-all">{String(value)}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Refund Form */}
          {showRefundForm && (
            <div className="space-y-4">
              <RefundPaymentForm
                payment={currentPayment}
                onRefundSuccess={(refundedPayment) => {
                  setCurrentPayment(refundedPayment);
                  setShowRefundForm(false);
                  toast({
                    title: 'Refund Processed',
                    description: 'The refund has been processed successfully.',
                  });
                }}
                onCancel={() => setShowRefundForm(false)}
              />
            </div>
          )}

          {/* Actions */}
          <div className="flex flex-col sm:flex-row gap-3">
            {isSuccess ? (
              <>
                <Button
                  onClick={() => navigate(`/orders/${payment.orderId}`)}
                  className="flex-1"
                >
                  View Order
                </Button>
                {payment.amount - (payment.refundedAmount || 0) > 0 && (
                  <Button
                    onClick={() => setShowRefundForm(!showRefundForm)}
                    variant="outline"
                    className="flex-1"
                  >
                    <RefreshCw className="mr-2 h-4 w-4" />
                    {showRefundForm ? 'Cancel Refund' : 'Process Refund'}
                  </Button>
                )}
                <Button
                  onClick={() => {
                    const receiptText = `
Payment Receipt
================
Transaction ID: ${payment.transactionId || payment.chargeId || 'N/A'}
Order ID: #${payment.orderId}
Amount: ${formatPrice(payment.amount, payment.currency || 'USD')}
Payment Method: ${payment.paymentMethod || 'N/A'}
Payment Gateway: ${payment.paymentGateway || 'N/A'}
Status: ${payment.status}
Date: ${payment.createdAt ? new Date(payment.createdAt).toLocaleString() : 'N/A'}
                    `.trim();
                    navigator.clipboard.writeText(receiptText);
                    toast({
                      title: 'Receipt Copied',
                      description: 'Payment receipt has been copied to clipboard.',
                    });
                  }}
                  variant="outline"
                  className="flex-1"
                >
                  <Copy className="mr-2 h-4 w-4" />
                  Copy Receipt
                </Button>
                <Button
                  onClick={() => navigate('/orders')}
                  variant="outline"
                  className="flex-1"
                >
                  View All Orders
                </Button>
              </>
            ) : (
              <>
                {onRetry && (
                  <Button
                    onClick={onRetry}
                    className="flex-1"
                  >
                    <RefreshCw className="mr-2 h-4 w-4" />
                    Retry Payment
                  </Button>
                )}
                <Button
                  onClick={() => navigate('/')}
                  variant="outline"
                  className="flex-1"
                >
                  Back to Home
                </Button>
              </>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

