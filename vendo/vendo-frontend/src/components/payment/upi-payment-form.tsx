import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2, Smartphone, QrCode, AlertCircle } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { ProcessPaymentRequest, PaymentResponse, PaymentGateway } from '@/types';
import { paymentApi } from '@/services/payment-api';
import { PaymentStatus } from './payment-status';

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

declare global {
  interface Window {
    google?: {
      pay?: {
        api: {
          PaymentsClient: any;
        };
      };
    };
  }
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
  const [error, setError] = useState<string | null>(null);
  const [paymentInitiated, setPaymentInitiated] = useState(false);
  const [chargeId, setChargeId] = useState<string | null>(null);
  const [googlePayAvailable, setGooglePayAvailable] = useState(false);

  useEffect(() => {
    // Check for Google Pay availability
    if (gateway === 'GOOGLE_PAY' && window.google?.pay?.api) {
      const paymentsClient = new window.google.pay.api.PaymentsClient({
        environment: import.meta.env.VITE_GOOGLE_PAY_ENVIRONMENT === 'production' ? 'PRODUCTION' : 'TEST',
      });

      paymentsClient
        .isReadyToPay({
          apiVersion: 2,
          apiVersionMinor: 0,
          allowedPaymentMethods: [
            {
              type: 'CARD',
              parameters: {
                allowedAuthMethods: ['PAN_ONLY', 'CRYPTOGRAM_3DS'],
                allowedCardNetworks: ['MASTERCARD', 'VISA'],
              },
            },
          ],
        })
        .then((response: any) => {
          setGooglePayAvailable(response.result);
        })
        .catch(() => {
          setGooglePayAvailable(false);
        });
    }
  }, [gateway]);

  const handleGooglePay = async () => {
    if (!window.google?.pay?.api) {
      onError('Google Pay is not available');
      return;
    }

    try {
      const paymentsClient = new window.google.pay.api.PaymentsClient({
        environment: import.meta.env.VITE_GOOGLE_PAY_ENVIRONMENT === 'production' ? 'PRODUCTION' : 'TEST',
      });

      const paymentDataRequest = {
        apiVersion: 2,
        apiVersionMinor: 0,
        merchantInfo: {
          merchantId: import.meta.env.VITE_GOOGLE_PAY_MERCHANT_ID || '',
          merchantName: 'Vendo',
        },
        allowedPaymentMethods: [
          {
            type: 'CARD',
            parameters: {
              allowedAuthMethods: ['PAN_ONLY', 'CRYPTOGRAM_3DS'],
              allowedCardNetworks: ['MASTERCARD', 'VISA'],
            },
            tokenizationSpecification: {
              type: 'PAYMENT_GATEWAY',
              parameters: {
                gateway: 'stripe',
                'stripe:version': '2018-10-31',
                'stripe:publishableKey': import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY || '',
              },
            },
          },
        ],
        transactionInfo: {
          totalPriceStatus: 'FINAL',
          totalPriceLabel: 'Total',
          totalPrice: amount.toString(),
          currencyCode: currency,
        },
      };

      paymentsClient
        .loadPaymentData(paymentDataRequest)
        .then(async (paymentData: any) => {
          setLoading(true);
          try {
            const paymentDataRequest: ProcessPaymentRequest = {
              orderId,
              userId: Number(localStorage.getItem('userId') || '0'),
              amount,
              currency,
              gateway: 'GOOGLE_PAY',
              paymentMethod: 'GOOGLE_PAY',
              customerEmail,
              customerName,
              customerPhone,
              paymentData: {
                payment_method_data: JSON.stringify(paymentData),
              },
              returnUrl: `${window.location.origin}/payment/success`,
              cancelUrl: `${window.location.origin}/payment/cancel`,
              description: `Payment for order #${orderId}`,
            };

            const payment = await paymentApi.processPayment(paymentDataRequest);
            setChargeId(payment.chargeId || null);
            setPaymentInitiated(true);

            if (payment.status === 'COMPLETED' || payment.status === 'completed') {
              onSuccess(payment);
            }
          } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Payment failed';
            setError(errorMessage);
            onError(errorMessage);
          } finally {
            setLoading(false);
          }
        })
        .catch((err: any) => {
          if (err.statusCode !== 'CANCELED') {
            const errorMessage = err.message || 'Google Pay payment failed';
            setError(errorMessage);
            onError(errorMessage);
          }
        });
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to initialize Google Pay';
      setError(errorMessage);
      onError(errorMessage);
    }
  };

  const handleUPIPayment = async () => {
    setLoading(true);
    setError(null);

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
      setChargeId(payment.chargeId || null);
      setPaymentInitiated(true);

      if (payment.redirectUrl) {
        // Redirect to payment gateway
        window.location.href = payment.redirectUrl;
      } else if (payment.gatewayData?.qr_code) {
        // Show QR code
        setQrCode(payment.gatewayData.qr_code as string);
        setShowQR(true);
      } else if (payment.status === 'COMPLETED' || payment.status === 'completed') {
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

  // Show payment status if payment was initiated
  if (paymentInitiated && chargeId) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Smartphone className="h-5 w-5" />
            {gatewayNames[gateway]} Payment
          </CardTitle>
        </CardHeader>
        <CardContent>
          <PaymentStatus
            chargeId={chargeId}
            onStatusChange={(payment) => {
              if (payment.status === 'COMPLETED' || payment.status === 'completed') {
                onSuccess(payment);
              } else if (payment.status === 'FAILED' || payment.status === 'failed') {
                onError(payment.errorMessage || 'Payment failed');
              }
            }}
          />
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Smartphone className="h-5 w-5" />
          {gatewayNames[gateway]} Payment
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {error && (
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {!showQR ? (
          <>
            {gateway === 'GOOGLE_PAY' && googlePayAvailable ? (
              <Button
                onClick={handleGooglePay}
                disabled={loading}
                className="w-full bg-[#4285F4] hover:bg-[#357AE8] text-white"
                size="lg"
              >
                {loading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Processing...
                  </>
                ) : (
                  <>
                    <svg className="mr-2 h-5 w-5" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
                      <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
                      <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
                      <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
                    </svg>
                    Pay with Google Pay
                  </>
                )}
              </Button>
            ) : (
              <>
                <div className="space-y-2">
                  <Label htmlFor="upiId">UPI ID / VPA {gateway !== 'UPI' ? '(Optional)' : ''}</Label>
                  <Input
                    id="upiId"
                    value={upiId}
                    onChange={(e) => setUpiId(e.target.value)}
                    placeholder={
                      gateway === 'GOOGLE_PAY'
                        ? 'yourname@okaxis or yourname@paytm'
                        : gateway === 'PHONEPE'
                        ? 'yourname@phonepe or yourname@ybl'
                        : 'yourname@paytm or yourname@phonepe'
                    }
                    disabled={loading}
                  />
                  <p className="text-sm text-muted-foreground">
                    {gateway === 'GOOGLE_PAY' || gateway === 'PHONEPE'
                      ? 'Enter your UPI ID to pay directly, or leave blank to generate QR code'
                      : 'Enter your UPI ID to pay directly, or leave blank to generate QR code'}
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
            )}
          </>
        ) : (
          <div className="space-y-4 text-center">
            <div className="flex justify-center">
              <QrCode className="h-32 w-32 text-primary" />
            </div>
            <div>
              <p className="text-sm font-medium mb-2">Scan QR Code to Pay</p>
              <p className="text-xs text-muted-foreground mb-4">
                Open your {gatewayNames[gateway]} app and scan this QR code to complete payment
              </p>
              {qrCode && (
                <div className="bg-white p-4 rounded-lg inline-block">
                  <img src={qrCode} alt="UPI QR Code" className="w-48 h-48" />
                </div>
              )}
            </div>
            {chargeId && (
              <PaymentStatus
                chargeId={chargeId}
                onStatusChange={(payment) => {
                  if (payment.status === 'COMPLETED' || payment.status === 'completed') {
                    onSuccess(payment);
                  } else if (payment.status === 'FAILED' || payment.status === 'failed') {
                    onError(payment.errorMessage || 'Payment failed');
                  }
                }}
              />
            )}
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

