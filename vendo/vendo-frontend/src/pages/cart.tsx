import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Trash2, ShoppingCart, Plus, Minus, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ProtectedRoute } from '@/components/protected-route';
import { cartApi, type CartItemResponse } from '@/services/cart-api';
import { formatPrice } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import { Link } from 'react-router-dom';

export function Cart() {
  const navigate = useNavigate();
  const { toast } = useToast();
  const queryClient = useQueryClient();

  // Fetch cart
  const { data: cart, isLoading } = useQuery({
    queryKey: ['cart'],
    queryFn: () => cartApi.getCart(),
  });

  // Update cart item mutation
  const updateItemMutation = useMutation({
    mutationFn: ({ itemId, quantity }: { itemId: number; quantity: number }) =>
      cartApi.updateCartItem(itemId, { quantity }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cart'] });
    },
  });

  // Remove cart item mutation
  const removeItemMutation = useMutation({
    mutationFn: (itemId: number) => cartApi.removeCartItem(itemId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cart'] });
    },
  });

  // Clear cart mutation
  const clearCartMutation = useMutation({
    mutationFn: () => cartApi.clearCart(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cart'] });
    },
  });

  const handleQuantityChange = (item: CartItemResponse, newQuantity: number) => {
    if (newQuantity < 1) {
      toast({
        title: 'Invalid quantity',
        description: 'Quantity must be at least 1',
        variant: 'destructive',
      });
      return;
    }

    // Find item ID - we'll use productId as itemId for now
    // In a real implementation, the backend should return item IDs
    const itemId = (item as any).id || item.productId;
    updateItemMutation.mutate({ itemId, quantity: newQuantity });
  };

  const handleRemoveItem = (item: CartItemResponse) => {
    const itemId = (item as any).id || item.productId;
    removeItemMutation.mutate(itemId);
  };

  const handleClearCart = () => {
    if (window.confirm('Are you sure you want to clear your cart?')) {
      clearCartMutation.mutate();
    }
  };

  const handleCheckout = () => {
    if (!cart || cart.items.length === 0) {
      toast({
        title: 'Cart is empty',
        description: 'Please add items to your cart before checkout',
        variant: 'destructive',
      });
      return;
    }
    navigate('/checkout');
  };

  if (isLoading) {
    return (
      <ProtectedRoute>
        <div className="container py-8 flex items-center justify-center">
          <Loader2 className="h-8 w-8 animate-spin" />
        </div>
      </ProtectedRoute>
    );
  }

  const cartItems = cart?.items || [];
  const subtotal = cart?.subtotal || 0;
  const shipping = cart?.shipping || 0;
  const tax = cart?.tax || 0;
  const total = cart?.total || subtotal + shipping + tax;

  return (
    <ProtectedRoute>
      <div className="container py-8">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="space-y-8"
        >
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-4xl font-bold mb-2">Shopping Cart</h1>
              <p className="text-muted-foreground">Review your items before checkout</p>
            </div>
            {cartItems.length > 0 && (
              <Button variant="outline" onClick={handleClearCart} disabled={clearCartMutation.isPending}>
                {clearCartMutation.isPending ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Clearing...
                  </>
                ) : (
                  'Clear Cart'
                )}
              </Button>
            )}
          </div>

          {cartItems.length === 0 ? (
            <Card className="text-center py-12">
              <CardContent>
                <ShoppingCart className="h-16 w-16 mx-auto text-muted-foreground mb-4" />
                <h3 className="text-xl font-semibold mb-2">Your cart is empty</h3>
                <p className="text-muted-foreground mb-6">
                  Start adding items to your cart to see them here.
                </p>
                <Button asChild>
                  <Link to="/products">Browse Products</Link>
                </Button>
              </CardContent>
            </Card>
          ) : (
            <div className="grid lg:grid-cols-3 gap-8">
              <div className="lg:col-span-2 space-y-4">
                {cartItems.map((item, index) => (
                  <Card key={`${item.productId}-${index}`}>
                    <CardContent className="p-6">
                      <div className="flex gap-4">
                        <div className="w-24 h-24 bg-muted rounded-lg flex items-center justify-center overflow-hidden">
                          {item.productImage ? (
                            <img
                              src={item.productImage}
                              alt={item.productName}
                              className="w-full h-full object-cover"
                            />
                          ) : (
                            <ShoppingCart className="h-8 w-8 text-muted-foreground" />
                          )}
                        </div>
                        <div className="flex-1">
                          <h3 className="font-semibold text-lg mb-1">
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
                          <div className="flex items-center gap-2 mb-3">
                            {item.salePrice && item.salePrice < item.price ? (
                              <>
                                <span className="text-lg font-bold text-primary">
                                  {formatPrice(item.salePrice)}
                                </span>
                                <span className="text-sm text-muted-foreground line-through">
                                  {formatPrice(item.price)}
                                </span>
                              </>
                            ) : (
                              <span className="text-lg font-bold">
                                {formatPrice(item.price)}
                              </span>
                            )}
                          </div>
                          <div className="flex items-center gap-3">
                            <div className="flex items-center border rounded-md">
                              <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8"
                                onClick={() => handleQuantityChange(item, item.quantity - 1)}
                                disabled={updateItemMutation.isPending || item.quantity <= 1}
                              >
                                <Minus className="h-4 w-4" />
                              </Button>
                              <span className="px-4 py-1 min-w-[3rem] text-center">
                                {item.quantity}
                              </span>
                              <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8"
                                onClick={() => handleQuantityChange(item, item.quantity + 1)}
                                disabled={updateItemMutation.isPending}
                              >
                                <Plus className="h-4 w-4" />
                              </Button>
                            </div>
                            <div className="text-sm text-muted-foreground">
                              Total: <span className="font-semibold">{formatPrice(item.itemTotal)}</span>
                            </div>
                          </div>
                        </div>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleRemoveItem(item)}
                          disabled={removeItemMutation.isPending}
                          className="text-destructive hover:text-destructive"
                        >
                          {removeItemMutation.isPending ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                          ) : (
                            <Trash2 className="h-4 w-4" />
                          )}
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
              <Card className="h-fit sticky top-4">
                <CardHeader>
                  <CardTitle>Order Summary</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Subtotal</span>
                      <span>{formatPrice(subtotal)}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Shipping</span>
                      <span>{shipping > 0 ? formatPrice(shipping) : 'Calculated at checkout'}</span>
                    </div>
                    {tax > 0 && (
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">Tax</span>
                        <span>{formatPrice(tax)}</span>
                      </div>
                    )}
                  </div>
                  <div className="flex justify-between font-bold text-lg pt-4 border-t">
                    <span>Total</span>
                    <span>{formatPrice(total)}</span>
                  </div>
                  <Button
                    className="w-full"
                    size="lg"
                    onClick={handleCheckout}
                    disabled={cartItems.length === 0}
                  >
                    Proceed to Checkout
                  </Button>
                  <Button variant="outline" className="w-full" asChild>
                    <Link to="/products">Continue Shopping</Link>
                  </Button>
                </CardContent>
              </Card>
            </div>
          )}
        </motion.div>
      </div>
    </ProtectedRoute>
  );
}
