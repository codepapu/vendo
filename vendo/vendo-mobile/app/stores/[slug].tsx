import { useQuery } from '@tanstack/react-query';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity, Image, ActivityIndicator, FlatList } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

export default function StoreDetailScreen() {
  const router = useRouter();
  const { slug } = useLocalSearchParams<{ slug: string }>();

  const { data: store, isLoading: isLoadingStore } = useQuery({
    queryKey: ['store', slug],
    queryFn: async () => {
      if (!slug) return null;
      // TODO: Implement getStoreBySlug API method
      return null;
    },
    enabled: !!slug,
  });

  const { data: products, isLoading: isLoadingProducts } = useQuery({
    queryKey: ['store-products', slug],
    queryFn: async () => {
      if (!slug) return null;
      // TODO: Implement getStoreProducts API method
      return [];
    },
    enabled: !!slug,
  });

  if (isLoadingStore) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#3b82f6" />
      </View>
    );
  }

  if (!store) {
    return (
      <View style={styles.container}>
        <View style={styles.emptyState}>
          <Ionicons name="storefront-outline" size={64} color="#9ca3af" />
          <Text style={styles.emptyTitle}>Store Not Found</Text>
          <Text style={styles.emptyText}>
            The store you're looking for doesn't exist or has been removed.
          </Text>
          <TouchableOpacity
            style={styles.button}
            onPress={() => router.back()}
          >
            <Text style={styles.buttonText}>Go Back</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity
          style={styles.backButton}
          onPress={() => router.back()}
        >
          <Ionicons name="arrow-back" size={24} color="#1f2937" />
        </TouchableOpacity>
        <View style={styles.storeHeader}>
          <View style={styles.storeImageContainer}>
            {store.logoUrl ? (
              <Image source={{ uri: store.logoUrl }} style={styles.storeImage} />
            ) : (
              <Ionicons name="storefront" size={48} color="#9ca3af" />
            )}
          </View>
          <View style={styles.storeInfo}>
            <View style={styles.storeTitleRow}>
              <Text style={styles.storeName}>{store.storeName}</Text>
              {store.verified && (
                <Ionicons name="checkmark-circle" size={24} color="#3b82f6" />
              )}
            </View>
            {store.rating && (
              <View style={styles.ratingContainer}>
                <Ionicons name="star" size={20} color="#fbbf24" />
                <Text style={styles.rating}>{store.rating.toFixed(1)}</Text>
                <Text style={styles.reviewCount}>({store.reviewCount || 0} reviews)</Text>
              </View>
            )}
            {store.description && (
              <Text style={styles.storeDescription}>{store.description}</Text>
            )}
            {store.totalSales && (
              <View style={styles.statsRow}>
                <Ionicons name="cube" size={16} color="#6b7280" />
                <Text style={styles.statsText}>
                  {store.totalSales.toLocaleString()} products sold
                </Text>
              </View>
            )}
          </View>
        </View>
      </View>

      <View style={styles.productsSection}>
        <Text style={styles.sectionTitle}>Products from {store.storeName}</Text>
        {isLoadingProducts ? (
          <View style={styles.loadingContainer}>
            <ActivityIndicator size="large" color="#3b82f6" />
          </View>
        ) : products && products.length > 0 ? (
          <FlatList
            data={products}
            numColumns={2}
            scrollEnabled={false}
            keyExtractor={(item: any) => item.id.toString()}
            renderItem={({ item }: any) => (
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
          />
        ) : (
          <View style={styles.emptyState}>
            <Ionicons name="cube-outline" size={64} color="#9ca3af" />
            <Text style={styles.emptyText}>
              This store doesn't have any products available yet.
            </Text>
          </View>
        )}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 40,
  },
  header: {
    padding: 20,
    backgroundColor: '#f3f4f6',
  },
  backButton: {
    marginBottom: 16,
  },
  storeHeader: {
    flexDirection: 'row',
    alignItems: 'flex-start',
  },
  storeImageContainer: {
    width: 80,
    height: 80,
    borderRadius: 12,
    backgroundColor: '#e5e7eb',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 16,
  },
  storeImage: {
    width: '100%',
    height: '100%',
    borderRadius: 12,
  },
  storeInfo: {
    flex: 1,
  },
  storeTitleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  storeName: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1f2937',
    marginRight: 8,
  },
  ratingContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  rating: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1f2937',
    marginLeft: 4,
  },
  reviewCount: {
    fontSize: 14,
    color: '#6b7280',
    marginLeft: 4,
  },
  storeDescription: {
    fontSize: 14,
    color: '#4b5563',
    lineHeight: 20,
    marginBottom: 8,
  },
  statsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 4,
  },
  statsText: {
    fontSize: 14,
    color: '#6b7280',
    marginLeft: 8,
  },
  productsSection: {
    padding: 20,
  },
  sectionTitle: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#1f2937',
    marginBottom: 16,
  },
  productCard: {
    width: '48%',
    margin: '1%',
    backgroundColor: '#f9fafb',
    borderRadius: 12,
    overflow: 'hidden',
    marginBottom: 16,
  },
  productImage: {
    width: '100%',
    height: 150,
    backgroundColor: '#e5e7eb',
  },
  productName: {
    fontSize: 14,
    fontWeight: '600',
    color: '#1f2937',
    padding: 12,
    paddingBottom: 4,
  },
  productPrice: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#3b82f6',
    paddingHorizontal: 12,
    paddingBottom: 12,
  },
  emptyState: {
    padding: 40,
    alignItems: 'center',
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#1f2937',
    marginTop: 16,
    marginBottom: 8,
  },
  emptyText: {
    fontSize: 16,
    color: '#6b7280',
    textAlign: 'center',
    marginBottom: 24,
  },
  button: {
    backgroundColor: '#3b82f6',
    padding: 14,
    borderRadius: 8,
    paddingHorizontal: 24,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
});

