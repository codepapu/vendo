import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';
import { PaymentConfirmation } from '@/components/payment/payment-confirmation';
import { paymentApi } from '@/services/payment-api';
import { PaymentResponse } from '@/types';

export function PaymentSuccessPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const orderId = searchParams.get('orderId');
  const chargeId = searchParams.get('chargeId');
  const paymentId = searchParams.get('paymentId');

  const [payment, setPayment] = useState<PaymentResponse | null>(null);
  const [loading, setLoading] = useState(true);

  // Fetch payment details
  const { data: paymentData, isLoading } = useQuery({
    queryKey: ['payment', chargeId || paymentId],
    queryFn: async () => {
      if (chargeId) {
        return paymentApi.getPaymentByChargeId(chargeId);
      } else if (paymentId) {
        return paymentApi.getPaymentById(Number(paymentId));
      }
      throw new Error('Payment ID or Charge ID is required');
    },
    enabled: !!(chargeId || paymentId),
    retry: 2,
  });

  useEffect(() => {
    if (paymentData) {
      setPayment(paymentData);
      setLoading(false);
    } else if (!isLoading && !chargeId && !paymentId) {
      // If no payment ID provided, try to get from order
      if (orderId) {
        paymentApi
          .getPaymentsByOrderId(Number(orderId))
          .then((payments) => {
            if (payments.length > 0) {
              setPayment(payments[0]);
            }
            setLoading(false);
          })
          .catch(() => {
            setLoading(false);
          });
      } else {
        setLoading(false);
      }
    }
  }, [paymentData, isLoading, chargeId, paymentId, orderId]);

  if (loading) {
    return (
      <div className="container max-w-2xl py-8">
        <Card>
          <CardContent className="flex items-center justify-center py-8">
            <Loader2 className="h-6 w-6 animate-spin text-primary" />
            <span className="ml-2">Loading payment details...</span>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!payment) {
    return (
      <div className="container max-w-2xl py-8">
        <Card>
          <CardContent className="py-8 text-center">
            <p className="text-muted-foreground mb-4">Payment details not found</p>
            <Button onClick={() => navigate('/orders')}>View Orders</Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return <PaymentConfirmation payment={payment} />;
}

