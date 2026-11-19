import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import {
Card,
CardContent,
CardDescription,
CardHeader,
CardTitle,
} from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import {
Dialog,
DialogContent,
DialogDescription,
DialogFooter,
DialogHeader,
DialogTitle,
} from '@/components/ui/dialog';
import {
Select,
SelectContent,
SelectItem,
SelectTrigger,
SelectValue,
} from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { ProtectedRoute } from '@/components/protected-route';
import { orderApi } from '@/services/order-api';
import { formatPrice } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import {
Loader2,
Package,
MapPin,
CreditCard,
Truck,
Calendar,
X,
AlertTriangle,
CheckCircle,
Clock,
} from 'lucide-react';
import type {
CancellationReason,
CancelOrderRequest
} from '@/types';

const statusColors: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300',
  CONFIRMED: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300',
  PROCESSING: 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-300',
  SHIPPED: 'bg-indigo-100 text-indigo-800 dark:bg-indigo-900 dark:text-indigo-300',
  DELIVERED: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300',
  CANCELLED: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300',
  REFUNDED: 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-300',
};

const statusIcons: Record<string, React.ReactNode> = {
  PENDING: <Clock className="h-5 w-5" />,
  CONFIRMED: <CheckCircle className="h-5 w-5" />,
  PROCESSING: <Package className="h-5 w-5" />,
  SHIPPED: <Truck className="h-5 w-5" />,
  DELIVERED: <CheckCircle className="h-5 w-5" />,
  CANCELLED: <X className="h-5 w-5" />,
  REFUNDED: <AlertTriangle className="h-5 w-5" />,
};

export function OrderDetail() {
  const { id } = useParams<{ id: string }>();
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [showCancelDialog, setShowCancelDialog] = useState(false);
  const [cancellationReason, setCancellationReason] = useState<CancellationReason | ''>('');
  const [customReason, setCustomReason] = useState('');

  // Fetch order details
  const { data: order, isLoading } = useQuery({
    queryKey: ['order', id],
    queryFn: () => orderApi.getOrderById(Number(id)),
    enabled: !!id,
  });

  // Fetch cancellation reasons
  const { data: cancellationReasons = [] } = useQuery({
    queryKey: ['cancellationReasons'],
    queryFn: () => orderApi.getCancellationReasons(),
  });

  // Cancel order mutation
  const cancelOrderMutation = useMutation({
    mutationFn: (request: CancelOrderRequest) =>
      orderApi.cancelOrder(Number(id), request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['order', id] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      setShowCancelDialog(false);
      setCancellationReason('');
      setCustomReason('');
    },
  });

  const handleCancelOrder = () => {
    if (!cancellationReason) {
      toast({
        title: 'Reason required',
        description: 'Please select a cancellation reason',
        variant: 'destructive',
      });
      return;
    }

    const request: CancelOrderRequest = {
      reason: cancellationReason as CancellationReason,
      customReason: cancellationReason === 'OTHER' ? customReason : undefined,
    };

    cancelOrderMutation.mutate(request);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const calculateEstimatedDelivery = (orderDate: string, shippingMethod?: string) => {
    const order = new Date(orderDate);
    let daysToAdd = 7; // Default 7 days

    if (shippingMethod) {
      if (shippingMethod.includes('EXPRESS')) {
        daysToAdd = 3;
      } else if (shippingMethod.includes('OVERNIGHT')) {
        daysToAdd = 1;
      }
    }

    const estimatedDate = new Date(order);
    estimatedDate.setDate(estimatedDate.getDate() + daysToAdd);
    return estimatedDate.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const canCancel = order?.status === 'PENDING' || order?.status === 'CONFIRMED';

  if (isLoading) {
    return (
      <ProtectedRoute>
        <div className="container py-8 flex items-center justify-center min-h-[60vh]">
          <Loader2 className="h-8 w-8 animate-spin" />
        </div>
      </ProtectedRoute>
    );
  }

  if (!order) {
    return (
      <ProtectedRoute>
        <div className="container py-8">
          <Card className="text-center py-12">
            <CardContent>
              <p className="text-muted-foreground mb-4">Order not found</p>
              <Button asChild>
                <Link to="/profile/orders">Back to Orders</Link>
              </Button>
            </CardContent>
          </Card>
        </div>
      </ProtectedRoute>
    );
  }

  const estimatedDelivery = calculateEstimatedDelivery(
    order.createdAt,
    order.shippingMethod
  );

  return (
    <ProtectedRoute>
      <div className="container py-8 max-w-6xl mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="space-y-6"
        >
          {/* Header */}
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-4xl font-bold mb-2">Order Details</h1>
              <p className="text-muted-foreground">
                Order #{order.code || order.id} • Placed on {formatDate(order.createdAt)}
              </p>
            </div>
            <Button variant="outline" asChild>
              <Link to="/profile/orders">Back to Orders</Link>
            </Button>
          </div>

          {/* Order Status */}
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>Order Status</CardTitle>
                  <CardDescription>Current status of your order</CardDescription>
                </div>
                <div className="flex items-center gap-2">
                  {statusIcons[order.status] || <Package className="h-5 w-5" />}
                  <span
                    className={`px-4 py-2 rounded-full text-sm font-medium ${
                      statusColors[order.status] || statusColors.PENDING
                    }`}
                  >
                    {order.status}
                  </span>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              {canCancel && (
                <div className="pt-4 border-t">
                  <Button
                    variant="destructive"
                    onClick={() => setShowCancelDialog(true)}
                  >
                    <X className="h-4 w-4 mr-2" />
                    Cancel Order
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>

          <div className="grid lg:grid-cols-3 gap-6">
            {/* Left Column - Order Items & Details */}
            <div className="lg:col-span-2 space-y-6">
              {/* Order Items */}
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Package className="h-5 w-5" />
                    Order Items
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {order.items && order.items.length > 0 ? (
                      order.items.map((item, index) => (
                        <div
                          key={index}
                          className="flex items-start gap-4 pb-4 border-b last:border-0"
                        >
                          <div className="w-20 h-20 bg-muted rounded-lg flex items-center justify-center">
                            <Package className="h-8 w-8 text-muted-foreground" />
                          </div>
                          <div className="flex-1">
                            <h3 className="font-semibold mb-1">
                              <Link
                                to={`/products/${item.productId}`}
                                className="hover:text-primary transition-colors"
                              >
                                {item.productName}
                              </Link>
                            </h3>
                            <p className="text-sm text-muted-foreground mb-2">
                              SKU: {item.productSku}
                            </p>
                            <div className="flex items-center justify-between">
                              <span className="text-sm text-muted-foreground">
                                Quantity: {item.quantity}
                              </span>
                              <span className="font-semibold">
                                {formatPrice(item.total)}
                              </span>
                            </div>
                          </div>
                        </div>
                      ))
                    ) : (
                      <p className="text-muted-foreground">No items found</p>
                    )}
                  </div>
                </CardContent>
              </Card>

              {/* Shipping Address */}
              {order.shippingAddress && (
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <MapPin className="h-5 w-5" />
                      Shipping Address
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-1">
                      <p className="font-medium">{order.shippingAddress.addressLine1}</p>
                      {order.shippingAddress.addressLine2 && (
                        <p>{order.shippingAddress.addressLine2}</p>
                      )}
                      <p>
                        {order.shippingAddress.city}, {order.shippingAddress.state}{' '}
                        {order.shippingAddress.postalCode}
                      </p>
                      <p>{order.shippingAddress.country}</p>
                      {order.shippingAddress.phone && (
                        <p className="text-sm text-muted-foreground mt-2">
                          Phone: {order.shippingAddress.phone}
                        </p>
                      )}
                    </div>
                  </CardContent>
                </Card>
              )}

              {/* Payment Information */}
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <CreditCard className="h-5 w-5" />
                    Payment Information
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Payment Status</span>
                    <span className="font-medium">
                      {order.paymentStatus || 'Pending'}
                    </span>
                  </div>
                  {order.paymentMethod && (
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Payment Method</span>
                      <span className="font-medium">{order.paymentMethod}</span>
                    </div>
                  )}
                  {/* Payment Button - Show if order is unpaid */}
                  {(!order.paymentStatus || 
                    order.paymentStatus === 'PENDING' || 
                    order.paymentStatus === 'pending' ||
                    order.paymentStatus === 'FAILED' ||
                    order.paymentStatus === 'failed') && (
                    <div className="pt-2 border-t">
                      <Button asChild className="w-full">
                        <Link to={`/payment?orderId=${order.id}`}>
                          <CreditCard className="h-4 w-4 mr-2" />
                          Complete Payment
                        </Link>
                      </Button>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>

            {/* Right Column - Order Summary & Tracking */}
            <div className="space-y-6">
              {/* Order Summary */}
              <Card>
                <CardHeader>
                  <CardTitle>Order Summary</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Subtotal</span>
                    <span>{formatPrice(order.subTotal || 0)}</span>
                  </div>
                  {order.taxAmount && order.taxAmount > 0 && (
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Tax</span>
                      <span>{formatPrice(order.taxAmount)}</span>
                    </div>
                  )}
                  {order.shippingAmount && order.shippingAmount > 0 && (
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Shipping</span>
                      <span>{formatPrice(order.shippingAmount)}</span>
                    </div>
                  )}
                  {order.discountAmount && order.discountAmount > 0 && (
                    <div className="flex justify-between text-sm text-green-600 dark:text-green-400">
                      <span>Discount</span>
                      <span>-{formatPrice(order.discountAmount)}</span>
                    </div>
                  )}
                  <div className="flex justify-between font-bold text-lg pt-3 border-t">
                    <span>Total</span>
                    <span>{formatPrice(order.amount || order.subTotal || 0)}</span>
                  </div>
                </CardContent>
              </Card>

              {/* Tracking Information */}
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Truck className="h-5 w-5" />
                    Tracking Information
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  {order.shippingMethod && (
                    <div>
                      <p className="text-sm text-muted-foreground mb-1">Shipping Method</p>
                      <p className="font-medium">{order.shippingMethod}</p>
                    </div>
                  )}
                  {/* Tracking number would come from shipment data */}
                  {order.trackingNumber ? (
                    <div>
                      <p className="text-sm text-muted-foreground mb-1">Tracking Number</p>
                      <p className="font-mono text-sm">{order.trackingNumber}</p>
                      <Button
                        variant="outline"
                        size="sm"
                        className="mt-2"
                        onClick={() => {
                          // Open tracking in new window (would use actual carrier tracking URL)
                          window.open(
                            `https://tracking.example.com/${order.trackingNumber}`,
                            '_blank'
                          );
                        }}
                      >
                        Track Package
                      </Button>
                    </div>
                  ) : (
                    <div>
                      <p className="text-sm text-muted-foreground">
                        Tracking number will be available once your order ships.
                      </p>
                    </div>
                  )}
                  <div>
                    <p className="text-sm text-muted-foreground mb-1 flex items-center gap-2">
                      <Calendar className="h-4 w-4" />
                      Estimated Delivery
                    </p>
                    <p className="font-medium">{estimatedDelivery}</p>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </motion.div>

        {/* Cancel Order Dialog */}
        <Dialog open={showCancelDialog} onOpenChange={setShowCancelDialog}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Cancel Order</DialogTitle>
              <DialogDescription>
                Are you sure you want to cancel this order? This action cannot be undone.
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="cancellation-reason">Cancellation Reason</Label>
                <Select
                  value={cancellationReason}
                  onValueChange={(value) =>
                    setCancellationReason(value as CancellationReason)
                  }
                >
                  <SelectTrigger id="cancellation-reason">
                    <SelectValue placeholder="Select a reason" />
                  </SelectTrigger>
                  <SelectContent>
                    {cancellationReasons.map((reason) => (
                      <SelectItem key={reason} value={reason}>
                        {reason.replace(/_/g, ' ').replace(/\b\w/g, (l) => l.toUpperCase())}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              {cancellationReason === 'OTHER' && (
                <div className="space-y-2">
                  <Label htmlFor="custom-reason">Please specify</Label>
                  <Textarea
                    id="custom-reason"
                    placeholder="Enter your reason for cancellation..."
                    value={customReason}
                    onChange={(e) => setCustomReason(e.target.value)}
                    rows={3}
                  />
                </div>
              )}
            </div>
            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => {
                  setShowCancelDialog(false);
                  setCancellationReason('');
                  setCustomReason('');
                }}
              >
                Keep Order
              </Button>
              <Button
                variant="destructive"
                onClick={handleCancelOrder}
                disabled={!cancellationReason || cancelOrderMutation.isPending}
              >
                {cancelOrderMutation.isPending ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Cancelling...
                  </>
                ) : (
                  'Cancel Order'
                )}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
    </ProtectedRoute>
  );
}
