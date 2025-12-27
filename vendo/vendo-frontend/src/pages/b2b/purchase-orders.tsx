import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { useB2BStore } from '@/stores/b2b-store';
import { b2bApi } from '@/services/b2b-api';
import { orderApi } from '@/services/order-api';
import { useToast } from '@/hooks/use-toast';

export default function PurchaseOrders() {
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [createPOModalOpen, setCreatePOModalOpen] = useState(false);
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
  const { loadPurchaseOrders } = useB2BStore();
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const { data: ordersData, refetch } = useQuery({
    queryKey: ['b2b-purchase-orders', statusFilter],
    queryFn: () => b2bApi.getPurchaseOrders(statusFilter !== 'all' ? statusFilter : undefined),
  });

  const { data: availableOrders } = useQuery({
    queryKey: ['available-orders-for-po'],
    queryFn: () => orderApi.getOrders(),
    enabled: createPOModalOpen,
  });

  const createPOMutation = useMutation({
    mutationFn: (orderId: number) => b2bApi.createPurchaseOrder({ orderId }),
    onSuccess: () => {
      toast({ title: 'Success', description: 'Purchase order created' });
      queryClient.invalidateQueries({ queryKey: ['b2b-purchase-orders'] });
      setCreatePOModalOpen(false);
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.response?.data?.message || 'Failed to create purchase order',
        variant: 'destructive',
      });
    },
  });

  const orders = ordersData?.content || [];

  return (
    <div className="container mx-auto py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Purchase Orders</h1>
        <div className="flex gap-4">
          <Dialog open={createPOModalOpen} onOpenChange={setCreatePOModalOpen}>
            <DialogTrigger asChild>
              <Button>Create PO</Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Create Purchase Order</DialogTitle>
                <DialogDescription>Select an order to create a purchase order</DialogDescription>
              </DialogHeader>
              <div className="space-y-2">
                <Select value={selectedOrderId?.toString()} onValueChange={(v) => setSelectedOrderId(parseInt(v))}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select an order" />
                  </SelectTrigger>
                  <SelectContent>
                    {availableOrders?.content.map((order) => (
                      <SelectItem key={order.id} value={order.id.toString()}>
                        {order.orderNumber} - ${order.totalAmount.toFixed(2)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Button
                  onClick={() => {
                    if (selectedOrderId) {
                      createPOMutation.mutate(selectedOrderId);
                    }
                  }}
                  disabled={!selectedOrderId || createPOMutation.isPending}
                  className="w-full"
                >
                  Create Purchase Order
                </Button>
              </div>
            </DialogContent>
          </Dialog>
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-40">
              <SelectValue placeholder="Filter by status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All</SelectItem>
              <SelectItem value="PENDING">Pending</SelectItem>
              <SelectItem value="APPROVED">Approved</SelectItem>
              <SelectItem value="REJECTED">Rejected</SelectItem>
              <SelectItem value="COMPLETED">Completed</SelectItem>
            </SelectContent>
          </Select>
          <Button onClick={() => refetch()}>Refresh</Button>
        </div>
      </div>

      <div className="space-y-4">
        {orders.length === 0 ? (
          <Card>
            <CardContent className="py-8 text-center">
              <p className="text-muted-foreground">No purchase orders found</p>
            </CardContent>
          </Card>
        ) : (
          orders.map((order) => (
            <Card key={order.id}>
              <CardHeader>
                <div className="flex justify-between items-start">
                  <div>
                    <CardTitle>{order.poNumber}</CardTitle>
                    <p className="text-sm text-muted-foreground">
                      Requested: {new Date(order.requestedAt).toLocaleDateString()}
                    </p>
                  </div>
                  <span
                    className={`px-3 py-1 rounded-full text-sm ${
                      order.status === 'APPROVED'
                        ? 'bg-green-100 text-green-800'
                        : order.status === 'REJECTED'
                        ? 'bg-red-100 text-red-800'
                        : 'bg-yellow-100 text-yellow-800'
                    }`}
                  >
                    {order.status}
                  </span>
                </div>
              </CardHeader>
              <CardContent>
                {order.order && (
                  <div className="mt-2">
                    <p className="text-sm font-medium">
                      Order: {order.order.orderNumber} - ${order.order.totalAmount.toFixed(2)}
                    </p>
                    {order.order.items && order.order.items.length > 0 && (
                      <div className="mt-2">
                        <p className="text-sm font-medium mb-1">Items:</p>
                        <div className="space-y-1">
                          {order.order.items.map((item) => (
                            <div key={item.id} className="text-sm text-muted-foreground">
                              {item.productName} x {item.quantity} - ${item.totalPrice.toFixed(2)}
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                )}
                {order.approvalNotes && (
                  <p className="text-sm text-muted-foreground mt-2">
                    Notes: {order.approvalNotes}
                  </p>
                )}
                <div className="mt-4">
                  <p className="text-sm font-medium mb-2">Approval Workflow:</p>
                  <div className="flex items-center gap-2">
                    <div className={`h-2 w-2 rounded-full ${
                      order.status === 'PENDING' ? 'bg-yellow-500' : 
                      order.status === 'APPROVED' ? 'bg-green-500' : 'bg-red-500'
                    }`} />
                    <span className="text-sm">{order.status}</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
  );
}

