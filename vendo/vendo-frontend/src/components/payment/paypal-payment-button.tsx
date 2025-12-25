import { useState, useEffect, useRef } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Loader2, Wallet, AlertCircle } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { ProcessPaymentRequest, PaymentResponse } from '@/types';
import { paymentApi } from '@/services/payment-api';

interface PayPalPaymentButtonProps {
  orderId: number;
  amount: number;
  currency: string;
  customerEmail?: string;
  customerName?: string;
  onSuccess: (payment: PaymentResponse) => void;
  onError: (error: string) => void;
}

declare global {
  interface Window {
    paypal: any;
  }
}

export function PayPalPaymentButton({
  orderId,
  amount,
  currency,
  customerEmail,
  customerName,
  onSuccess,
  onError,
}: PayPalPaymentButtonProps) {
  const [loading, setLoading] = useState(false);
  const [paypalLoaded, setPaypalLoaded] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const paypalButtonContainerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const clientId = import.meta.env.VITE_PAYPAL_CLIENT_ID || '';
    if (!clientId) {
      console.warn('PayPal client ID not found');
      setPaypalLoaded(true); // Allow fallback mode
      return;
    }

    // Load PayPal SDK
    const script = document.createElement('script');
    script.src = `https://www.paypal.com/sdk/js?client-id=${clientId}&currency=${currency}&intent=capture`;
    script.async = true;
    script.onload = () => {
      if (window.paypal && paypalButtonContainerRef.current) {
        setPaypalLoaded(true);
        renderPayPalButtons();
      }
    };
    script.onerror = () => {
      console.error('Failed to load PayPal SDK');
      setPaypalLoaded(true); // Allow fallback mode
    };
    document.body.appendChild(script);

    return () => {
      if (document.body.contains(script)) {
        document.body.removeChild(script);
      }
    };
  }, [currency, orderId, amount]);

  const renderPayPalButtons = () => {
    if (!window.paypal || !paypalButtonContainerRef.current) return;

    // Clear existing buttons
    paypalButtonContainerRef.current.innerHTML = '';

    window.paypal
      .Buttons({
        style: {
          layout: 'vertical',
          color: 'blue',
          shape: 'rect',
          label: 'paypal',
        },
        createOrder: async (data: any, actions: any) => {
          try {
            // Create payment on backend first
            const paymentData: ProcessPaymentRequest = {
              orderId,
              userId: Number(localStorage.getItem('userId') || '0'),
              amount,
              currency,
              gateway: 'PAYPAL',
              paymentMethod: 'PAYPAL',
              customerEmail,
              customerName,
              returnUrl: `${window.location.origin}/payment/success`,
              cancelUrl: `${window.location.origin}/payment/cancel`,
              description: `Payment for order #${orderId}`,
            };

            const payment = await paymentApi.processPayment(paymentData);

            // Return PayPal order ID from gateway data
            if (payment.gatewayData?.paypal_order_id) {
              return payment.gatewayData.paypal_order_id as string;
            }

            // If backend returns redirect URL, we'll handle it differently
            if (payment.redirectUrl) {
              throw new Error('Redirect-based payment not supported with buttons');
            }

            throw new Error('Invalid PayPal order response');
          } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to create PayPal order';
            setError(errorMessage);
            onError(errorMessage);
            throw err;
          }
        },
        onApprove: async (data: any, actions: any) => {
          try {
            setLoading(true);
            // Capture the payment
            const order = await actions.order.capture();

            // Verify payment on backend
            if (order.id && order.purchase_units?.[0]?.payments?.captures?.[0]?.id) {
              const captureId = order.purchase_units[0].payments.captures[0].id;
              const verifiedPayment = await paymentApi.getPaymentByChargeId(captureId);
              onSuccess(verifiedPayment);
            } else {
              throw new Error('Invalid PayPal order response');
            }
          } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Payment verification failed';
            setError(errorMessage);
            onError(errorMessage);
          } finally {
            setLoading(false);
          }
        },
        onError: (err: any) => {
          const errorMessage = err?.message || 'PayPal payment failed';
          setError(errorMessage);
          onError(errorMessage);
        },
        onCancel: () => {
          onError('Payment cancelled by user');
        },
      })
      .render(paypalButtonContainerRef.current);
  };

  const handleFallbackPayment = async () => {
    setLoading(true);
    setError(null);

    try {
      const paymentData: ProcessPaymentRequest = {
        orderId,
        userId: Number(localStorage.getItem('userId') || '0'),
        amount,
        currency,
        gateway: 'PAYPAL',
        paymentMethod: 'PAYPAL',
        customerEmail,
        customerName,
        returnUrl: `${window.location.origin}/payment/success`,
        cancelUrl: `${window.location.origin}/payment/cancel`,
        description: `Payment for order #${orderId}`,
      };

      const payment = await paymentApi.processPayment(paymentData);

      if (payment.redirectUrl) {
        window.location.href = payment.redirectUrl;
      } else {
        onSuccess(payment);
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Payment failed';
      setError(errorMessage);
      onError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Wallet className="h-5 w-5" />
          PayPal Payment
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {error && (
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {paypalLoaded && window.paypal ? (
          <div ref={paypalButtonContainerRef} className="min-h-[50px]" />
        ) : (
          <Button
            onClick={handleFallbackPayment}
            disabled={loading}
            className="w-full bg-[#0070ba] hover:bg-[#005ea6] text-white"
            size="lg"
          >
            {loading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Processing...
              </>
            ) : (
              <>
                <svg
                  className="mr-2 h-5 w-5"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <path d="M7.076 21.337H2.47a.641.641 0 0 1-.633-.74L4.944.901C5.026.382 5.474 0 5.998 0h7.46c2.57 0 4.578.543 5.69 1.81 1.174 1.305.968 3.624.859 4.858l-.003.048v.048l-.03 1.138c-.085 3.225-.277 4.13 1.2 5.587.31.303.664.496 1.04.646.39.155.81.232 1.238.232.62 0 1.214-.156 1.728-.46.515-.305.93-.74 1.216-1.26.285-.52.43-1.102.43-1.692 0-.59-.145-1.172-.43-1.692a3.635 3.635 0 0 0-1.216-1.26 3.97 3.97 0 0 0-1.728-.46c-.428 0-.848.077-1.238.232a3.97 3.97 0 0 0-1.04.646c-1.477 1.457-1.285 2.362-1.2 5.587l.03 1.138v.048l.003.048c.109 1.234.315 3.553-.859 4.858-1.112 1.267-3.12 1.81-5.69 1.81H5.998c-.524 0-.972-.382-1.054-.901l-3.107-19.696a.641.641 0 0 1 .633-.74h4.606a.641.641 0 0 1 .633.74L7.076 21.337z" />
                </svg>
                Pay with PayPal
              </>
            )}
          </Button>
        )}
      </CardContent>
    </Card>
  );
}

