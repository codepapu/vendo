import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useToast } from '@/hooks/use-toast';
import { returnApi } from '@/services/return-api';
import { orderApi } from '@/services/order-api';
import { shippingApi } from '@/services/shipping-api';

export default function ReturnsPage() {
  const navigate = useNavigate();
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [selectedOrder, setSelectedOrder] = useState<number | null>(null);
  const [selectedItems, setSelectedItems] = useState<number[]>([]);
  const [reason, setReason] = useState('');
  const [returnRequest, setReturnRequest] = useState<any>(null);

  const { data: orders } = useQuery({
    queryKey: ['user-orders'],
    queryFn: () => orderApi.getOrders(),
  });

  const createReturnMutation = useMutation({
    mutationFn: (data: any) => returnApi.createReturnRequest(data),
    onSuccess: (data) => {
      setReturnRequest(data);
      toast({
        title: 'Success',
        description: 'Return request created successfully',
      });
      queryClient.invalidateQueries({ queryKey: ['returns'] });
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.response?.data?.message || 'Failed to create return request',
        variant: 'destructive',
      });
    },
  });

  const generateLabelMutation = useMutation({
    mutationFn: (returnId: number) => {
      // Generate return label via shipping service
      return shippingApi.createShipmentLabel({
        orderId: selectedOrder!,
        shippingMethodId: 1, // Default
        carrier: 'DEFAULT',
      });
    },
    onSuccess: (data) => {
      toast({
        title: 'Success',
        description: 'Return label generated',
      });
    },
  });

  const handleSubmit = () => {
    if (!selectedOrder || selectedItems.length === 0 || !reason) {
      toast({
        title: 'Error',
        description: 'Please fill all required fields',
        variant: 'destructive',
      });
      return;
    }

    createReturnMutation.mutate({
      orderId: selectedOrder,
      reason,
      items: selectedItems.map(itemId => ({
        orderItemId: itemId,
        quantity: 1,
      })),
    });
  };

  const order = orders?.content.find(o => o.id === selectedOrder);

  return (
    <div className="container mx-auto py-8 max-w-4xl">
      <h1 className="text-3xl font-bold mb-6">Request Return</h1>

      <Card>
        <CardHeader>
          <CardTitle>Select Order</CardTitle>
        </CardHeader>
        <CardContent>
          <Select value={selectedOrder?.toString()} onValueChange={(v) => setSelectedOrder(parseInt(v))}>
            <SelectTrigger>
              <SelectValue placeholder="Select an order" />
            </SelectTrigger>
            <SelectContent>
              {orders?.content.map((o) => (
                <SelectItem key={o.id} value={o.id.toString()}>
                  {o.orderNumber} - ${o.totalAmount.toFixed(2)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </CardContent>
      </Card>

      {order && (
        <Card className="mt-4">
          <CardHeader>
            <CardTitle>Select Items to Return</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {order.items?.map((item) => (
                <div key={item.id} className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    checked={selectedItems.includes(item.id)}
                    onChange={(e) => {
                      if (e.target.checked) {
                        setSelectedItems([...selectedItems, item.id]);
                      } else {
                        setSelectedItems(selectedItems.filter(id => id !== item.id));
                      }
                    }}
                  />
                  <Label>{item.productName} x {item.quantity}</Label>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      <Card className="mt-4">
        <CardHeader>
          <CardTitle>Return Reason</CardTitle>
        </CardHeader>
        <CardContent>
          <Textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="Please provide a reason for the return"
            rows={4}
          />
        </CardContent>
      </Card>

      {!returnRequest ? (
        <Button
          onClick={handleSubmit}
          disabled={createReturnMutation.isPending}
          className="mt-4 w-full"
        >
          {createReturnMutation.isPending ? 'Submitting...' : 'Submit Return Request'}
        </Button>
      ) : (
        <Card className="mt-4">
          <CardHeader>
            <CardTitle>Return Request Created</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div>
                <p className="font-medium">Return Code: {returnRequest.code}</p>
                <p className="text-sm text-muted-foreground">Status: {returnRequest.status}</p>
              </div>
              <div className="flex gap-2">
                <Button
                  onClick={() => generateLabelMutation.mutate(returnRequest.id)}
                  disabled={generateLabelMutation.isPending}
                >
                  {generateLabelMutation.isPending ? 'Generating...' : 'Generate Return Label'}
                </Button>
                <Button variant="outline" onClick={() => navigate('/returns/history')}>
                  View Return History
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

