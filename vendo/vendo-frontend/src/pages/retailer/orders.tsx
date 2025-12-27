import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { useRetailerStore } from '@/stores/retailer-store';
import { useAuthStore } from '@/stores/auth-store';
import { retailerApi } from '@/services/retailer-api';
import { orderApi } from '@/services/order-api';
import type { RetailerOrder } from '@/types/retailer';

export default function RetailerOrders() {
  const { user } = useAuthStore();
  const { currentRetailer, orders, loadOrders, isLoadingOrders } = useRetailerStore();
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [selectedOrder, setSelectedOrder] = useState<RetailerOrder | null>(null);
  const [isOrderDetailsOpen, setIsOrderDetailsOpen] = useState(false);
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false);
  const [newStatus, setNewStatus] = useState<string>('');

  useEffect(() => {
    if (currentRetailer?.id) {
      loadOrders(currentRetailer.id, {
        status: statusFilter || undefined,
      });
    }
  }, [currentRetailer?.id, statusFilter]);

  const handleViewOrder = (order: RetailerOrder) => {
    setSelectedOrder(order);
    setNewStatus(order.status);
    setIsOrderDetailsOpen(true);
  };

  const handleUpdateStatus = async () => {
    if (!selectedOrder || !newStatus || newStatus === selectedOrder.status) {
      return;
    }

    setIsUpdatingStatus(true);
    try {
      const updated = await orderApi.updateOrderStatus(selectedOrder.id, newStatus);
      if (updated && currentRetailer?.id) {
        // Reload orders to get updated data
        await loadOrders(currentRetailer.id, {
          status: statusFilter || undefined,
        });
        // Update selected order
        setSelectedOrder({ ...selectedOrder, status: newStatus });
      }
    } catch (error) {
      console.error('Failed to update order status:', error);
    } finally {
      setIsUpdatingStatus(false);
    }
  };

  const getNextStatusOptions = (currentStatus: string): string[] => {
    const status = currentStatus.toUpperCase();
    switch (status) {
      case 'PENDING':
        return ['CONFIRMED'];
      case 'CONFIRMED':
        return ['PROCESSING'];
      case 'PROCESSING':
        return ['SHIPPED'];
      case 'SHIPPED':
        return ['DELIVERED'];
      default:
        return [];
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toUpperCase()) {
      case 'PENDING':
        return 'bg-yellow-500';
      case 'CONFIRMED':
      case 'PROCESSING':
        return 'bg-blue-500';
      case 'SHIPPED':
        return 'bg-purple-500';
      case 'DELIVERED':
        return 'bg-green-500';
      case 'CANCELLED':
        return 'bg-red-500';
      default:
        return 'bg-gray-500';
    }
  };

  if (!currentRetailer) {
    return (
      <div className="container mx-auto py-8">
        <Card>
          <CardContent className="pt-6">
            <p>You are not registered as a retailer yet.</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Order Management</h1>
      </div>

      {/* Filters */}
      <div className="flex gap-4 mb-6">
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Filter by status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="">All Orders</SelectItem>
            <SelectItem value="PENDING">Pending</SelectItem>
            <SelectItem value="CONFIRMED">Confirmed</SelectItem>
            <SelectItem value="PROCESSING">Processing</SelectItem>
            <SelectItem value="SHIPPED">Shipped</SelectItem>
            <SelectItem value="DELIVERED">Delivered</SelectItem>
            <SelectItem value="CANCELLED">Cancelled</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Orders List */}
      {isLoadingOrders ? (
        <p>Loading orders...</p>
      ) : orders.length === 0 ? (
        <Card>
          <CardContent className="pt-6">
            <p className="text-center text-muted-foreground">No orders found</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => (
            <Card key={order.id} className="cursor-pointer hover:bg-muted" onClick={() => handleViewOrder(order)}>
              <CardContent className="pt-6">
                <div className="flex justify-between items-start">
                  <div>
                    <div className="flex items-center gap-2 mb-2">
                      <h3 className="text-lg font-semibold">Order #{order.code}</h3>
                      <Badge className={getStatusColor(order.status)}>
                        {order.status}
                      </Badge>
                      {order.paymentStatus && (
                        <Badge variant="outline">
                          {order.paymentStatus}
                        </Badge>
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground">
                      {new Date(order.createdAt).toLocaleDateString()} at{' '}
                      {new Date(order.createdAt).toLocaleTimeString()}
                    </p>
                    <p className="text-sm text-muted-foreground mt-1">
                      {order.items.length} item(s)
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-xl font-bold">${order.amount.toFixed(2)}</p>
                    <p className="text-sm text-muted-foreground">
                      {order.orderType}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Order Details Dialog */}
      <Dialog open={isOrderDetailsOpen} onOpenChange={setIsOrderDetailsOpen}>
        <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Order Details - #{selectedOrder?.code}</DialogTitle>
            <DialogDescription>
              View order information and customer details
            </DialogDescription>
          </DialogHeader>
          {selectedOrder && (
            <div className="space-y-6">
              {/* Order Info */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-muted-foreground">Order Number</p>
                  <p className="font-semibold">{selectedOrder.code}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Status</p>
                  <Badge className={getStatusColor(selectedOrder.status)}>
                    {selectedOrder.status}
                  </Badge>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Order Date</p>
                  <p className="font-semibold">
                    {new Date(selectedOrder.createdAt).toLocaleString()}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Payment Status</p>
                  <Badge variant="outline">
                    {selectedOrder.paymentStatus || 'N/A'}
                  </Badge>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Payment Method</p>
                  <p className="font-semibold">
                    {selectedOrder.paymentMethod || 'N/A'}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Order Type</p>
                  <p className="font-semibold">{selectedOrder.orderType}</p>
                </div>
              </div>

              {/* Order Items */}
              <div>
                <h4 className="font-semibold mb-3">Order Items</h4>
                <div className="space-y-2">
                  {selectedOrder.items.map((item) => (
                    <div key={item.id} className="flex justify-between items-center p-3 border rounded">
                      <div>
                        <p className="font-medium">{item.productName}</p>
                        <p className="text-sm text-muted-foreground">
                          SKU: {item.productSku} | Qty: {item.quantity}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="font-semibold">${item.total.toFixed(2)}</p>
                        <p className="text-sm text-muted-foreground">
                          ${item.price.toFixed(2)} each
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Order Totals */}
              <div className="border-t pt-4">
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <p className="text-muted-foreground">Subtotal</p>
                    <p className="font-semibold">${selectedOrder.subTotal.toFixed(2)}</p>
                  </div>
                  {selectedOrder.taxAmount > 0 && (
                    <div className="flex justify-between">
                      <p className="text-muted-foreground">Tax</p>
                      <p className="font-semibold">${selectedOrder.taxAmount.toFixed(2)}</p>
                    </div>
                  )}
                  {selectedOrder.shippingAmount > 0 && (
                    <div className="flex justify-between">
                      <p className="text-muted-foreground">Shipping</p>
                      <p className="font-semibold">${selectedOrder.shippingAmount.toFixed(2)}</p>
                    </div>
                  )}
                  {selectedOrder.discountAmount > 0 && (
                    <div className="flex justify-between">
                      <p className="text-muted-foreground">Discount</p>
                      <p className="font-semibold text-green-500">
                        -${selectedOrder.discountAmount.toFixed(2)}
                      </p>
                    </div>
                  )}
                  <div className="flex justify-between text-lg font-bold border-t pt-2">
                    <p>Total</p>
                    <p>${selectedOrder.amount.toFixed(2)}</p>
                  </div>
                </div>
              </div>

              {/* Shipping Address */}
              {selectedOrder.shippingAddress && (
                <div>
                  <h4 className="font-semibold mb-2">Shipping Address</h4>
                  <div className="p-3 border rounded">
                    <p className="font-medium">
                      {selectedOrder.shippingAddress.contactName || 'N/A'}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {selectedOrder.shippingAddress.addressLine1}
                    </p>
                    {selectedOrder.shippingAddress.addressLine2 && (
                      <p className="text-sm text-muted-foreground">
                        {selectedOrder.shippingAddress.addressLine2}
                      </p>
                    )}
                    <p className="text-sm text-muted-foreground">
                      {selectedOrder.shippingAddress.city}, {selectedOrder.shippingAddress.state}{' '}
                      {selectedOrder.shippingAddress.postalCode}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {selectedOrder.shippingAddress.country}
                    </p>
                    {selectedOrder.shippingAddress.phone && (
                      <p className="text-sm text-muted-foreground">
                        Phone: {selectedOrder.shippingAddress.phone}
                      </p>
                    )}
                  </div>
                </div>
              )}

              {/* Update Status Section */}
              <div className="border-t pt-4">
                <h4 className="font-semibold mb-3">Update Order Status</h4>
                <div className="flex gap-4 items-end">
                  <div className="flex-1 space-y-2">
                    <Label htmlFor="orderStatus">New Status</Label>
                    <Select value={newStatus} onValueChange={setNewStatus}>
                      <SelectTrigger id="orderStatus">
                        <SelectValue placeholder="Select status" />
                      </SelectTrigger>
                      <SelectContent>
                        {getNextStatusOptions(selectedOrder.status).map((status) => (
                          <SelectItem key={status} value={status}>
                            {status}
                          </SelectItem>
                        ))}
                        {getNextStatusOptions(selectedOrder.status).length === 0 && (
                          <SelectItem value={selectedOrder.status} disabled>
                            {selectedOrder.status} (No further actions available)
                          </SelectItem>
                        )}
                      </SelectContent>
                    </Select>
                    {getNextStatusOptions(selectedOrder.status).length === 0 && (
                      <p className="text-xs text-muted-foreground">
                        This order has reached its final status.
                      </p>
                    )}
                  </div>
                  <Button
                    onClick={handleUpdateStatus}
                    disabled={
                      isUpdatingStatus ||
                      !newStatus ||
                      newStatus === selectedOrder.status ||
                      getNextStatusOptions(selectedOrder.status).length === 0
                    }
                  >
                    {isUpdatingStatus ? 'Updating...' : 'Update Status'}
                  </Button>
                </div>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}

