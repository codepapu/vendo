import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { CheckCircle, Package, Loader2, Home, ShoppingBag } from 'lucide-react';
import { orderApi } from '@/services/order-api';
import { formatPrice } from '@/lib/utils';
import type { OrderResponse } from '@/types';

export function OrderConfirmation() {
  const [searchParams] = useSearchParams();
  const orderId = searchParams.get('orderId');
  const orderCode = searchParams.get('orderCode');

  const { data: order, isLoading } = useQuery({
    queryKey: ['order', orderId],
    queryFn: () => {
      if (orderId) {
        return orderApi.getOrderById(Number(orderId));
      }
      if (orderCode) {
        return orderApi.getOrderByCode(orderCode);
      }
      return null;
    },
    enabled: !!(orderId || orderCode),
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

        {/* Actions */}
        <div className="flex gap-4 justify-center">
          <Button variant="outline" asChild>
            <Link to="/">
              <Home className="h-4 w-4 mr-2" />
              Back to Home
            </Link>
          </Button>
          <Button asChild>
            <Link to="/profile/orders">
              <ShoppingBag className="h-4 w-4 mr-2" />
              View All Orders
            </Link>
          </Button>
        </div>
      </motion.div>
    </div>
  );
}
