import { useState, FormEvent, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { useAuthStore } from '@/stores/auth-store';
import { useB2BStore } from '@/stores/b2b-store';
import { type CartPricingResponse } from '@/services/cart-api';
import { b2bApi } from '@/services/b2b-api';
import { orderApi } from '@/services/order-api';
import { addressApi } from '@/services/address-api';
import { shippingApi } from '@/services/shipping-api';
import { formatPrice } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import { Loader2, Building2, CreditCard, MapPin, FileText, Truck } from 'lucide-react';

interface B2BCheckoutProps {
  cart: CartPricingResponse;
  onComplete: (poId: number) => void;
}

export function B2BCheckout({ cart, onComplete }: B2BCheckoutProps) {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const { currentBusinessAccount, loadBusinessAccount } = useB2BStore();

  // State
  const [selectedShippingAddressId, setSelectedShippingAddressId] = useState<number | null>(null);
  const [selectedShippingMethodId, setSelectedShippingMethodId] = useState<number | null>(null);
  const [poNotes, setPoNotes] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Fetch business account
  useEffect(() => {
    if (!currentBusinessAccount) {
      loadBusinessAccount();
    }
  }, [currentBusinessAccount, loadBusinessAccount]);

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

  // Set default selections
  useEffect(() => {
    if (addresses.length > 0 && !selectedShippingAddressId) {
      const defaultAddress = addresses.find((a) => a.isDefault) || addresses[0];
      setSelectedShippingAddressId(defaultAddress.id);
    }
  }, [addresses, selectedShippingAddressId]);

  useEffect(() => {
    if (shippingMethods.length > 0 && !selectedShippingMethodId) {
      setSelectedShippingMethodId(shippingMethods[0].id);
    }
  }, [shippingMethods, selectedShippingMethodId]);

  // Create order mutation (first step)
  const createOrderMutation = useMutation({
    mutationFn: (orderData: any) => orderApi.createOrder(orderData),
    onSuccess: async (order) => {
      if (order && order.id && currentBusinessAccount) {
        // After order is created, create PO
        createPOMutation.mutate({
          orderId: order.id,
          businessAccountId: currentBusinessAccount.id,
          notes: poNotes,
        });
      } else {
        toast({
          title: 'Order Creation Failed',
          description: 'Failed to create order. Please try again.',
          variant: 'destructive',
        });
        setIsSubmitting(false);
      }
    },
    onError: (error: any) => {
      toast({
        title: 'Order Creation Failed',
        description: error.message || 'Failed to create order. Please try again.',
        variant: 'destructive',
      });
      setIsSubmitting(false);
    },
  });

  // Create purchase order mutation
  const createPOMutation = useMutation({
    mutationFn: (poData: any) => b2bApi.createPurchaseOrder(poData),
    onSuccess: (po) => {
      queryClient.invalidateQueries({ queryKey: ['cart'] });
      queryClient.invalidateQueries({ queryKey: ['b2b-purchase-orders'] });
      toast({
        title: 'Purchase Order Created',
        description: `PO ${po.poNumber} has been created and is pending approval`,
      });
      onComplete(po.id);
    },
    onError: (error: any) => {
      toast({
        title: 'Purchase Order Creation Failed',
        description: error.message || 'Failed to create purchase order. Please try again.',
        variant: 'destructive',
      });
      setIsSubmitting(false);
    },
  });

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    if (!currentBusinessAccount) {
      toast({
        title: 'Business Account Required',
        description: 'Please ensure your business account is active',
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

    // Check credit limit
    const availableCredit = (currentBusinessAccount.creditLimit || 0) - (currentBusinessAccount.totalSpent || 0);
    if (cart.total > availableCredit) {
      toast({
        title: 'Insufficient Credit',
        description: `Order total exceeds available credit. Available: ${formatPrice(availableCredit)}`,
        variant: 'destructive',
      });
      return;
    }

    setIsSubmitting(true);

    const shippingAddress = addresses.find((a) => a.id === selectedShippingAddressId);
    if (!shippingAddress) {
      toast({
        title: 'Address error',
        description: 'Please select a valid shipping address',
        variant: 'destructive',
      });
      setIsSubmitting(false);
      return;
    }

    const selectedShippingMethod = shippingMethods.find((m) => m.id === selectedShippingMethodId);
    if (!selectedShippingMethod) {
      toast({
        title: 'Shipping method error',
        description: 'Please select a valid shipping method',
        variant: 'destructive',
      });
      setIsSubmitting(false);
      return;
    }

    // First create an order, then create PO from it
    const orderData = {
      items: cart.items.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
        retailerId: item.retailerId,
      })),
      shippingAddress: {
        name: shippingAddress.contactName || 'Customer',
        phone: shippingAddress.phone || '',
        email: useAuthStore.getState().user?.email || '',
        address: shippingAddress.addressLine1,
        city: shippingAddress.city,
        state: shippingAddress.state,
        zipCode: shippingAddress.postalCode,
        country: shippingAddress.country,
      },
      billingAddress: {
        name: shippingAddress.contactName || 'Customer',
        phone: shippingAddress.phone || '',
        email: useAuthStore.getState().user?.email || '',
        address: shippingAddress.addressLine1,
        city: shippingAddress.city,
        state: shippingAddress.state,
        zipCode: shippingAddress.postalCode,
        country: shippingAddress.country,
      },
      shippingMethod: selectedShippingMethod.code || 'STANDARD',
      notes: poNotes,
      orderType: 'B2B',
      businessAccountId: currentBusinessAccount.id,
    };

    createOrderMutation.mutate(orderData, {
      onSettled: () => setIsSubmitting(false),
    });
  };

  if (!currentBusinessAccount) {
    return (
      <div className="flex items-center justify-center py-8">
        <Loader2 className="h-8 w-8 animate-spin" />
      </div>
    );
  }

  const availableCredit = (currentBusinessAccount.creditLimit || 0) - (currentBusinessAccount.totalSpent || 0);
  const creditLimit = currentBusinessAccount.creditLimit || 0;
  const selectedShippingMethod = shippingMethods.find((m) => m.id === selectedShippingMethodId);
  const shipping = selectedShippingMethod?.baseCost || 0;
  const total = cart.subtotal + cart.tax + shipping;

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Credit Information */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <CreditCard className="h-5 w-5" />
            Credit Information
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          <div className="flex justify-between">
            <span className="text-muted-foreground">Credit Limit:</span>
            <span className="font-semibold">{formatPrice(creditLimit)}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Available Credit:</span>
            <span className={`font-semibold ${availableCredit < total ? 'text-destructive' : 'text-green-600'}`}>
              {formatPrice(availableCredit)}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Order Total:</span>
            <span className="font-semibold">{formatPrice(total)}</span>
          </div>
          {availableCredit < total && (
            <p className="text-sm text-destructive mt-2">
              Insufficient credit. Please contact your account manager to increase your credit limit.
            </p>
          )}
        </CardContent>
      </Card>

      {/* Volume Pricing Summary */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Building2 className="h-5 w-5" />
            Volume Pricing Summary
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            {cart.items.map((item) => (
              <div key={item.itemId} className="flex justify-between text-sm">
                <span className="text-muted-foreground">
                  {item.productName} x {item.quantity}
                  {item.isB2BPricing && (
                    <span className="ml-2 text-xs text-green-600">(B2B Pricing)</span>
                  )}
                </span>
                <span>{formatPrice(item.itemTotal)}</span>
              </div>
            ))}
          </div>
          <div className="mt-4 pt-4 border-t space-y-2">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Subtotal:</span>
              <span>{formatPrice(cart.subtotal)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Tax:</span>
              <span>{formatPrice(cart.tax)}</span>
            </div>
            <div className="flex justify-between font-semibold">
              <span>Total:</span>
              <span>{formatPrice(total)}</span>
            </div>
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
                      onChange={() => setSelectedShippingAddressId(address.id)}
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
                  <div className="text-sm text-muted-foreground">{method.description}</div>
                )}
              </div>
              <div className="font-semibold">{formatPrice(method.baseCost)}</div>
            </label>
          ))}
        </CardContent>
      </Card>

      {/* Purchase Order Notes */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            Purchase Order Notes
          </CardTitle>
          <CardDescription>
            Add any special instructions or notes for this purchase order
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Textarea
            placeholder="Enter purchase order notes..."
            value={poNotes}
            onChange={(e) => setPoNotes(e.target.value)}
            rows={4}
          />
        </CardContent>
      </Card>

      {/* Approval Workflow Info */}
      <Card>
        <CardHeader>
          <CardTitle>Approval Workflow</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground">
            Your purchase order will be submitted for approval. You will receive a notification once it has been
            reviewed and approved by our team.
          </p>
        </CardContent>
      </Card>

      {/* Submit Button */}
      <Button
        type="submit"
        className="w-full"
        size="lg"
        disabled={
          isSubmitting || 
          createOrderMutation.isPending || 
          createPOMutation.isPending ||
          !selectedShippingAddressId || 
          !selectedShippingMethodId || 
          availableCredit < total
        }
      >
        {(isSubmitting || createOrderMutation.isPending || createPOMutation.isPending) ? (
          <>
            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
            {createPOMutation.isPending ? 'Creating Purchase Order...' : 'Creating Order...'}
          </>
        ) : (
          'Create Purchase Order'
        )}
      </Button>
    </form>
  );
}

