import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { returnApi } from '@/services/return-api';
import { Button } from '@/components/ui/button';
import { useNavigate } from 'react-router-dom';

export default function ReturnsHistory() {
  const navigate = useNavigate();

  const { data: returns, isLoading } = useQuery({
    queryKey: ['return-history'],
    queryFn: () => returnApi.getReturnHistory(),
  });

  if (isLoading) {
    return <div className="container mx-auto py-8">Loading...</div>;
  }

  return (
    <div className="container mx-auto py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Return History</h1>
        <Button onClick={() => navigate('/returns')}>Request New Return</Button>
      </div>

      <div className="space-y-4">
        {!returns || returns.length === 0 ? (
          <Card>
            <CardContent className="py-8 text-center">
              <p className="text-muted-foreground">No return requests found</p>
            </CardContent>
          </Card>
        ) : (
          returns.map((returnRequest) => (
            <Card key={returnRequest.id}>
              <CardHeader>
                <div className="flex justify-between items-start">
                  <div>
                    <CardTitle>Return #{returnRequest.code}</CardTitle>
                    <p className="text-sm text-muted-foreground">
                      Order: {returnRequest.orderId} | Created: {new Date(returnRequest.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                  <span
                    className={`px-3 py-1 rounded-full text-sm ${
                      returnRequest.status === 'APPROVED' || returnRequest.status === 'COMPLETED'
                        ? 'bg-green-100 text-green-800'
                        : returnRequest.status === 'REJECTED'
                        ? 'bg-red-100 text-red-800'
                        : 'bg-yellow-100 text-yellow-800'
                    }`}
                  >
                    {returnRequest.status}
                  </span>
                </div>
              </CardHeader>
              <CardContent>
                <p className="mb-2">Reason: {returnRequest.reason}</p>
                <p className="mb-2">Refund Amount: ${returnRequest.refundAmount?.toFixed(2)}</p>
                <p className="mb-2">Refund Status: {returnRequest.refundStatus}</p>
                
                {returnRequest.items && returnRequest.items.length > 0 && (
                  <div className="mb-4">
                    <p className="font-medium mb-2">Items:</p>
                    {returnRequest.items.map((item) => (
                      <p key={item.id} className="text-sm">
                        {item.productName} x {item.quantity} - ${item.refundAmount.toFixed(2)}
                      </p>
                    ))}
                  </div>
                )}
                
                {returnRequest.history && returnRequest.history.length > 0 && (
                  <div>
                    <p className="font-medium mb-2">Status History:</p>
                    {returnRequest.history.map((h) => (
                      <p key={h.id} className="text-sm text-muted-foreground">
                        {h.status}: {h.description} ({new Date(h.createdAt).toLocaleDateString()})
                      </p>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
  );
}

