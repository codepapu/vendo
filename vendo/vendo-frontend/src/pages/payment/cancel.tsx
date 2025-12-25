import { useNavigate, useSearchParams } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { XCircle, ArrowLeft, RefreshCw } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';

export function PaymentCancelPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const orderId = searchParams.get('orderId');

  const handleRetry = () => {
    if (orderId) {
      navigate(`/payment?orderId=${orderId}`);
    } else {
      navigate('/orders');
    }
  };

  return (
    <div className="container max-w-2xl py-8">
      <Card>
        <CardHeader>
          <div className="flex flex-col items-center text-center space-y-4">
            <XCircle className="h-12 w-12 text-yellow-500" />
            <CardTitle className="text-2xl text-yellow-600">
              Payment Cancelled
            </CardTitle>
            <p className="text-muted-foreground">
              Your payment was cancelled. No charges were made to your account.
            </p>
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          <Alert>
            <AlertDescription>
              You can retry the payment or return to your order to complete it later.
            </AlertDescription>
          </Alert>

          <div className="flex flex-col sm:flex-row gap-3">
            <Button onClick={handleRetry} className="flex-1">
              <RefreshCw className="mr-2 h-4 w-4" />
              Retry Payment
            </Button>
            {orderId && (
              <Button
                onClick={() => navigate(`/orders/${orderId}`)}
                variant="outline"
                className="flex-1"
              >
                View Order
              </Button>
            )}
            <Button
              onClick={() => navigate('/orders')}
              variant="outline"
              className="flex-1"
            >
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Orders
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

