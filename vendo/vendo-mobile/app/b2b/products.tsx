import { useState } from 'react';
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  TouchableOpacity,
  Image,
  TextInput,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { useRouter } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { productsApi } from '@/lib/api';
import { b2bApi } from '@/services/b2b-api';
import { Product } from '@vendo/shared';
import { Ionicons } from '@expo/vector-icons';

export default function B2BProducts() {
  const router = useRouter();
  const [selectedProduct, setSelectedProduct] = useState<number | null>(null);
  const [quantities, setQuantities] = useState<{ [key: number]: number }>({});
  const [pricing, setPricing] = useState<{ [key: number]: any }>({});

  const { data: products, isLoading } = useQuery({
    queryKey: ['products'],
    queryFn: async () => {
      const response = await productsApi.getProducts();
      return response.data;
    },
  });

  const handleGetPricing = async (productId: number) => {
    const quantity = quantities[productId] || 1;
    if (quantity < 1) {
      Alert.alert('Error', 'Quantity must be at least 1');
      return;
    }

    try {
      const price = await b2bApi.getPricing(productId, quantity);
      if (price) {
        setPricing({ ...pricing, [productId]: price });
        setSelectedProduct(productId);
      }
    } catch (error) {
      Alert.alert('Error', 'Failed to get B2B pricing');
    }
  };

  const handleQuantityChange = (productId: number, value: string) => {
    const qty = parseInt(value) || 1;
    setQuantities({ ...quantities, [productId]: qty });
    // Clear pricing when quantity changes
    if (pricing[productId]) {
      const newPricing = { ...pricing };
      delete newPricing[productId];
      setPricing(newPricing);
    }
  };

  if (isLoading) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color="#3b82f6" />
        <Text style={styles.loadingText}>Loading products...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>B2B Product Catalog</Text>
        <Text style={styles.subtitle}>Volume pricing available</Text>
      </View>

      <FlatList
        data={products}
        numColumns={2}
        contentContainerStyle={styles.list}
        keyExtractor={(item: Product) => item.id.toString()}
        renderItem={({ item }: { item: Product }) => {
          const quantity = quantities[item.id] || 1;
          const productPricing = pricing[item.id];
          const isSelected = selectedProduct === item.id;

          return (
            <View style={styles.productCard}>
              <TouchableOpacity
                onPress={() => router.push(`/product/${item.id}`)}
              >
                <Image
                  source={{
                    uri: item.images?.[0] || 'https://via.placeholder.com/200',
                  }}
                  style={styles.productImage}
                />
                <Text style={styles.productName} numberOfLines={2}>
                  {item.name}
                </Text>
                <Text style={styles.productPrice}>
                  ${item.discountPrice || item.price}
                </Text>
              </TouchableOpacity>

              <View style={styles.b2bSection}>
                <Text style={styles.b2bLabel}>B2B Pricing</Text>
                <TextInput
                  style={styles.quantityInput}
                  value={quantities[item.id]?.toString() || '1'}
                  onChangeText={(value) => handleQuantityChange(item.id, value)}
                  keyboardType="numeric"
                  placeholder="Qty"
                />
                <TouchableOpacity
                  style={[
                    styles.pricingButton,
                    isSelected && styles.pricingButtonActive,
                  ]}
                  onPress={() => handleGetPricing(item.id)}
                >
                  <Text
                    style={[
                      styles.pricingButtonText,
                      isSelected && styles.pricingButtonTextActive,
                    ]}
                  >
                    Get Pricing
                  </Text>
                </TouchableOpacity>

                {productPricing && (
                  <View style={styles.pricingInfo}>
                    <View style={styles.pricingRow}>
                      <Text style={styles.pricingLabel}>Unit Price:</Text>
                      <Text style={styles.pricingValue}>
                        ${productPricing.unitPrice.toFixed(2)}
                      </Text>
                    </View>
                    {productPricing.discountPercentage && (
                      <View style={styles.pricingRow}>
                        <Text style={styles.pricingLabel}>Discount:</Text>
                        <Text style={styles.pricingValue}>
                          {productPricing.discountPercentage.toFixed(1)}%
                        </Text>
                      </View>
                    )}
                    <View style={styles.pricingRow}>
                      <Text style={styles.pricingLabel}>Total:</Text>
                      <Text style={[styles.pricingValue, styles.pricingTotal]}>
                        ${productPricing.totalPrice.toFixed(2)}
                      </Text>
                    </View>
                  </View>
                )}
              </View>
            </View>
          );
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f9fafb',
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  loadingText: {
    marginTop: 12,
    fontSize: 16,
    color: '#6b7280',
  },
  header: {
    padding: 20,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 4,
    color: '#111827',
  },
  subtitle: {
    fontSize: 14,
    color: '#6b7280',
  },
  list: {
    padding: 12,
  },
  productCard: {
    flex: 1,
    margin: 6,
    backgroundColor: '#fff',
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
    padding: 12,
  },
  productImage: {
    width: '100%',
    height: 140,
    borderRadius: 8,
    marginBottom: 8,
    backgroundColor: '#f3f4f6',
  },
  productName: {
    fontSize: 14,
    fontWeight: '600',
    marginBottom: 4,
    color: '#111827',
  },
  productPrice: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#3b82f6',
    marginBottom: 12,
  },
  b2bSection: {
    borderTopWidth: 1,
    borderTopColor: '#e5e7eb',
    paddingTop: 12,
  },
  b2bLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: '#6b7280',
    marginBottom: 8,
  },
  quantityInput: {
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 8,
    padding: 8,
    marginBottom: 8,
    fontSize: 14,
    backgroundColor: '#f9fafb',
  },
  pricingButton: {
    backgroundColor: '#e5e7eb',
    borderRadius: 8,
    padding: 10,
    alignItems: 'center',
    marginBottom: 8,
  },
  pricingButtonActive: {
    backgroundColor: '#3b82f6',
  },
  pricingButtonText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#111827',
  },
  pricingButtonTextActive: {
    color: '#fff',
  },
  pricingInfo: {
    backgroundColor: '#f0f9ff',
    borderRadius: 8,
    padding: 8,
  },
  pricingRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 4,
  },
  pricingLabel: {
    fontSize: 11,
    color: '#6b7280',
  },
  pricingValue: {
    fontSize: 11,
    fontWeight: '600',
    color: '#111827',
  },
  pricingTotal: {
    fontSize: 14,
    color: '#3b82f6',
  },
});

