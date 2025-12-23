import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2, Smartphone, QrCode } from 'lucide-react';
import { ProcessPaymentRequest, PaymentResponse, PaymentGateway } from '@/types';
import { paymentApi } from '@/services/payment-api';

interface UPIPaymentFormProps {
  orderId: number;
  amount: number;
  currency: string;
  gateway: PaymentGateway; // GOOGLE_PAY, PHONEPE, or UPI
  customerEmail?: string;
  customerName?: string;
  customerPhone?: string;
  onSuccess: (payment: PaymentResponse) => void;
  onError: (error: string) => void;
}

export function UPIPaymentForm({
  orderId,
  amount,
  currency,
  gateway,
  customerEmail,
  customerName,
  customerPhone,
  onSuccess,
  onError,
}: UPIPaymentFormProps) {
  const [loading, setLoading] = useState(false);
  const [upiId, setUpiId] = useState('');
  const [showQR, setShowQR] = useState(false);
  const [qrCode, setQrCode] = useState('');

  const handleUPIPayment = async () => {
    setLoading(true);

    try {
      const paymentData: ProcessPaymentRequest = {
        orderId,
        userId: Number(localStorage.getItem('userId') || '0'),
        amount,
        currency,
        gateway,
        paymentMethod: gateway === 'GOOGLE_PAY' ? 'GOOGLE_PAY' : gateway === 'PHONEPE' ? 'PHONEPE' : 'UPI',
        customerEmail,
        customerName,
        customerPhone,
        paymentData: upiId ? { upi_id: upiId, vpa: upiId } : undefined,
        returnUrl: `${window.location.origin}/payment/success`,
        cancelUrl: `${window.location.origin}/payment/cancel`,
        description: `Payment for order #${orderId}`,
      };

      const payment = await paymentApi.processPayment(paymentData);

      if (payment.redirectUrl) {
        // Redirect to payment gateway
        window.location.href = payment.redirectUrl;
      } else if (payment.gatewayData?.qr_code) {
        // Show QR code
        setQrCode(payment.gatewayData.qr_code as string);
        setShowQR(true);
      } else {
        onSuccess(payment);
      }
    } catch (error) {
      onError(error instanceof Error ? error.message : 'Payment failed');
    } finally {
      setLoading(false);
    }
  };

  const gatewayNames: Record<PaymentGateway, string> = {
    GOOGLE_PAY: 'Google Pay',
    PHONEPE: 'PhonePe',
    UPI: 'UPI',
    STRIPE: 'Stripe',
    PAYPAL: 'PayPal',
    RAZORPAY: 'Razorpay',
    PAYSTACK: 'Paystack',
    SSLCOMMERZ: 'SSLCommerz',
    MOLLIE: 'Mollie',
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Smartphone className="h-5 w-5" />
          {gatewayNames[gateway]} Payment
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {!showQR ? (
          <>
            <div className="space-y-2">
              <Label htmlFor="upiId">UPI ID / VPA (Optional)</Label>
              <Input
                id="upiId"
                value={upiId}
                onChange={(e) => setUpiId(e.target.value)}
                placeholder="yourname@paytm or yourname@phonepe"
                disabled={loading}
              />
              <p className="text-sm text-muted-foreground">
                Enter your UPI ID to pay directly, or leave blank to generate QR code
              </p>
            </div>

            <Button
              onClick={handleUPIPayment}
              disabled={loading}
              className="w-full"
              size="lg"
            >
              {loading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Processing...
                </>
              ) : (
                <>
                  Pay {new Intl.NumberFormat('en-IN', {
                    style: 'currency',
                    currency: currency,
                  }).format(amount)}
                </>
              )}
            </Button>
          </>
        ) : (
          <div className="space-y-4 text-center">
            <div className="flex justify-center">
              <QrCode className="h-32 w-32 text-primary" />
            </div>
            <div>
              <p className="text-sm font-medium mb-2">Scan QR Code to Pay</p>
              <p className="text-xs text-muted-foreground mb-4">
                Open your UPI app and scan this QR code to complete payment
              </p>
              {qrCode && (
                <div className="bg-white p-4 rounded-lg inline-block">
                  <img src={qrCode} alt="UPI QR Code" className="w-48 h-48" />
                </div>
              )}
            </div>
            <Button
              onClick={() => {
                setShowQR(false);
                setQrCode('');
              }}
              variant="outline"
              className="w-full"
            >
              Back
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

