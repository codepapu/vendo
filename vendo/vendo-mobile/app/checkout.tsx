import { View, Text, ScrollView, StyleSheet, TouchableOpacity, TextInput, Alert, ActivityIndicator } from 'react-native';
import { useRouter } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '@/stores/auth-store';
import { ProtectedRoute } from '@/components/protected-route';
import { cartApi } from '@/services/cart-api';
import { addressApi } from '@/services/address-api';
import { shippingApi } from '@/services/shipping-api';
import { couponApi } from '@/services/coupon-api';
import { orderApi } from '@/services/order-api';
import { Ionicons } from '@expo/vector-icons';
import { useState, useEffect } from 'react';

function CheckoutScreen() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { user } = useAuthStore();
  const [selectedShippingAddressId, setSelectedShippingAddressId] = useState<number | null>(null);
  const [selectedShippingMethodId, setSelectedShippingMethodId] = useState<number | null>(null);
  const [couponCode, setCouponCode] = useState('');
  const [appliedCoupon, setAppliedCoupon] = useState<any>(null);
  const [orderNotes, setOrderNotes] = useState('');

  const { data: cart } = useQuery({
    queryKey: ['cart'],
    queryFn: () => cartApi.getCart(),
  });

  const { data: addresses = [] } = useQuery({
    queryKey: ['addresses'],
    queryFn: () => addressApi.getAddresses(),
  });

  const { data: shippingMethods = [] } = useQuery({
    queryKey: ['shippingMethods'],
    queryFn: () => shippingApi.getAvailableShippingMethods(),
  });

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

  const validateCouponMutation = useMutation({
    mutationFn: (code: string) => {
      const subtotal = cart?.subtotal || 0;
      return couponApi.validateCoupon(code, subtotal);
    },
    onSuccess: (data) => {
      if (data && data.isValid) {
        setAppliedCoupon(data);
        Alert.alert('Success', `Coupon applied! Discount: $${data.calculatedDiscount?.toFixed(2) || '0.00'}`);
      } else {
        Alert.alert('Invalid Coupon', data?.validationMessage || 'This coupon code is not valid');
        setAppliedCoupon(null);
      }
    },
  });

  const createOrderMutation = useMutation({
    mutationFn: (orderData: any) => orderApi.createOrder(orderData),
    onSuccess: (order) => {
      if (order) {
        queryClient.invalidateQueries({ queryKey: ['cart'] });
        queryClient.invalidateQueries({ queryKey: ['orders'] });
        router.push(`/orders/${order.id}`);
      }
    },
  });

  const handleCouponSubmit = () => {
    if (!couponCode.trim()) return;
    validateCouponMutation.mutate(couponCode);
  };

  const handleSubmit = () => {
    if (!cart || cart.items.length === 0) {
      Alert.alert('Error', 'Your cart is empty');
      return;
    }

    if (!selectedShippingAddressId) {
      Alert.alert('Error', 'Please select a shipping address');
      return;
    }

    if (!selectedShippingMethodId) {
      Alert.alert('Error', 'Please select a shipping method');
      return;
    }

    const shippingAddress = addresses.find((a) => a.id === selectedShippingAddressId);
    if (!shippingAddress) {
      Alert.alert('Error', 'Invalid shipping address');
      return;
    }

    const selectedShippingMethod = shippingMethods.find((m) => m.id === selectedShippingMethodId);
    const shippingCost = selectedShippingMethod?.baseCost || 0;

    const orderData = {
      items: cart.items.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
        variationId: item.variationId,
      })),
      shippingAddress: {
        name: shippingAddress.contactName || 'Customer',
        phone: shippingAddress.phone || '',
        email: user?.email || '',
        address: shippingAddress.addressLine1,
        city: shippingAddress.city,
        state: shippingAddress.state,
        zipCode: shippingAddress.postalCode,
        country: shippingAddress.country,
      },
      billingAddress: {
        name: shippingAddress.contactName || 'Customer',
        phone: shippingAddress.phone || '',
        email: user?.email || '',
        address: shippingAddress.addressLine1,
        city: shippingAddress.city,
        state: shippingAddress.state,
        zipCode: shippingAddress.postalCode,
        country: shippingAddress.country,
      },
      shippingMethod: selectedShippingMethod?.code || 'STANDARD',
      couponCode: appliedCoupon?.code || null,
      notes: orderNotes,
    };

    createOrderMutation.mutate(orderData);
  };

  if (!cart || cart.items.length === 0) {
    return (
      <View style={styles.centerContainer}>
        <Text style={styles.title}>Your cart is empty</Text>
        <TouchableOpacity style={styles.button} onPress={() => router.push('/(tabs)/cart')}>
          <Text style={styles.buttonText}>Go to Cart</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const subtotal = cart.subtotal;
  const tax = cart.tax || subtotal * 0.1;
  const selectedShippingMethod = shippingMethods.find((m) => m.id === selectedShippingMethodId);
  const shipping = selectedShippingMethod?.baseCost || 0;
  const discount = appliedCoupon?.calculatedDiscount || 0;
  const total = subtotal + tax + shipping - discount;

  return (
    <View style={styles.container}>
      <ScrollView style={styles.scrollView} contentContainerStyle={styles.scrollContent}>
        <Text style={styles.pageTitle}>Checkout</Text>

        {/* Shipping Address */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Shipping Address</Text>
          {addresses.length > 0 ? (
            addresses.map((address) => (
              <TouchableOpacity
                key={address.id}
                style={[
                  styles.addressCard,
                  selectedShippingAddressId === address.id && styles.addressCardSelected,
                ]}
                onPress={() => setSelectedShippingAddressId(address.id)}
              >
                <View style={styles.addressContent}>
                  <View style={styles.radioButton}>
                    {selectedShippingAddressId === address.id && (
                      <View style={styles.radioButtonSelected} />
                    )}
                  </View>
                  <View style={styles.addressInfo}>
                    <Text style={styles.addressName}>{address.contactName}</Text>
                    <Text style={styles.addressText}>
                      {address.addressLine1}
                      {address.addressLine2 && `, ${address.addressLine2}`}
                    </Text>
                    <Text style={styles.addressText}>
                      {address.city}, {address.state} {address.postalCode}
                    </Text>
                    <Text style={styles.addressText}>{address.country}</Text>
                    {address.isDefault && (
                      <Text style={styles.defaultBadge}>Default</Text>
                    )}
                  </View>
                </View>
              </TouchableOpacity>
            ))
          ) : (
            <View style={styles.emptyState}>
              <Text style={styles.emptyText}>No addresses found</Text>
              <TouchableOpacity style={styles.button} onPress={() => router.push('/profile')}>
                <Text style={styles.buttonText}>Add Address</Text>
              </TouchableOpacity>
            </View>
          )}
        </View>

        {/* Shipping Method */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Shipping Method</Text>
          {shippingMethods.map((method) => (
            <TouchableOpacity
              key={method.id}
              style={[
                styles.shippingCard,
                selectedShippingMethodId === method.id && styles.shippingCardSelected,
              ]}
              onPress={() => setSelectedShippingMethodId(method.id)}
            >
              <View style={styles.shippingContent}>
                <View style={styles.radioButton}>
                  {selectedShippingMethodId === method.id && (
                    <View style={styles.radioButtonSelected} />
                  )}
                </View>
                <View style={styles.shippingInfo}>
                  <Text style={styles.shippingName}>{method.name}</Text>
                  {method.description && (
                    <Text style={styles.shippingDescription}>{method.description}</Text>
                  )}
                  {method.estimatedDays && (
                    <Text style={styles.shippingEstimate}>
                      Estimated: {method.estimatedDays} business days
                    </Text>
                  )}
                </View>
                <Text style={styles.shippingPrice}>${method.baseCost.toFixed(2)}</Text>
              </View>
            </TouchableOpacity>
          ))}
        </View>

        {/* Coupon Code */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Coupon Code</Text>
          {appliedCoupon ? (
            <View style={styles.couponApplied}>
              <View style={styles.couponInfo}>
                <Text style={styles.couponCode}>{appliedCoupon.code}</Text>
                <Text style={styles.couponDiscount}>
                  Discount: ${appliedCoupon.calculatedDiscount?.toFixed(2) || '0.00'}
                </Text>
              </View>
              <TouchableOpacity
                onPress={() => {
                  setAppliedCoupon(null);
                  setCouponCode('');
                }}
              >
                <Ionicons name="close" size={24} color="#ef4444" />
              </TouchableOpacity>
            </View>
          ) : (
            <View style={styles.couponInput}>
              <TextInput
                style={styles.input}
                placeholder="Enter coupon code"
                value={couponCode}
                onChangeText={setCouponCode}
                autoCapitalize="characters"
              />
              <TouchableOpacity
                style={styles.applyButton}
                onPress={handleCouponSubmit}
                disabled={!couponCode.trim() || validateCouponMutation.isPending}
              >
                {validateCouponMutation.isPending ? (
                  <ActivityIndicator color="#fff" size="small" />
                ) : (
                  <Text style={styles.applyButtonText}>Apply</Text>
                )}
              </TouchableOpacity>
            </View>
          )}
        </View>

        {/* Order Notes */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Order Notes (Optional)</Text>
          <TextInput
            style={styles.textArea}
            placeholder="Special instructions for your order..."
            value={orderNotes}
            onChangeText={setOrderNotes}
            multiline
            numberOfLines={4}
          />
        </View>

        {/* Order Summary */}
        <View style={styles.summary}>
          <Text style={styles.summaryTitle}>Order Summary</Text>
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Subtotal</Text>
            <Text style={styles.summaryValue}>${subtotal.toFixed(2)}</Text>
          </View>
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Shipping</Text>
            <Text style={styles.summaryValue}>
              {selectedShippingMethod ? `$${shipping.toFixed(2)}` : 'Select method'}
            </Text>
          </View>
          {tax > 0 && (
            <View style={styles.summaryRow}>
              <Text style={styles.summaryLabel}>Tax</Text>
              <Text style={styles.summaryValue}>${tax.toFixed(2)}</Text>
            </View>
          )}
          {discount > 0 && (
            <View style={styles.summaryRow}>
              <Text style={[styles.summaryLabel, styles.discountLabel]}>Discount</Text>
              <Text style={[styles.summaryValue, styles.discountValue]}>
                -${discount.toFixed(2)}
              </Text>
            </View>
          )}
          <View style={[styles.summaryRow, styles.totalRow]}>
            <Text style={styles.totalLabel}>Total</Text>
            <Text style={styles.totalValue}>${total.toFixed(2)}</Text>
          </View>
        </View>
      </ScrollView>

      <View style={styles.footer}>
        <TouchableOpacity
          style={[
            styles.checkoutButton,
            (!selectedShippingAddressId || !selectedShippingMethodId || createOrderMutation.isPending) &&
              styles.checkoutButtonDisabled,
          ]}
          onPress={handleSubmit}
          disabled={!selectedShippingAddressId || !selectedShippingMethodId || createOrderMutation.isPending}
        >
          {createOrderMutation.isPending ? (
            <ActivityIndicator color="#fff" />
          ) : (
            <>
              <Text style={styles.checkoutButtonText}>Place Order</Text>
              <Ionicons name="arrow-forward" size={20} color="#fff" />
            </>
          )}
        </TouchableOpacity>
      </View>
    </View>
  );
}

export default function Checkout() {
  return (
    <ProtectedRoute>
      <CheckoutScreen />
    </ProtectedRoute>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  scrollView: {
    flex: 1,
  },
  scrollContent: {
    padding: 16,
    paddingBottom: 100,
  },
  pageTitle: {
    fontSize: 32,
    fontWeight: 'bold',
    marginBottom: 24,
    color: '#111827',
  },
  section: {
    marginBottom: 24,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 12,
    color: '#111827',
  },
  addressCard: {
    borderWidth: 1,
    borderColor: '#e5e7eb',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    backgroundColor: '#f9fafb',
  },
  addressCardSelected: {
    borderColor: '#3b82f6',
    backgroundColor: '#eff6ff',
  },
  addressContent: {
    flexDirection: 'row',
    alignItems: 'flex-start',
  },
  radioButton: {
    width: 24,
    height: 24,
    borderRadius: 12,
    borderWidth: 2,
    borderColor: '#3b82f6',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
    marginTop: 2,
  },
  radioButtonSelected: {
    width: 12,
    height: 12,
    borderRadius: 6,
    backgroundColor: '#3b82f6',
  },
  addressInfo: {
    flex: 1,
  },
  addressName: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 4,
    color: '#111827',
  },
  addressText: {
    fontSize: 14,
    color: '#6b7280',
    marginBottom: 2,
  },
  defaultBadge: {
    fontSize: 12,
    color: '#3b82f6',
    marginTop: 4,
  },
  shippingCard: {
    borderWidth: 1,
    borderColor: '#e5e7eb',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    backgroundColor: '#f9fafb',
  },
  shippingCardSelected: {
    borderColor: '#3b82f6',
    backgroundColor: '#eff6ff',
  },
  shippingContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  shippingInfo: {
    flex: 1,
    marginLeft: 12,
  },
  shippingName: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 4,
    color: '#111827',
  },
  shippingDescription: {
    fontSize: 14,
    color: '#6b7280',
    marginBottom: 2,
  },
  shippingEstimate: {
    fontSize: 12,
    color: '#9ca3af',
    marginTop: 4,
  },
  shippingPrice: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#3b82f6',
  },
  couponInput: {
    flexDirection: 'row',
    gap: 12,
  },
  input: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#e5e7eb',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    backgroundColor: '#fff',
  },
  applyButton: {
    backgroundColor: '#3b82f6',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
    justifyContent: 'center',
  },
  applyButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  couponApplied: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#d1fae5',
    padding: 16,
    borderRadius: 8,
  },
  couponInfo: {
    flex: 1,
  },
  couponCode: {
    fontSize: 16,
    fontWeight: '600',
    color: '#065f46',
    marginBottom: 4,
  },
  couponDiscount: {
    fontSize: 14,
    color: '#047857',
  },
  textArea: {
    borderWidth: 1,
    borderColor: '#e5e7eb',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    minHeight: 100,
    textAlignVertical: 'top',
    backgroundColor: '#fff',
  },
  summary: {
    backgroundColor: '#f9fafb',
    borderRadius: 12,
    padding: 16,
    marginTop: 8,
  },
  summaryTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 16,
    color: '#111827',
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  summaryLabel: {
    fontSize: 16,
    color: '#6b7280',
  },
  summaryValue: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  discountLabel: {
    color: '#059669',
  },
  discountValue: {
    color: '#059669',
  },
  totalRow: {
    marginTop: 8,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#e5e7eb',
  },
  totalLabel: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#111827',
  },
  totalValue: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#3b82f6',
  },
  footer: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    padding: 16,
    borderTopWidth: 1,
    borderTopColor: '#e5e7eb',
    backgroundColor: '#fff',
  },
  checkoutButton: {
    backgroundColor: '#3b82f6',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 16,
    borderRadius: 12,
    gap: 8,
  },
  checkoutButtonDisabled: {
    backgroundColor: '#9ca3af',
  },
  checkoutButtonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: '600',
  },
  emptyState: {
    alignItems: 'center',
    padding: 24,
  },
  emptyText: {
    fontSize: 16,
    color: '#6b7280',
    marginBottom: 16,
  },
  button: {
    backgroundColor: '#3b82f6',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 16,
  },
});

