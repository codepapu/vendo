import { useState, FormEvent, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { guestCheckoutApi, type GuestOrderRequest } from '@/services/guest-checkout-api';
import { shippingApi } from '@/services/shipping-api';
import { couponApi } from '@/services/coupon-api';
import { formatPrice } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import { Loader2, MapPin, Truck, CreditCard, Tag, UserPlus } from 'lucide-react';
import type { ShippingMethod, PaymentMethod, CouponResponse } from '@/types';

export function GuestCheckout() {
  const navigate = useNavigate();
  const { toast } = useToast();
  const queryClient = useQueryClient();

  // Form state
  const [formData, setFormData] = useState({
    email: '',
    firstName: '',
    lastName: '',
    phone: '',
    shippingAddress: {
      name: '',
      phone: '',
      email: '',
      address: '',
      city: '',
      state: '',
      zipCode: '',
      country: '',
    },
    billingAddress: {
      name: '',
      phone: '',
      email: '',
      address: '',
      city: '',
      state: '',
      zipCode: '',
      country: '',
    },
    useSameAddress: true,
    shippingMethod: '',
    paymentMethod: 'CREDIT_CARD' as PaymentMethod,
    couponCode: '',
    notes: '',
    createAccount: false,
    password: '',
    confirmPassword: '',
  });

  // Cart items (would come from localStorage or session)
  const [cartItems] = useState(() => {
    // In production, would get from localStorage or session
    const stored = localStorage.getItem('guestCart');
    return stored ? JSON.parse(stored) : [];
  });

  // Fetch shipping methods
  const { data: shippingMethods = [] } = useQuery({
    queryKey: ['shippingMethods'],
    queryFn: () => shippingApi.getAvailableShippingMethods(),
  });

  // Set default shipping method
  useEffect(() => {
    if (shippingMethods.length > 0 && !formData.shippingMethod) {
      setFormData((prev) => ({ ...prev, shippingMethod: shippingMethods[0].code }));
    }
  }, [shippingMethods]);

  // Validate coupon mutation
  const validateCouponMutation = useMutation({
    mutationFn: (code: string) => {
      const subtotal = calculateSubtotal();
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

  const [appliedCoupon, setAppliedCoupon] = useState<CouponResponse | null>(null);
  const [isValidatingCoupon, setIsValidatingCoupon] = useState(false);

  // Guest checkout mutation
  const guestCheckoutMutation = useMutation({
    mutationFn: (data: GuestOrderRequest) => guestCheckoutApi.processGuestCheckout(data),
    onSuccess: (response) => {
      if (response) {
        // Store session token
        localStorage.setItem('guestSessionToken', response.sessionToken);
        localStorage.removeItem('guestCart'); // Clear guest cart
        
        // Navigate to confirmation
        navigate(`/checkout/confirmation?orderId=${response.orderId}&orderCode=${response.orderNumber}&guest=true`);
        
        if (response.accountCreated) {
          toast({
            title: 'Account Created!',
            description: 'Your account has been created. Please check your email for verification.',
          });
        }
      }
    },
  });

  const calculateSubtotal = () => {
    return cartItems.reduce((sum: number, item: any) => sum + (item.price * item.quantity), 0);
  };

  const handleCouponSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!formData.couponCode.trim()) {
      return;
    }
    setIsValidatingCoupon(true);
    validateCouponMutation.mutate(formData.couponCode, {
      onSettled: () => setIsValidatingCoupon(false),
    });
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    if (cartItems.length === 0) {
      toast({
        title: 'Cart is empty',
        description: 'Please add items to your cart',
        variant: 'destructive',
      });
      return;
    }

    if (formData.createAccount && formData.password !== formData.confirmPassword) {
      toast({
        title: 'Password mismatch',
        description: 'Passwords do not match',
        variant: 'destructive',
      });
      return;
    }

    if (formData.createAccount && (!formData.password || formData.password.length < 6)) {
      toast({
        title: 'Invalid password',
        description: 'Password must be at least 6 characters',
        variant: 'destructive',
      });
      return;
    }

    const selectedShippingMethod = shippingMethods.find((m) => m.code === formData.shippingMethod);
    const shippingCost = selectedShippingMethod?.baseCost || 0;
    const subtotal = calculateSubtotal();
    const tax = subtotal * 0.1;
    const shipping = shippingCost;
    const discount = appliedCoupon?.calculatedDiscount || 0;

    const orderData: GuestOrderRequest = {
      items: cartItems.map((item: any) => ({
        productId: item.productId,
        quantity: item.quantity,
        variationId: item.variationId,
      })),
      shippingAddress: {
        name: formData.shippingAddress.name || `${formData.firstName} ${formData.lastName}`.trim(),
        phone: formData.shippingAddress.phone || formData.phone,
        email: formData.shippingAddress.email || formData.email,
        address: formData.shippingAddress.address,
        city: formData.shippingAddress.city,
        state: formData.shippingAddress.state,
        zipCode: formData.shippingAddress.zipCode,
        country: formData.shippingAddress.country,
      },
      billingAddress: formData.useSameAddress
        ? {
            name: formData.shippingAddress.name || `${formData.firstName} ${formData.lastName}`.trim(),
            phone: formData.shippingAddress.phone || formData.phone,
            email: formData.shippingAddress.email || formData.email,
            address: formData.shippingAddress.address,
            city: formData.shippingAddress.city,
            state: formData.shippingAddress.state,
            zipCode: formData.shippingAddress.zipCode,
            country: formData.shippingAddress.country,
          }
        : {
            name: formData.billingAddress.name || `${formData.firstName} ${formData.lastName}`.trim(),
            phone: formData.billingAddress.phone || formData.phone,
            email: formData.billingAddress.email || formData.email,
            address: formData.billingAddress.address,
            city: formData.billingAddress.city,
            state: formData.billingAddress.state,
            zipCode: formData.billingAddress.zipCode,
            country: formData.billingAddress.country,
          },
      email: formData.email,
      firstName: formData.firstName,
      lastName: formData.lastName,
      phone: formData.phone,
      shippingMethod: formData.shippingMethod,
      couponCode: appliedCoupon?.code || null,
      notes: formData.notes,
      createAccount: formData.createAccount || undefined,
      password: formData.createAccount ? formData.password : undefined,
    };

    guestCheckoutMutation.mutate(orderData);
  };

  if (cartItems.length === 0) {
    return (
      <div className="container py-8">
        <Card className="text-center py-12">
          <CardContent>
            <p className="text-muted-foreground mb-4">Your cart is empty</p>
            <Button asChild>
              <a href="/products">Browse Products</a>
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const subtotal = calculateSubtotal();
  const selectedShippingMethod = shippingMethods.find((m) => m.code === formData.shippingMethod);
  const shipping = selectedShippingMethod?.baseCost || 0;
  const tax = subtotal * 0.1;
  const discount = appliedCoupon?.calculatedDiscount || 0;
  const total = subtotal + tax + shipping - discount;

  return (
    <div className="container py-8">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="max-w-6xl mx-auto space-y-8"
      >
        <div>
          <h1 className="text-4xl font-bold mb-2">Guest Checkout</h1>
          <p className="text-muted-foreground">Complete your order as a guest</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="grid lg:grid-cols-3 gap-8">
            {/* Left Column - Forms */}
            <div className="lg:col-span-2 space-y-6">
              {/* Customer Information */}
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <UserPlus className="h-5 w-5" />
                    Customer Information
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="firstName">First Name *</Label>
                      <Input
                        id="firstName"
                        required
                        value={formData.firstName}
                        onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="lastName">Last Name</Label>
                      <Input
                        id="lastName"
                        value={formData.lastName}
                        onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                      />
                    </div>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="email">Email *</Label>
                    <Input
                      id="email"
                      type="email"
                      required
                      value={formData.email}
                      onChange={(e) => {
                        setFormData({
                          ...formData,
                          email: e.target.value,
                          shippingAddress: { ...formData.shippingAddress, email: e.target.value },
                        });
                      }}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="phone">Phone</Label>
                    <Input
                      id="phone"
                      type="tel"
                      value={formData.phone}
                      onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    />
                  </div>
                  
                  {/* Create Account Option */}
                  <div className="pt-4 border-t">
                    <label className="flex items-center gap-2 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={formData.createAccount}
                        onChange={(e) => setFormData({ ...formData, createAccount: e.target.checked })}
                        className="h-4 w-4 rounded border-gray-300"
                      />
                      <span className="text-sm font-medium">Create an account for faster checkout next time</span>
                    </label>
                    {formData.createAccount && (
                      <div className="mt-4 space-y-4 pl-6">
                        <div className="space-y-2">
                          <Label htmlFor="password">Password *</Label>
                          <Input
                            id="password"
                            type="password"
                            required={formData.createAccount}
                            minLength={6}
                            value={formData.password}
                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                          />
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="confirmPassword">Confirm Password *</Label>
                          <Input
                            id="confirmPassword"
                            type="password"
                            required={formData.createAccount}
                            minLength={6}
                            value={formData.confirmPassword}
                            onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                          />
                        </div>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>

              {/* Shipping Address */}
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <MapPin className="h-5 w-5" />
                    Shipping Address
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="shipping-name">Full Name *</Label>
                    <Input
                      id="shipping-name"
                      required
                      value={formData.shippingAddress.name}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          shippingAddress: { ...formData.shippingAddress, name: e.target.value },
                        })
                      }
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="shipping-address">Address *</Label>
                    <Input
                      id="shipping-address"
                      required
                      value={formData.shippingAddress.address}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          shippingAddress: { ...formData.shippingAddress, address: e.target.value },
                        })
                      }
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="shipping-city">City *</Label>
                      <Input
                        id="shipping-city"
                        required
                        value={formData.shippingAddress.city}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            shippingAddress: { ...formData.shippingAddress, city: e.target.value },
                          })
                        }
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="shipping-state">State *</Label>
                      <Input
                        id="shipping-state"
                        required
                        value={formData.shippingAddress.state}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            shippingAddress: { ...formData.shippingAddress, state: e.target.value },
                          })
                        }
                      />
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="shipping-zip">ZIP Code *</Label>
                      <Input
                        id="shipping-zip"
                        required
                        value={formData.shippingAddress.zipCode}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            shippingAddress: { ...formData.shippingAddress, zipCode: e.target.value },
                          })
                        }
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="shipping-country">Country *</Label>
                      <Input
                        id="shipping-country"
                        required
                        value={formData.shippingAddress.country}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            shippingAddress: { ...formData.shippingAddress, country: e.target.value },
                          })
                        }
                      />
                    </div>
                  </div>
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
                        checked={formData.useSameAddress}
                        onChange={(e) => setFormData({ ...formData, useSameAddress: e.target.checked })}
                      />
                      <span>Same as shipping address</span>
                    </label>
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {!formData.useSameAddress && (
                    <div className="space-y-4">
                      <div className="space-y-2">
                        <Label htmlFor="billing-name">Full Name *</Label>
                        <Input
                          id="billing-name"
                          required={!formData.useSameAddress}
                          value={formData.billingAddress.name}
                          onChange={(e) =>
                            setFormData({
                              ...formData,
                              billingAddress: { ...formData.billingAddress, name: e.target.value },
                            })
                          }
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="billing-address">Address *</Label>
                        <Input
                          id="billing-address"
                          required={!formData.useSameAddress}
                          value={formData.billingAddress.address}
                          onChange={(e) =>
                            setFormData({
                              ...formData,
                              billingAddress: { ...formData.billingAddress, address: e.target.value },
                            })
                          }
                        />
                      </div>
                      <div className="grid grid-cols-2 gap-4">
                        <div className="space-y-2">
                          <Label htmlFor="billing-city">City *</Label>
                          <Input
                            id="billing-city"
                            required={!formData.useSameAddress}
                            value={formData.billingAddress.city}
                            onChange={(e) =>
                              setFormData({
                                ...formData,
                                billingAddress: { ...formData.billingAddress, city: e.target.value },
                              })
                            }
                          />
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="billing-state">State *</Label>
                          <Input
                            id="billing-state"
                            required={!formData.useSameAddress}
                            value={formData.billingAddress.state}
                            onChange={(e) =>
                              setFormData({
                                ...formData,
                                billingAddress: { ...formData.billingAddress, state: e.target.value },
                              })
                            }
                          />
                        </div>
                      </div>
                      <div className="grid grid-cols-2 gap-4">
                        <div className="space-y-2">
                          <Label htmlFor="billing-zip">ZIP Code *</Label>
                          <Input
                            id="billing-zip"
                            required={!formData.useSameAddress}
                            value={formData.billingAddress.zipCode}
                            onChange={(e) =>
                              setFormData({
                                ...formData,
                                billingAddress: { ...formData.billingAddress, zipCode: e.target.value },
                              })
                            }
                          />
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="billing-country">Country *</Label>
                          <Input
                            id="billing-country"
                            required={!formData.useSameAddress}
                            value={formData.billingAddress.country}
                            onChange={(e) =>
                              setFormData({
                                ...formData,
                                billingAddress: { ...formData.billingAddress, country: e.target.value },
                              })
                            }
                          />
                        </div>
                      </div>
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
                        formData.shippingMethod === method.code
                          ? 'border-primary bg-primary/5'
                          : ''
                      }`}
                    >
                      <input
                        type="radio"
                        name="shippingMethod"
                        value={method.code}
                        checked={formData.shippingMethod === method.code}
                        onChange={(e) => setFormData({ ...formData, shippingMethod: e.target.value })}
                        className="mt-1"
                      />
                      <div className="flex-1">
                        <div className="font-medium">{method.name}</div>
                        {method.description && (
                          <div className="text-sm text-muted-foreground">{method.description}</div>
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
                        formData.paymentMethod === method
                          ? 'border-primary bg-primary/5'
                          : ''
                      }`}
                    >
                      <input
                        type="radio"
                        name="paymentMethod"
                        value={method}
                        checked={formData.paymentMethod === method}
                        onChange={(e) => setFormData({ ...formData, paymentMethod: e.target.value as PaymentMethod })}
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
                        onClick={() => {
                          setAppliedCoupon(null);
                          setFormData({ ...formData, couponCode: '' });
                        }}
                      >
                        <span className="sr-only">Remove</span>
                      </Button>
                    </div>
                  ) : (
                    <form onSubmit={handleCouponSubmit} className="flex gap-2">
                      <Input
                        placeholder="Enter coupon code"
                        value={formData.couponCode}
                        onChange={(e) => setFormData({ ...formData, couponCode: e.target.value.toUpperCase() })}
                        className="flex-1"
                      />
                      <Button type="submit" disabled={!formData.couponCode.trim() || isValidatingCoupon}>
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
                    value={formData.notes}
                    onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
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
                  {cartItems.map((item: any, index: number) => (
                    <div key={index} className="flex justify-between text-sm">
                      <span className="text-muted-foreground">
                        {item.productName || `Product ${item.productId}`} x {item.quantity}
                      </span>
                      <span>{formatPrice(item.price * item.quantity)}</span>
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
                    <span>{selectedShippingMethod ? formatPrice(shipping) : 'Select method'}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Tax</span>
                    <span>{formatPrice(tax)}</span>
                  </div>
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
                  disabled={guestCheckoutMutation.isPending || !formData.shippingMethod}
                >
                  {guestCheckoutMutation.isPending ? (
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
}
