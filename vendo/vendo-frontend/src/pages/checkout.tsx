import { useState, FormEvent, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { ProtectedRoute } from '@/components/protected-route';
import { useAuthStore } from '@/stores/auth-store';
import { cartApi } from '@/services/cart-api';
import { addressApi } from '@/services/address-api';
import { shippingApi } from '@/services/shipping-api';
import { couponApi } from '@/services/coupon-api';
import { orderApi } from '@/services/order-api';
import { userApi } from '@/services/user-api';
import { formatPrice } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import { Loader2, Check, X, MapPin, Truck, CreditCard, Tag } from 'lucide-react';
import type { AddressResponse, ShippingMethod, PaymentMethod, CouponResponse } from '@/types';

export function Checkout() {
  const navigate = useNavigate();
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const { isAuthenticated } = useAuthStore();

  // State
  const [selectedShippingAddressId, setSelectedShippingAddressId] = useState<number | null>(null);
  const [selectedBillingAddressId, setSelectedBillingAddressId] = useState<number | null>(null);
  const [useSameAddress, setUseSameAddress] = useState(true);
  const [selectedShippingMethodId, setSelectedShippingMethodId] = useState<number | null>(null);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<PaymentMethod>('CREDIT_CARD');
  const [couponCode, setCouponCode] = useState('');
  const [appliedCoupon, setAppliedCoupon] = useState<CouponResponse | null>(null);
  const [isValidatingCoupon, setIsValidatingCoupon] = useState(false);
  const [orderNotes, setOrderNotes] = useState('');

  // Fetch cart
  const { data: cart } = useQuery({
    queryKey: ['cart'],
    queryFn: () => cartApi.getCart(),
  });

  // Fetch addresses
  const { data: addresses = [] } = useQuery({
    queryKey: ['addresses'],
    queryFn: () => addressApi.getAddresses(),
  });

  // Fetch shipping methods
  const { data: shippingMethods = [] } = useQuery({
    queryKey: ['shippingMethods'],
    queryFn: () => shippingApi.getAvailableShippingMethods(),
  });

  // Fetch user profile for email
  const { data: userProfile } = useQuery({
    queryKey: ['userProfile'],
    queryFn: () => userApi.getProfile(),
    enabled: !!useAuthStore.getState().isAuthenticated,
  });

  // Set default selections
  useEffect(() => {
    if (addresses.length > 0 && !selectedShippingAddressId) {
      const defaultAddress = addresses.find((a) => a.isDefault) || addresses[0];
      setSelectedShippingAddressId(defaultAddress.id);
      if (useSameAddress) {
        setSelectedBillingAddressId(defaultAddress.id);
      }
    }
  }, [addresses, selectedShippingAddressId, useSameAddress]);

  useEffect(() => {
    if (shippingMethods.length > 0 && !selectedShippingMethodId) {
      setSelectedShippingMethodId(shippingMethods[0].id);
    }
  }, [shippingMethods, selectedShippingMethodId]);

  // Validate coupon mutation
  const validateCouponMutation = useMutation({
    mutationFn: (code: string) => {
      const subtotal = cart?.subtotal || 0;
      return couponApi.validateCoupon(code, subtotal);
    },
    onSuccess: (data) => {
      if (data && data.isValid) {
        setAppliedCoupon(data);
        toast({
          title: 'Coupon applied!',
          description: `Discount: ${data.calculatedDiscount ? formatPrice(data.calculatedDiscount) : 'Applied'}`,
        });
      } else {
        toast({
          title: 'Invalid coupon',
          description: data?.validationMessage || 'This coupon code is not valid',
          variant: 'destructive',
        });
        setAppliedCoupon(null);
      }
    },
  });

  // Create order mutation
  const createOrderMutation = useMutation({
    mutationFn: (orderData: any) => orderApi.createOrder(orderData),
    onSuccess: (order) => {
      if (order) {
        queryClient.invalidateQueries({ queryKey: ['cart'] });
        queryClient.invalidateQueries({ queryKey: ['orders'] });
        navigate(`/checkout/confirmation?orderId=${order.id}&orderCode=${order.code || order.id}`);
      }
    },
  });

  const handleCouponSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!couponCode.trim()) {
      return;
    }
    setIsValidatingCoupon(true);
    validateCouponMutation.mutate(couponCode, {
      onSettled: () => setIsValidatingCoupon(false),
    });
  };

  const handleRemoveCoupon = () => {
    setAppliedCoupon(null);
    setCouponCode('');
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    if (!cart || cart.items.length === 0) {
      toast({
        title: 'Cart is empty',
        description: 'Please add items to your cart',
        variant: 'destructive',
      });
      return;
    }

    if (!selectedShippingAddressId) {
      toast({
        title: 'Shipping address required',
        description: 'Please select a shipping address',
        variant: 'destructive',
      });
      return;
    }

    if (!selectedShippingMethodId) {
      toast({
        title: 'Shipping method required',
        description: 'Please select a shipping method',
        variant: 'destructive',
      });
      return;
    }

    const shippingAddress = addresses.find((a) => a.id === selectedShippingAddressId);
    const billingAddress = useSameAddress
      ? shippingAddress
      : addresses.find((a) => a.id === selectedBillingAddressId);

    if (!shippingAddress || !billingAddress) {
      toast({
        title: 'Address error',
        description: 'Please select valid addresses',
        variant: 'destructive',
      });
      return;
    }

    const selectedShippingMethod = shippingMethods.find((m) => m.id === selectedShippingMethodId);
    const shippingCost = selectedShippingMethod?.baseCost || 0;

    // Calculate totals
    const subtotal = cart.subtotal;
    const tax = cart.tax || subtotal * 0.1; // 10% tax if not calculated
    const shipping = shippingCost;
    const discount = appliedCoupon?.calculatedDiscount || 0;
    const total = subtotal + tax + shipping - discount;

    const userEmail = userProfile?.email || useAuthStore.getState().user?.email || '';

    const orderData = {
      items: cart.items.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
        variationId: item.variationId,
      })),
      shippingAddress: {
        name: shippingAddress.contactName || 'Customer',
        phone: shippingAddress.phone || '',
        email: userEmail,
        address: shippingAddress.addressLine1,
        city: shippingAddress.city,
        state: shippingAddress.state,
        zipCode: shippingAddress.postalCode,
        country: shippingAddress.country,
      },
      billingAddress: {
        name: billingAddress.contactName || 'Customer',
        phone: billingAddress.phone || '',
        email: userEmail,
        address: billingAddress.addressLine1,
        city: billingAddress.city,
        state: billingAddress.state,
        zipCode: billingAddress.postalCode,
        country: billingAddress.country,
      },
      shippingMethod: selectedShippingMethod?.code || 'STANDARD',
      couponCode: appliedCoupon?.code || null,
      notes: orderNotes,
    };

    createOrderMutation.mutate(orderData);
  };

  if (!cart || cart.items.length === 0) {
    return (
      <ProtectedRoute>
        <div className="container py-8">
          <Card className="text-center py-12">
            <CardContent>
              <p className="text-muted-foreground mb-4">Your cart is empty</p>
              <Button asChild>
                <a href="/cart">Go to Cart</a>
              </Button>
            </CardContent>
          </Card>
        </div>
      </ProtectedRoute>
    );
  }

  const subtotal = cart.subtotal;
  const tax = cart.tax || subtotal * 0.1;
  const selectedShippingMethod = shippingMethods.find((m) => m.id === selectedShippingMethodId);
  const shipping = selectedShippingMethod?.baseCost || 0;
  const discount = appliedCoupon?.calculatedDiscount || 0;
  const total = subtotal + tax + shipping - discount;

  // Guest checkout option - show login prompt but allow continuation
  const CheckoutContent = () => (
    <div className="container py-8">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="max-w-6xl mx-auto space-y-8"
        >
          <div>
            <h1 className="text-4xl font-bold mb-2">Checkout</h1>
            <p className="text-muted-foreground">Complete your order</p>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="grid lg:grid-cols-3 gap-8">
              {/* Left Column - Forms */}
              <div className="lg:col-span-2 space-y-6">
                {/* Shipping Address */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <MapPin className="h-5 w-5" />
                      Shipping Address
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    {addresses.length > 0 ? (
                      <>
                        <div className="space-y-2">
                          {addresses.map((address) => (
                            <label
                              key={address.id}
                              className={`flex items-start gap-3 p-4 border rounded-lg cursor-pointer hover:bg-muted transition-colors ${
                                selectedShippingAddressId === address.id
                                  ? 'border-primary bg-primary/5'
                                  : ''
                              }`}
                            >
                              <input
                                type="radio"
                                name="shippingAddress"
                                value={address.id}
                                checked={selectedShippingAddressId === address.id}
                                onChange={() => {
                                  setSelectedShippingAddressId(address.id);
                                  if (useSameAddress) {
                                    setSelectedBillingAddressId(address.id);
                                  }
                                }}
                                className="mt-1"
                              />
                              <div className="flex-1">
                                <div className="font-medium">{address.contactName}</div>
                                <div className="text-sm text-muted-foreground">
                                  {address.addressLine1}
                                  {address.addressLine2 && `, ${address.addressLine2}`}
                                  <br />
                                  {address.city}, {address.state} {address.postalCode}
                                  <br />
                                  {address.country}
                                </div>
                                {address.isDefault && (
                                  <span className="text-xs text-primary mt-1 inline-block">
                                    Default
                                  </span>
                                )}
                              </div>
                            </label>
                          ))}
                        </div>
                        <Button type="button" variant="outline" asChild>
                          <a href="/profile/addresses">Manage Addresses</a>
                        </Button>
                      </>
                    ) : (
                      <div className="text-center py-4">
                        <p className="text-muted-foreground mb-4">No addresses found</p>
                        <Button type="button" variant="outline" asChild>
                          <a href="/profile/addresses">Add Address</a>
                        </Button>
                      </div>
                    )}
                  </CardContent>
                </Card>

                {/* Billing Address */}
                <Card>
                  <CardHeader>
                    <CardTitle>Billing Address</CardTitle>
                    <CardDescription>
                      <label className="flex items-center gap-2 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={useSameAddress}
                          onChange={(e) => {
                            setUseSameAddress(e.target.checked);
                            if (e.target.checked) {
                              setSelectedBillingAddressId(selectedShippingAddressId);
                            }
                          }}
                        />
                        <span>Same as shipping address</span>
                      </label>
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    {!useSameAddress && (
                      <div className="space-y-2">
                        {addresses.map((address) => (
                          <label
                            key={address.id}
                            className={`flex items-start gap-3 p-4 border rounded-lg cursor-pointer hover:bg-muted transition-colors ${
                              selectedBillingAddressId === address.id
                                ? 'border-primary bg-primary/5'
                                : ''
                            }`}
                          >
                            <input
                              type="radio"
                              name="billingAddress"
                              value={address.id}
                              checked={selectedBillingAddressId === address.id}
                              onChange={() => setSelectedBillingAddressId(address.id)}
                              className="mt-1"
                            />
                            <div className="flex-1">
                              <div className="font-medium">{address.contactName}</div>
                              <div className="text-sm text-muted-foreground">
                                {address.addressLine1}
                                {address.addressLine2 && `, ${address.addressLine2}`}
                                <br />
                                {address.city}, {address.state} {address.postalCode}
                                <br />
                                {address.country}
                              </div>
                            </div>
                          </label>
                        ))}
                      </div>
                    )}
                  </CardContent>
                </Card>

                {/* Shipping Method */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Truck className="h-5 w-5" />
                      Shipping Method
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-2">
                    {shippingMethods.map((method) => (
                      <label
                        key={method.id}
                        className={`flex items-start gap-3 p-4 border rounded-lg cursor-pointer hover:bg-muted transition-colors ${
                          selectedShippingMethodId === method.id
                            ? 'border-primary bg-primary/5'
                            : ''
                        }`}
                      >
                        <input
                          type="radio"
                          name="shippingMethod"
                          value={method.id}
                          checked={selectedShippingMethodId === method.id}
                          onChange={() => setSelectedShippingMethodId(method.id)}
                          className="mt-1"
                        />
                        <div className="flex-1">
                          <div className="font-medium">{method.name}</div>
                          {method.description && (
                            <div className="text-sm text-muted-foreground">
                              {method.description}
                            </div>
                          )}
                          {method.estimatedDays && (
                            <div className="text-xs text-muted-foreground mt-1">
                              Estimated delivery: {method.estimatedDays} business days
                            </div>
                          )}
                        </div>
                        <div className="font-semibold">{formatPrice(method.baseCost)}</div>
                      </label>
                    ))}
                  </CardContent>
                </Card>

                {/* Payment Method */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <CreditCard className="h-5 w-5" />
                      Payment Method
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-2">
                    {(['CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL', 'CASH_ON_DELIVERY'] as PaymentMethod[]).map((method) => (
                      <label
                        key={method}
                        className={`flex items-center gap-3 p-4 border rounded-lg cursor-pointer hover:bg-muted transition-colors ${
                          selectedPaymentMethod === method
                            ? 'border-primary bg-primary/5'
                            : ''
                        }`}
                      >
                        <input
                          type="radio"
                          name="paymentMethod"
                          value={method}
                          checked={selectedPaymentMethod === method}
                          onChange={(e) => setSelectedPaymentMethod(e.target.value as PaymentMethod)}
                        />
                        <span className="font-medium">
                          {method.replace(/_/g, ' ').replace(/\b\w/g, (l) => l.toUpperCase())}
                        </span>
                      </label>
                    ))}
                  </CardContent>
                </Card>

                {/* Coupon Code */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Tag className="h-5 w-5" />
                      Coupon Code
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    {appliedCoupon ? (
                      <div className="flex items-center justify-between p-4 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
                        <div>
                          <div className="font-medium text-green-800 dark:text-green-200">
                            {appliedCoupon.code}
                          </div>
                          <div className="text-sm text-green-600 dark:text-green-300">
                            {appliedCoupon.name}
                            {appliedCoupon.calculatedDiscount && (
                              <> - {formatPrice(appliedCoupon.calculatedDiscount)} discount</>
                            )}
                          </div>
                        </div>
                        <Button
                          type="button"
                          variant="ghost"
                          size="icon"
                          onClick={handleRemoveCoupon}
                        >
                          <X className="h-4 w-4" />
                        </Button>
                      </div>
                    ) : (
                      <form onSubmit={handleCouponSubmit} className="flex gap-2">
                        <Input
                          placeholder="Enter coupon code"
                          value={couponCode}
                          onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
                          className="flex-1"
                        />
                        <Button
                          type="submit"
                          disabled={!couponCode.trim() || isValidatingCoupon}
                        >
                          {isValidatingCoupon ? (
                            <>
                              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                              Validating...
                            </>
                          ) : (
                            'Apply'
                          )}
                        </Button>
                      </form>
                    )}
                  </CardContent>
                </Card>

                {/* Order Notes */}
                <Card>
                  <CardHeader>
                    <CardTitle>Order Notes (Optional)</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <textarea
                      className="w-full min-h-[100px] rounded-md border border-input bg-background px-3 py-2 text-sm"
                      placeholder="Special instructions for your order..."
                      value={orderNotes}
                      onChange={(e) => setOrderNotes(e.target.value)}
                    />
                  </CardContent>
                </Card>
              </div>

              {/* Right Column - Order Summary */}
              <Card className="h-fit sticky top-4">
                <CardHeader>
                  <CardTitle>Order Summary</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    {cart.items.map((item, index) => (
                      <div key={`${item.productId}-${index}`} className="flex justify-between text-sm">
                        <span className="text-muted-foreground">
                          {item.productName} x {item.quantity}
                        </span>
                        <span>{formatPrice(item.itemTotal)}</span>
                      </div>
                    ))}
                  </div>
                  <div className="space-y-2 pt-4 border-t">
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Subtotal</span>
                      <span>{formatPrice(subtotal)}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Shipping</span>
                      <span>
                        {selectedShippingMethod
                          ? formatPrice(shipping)
                          : 'Select method'}
                      </span>
                    </div>
                    {tax > 0 && (
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">Tax</span>
                        <span>{formatPrice(tax)}</span>
                      </div>
                    )}
                    {discount > 0 && (
                      <div className="flex justify-between text-sm text-green-600 dark:text-green-400">
                        <span>Discount</span>
                        <span>-{formatPrice(discount)}</span>
                      </div>
                    )}
                  </div>
                  <div className="flex justify-between font-bold text-lg pt-4 border-t">
                    <span>Total</span>
                    <span>{formatPrice(total)}</span>
                  </div>
                  <Button
                    type="submit"
                    className="w-full"
                    size="lg"
                    disabled={createOrderMutation.isPending || !selectedShippingAddressId || !selectedShippingMethodId}
                  >
                    {createOrderMutation.isPending ? (
                      <>
                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        Processing...
                      </>
                    ) : (
                      'Place Order'
                    )}
                  </Button>
                </CardContent>
              </Card>
            </div>
          </form>
        </motion.div>
      </div>
  );

  // If not authenticated, show guest checkout option
  if (!isAuthenticated) {
    return (
      <div className="container py-8">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="max-w-2xl mx-auto space-y-6"
        >
          <Card>
            <CardHeader>
              <CardTitle>Checkout Options</CardTitle>
              <CardDescription>
                Choose how you'd like to proceed with checkout
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <p className="text-sm text-muted-foreground">
                  You can checkout as a guest or create an account for faster checkout next time.
                </p>
              </div>
              <div className="flex gap-4">
                <Button className="flex-1" asChild>
                  <Link to="/login?redirect=/checkout">Login to Checkout</Link>
                </Button>
                <Button variant="outline" className="flex-1" asChild>
                  <Link to="/register?redirect=/checkout">Create Account</Link>
                </Button>
              </div>
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <span className="w-full border-t" />
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                  <span className="bg-background px-2 text-muted-foreground">Or</span>
                </div>
              </div>
              <Button
                variant="outline"
                className="w-full"
                asChild
              >
                <Link to="/checkout/guest">Continue as Guest</Link>
              </Button>
            </CardContent>
          </Card>
        </motion.div>
      </div>
    );
  }

  return <ProtectedRoute>{CheckoutContent()}</ProtectedRoute>;
}
