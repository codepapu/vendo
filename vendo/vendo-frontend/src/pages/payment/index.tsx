import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Loader2, ArrowLeft, Lock, AlertCircle } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { ProtectedRoute } from '@/components/protected-route';
import { PaymentMethodSelector } from '@/components/payment/payment-method-selector';
import { PaymentGatewayAdapter } from '@/components/payment/payment-gateway-adapter';
import { PaymentConfirmation } from '@/components/payment/payment-confirmation';
import { PaymentErrorBoundary } from '@/components/payment/payment-error-boundary';
import { orderApi } from '@/services/order-api';
import { userApi } from '@/services/user-api';
import { paymentApi } from '@/services/payment-api';
import { useToast } from '@/hooks/use-toast';
import { PaymentGateway, PaymentMethod, PaymentResponse } from '@/types';
import { formatPrice } from '@/lib/utils';

export function PaymentPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { toast } = useToast();

  const orderId = Number(searchParams.get('orderId'));
  const chargeId = searchParams.get('chargeId');

  const [selectedGateway, setSelectedGateway] = useState<PaymentGateway | null>(null);
  const [selectedMethod, setSelectedMethod] = useState<PaymentMethod | null>(null);
  const [payment, setPayment] = useState<PaymentResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Fetch order details
  const { data: order, isLoading: orderLoading } = useQuery({
    queryKey: ['order', orderId],
    queryFn: () => orderApi.getOrderById(orderId),
    enabled: !!orderId,
  });

  // Fetch user profile
  const { data: userProfile } = useQuery({
    queryKey: ['userProfile'],
    queryFn: () => userApi.getProfile(),
  });

  // If chargeId is provided, show payment status
  useEffect(() => {
    if (chargeId) {
      paymentApi
        .getPaymentByChargeId(chargeId)
        .then((paymentData) => {
          setPayment(paymentData);
        })
        .catch((err) => {
          setError(err instanceof Error ? err.message : 'Failed to fetch payment status');
        });
    }
  }, [chargeId]);

  if (!orderId) {
    return (
      <div className="container max-w-2xl py-8">
        <Card>
          <CardContent className="py-8">
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>Order ID is required</AlertDescription>
            </Alert>
            <Button onClick={() => navigate('/orders')} className="mt-4">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Orders
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (orderLoading) {
    return (
      <div className="container max-w-2xl py-8">
        <Card>
          <CardContent className="flex items-center justify-center py-8">
            <Loader2 className="h-6 w-6 animate-spin text-primary" />
            <span className="ml-2">Loading order details...</span>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="container max-w-2xl py-8">
        <Card>
          <CardContent className="py-8">
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>Order not found</AlertDescription>
            </Alert>
            <Button onClick={() => navigate('/orders')} className="mt-4">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Orders
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  // If payment is completed, show confirmation
  if (payment && (payment.status === 'COMPLETED' || payment.status === 'completed')) {
    return (
      <div className="container max-w-2xl py-8">
        <PaymentConfirmation payment={payment} />
      </div>
    );
  }

  const amount = order.amount || 0;
  const currency = 'USD';
  const profileName = [userProfile?.firstName, userProfile?.lastName].filter(Boolean).join(' ');

  const handlePaymentSuccess = (paymentResponse: PaymentResponse) => {
    setPayment(paymentResponse);
    toast({
      title: 'Payment Successful',
      description: 'Your payment has been processed successfully.',
    });
    // Redirect to success page after a short delay
    setTimeout(() => {
      navigate(`/payment/success?orderId=${orderId}&chargeId=${paymentResponse.chargeId || paymentResponse.id}`);
    }, 1500);
  };

  const handlePaymentError = (errorMessage: string) => {
    setError(errorMessage);
    toast({
      title: 'Payment Failed',
      description: errorMessage,
      variant: 'destructive',
    });
  };

  const renderPaymentForm = () => {
    if (!selectedGateway || !selectedMethod) {
      return null;
    }

    try {
      return PaymentGatewayAdapter.getGatewayComponent(selectedGateway, {
        orderId,
        amount,
        currency,
        customerEmail: userProfile?.email,
        customerName: profileName || order.shippingAddress?.contactName,
        customerPhone: userProfile?.phone || order.shippingAddress?.phone,
        paymentMethod: selectedMethod,
        onSuccess: handlePaymentSuccess,
        onError: handlePaymentError,
      });
    } catch (error) {
      return (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            {error instanceof Error ? error.message : 'Payment gateway not supported'}
          </AlertDescription>
        </Alert>
      );
    }
  };

  return (
    <div className="container max-w-4xl py-8">
      <div className="mb-6">
        <Button
          variant="ghost"
          onClick={() => navigate(`/orders/${orderId}`)}
          className="mb-4"
        >
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Order
        </Button>
        <h1 className="text-3xl font-bold">Complete Payment</h1>
        <p className="text-muted-foreground mt-2">
          Order #{orderId} • {formatPrice(amount, currency)}
        </p>
      </div>

      {error && (
        <Alert variant="destructive" className="mb-6">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div className="grid gap-6 md:grid-cols-2">
        {/* Left Column - Payment Selection */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Order Summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Subtotal</span>
                  <span>{formatPrice(order.subTotal || 0, currency)}</span>
                </div>
                {order.shippingAmount > 0 && (
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Shipping</span>
                    <span>{formatPrice(order.shippingAmount, currency)}</span>
                  </div>
                )}
                {order.taxAmount > 0 && (
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Tax</span>
                    <span>{formatPrice(order.taxAmount, currency)}</span>
                  </div>
                )}
                {order.discountAmount > 0 && (
                  <div className="flex justify-between text-green-600">
                    <span>Discount</span>
                    <span>-{formatPrice(order.discountAmount, currency)}</span>
                  </div>
                )}
                <div className="flex justify-between font-bold text-lg pt-2 border-t">
                  <span>Total</span>
                  <span>{formatPrice(amount, currency)}</span>
                </div>
              </div>
            </CardContent>
          </Card>

          <PaymentMethodSelector
            selectedGateway={selectedGateway}
            selectedMethod={selectedMethod}
            onGatewaySelect={setSelectedGateway}
            onMethodSelect={setSelectedMethod}
          />
        </div>

        {/* Right Column - Payment Form */}
        <div className="space-y-6">
          {selectedGateway && selectedMethod ? (
            <PaymentErrorBoundary>
              <Card>
                <CardHeader>
                  <CardTitle>Payment Details</CardTitle>
                  <CardDescription>
                    Complete your payment securely. Your information is encrypted and secure.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2 text-sm text-muted-foreground mb-4">
                    <Lock className="h-4 w-4" />
                    <span>Secure payment processing</span>
                  </div>
                  {renderPaymentForm()}
                </CardContent>
              </Card>
            </PaymentErrorBoundary>
          ) : (
            <Card>
              <CardContent className="py-12 text-center">
                <p className="text-muted-foreground">
                  Please select a payment method to continue
                </p>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}

export default function PaymentPageWrapper() {
  return (
    <ProtectedRoute>
      <PaymentPage />
    </ProtectedRoute>
  );
}

