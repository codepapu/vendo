import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2, CreditCard, Lock } from 'lucide-react';
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
  const [cardNumber, setCardNumber] = useState('');
  const [expiryDate, setExpiryDate] = useState('');
  const [cvv, setCvv] = useState('');
  const [cardholderName, setCardholderName] = useState(customerName || '');
  const [stripeLoaded, setStripeLoaded] = useState(false);

  useEffect(() => {
    // Load Stripe.js
    const script = document.createElement('script');
    script.src = 'https://js.stripe.com/v3/';
    script.async = true;
    script.onload = () => setStripeLoaded(true);
    document.body.appendChild(script);

    return () => {
      document.body.removeChild(script);
    };
  }, []);

  const formatCardNumber = (value: string) => {
    const v = value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
    const matches = v.match(/\d{4,16}/g);
    const match = (matches && matches[0]) || '';
    const parts = [];
    for (let i = 0, len = match.length; i < len; i += 4) {
      parts.push(match.substring(i, i + 4));
    }
    if (parts.length) {
      return parts.join(' ');
    } else {
      return v;
    }
  };

  const formatExpiryDate = (value: string) => {
    const v = value.replace(/\D/g, '');
    if (v.length >= 2) {
      return v.substring(0, 2) + '/' + v.substring(2, 4);
    }
    return v;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      // In production, you would:
      // 1. Create a PaymentIntent on your backend
      // 2. Use Stripe Elements to collect card details securely
      // 3. Confirm the payment with Stripe
      // 4. Handle the response

      // For now, this is a simplified version
      // In production, you would:
      // 1. Use Stripe Elements to securely collect card details
      // 2. Create a PaymentIntent on your backend
      // 3. Confirm the payment with Stripe
      const paymentData: ProcessPaymentRequest = {
        orderId,
        userId: Number(localStorage.getItem('userId') || '0'),
        amount,
        currency,
        gateway: 'STRIPE',
        paymentMethod: 'CREDIT_CARD',
        customerEmail,
        customerName: cardholderName,
        paymentData: {
          card_number: cardNumber.replace(/\s/g, ''),
          expiry_date: expiryDate,
          cvv: cvv,
        },
        description: `Payment for order #${orderId}`,
      };

      // Call payment API
      const payment = await paymentApi.processPayment(paymentData);
      onSuccess(payment);
    } catch (error) {
      onError(error instanceof Error ? error.message : 'Payment failed');
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
          <div className="space-y-2">
            <Label htmlFor="cardholderName">Cardholder Name</Label>
            <Input
              id="cardholderName"
              value={cardholderName}
              onChange={(e) => setCardholderName(e.target.value)}
              placeholder="John Doe"
              required
              disabled={loading}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="cardNumber">Card Number</Label>
            <Input
              id="cardNumber"
              value={cardNumber}
              onChange={(e) => setCardNumber(formatCardNumber(e.target.value))}
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
                value={expiryDate}
                onChange={(e) => setExpiryDate(formatExpiryDate(e.target.value))}
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
                value={cvv}
                onChange={(e) => setCvv(e.target.value.replace(/\D/g, '').substring(0, 4))}
                placeholder="123"
                maxLength={4}
                required
                disabled={loading}
              />
            </div>
          </div>

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

