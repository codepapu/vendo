import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { View, Text, ScrollView, StyleSheet, TextInput, TouchableOpacity, Image, ActivityIndicator } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';

export default function StoresScreen() {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');

  const { data: stores, isLoading } = useQuery({
    queryKey: ['stores', 'public'],
    queryFn: async () => {
      try {
        const API_BASE_URL = 'http://localhost:8080/api';
        const response = await fetch(`${API_BASE_URL}/v1/retailers?status=ACTIVE`, {
          headers: {
            'Content-Type': 'application/json',
          },
        });
        if (!response.ok) throw new Error('Failed to fetch stores');
        const data = await response.json();
        // Handle paginated response (content) or direct data array
        return data.data?.content || data.data || [];
      } catch (error) {
        console.error('Error fetching stores:', error);
        return [];
      }
    },
  });

  const filteredStores = stores?.filter((store: any) =>
    store.storeName.toLowerCase().includes(searchQuery.toLowerCase())
  ) || [];

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Our Stores</Text>
        <Text style={styles.subtitle}>
          Discover trusted retailers and shop from verified stores
        </Text>
      </View>

      <View style={styles.searchSection}>
        <View style={styles.searchContainer}>
          <Ionicons name="search" size={20} color="#6b7280" style={styles.searchIcon} />
          <TextInput
            style={styles.searchInput}
            placeholder="Search stores..."
            value={searchQuery}
            onChangeText={setSearchQuery}
          />
        </View>
      </View>

      {isLoading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#3b82f6" />
        </View>
      ) : filteredStores.length > 0 ? (
        <View style={styles.list}>
          {filteredStores.map((store: any) => (
            <TouchableOpacity
              key={store.id}
              style={styles.storeCard}
              onPress={() => router.push(`/stores/${store.storeSlug}`)}
            >
              <View style={styles.storeImageContainer}>
                {store.logoUrl ? (
                  <Image source={{ uri: store.logoUrl }} style={styles.storeImage} />
                ) : (
                  <Ionicons name="storefront" size={32} color="#9ca3af" />
                )}
              </View>
              <View style={styles.storeInfo}>
                <View style={styles.storeHeader}>
                  <Text style={styles.storeName}>{store.storeName}</Text>
                  {store.verified && (
                    <Ionicons name="checkmark-circle" size={20} color="#3b82f6" />
                  )}
                </View>
                {store.rating && (
                  <View style={styles.ratingContainer}>
                    <Ionicons name="star" size={16} color="#fbbf24" />
                    <Text style={styles.rating}>{store.rating.toFixed(1)}</Text>
                  </View>
                )}
                {store.description && (
                  <Text style={styles.storeDescription} numberOfLines={2}>
                    {store.description}
                  </Text>
                )}
                {store.totalSales && (
                  <Text style={styles.storeSales}>
                    {store.totalSales.toLocaleString()} sales
                  </Text>
                )}
              </View>
              <Ionicons name="chevron-forward" size={24} color="#9ca3af" />
            </TouchableOpacity>
          ))}
        </View>
      ) : (
        <View style={styles.emptyState}>
          <Ionicons name="storefront-outline" size={64} color="#9ca3af" />
          <Text style={styles.emptyText}>
            {searchQuery ? 'No stores found' : 'No stores available'}
          </Text>
        </View>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  header: {
    padding: 20,
    backgroundColor: '#f3f4f6',
    alignItems: 'center',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#1f2937',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#6b7280',
    textAlign: 'center',
  },
  searchSection: {
    padding: 20,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  searchContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 8,
    paddingHorizontal: 12,
    backgroundColor: '#fff',
  },
  searchIcon: {
    marginRight: 8,
  },
  searchInput: {
    flex: 1,
    fontSize: 16,
    paddingVertical: 12,
  },
  loadingContainer: {
    padding: 40,
    alignItems: 'center',
  },
  list: {
    padding: 12,
  },
  storeCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f9fafb',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
  },
  storeImageContainer: {
    width: 64,
    height: 64,
    borderRadius: 8,
    backgroundColor: '#e5e7eb',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  storeImage: {
    width: '100%',
    height: '100%',
    borderRadius: 8,
  },
  storeInfo: {
    flex: 1,
  },
  storeHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 4,
  },
  storeName: {
    fontSize: 18,
    fontWeight: '600',
    color: '#1f2937',
    marginRight: 8,
  },
  ratingContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 4,
  },
  rating: {
    fontSize: 14,
    fontWeight: '600',
    color: '#1f2937',
    marginLeft: 4,
  },
  storeDescription: {
    fontSize: 14,
    color: '#6b7280',
    marginBottom: 4,
  },
  storeSales: {
    fontSize: 12,
    color: '#9ca3af',
  },
  emptyState: {
    padding: 40,
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 16,
    color: '#6b7280',
    marginTop: 16,
  },
});

