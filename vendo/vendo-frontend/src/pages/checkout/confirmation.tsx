import { useSearchParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { CheckCircle, Package, Loader2, Home, ShoppingBag, UserPlus, CreditCard } from 'lucide-react';
import { orderApi } from '@/services/order-api';
import { guestCheckoutApi } from '@/services/guest-checkout-api';
import { formatPrice } from '@/lib/utils';

export function OrderConfirmation() {
  const [searchParams] = useSearchParams();
  const orderId = searchParams.get('orderId');
  const orderCode = searchParams.get('orderCode');
  const isGuest = searchParams.get('guest') === 'true';
  const sessionToken = localStorage.getItem('guestSessionToken');

  const { data: order, isLoading } = useQuery({
    queryKey: ['order', orderId, isGuest],
    queryFn: async () => {
      if (isGuest && sessionToken) {
        return guestCheckoutApi.getGuestOrderByToken(sessionToken);
      }
      if (orderId) {
        return orderApi.getOrderById(Number(orderId));
      }
      if (orderCode) {
        return orderApi.getOrderByCode(orderCode);
      }
      return null;
    },
    enabled: !!(orderId || orderCode || (isGuest && sessionToken)),
  });

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (isLoading) {
    return (
      <div className="container py-8 flex items-center justify-center min-h-[60vh]">
        <Loader2 className="h-8 w-8 animate-spin" />
      </div>
    );
  }

  if (!order) {
    return (
      <div className="container py-8">
        <Card className="text-center py-12">
          <CardContent>
            <p className="text-muted-foreground mb-4">Order not found</p>
            <Button asChild>
              <Link to="/">Go to Home</Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="container py-8 max-w-4xl mx-auto">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="space-y-8"
      >
        {/* Success Header */}
        <div className="text-center space-y-4">
          <div className="flex justify-center">
            <div className="rounded-full bg-green-100 dark:bg-green-900/20 p-4">
              <CheckCircle className="h-12 w-12 text-green-600 dark:text-green-400" />
            </div>
          </div>
          <div>
            <h1 className="text-4xl font-bold mb-2">Order Confirmed!</h1>
            <p className="text-muted-foreground text-lg">
              Thank you for your purchase. We've received your order.
            </p>
          </div>
        </div>

        {/* Order Details */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Package className="h-5 w-5" />
              Order Details
            </CardTitle>
            <CardDescription>
              Order #{order.code || order.id} • Placed on {formatDate(order.createdAt)}
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            {/* Order Items */}
            {order.items && order.items.length > 0 && (
              <div>
                <h3 className="font-semibold mb-3">Items Ordered</h3>
                <div className="space-y-2">
                  {order.items.map((item, index) => (
                    <div
                      key={index}
                      className="flex justify-between items-center py-2 border-b last:border-0"
                    >
                      <div>
                        <div className="font-medium">{item.productName}</div>
                        <div className="text-sm text-muted-foreground">
                          Quantity: {item.quantity} × {formatPrice(item.price)}
                        </div>
                      </div>
                      <div className="font-semibold">{formatPrice(item.total)}</div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Order Summary */}
            <div className="pt-4 border-t space-y-2">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Subtotal</span>
                <span>{formatPrice(order.subTotal || 0)}</span>
              </div>
              {order.taxAmount && order.taxAmount > 0 && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Tax</span>
                  <span>{formatPrice(order.taxAmount)}</span>
                </div>
              )}
              {order.shippingAmount && order.shippingAmount > 0 && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Shipping</span>
                  <span>{formatPrice(order.shippingAmount)}</span>
                </div>
              )}
              {order.discountAmount && order.discountAmount > 0 && (
                <div className="flex justify-between text-green-600 dark:text-green-400">
                  <span>Discount</span>
                  <span>-{formatPrice(order.discountAmount)}</span>
                </div>
              )}
              <div className="flex justify-between font-bold text-lg pt-2 border-t">
                <span>Total</span>
                <span>{formatPrice(order.amount || order.subTotal || 0)}</span>
              </div>
            </div>

            {/* Shipping Address */}
            {order.shippingAddress && (
              <div className="pt-4 border-t">
                <h3 className="font-semibold mb-2">Shipping Address</h3>
                <div className="text-sm text-muted-foreground">
                  <p>{order.shippingAddress.addressLine1}</p>
                  {order.shippingAddress.addressLine2 && (
                    <p>{order.shippingAddress.addressLine2}</p>
                  )}
                  <p>
                    {order.shippingAddress.city}, {order.shippingAddress.state}{' '}
                    {order.shippingAddress.postalCode}
                  </p>
                  <p>{order.shippingAddress.country}</p>
                </div>
              </div>
            )}

            {/* Order Status */}
            <div className="pt-4 border-t">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-semibold mb-1">Order Status</h3>
                  <p className="text-sm text-muted-foreground">
                    Your order is being processed and will be shipped soon.
                  </p>
                </div>
                <span
                  className={`px-3 py-1 rounded-full text-xs font-medium ${
                    order.status === 'PENDING'
                      ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300'
                      : order.status === 'CONFIRMED'
                      ? 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300'
                      : 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300'
                  }`}
                >
                  {order.status}
                </span>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Guest Account Creation Prompt */}
        {isGuest && (
          <Card className="border-primary">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <UserPlus className="h-5 w-5" />
                Create an Account
              </CardTitle>
              <CardDescription>
                Create an account to track your orders and enjoy faster checkout next time!
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <p className="text-sm text-muted-foreground">
                  You can create an account using the email address used for this order.
                </p>
                <div className="flex gap-4">
                  <Button asChild>
                    <Link to={`/register?email=${encodeURIComponent(localStorage.getItem('guestEmail') || '')}&redirect=/profile/orders`}>
                      Create Account
                    </Link>
                  </Button>
                  <Button variant="outline" asChild>
                    <Link to="/">
                      Continue as Guest
                    </Link>
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Actions */}
        <div className="flex gap-4 justify-center flex-wrap">
          {/* Payment Button - Show if order is unpaid */}
          {order && (!order.paymentStatus || order.paymentStatus === 'PENDING' || order.paymentStatus === 'pending') && (
            <Button asChild className="bg-primary">
              <Link to={`/payment?orderId=${order.id}`}>
                <CreditCard className="h-4 w-4 mr-2" />
                Complete Payment
              </Link>
            </Button>
          )}
          <Button variant="outline" asChild>
            <Link to="/">
              <Home className="h-4 w-4 mr-2" />
              Back to Home
            </Link>
          </Button>
          {!isGuest && (
            <Button asChild>
              <Link to="/profile/orders">
                <ShoppingBag className="h-4 w-4 mr-2" />
                View All Orders
              </Link>
            </Button>
          )}
        </div>
      </motion.div>
    </div>
  );
}
