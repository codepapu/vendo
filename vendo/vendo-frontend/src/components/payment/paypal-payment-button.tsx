import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';
import { ProcessPaymentRequest, PaymentResponse } from '@/types';
import { paymentApi } from '@/services/payment-api';

interface PayPalPaymentButtonProps {
  orderId: number;
  amount: number;
  currency: string;
  customerEmail?: string;
  onSuccess: (payment: PaymentResponse) => void;
  onError: (error: string) => void;
}

export function PayPalPaymentButton({
  orderId,
  amount,
  currency,
  customerEmail,
  onSuccess,
  onError,
}: PayPalPaymentButtonProps) {
  const [loading, setLoading] = useState(false);
  const [paypalLoaded, setPaypalLoaded] = useState(false);

  useEffect(() => {
    // Load PayPal SDK
    const script = document.createElement('script');
    script.src = `https://www.paypal.com/sdk/js?client-id=${import.meta.env.VITE_PAYPAL_CLIENT_ID || ''}&currency=${currency}`;
    script.async = true;
    script.onload = () => setPaypalLoaded(true);
    script.onerror = () => {
      console.error('Failed to load PayPal SDK');
      setPaypalLoaded(false);
    };
    document.body.appendChild(script);

    return () => {
      if (document.body.contains(script)) {
        document.body.removeChild(script);
      }
    };
  }, [currency]);

  const handlePayPalPayment = async () => {
    setLoading(true);

    try {
      const paymentData: ProcessPaymentRequest = {
        orderId,
        userId: Number(localStorage.getItem('userId') || '0'),
        amount,
        currency,
        gateway: 'PAYPAL',
        paymentMethod: 'PAYPAL',
        customerEmail,
        returnUrl: `${window.location.origin}/payment/success`,
        cancelUrl: `${window.location.origin}/payment/cancel`,
        description: `Payment for order #${orderId}`,
      };

      const payment = await paymentApi.processPayment(paymentData);

      if (payment.redirectUrl) {
        // Redirect to PayPal for approval
        window.location.href = payment.redirectUrl;
      } else {
        onSuccess(payment);
      }
    } catch (error) {
      onError(error instanceof Error ? error.message : 'Payment failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Button
      onClick={handlePayPalPayment}
      disabled={loading || !paypalLoaded}
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
  );
}

