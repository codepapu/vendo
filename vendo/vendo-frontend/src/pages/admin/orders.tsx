import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Eye, Search, Calendar } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { adminApi } from '@/services/admin-api';
import { formatDate } from '@/lib/utils';
import type { OrderResponse } from '@/types';

/**
 * Admin page for managing orders
 */
export function AdminOrders() {
  const queryClient = useQueryClient();
  const [searchKeyword, setSearchKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [orderTypeFilter, setOrderTypeFilter] = useState<string>('');
  const [customerFilter, setCustomerFilter] = useState<string>('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [selectedOrders, setSelectedOrders] = useState<number[]>([]);
  const [viewingOrder, setViewingOrder] = useState<OrderResponse | null>(null);
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
  const [isStatusModalOpen, setIsStatusModalOpen] = useState(false);
  const [updatingOrderId, setUpdatingOrderId] = useState<number | null>(null);
  const [newStatus, setNewStatus] = useState('');

  // Fetch orders
  const { data: ordersData, isLoading } = useQuery({
    queryKey: ['admin-orders', statusFilter, orderTypeFilter, dateFrom, dateTo, searchKeyword, customerFilter],
    queryFn: () => adminApi.getAllOrders({
      status: statusFilter || undefined,
      orderType: orderTypeFilter || undefined,
      dateFrom: dateFrom || undefined,
      dateTo: dateTo || undefined,
      search: searchKeyword || undefined,
      customerId: customerFilter ? parseInt(customerFilter) : undefined,
      page: 0,
      size: 100,
    }),
  });

  // Update status mutation
  const updateStatusMutation = useMutation({
    mutationFn: ({ orderId, status }: { orderId: number; status: string }) =>
      adminApi.updateOrderStatus(orderId, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-orders'] });
      setIsStatusModalOpen(false);
      setUpdatingOrderId(null);
      setNewStatus('');
    },
  });

  // Bulk status update mutation
  const bulkStatusMutation = useMutation({
    mutationFn: ({ orderIds, status }: { orderIds: number[]; status: string }) =>
      adminApi.bulkUpdateOrderStatus(orderIds, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-orders'] });
      setSelectedOrders([]);
    },
  });

  // Cancel order mutation
  const cancelMutation = useMutation({
    mutationFn: (orderId: number) => adminApi.cancelOrder(orderId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-orders'] });
    },
  });

  // Refund mutation
  const refundMutation = useMutation({
    mutationFn: ({ orderId, amount }: { orderId: number; amount?: number }) =>
      adminApi.processRefund(orderId, amount),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-orders'] });
    },
  });

  const handleViewDetails = (order: OrderResponse) => {
    setViewingOrder(order);
    setIsDetailsModalOpen(true);
  };

  const handleUpdateStatus = (orderId: number) => {
    setUpdatingOrderId(orderId);
    setIsStatusModalOpen(true);
  };

  const handleConfirmStatusUpdate = () => {
    if (!updatingOrderId || !newStatus) return;
    updateStatusMutation.mutate({ orderId: updatingOrderId, status: newStatus });
  };

  const handleCancelOrder = (orderId: number) => {
    if (confirm('Are you sure you want to cancel this order?')) {
      cancelMutation.mutate(orderId);
    }
  };

  const handleRefund = (orderId: number) => {
    if (confirm('Are you sure you want to process a refund for this order?')) {
      refundMutation.mutate({ orderId });
    }
  };

  const handleToggleSelect = (id: number) => {
    setSelectedOrders(prev =>
      prev.includes(id) ? prev.filter(o => o !== id) : [...prev, id]
    );
  };

  const handleSelectAll = () => {
    const orders = ordersData?.content || [];
    if (selectedOrders.length === orders.length) {
      setSelectedOrders([]);
    } else {
      setSelectedOrders(orders.map(o => o.id));
    }
  };

  const handleBulkStatusUpdate = (status: string) => {
    if (selectedOrders.length === 0) return;
    if (confirm(`Are you sure you want to update ${selectedOrders.length} order(s) to ${status}?`)) {
      bulkStatusMutation.mutate({ orderIds: selectedOrders, status });
    }
  };

  const orders = ordersData?.content || [];

  return (
    <div className="container mx-auto py-8 px-4">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold">Order Management</h1>
          <p className="text-muted-foreground mt-1">Manage all orders in the system</p>
        </div>
      </div>

      {/* Filters */}
      <Card className="p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-6 gap-4">
          <div className="md:col-span-2">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search orders..."
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>
          <div>
            <Input
              placeholder="Customer ID"
              type="number"
              value={customerFilter}
              onChange={(e) => setCustomerFilter(e.target.value)}
            />
          </div>
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger>
              <SelectValue placeholder="All Status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="">All Status</SelectItem>
              <SelectItem value="PENDING">Pending</SelectItem>
              <SelectItem value="CONFIRMED">Confirmed</SelectItem>
              <SelectItem value="PROCESSING">Processing</SelectItem>
              <SelectItem value="SHIPPED">Shipped</SelectItem>
              <SelectItem value="DELIVERED">Delivered</SelectItem>
              <SelectItem value="CANCELLED">Cancelled</SelectItem>
              <SelectItem value="REFUNDED">Refunded</SelectItem>
            </SelectContent>
          </Select>
          <Select value={orderTypeFilter} onValueChange={setOrderTypeFilter}>
            <SelectTrigger>
              <SelectValue placeholder="All Types" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="">All Types</SelectItem>
              <SelectItem value="B2C">B2C</SelectItem>
              <SelectItem value="B2B">B2B</SelectItem>
              <SelectItem value="RETAILER">Retailer</SelectItem>
            </SelectContent>
          </Select>
          <div className="flex gap-2">
            <Input
              type="date"
              placeholder="From"
              value={dateFrom}
              onChange={(e) => setDateFrom(e.target.value)}
              className="flex-1"
            />
            <Input
              type="date"
              placeholder="To"
              value={dateTo}
              onChange={(e) => setDateTo(e.target.value)}
              className="flex-1"
            />
          </div>
        </div>
        {selectedOrders.length > 0 && (
          <div className="mt-4 flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => handleBulkStatusUpdate('CONFIRMED')}
              disabled={bulkStatusMutation.isPending}
            >
              Mark Confirmed ({selectedOrders.length})
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => handleBulkStatusUpdate('PROCESSING')}
              disabled={bulkStatusMutation.isPending}
            >
              Mark Processing ({selectedOrders.length})
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => handleBulkStatusUpdate('SHIPPED')}
              disabled={bulkStatusMutation.isPending}
            >
              Mark Shipped ({selectedOrders.length})
            </Button>
          </div>
        )}
      </Card>

      {/* Orders Table */}
      {isLoading ? (
        <div className="text-center py-12">Loading...</div>
      ) : orders.length === 0 ? (
        <Card className="p-12 text-center">
          <p className="text-muted-foreground">No orders found.</p>
        </Card>
      ) : (
        <Card>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b">
                  <th className="p-4 text-left">
                    <input
                      type="checkbox"
                      checked={selectedOrders.length === orders.length && orders.length > 0}
                      onChange={handleSelectAll}
                      className="rounded"
                    />
                  </th>
                  <th className="p-4 text-left">Order Number</th>
                  <th className="p-4 text-left">Customer</th>
                  <th className="p-4 text-left">Type</th>
                  <th className="p-4 text-left">Total</th>
                  <th className="p-4 text-left">Status</th>
                  <th className="p-4 text-left">Date</th>
                  <th className="p-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => (
                  <tr key={order.id} className="border-b hover:bg-muted/50">
                    <td className="p-4">
                      <input
                        type="checkbox"
                        checked={selectedOrders.includes(order.id)}
                        onChange={() => handleToggleSelect(order.id)}
                        className="rounded"
                      />
                    </td>
                    <td className="p-4 font-medium">{order.code}</td>
                    <td className="p-4">
                      {order.userId ? `User #${order.userId}` : 'Guest'}
                    </td>
                    <td className="p-4">
                      <span className="px-2 py-1 rounded text-xs bg-blue-100 text-blue-800">
                        {order.orderType || 'B2C'}
                      </span>
                    </td>
                    <td className="p-4 font-semibold">${order.amount.toFixed(2)}</td>
                    <td className="p-4">
                      <span className={`px-2 py-1 rounded text-xs ${
                        order.status === 'DELIVERED' ? 'bg-green-100 text-green-800' :
                        order.status === 'CANCELLED' || order.status === 'REFUNDED' ? 'bg-red-100 text-red-800' :
                        order.status === 'SHIPPED' ? 'bg-blue-100 text-blue-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {order.status}
                      </span>
                    </td>
                    <td className="p-4 text-sm text-muted-foreground">
                      {formatDate(order.createdAt)}
                    </td>
                    <td className="p-4">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleViewDetails(order)}
                        >
                          <Eye className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleUpdateStatus(order.id)}
                        >
                          Update
                        </Button>
                        {order.status !== 'CANCELLED' && order.status !== 'REFUNDED' && (
                          <>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleCancelOrder(order.id)}
                              disabled={cancelMutation.isPending}
                            >
                              Cancel
                            </Button>
                            {order.paymentStatus === 'PAID' && (
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handleRefund(order.id)}
                                disabled={refundMutation.isPending}
                              >
                                Refund
                              </Button>
                            )}
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}

      {/* Order Details Modal */}
      <Dialog open={isDetailsModalOpen} onOpenChange={setIsDetailsModalOpen}>
        <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Order Details - {viewingOrder?.code}</DialogTitle>
          </DialogHeader>
          {viewingOrder && (
            <div className="space-y-6">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-muted-foreground">Order Number</Label>
                  <p className="font-semibold">{viewingOrder.code}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Status</Label>
                  <p className="font-semibold">{viewingOrder.status}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Order Type</Label>
                  <p className="font-semibold">{viewingOrder.orderType || 'B2C'}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Payment Status</Label>
                  <p className="font-semibold">{viewingOrder.paymentStatus || 'N/A'}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Order Date</Label>
                  <p className="font-semibold">{formatDate(viewingOrder.createdAt)}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Total Amount</Label>
                  <p className="font-semibold text-lg">${viewingOrder.amount.toFixed(2)}</p>
                </div>
              </div>

              <div>
                <Label className="text-muted-foreground mb-2 block">Order Items</Label>
                <div className="border rounded-lg">
                  <table className="w-full">
                    <thead className="bg-muted">
                      <tr>
                        <th className="p-3 text-left">Product</th>
                        <th className="p-3 text-left">SKU</th>
                        <th className="p-3 text-left">Quantity</th>
                        <th className="p-3 text-right">Price</th>
                        <th className="p-3 text-right">Total</th>
                      </tr>
                    </thead>
                    <tbody>
                      {viewingOrder.items.map((item) => (
                        <tr key={item.id} className="border-t">
                          <td className="p-3">{item.productName}</td>
                          <td className="p-3 text-sm text-muted-foreground">{item.productSku}</td>
                          <td className="p-3">{item.quantity}</td>
                          <td className="p-3 text-right">${item.price.toFixed(2)}</td>
                          <td className="p-3 text-right font-semibold">${item.total.toFixed(2)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-muted-foreground mb-2 block">Shipping Address</Label>
                  {viewingOrder.shippingAddress ? (
                    <div className="border rounded-lg p-4">
                      <p className="font-semibold">{viewingOrder.shippingAddress.contactName}</p>
                      <p className="text-sm">{viewingOrder.shippingAddress.addressLine1}</p>
                      {viewingOrder.shippingAddress.addressLine2 && (
                        <p className="text-sm">{viewingOrder.shippingAddress.addressLine2}</p>
                      )}
                      <p className="text-sm">
                        {viewingOrder.shippingAddress.city}, {viewingOrder.shippingAddress.state} {viewingOrder.shippingAddress.postalCode}
                      </p>
                      <p className="text-sm">{viewingOrder.shippingAddress.country}</p>
                    </div>
                  ) : (
                    <p className="text-muted-foreground">No shipping address</p>
                  )}
                </div>
                <div>
                  <Label className="text-muted-foreground mb-2 block">Billing Address</Label>
                  {viewingOrder.billingAddress ? (
                    <div className="border rounded-lg p-4">
                      <p className="font-semibold">{viewingOrder.billingAddress.contactName}</p>
                      <p className="text-sm">{viewingOrder.billingAddress.addressLine1}</p>
                      {viewingOrder.billingAddress.addressLine2 && (
                        <p className="text-sm">{viewingOrder.billingAddress.addressLine2}</p>
                      )}
                      <p className="text-sm">
                        {viewingOrder.billingAddress.city}, {viewingOrder.billingAddress.state} {viewingOrder.billingAddress.postalCode}
                      </p>
                      <p className="text-sm">{viewingOrder.billingAddress.country}</p>
                    </div>
                  ) : (
                    <p className="text-muted-foreground">No billing address</p>
                  )}
                </div>
              </div>

              <div className="border-t pt-4">
                <div className="flex justify-between items-center mb-2">
                  <span className="text-muted-foreground">Subtotal:</span>
                  <span className="font-semibold">${viewingOrder.subTotal.toFixed(2)}</span>
                </div>
                <div className="flex justify-between items-center mb-2">
                  <span className="text-muted-foreground">Tax:</span>
                  <span className="font-semibold">${viewingOrder.taxAmount.toFixed(2)}</span>
                </div>
                <div className="flex justify-between items-center mb-2">
                  <span className="text-muted-foreground">Shipping:</span>
                  <span className="font-semibold">${viewingOrder.shippingAmount.toFixed(2)}</span>
                </div>
                {viewingOrder.discountAmount > 0 && (
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-muted-foreground">Discount:</span>
                    <span className="font-semibold text-green-600">-${viewingOrder.discountAmount.toFixed(2)}</span>
                  </div>
                )}
                <div className="flex justify-between items-center pt-2 border-t">
                  <span className="text-lg font-semibold">Total:</span>
                  <span className="text-lg font-bold">${viewingOrder.amount.toFixed(2)}</span>
                </div>
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDetailsModalOpen(false)}>
              Close
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Update Status Modal */}
      <Dialog open={isStatusModalOpen} onOpenChange={setIsStatusModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Update Order Status</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="status">New Status</Label>
              <Select value={newStatus} onValueChange={setNewStatus}>
                <SelectTrigger>
                  <SelectValue placeholder="Select status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="PENDING">Pending</SelectItem>
                  <SelectItem value="CONFIRMED">Confirmed</SelectItem>
                  <SelectItem value="PROCESSING">Processing</SelectItem>
                  <SelectItem value="SHIPPED">Shipped</SelectItem>
                  <SelectItem value="DELIVERED">Delivered</SelectItem>
                  <SelectItem value="CANCELLED">Cancelled</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsStatusModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleConfirmStatusUpdate} disabled={!newStatus || updateStatusMutation.isPending}>
              Update Status
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
