import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';
import { ProcessPaymentRequest, PaymentResponse } from '@/types';
import { paymentApi } from '@/services/payment-api';

declare global {
  interface Window {
    Razorpay: any;
  }
}

interface RazorpayCheckoutProps {
  orderId: number;
  amount: number;
  currency: string;
  customerEmail?: string;
  customerName?: string;
  customerPhone?: string;
  paymentMethod: 'CREDIT_CARD' | 'DEBIT_CARD' | 'NET_BANKING' | 'WALLET' | 'UPI';
  onSuccess: (payment: PaymentResponse) => void;
  onError: (error: string) => void;
}

export function RazorpayCheckout({
  orderId,
  amount,
  currency,
  customerEmail,
  customerName,
  customerPhone,
  paymentMethod,
  onSuccess,
  onError,
}: RazorpayCheckoutProps) {
  const [loading, setLoading] = useState(false);
  const [razorpayLoaded, setRazorpayLoaded] = useState(false);

  useEffect(() => {
    // Load Razorpay SDK
    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.async = true;
    script.onload = () => setRazorpayLoaded(true);
    script.onerror = () => {
      console.error('Failed to load Razorpay SDK');
      setRazorpayLoaded(false);
    };
    document.body.appendChild(script);

    return () => {
      if (document.body.contains(script)) {
        document.body.removeChild(script);
      }
    };
  }, []);

  const handleRazorpayPayment = async () => {
    if (!window.Razorpay) {
      onError('Razorpay SDK not loaded');
      return;
    }

    setLoading(true);

    try {
      // First, create payment order on backend
      const paymentData: ProcessPaymentRequest = {
        orderId,
        userId: Number(localStorage.getItem('userId') || '0'),
        amount,
        currency,
        gateway: 'RAZORPAY',
        paymentMethod,
        customerEmail,
        customerName,
        customerPhone,
        returnUrl: `${window.location.origin}/payment/success`,
        cancelUrl: `${window.location.origin}/payment/cancel`,
        description: `Payment for order #${orderId}`,
      };

      const payment = await paymentApi.processPayment(paymentData);

      if (!payment.chargeId || !payment.gatewayData) {
        throw new Error('Invalid payment response');
      }

      const razorpayKeyId = payment.gatewayData.key_id as string;
      const razorpayOrderId = payment.gatewayData.razorpay_order_id as string;

      const options = {
        key: razorpayKeyId,
        amount: amount * 100, // Convert to paise
        currency: currency,
        name: 'Vendo',
        description: `Payment for order #${orderId}`,
        order_id: razorpayOrderId,
        handler: async function (response: any) {
          try {
            // Verify payment on backend
            const verifiedPayment = await paymentApi.getPaymentByChargeId(response.razorpay_payment_id);
            onSuccess(verifiedPayment);
          } catch (error) {
            onError(error instanceof Error ? error.message : 'Payment verification failed');
          }
        },
        prefill: {
          name: customerName || '',
          email: customerEmail || '',
          contact: customerPhone || '',
        },
        notes: {
          order_id: orderId.toString(),
        },
        theme: {
          color: '#6366f1',
        },
        modal: {
          ondismiss: function () {
            setLoading(false);
          },
        },
      };

      const razorpay = new window.Razorpay(options);
      razorpay.open();
    } catch (error) {
      onError(error instanceof Error ? error.message : 'Payment failed');
      setLoading(false);
    }
  };

  return (
    <Button
      onClick={handleRazorpayPayment}
      disabled={loading || !razorpayLoaded}
      className="w-full bg-[#0f3460] hover:bg-[#0d2d4f] text-white"
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
            <path d="M12 0C5.373 0 0 5.373 0 12s5.373 12 12 12 12-5.373 12-12S18.627 0 12 0zm0 2.4c5.302 0 9.6 4.298 9.6 9.6s-4.298 9.6-9.6 9.6S2.4 17.302 2.4 12 6.698 2.4 12 2.4z" />
          </svg>
          Pay with Razorpay
        </>
      )}
    </Button>
  );
}

