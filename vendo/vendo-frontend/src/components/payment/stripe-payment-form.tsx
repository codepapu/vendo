import { useState, useEffect, useRef } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2, CreditCard, Lock, AlertCircle } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { ProcessPaymentRequest, PaymentResponse } from '@/types';
import { paymentApi } from '@/services/payment-api';

interface StripePaymentFormProps {
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
    Stripe: any;
  }
}

export function StripePaymentForm({
  orderId,
  amount,
  currency,
  customerEmail,
  customerName,
  onSuccess,
  onError,
}: StripePaymentFormProps) {
  const [loading, setLoading] = useState(false);
  const [stripeLoaded, setStripeLoaded] = useState(false);
  const [stripe, setStripe] = useState<any>(null);
  const [elements, setElements] = useState<any>(null);
  const [cardElement, setCardElement] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);
  const [clientSecret, setClientSecret] = useState<string | null>(null);
  const cardElementRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // Load Stripe.js
    const script = document.createElement('script');
    script.src = 'https://js.stripe.com/v3/';
    script.async = true;
    script.onload = () => {
      const stripeKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY || '';
      if (stripeKey && window.Stripe) {
        const stripeInstance = window.Stripe(stripeKey);
        setStripe(stripeInstance);
        setStripeLoaded(true);
      } else {
        console.warn('Stripe key not found, using fallback mode');
        setStripeLoaded(true);
      }
    };
    script.onerror = () => {
      console.error('Failed to load Stripe SDK');
      setStripeLoaded(true); // Still allow form to work in fallback mode
    };
    document.body.appendChild(script);

    return () => {
      if (document.body.contains(script)) {
        document.body.removeChild(script);
      }
    };
  }, []);

  useEffect(() => {
    if (stripe && cardElementRef.current && !cardElement) {
      const elementsInstance = stripe.elements({
        appearance: {
          theme: 'stripe',
          variables: {
            colorPrimary: '#6366f1',
            colorBackground: '#ffffff',
            colorText: '#1f2937',
            colorDanger: '#ef4444',
            fontFamily: 'system-ui, sans-serif',
            spacingUnit: '4px',
            borderRadius: '8px',
          },
        },
      });
      setElements(elementsInstance);

      const card = elementsInstance.create('card', {
        style: {
          base: {
            fontSize: '16px',
            color: '#1f2937',
            '::placeholder': {
              color: '#9ca3af',
            },
          },
        },
      });
      setCardElement(card);
      card.mount(cardElementRef.current);
      card.on('change', (event: any) => {
        if (event.error) {
          setError(event.error.message);
        } else {
          setError(null);
        }
      });
    }

    return () => {
      if (cardElement) {
        cardElement.destroy();
      }
    };
  }, [stripe, cardElement]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // First, create payment intent on backend
      const paymentData: ProcessPaymentRequest = {
        orderId,
        userId: Number(localStorage.getItem('userId') || '0'),
        amount,
        currency,
        gateway: 'STRIPE',
        paymentMethod: 'CREDIT_CARD',
        customerEmail,
        customerName,
        description: `Payment for order #${orderId}`,
      };

      // Process payment - backend will create PaymentIntent
      const payment = await paymentApi.processPayment(paymentData);

      // If we have Stripe Elements and client secret, use it
      if (stripe && cardElement && payment.gatewayData?.client_secret) {
        const { error: confirmError, paymentIntent } = await stripe.confirmCardPayment(
          payment.gatewayData.client_secret,
          {
            payment_method: {
              card: cardElement,
              billing_details: {
                name: customerName || '',
                email: customerEmail || '',
              },
            },
          }
        );

        if (confirmError) {
          throw new Error(confirmError.message);
        }

        if (paymentIntent && paymentIntent.status === 'succeeded') {
          // Fetch updated payment status
          const updatedPayment = await paymentApi.getPaymentByChargeId(payment.chargeId || payment.transactionId || '');
          onSuccess(updatedPayment);
        } else {
          onSuccess(payment);
        }
      } else {
        // Fallback: use backend processing
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
          <CreditCard className="h-5 w-5" />
          Card Details
        </CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {stripeLoaded && stripe ? (
            <>
              <div className="space-y-2">
                <Label>Card Information</Label>
                <div
                  ref={cardElementRef}
                  className="p-3 border border-input rounded-md bg-background"
                  id="stripe-card-element"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="cardholderName">Cardholder Name</Label>
                <Input
                  id="cardholderName"
                  defaultValue={customerName || ''}
                  placeholder="John Doe"
                  required
                  disabled={loading}
                />
              </div>
            </>
          ) : (
            <>
              <div className="space-y-2">
                <Label htmlFor="cardholderName">Cardholder Name</Label>
                <Input
                  id="cardholderName"
                  defaultValue={customerName || ''}
                  placeholder="John Doe"
                  required
                  disabled={loading}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="cardNumber">Card Number</Label>
                <Input
                  id="cardNumber"
                  placeholder="1234 5678 9012 3456"
                  maxLength={19}
                  required
                  disabled={loading}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="expiryDate">Expiry Date</Label>
                  <Input
                    id="expiryDate"
                    placeholder="MM/YY"
                    maxLength={5}
                    required
                    disabled={loading}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="cvv">CVV</Label>
                  <Input
                    id="cvv"
                    type="password"
                    placeholder="123"
                    maxLength={4}
                    required
                    disabled={loading}
                  />
                </div>
              </div>
            </>
          )}

          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Lock className="h-4 w-4" />
            <span>Your payment information is secure and encrypted</span>
          </div>

          <Button type="submit" className="w-full" disabled={loading || !stripeLoaded}>
            {loading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Processing...
              </>
            ) : (
              <>
                Pay {new Intl.NumberFormat('en-US', {
                  style: 'currency',
                  currency: currency,
                }).format(amount)}
              </>
            )}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

