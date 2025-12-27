import { useQuery } from '@tanstack/react-query';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity, Image, ActivityIndicator } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';

export default function CategoriesScreen() {
  const router = useRouter();

  const { data: categories, isLoading } = useQuery({
    queryKey: ['categories'],
    queryFn: async () => {
      // TODO: Implement getCategories API method
      return [];
    },
  });

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Shop by Category</Text>
        <Text style={styles.subtitle}>
          Browse our wide selection of products organized by category
        </Text>
      </View>

      {isLoading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#3b82f6" />
        </View>
      ) : categories && categories.length > 0 ? (
        <View style={styles.grid}>
          {categories.map((category: any) => (
            <TouchableOpacity
              key={category.id}
              style={styles.categoryCard}
              onPress={() => router.push(`/products?category=${category.id}`)}
            >
              <View style={styles.categoryImageContainer}>
                {category.image ? (
                  <Image source={{ uri: category.image }} style={styles.categoryImage} />
                ) : (
                  <Ionicons name="grid" size={48} color="#9ca3af" />
                )}
              </View>
              <Text style={styles.categoryName}>{category.name}</Text>
              <Text style={styles.categoryCount}>
                {category.productCount || 0} products
              </Text>
            </TouchableOpacity>
          ))}
        </View>
      ) : (
        <View style={styles.emptyState}>
          <Ionicons name="grid-outline" size={64} color="#9ca3af" />
          <Text style={styles.emptyText}>No categories available</Text>
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
  loadingContainer: {
    padding: 40,
    alignItems: 'center',
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    padding: 12,
  },
  categoryCard: {
    width: '48%',
    margin: '1%',
    backgroundColor: '#f9fafb',
    borderRadius: 12,
    overflow: 'hidden',
    marginBottom: 16,
  },
  categoryImageContainer: {
    width: '100%',
    height: 120,
    backgroundColor: '#e5e7eb',
    justifyContent: 'center',
    alignItems: 'center',
  },
  categoryImage: {
    width: '100%',
    height: '100%',
  },
  categoryName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1f2937',
    padding: 12,
    paddingBottom: 4,
  },
  categoryCount: {
    fontSize: 14,
    color: '#6b7280',
    paddingHorizontal: 12,
    paddingBottom: 12,
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

