import { View, Text, FlatList, StyleSheet, TouchableOpacity, Image } from 'react-native';
import { useRouter } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { productsApi } from '@/lib/api';
import { Product } from '@vendo/shared';

export default function ProductsScreen() {
  const router = useRouter();
  const { data: products, isLoading } = useQuery({
    queryKey: ['products'],
    queryFn: async () => {
      const response = await productsApi.getProducts();
      return response.data;
    },
  });

  if (isLoading) {
    return (
      <View style={styles.container}>
        <Text>Loading products...</Text>
      </View>
    );
  }

  return (
    <FlatList
      data={products}
      numColumns={2}
      contentContainerStyle={styles.list}
      renderItem={({ item }: { item: Product }) => (
        <TouchableOpacity
          style={styles.productCard}
          onPress={() => router.push(`/product/${item.id}`)}
        >
          <Image
            source={{ uri: item.images?.[0] || 'https://via.placeholder.com/200' }}
            style={styles.productImage}
          />
          <Text style={styles.productName} numberOfLines={2}>
            {item.name}
          </Text>
          <Text style={styles.productPrice}>
            ${item.discountPrice || item.price}
          </Text>
        </TouchableOpacity>
      )}
      keyExtractor={(item: Product) => item.id.toString()}
    />
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
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
    height: 160,
    borderRadius: 8,
    marginBottom: 8,
    backgroundColor: '#f3f4f6',
  },
  productName: {
    fontSize: 14,
    fontWeight: '600',
    marginBottom: 4,
  },
  productPrice: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#3b82f6',
  },
});
