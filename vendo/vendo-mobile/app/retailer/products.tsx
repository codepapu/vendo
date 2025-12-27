import { useState, useEffect } from 'react';
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
  Modal,
  ScrollView,
} from 'react-native';
import { useRouter } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
// Note: expo-image-picker needs to be installed: npx expo install expo-image-picker
// import * as ImagePicker from 'expo-image-picker';
import { retailerApi } from '@/services/retailer-api';
import { productsApi } from '@/lib/api';
import { useAuthStore } from '@/stores/auth-store';
import { Ionicons } from '@expo/vector-icons';

export default function RetailerProducts() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { user } = useAuthStore();
  const [searchTerm, setSearchTerm] = useState('');
  const [isActiveFilter, setIsActiveFilter] = useState<boolean | undefined>(undefined);
  const [isAddProductOpen, setIsAddProductOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<any>(null);
  const [productFormData, setProductFormData] = useState({
    price: '',
    compareAtPrice: '',
    costPrice: '',
    stockQuantity: '',
    lowStockThreshold: '10',
  });
  const [uploadingImage, setUploadingImage] = useState(false);

  const { data: retailer } = useQuery({
    queryKey: ['retailer', user?.id],
    queryFn: () => (user?.id ? retailerApi.getRetailerByUserId(user.id) : null),
    enabled: !!user?.id,
  });

  const { data: productsData, isLoading } = useQuery({
    queryKey: ['retailer-products', retailer?.id, searchTerm, isActiveFilter],
    queryFn: () =>
      retailer?.id
        ? retailerApi.getProducts(retailer.id, {
            searchTerm: searchTerm || undefined,
            isActive: isActiveFilter,
          })
        : null,
    enabled: !!retailer?.id,
  });

  const { data: availableProducts } = useQuery({
    queryKey: ['available-products'],
    queryFn: async () => {
      const response = await productsApi.getProducts();
      return response.data;
    },
    enabled: isAddProductOpen,
  });

  const addProductMutation = useMutation({
    mutationFn: (data: any) =>
      retailer?.id ? retailerApi.addProduct(retailer.id, data) : null,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['retailer-products'] });
      setIsAddProductOpen(false);
      setSelectedProduct(null);
      setProductFormData({
        price: '',
        compareAtPrice: '',
        costPrice: '',
        stockQuantity: '',
        lowStockThreshold: '10',
      });
      Alert.alert('Success', 'Product added successfully');
    },
    onError: () => {
      Alert.alert('Error', 'Failed to add product');
    },
  });

  const updateProductMutation = useMutation({
    mutationFn: ({ productRetailerId, data }: { productRetailerId: number; data: any }) =>
      retailer?.id
        ? retailerApi.updateProduct(retailer.id, productRetailerId, data)
        : null,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['retailer-products'] });
      Alert.alert('Success', 'Product updated successfully');
    },
    onError: () => {
      Alert.alert('Error', 'Failed to update product');
    },
  });

  const pickImage = async () => {
    // Note: Install expo-image-picker: npx expo install expo-image-picker
    // Then uncomment the code below
    Alert.alert(
      'Image Upload',
      'Image upload functionality requires expo-image-picker. Install it with: npx expo install expo-image-picker'
    );
    
    /* Uncomment after installing expo-image-picker:
    const permissionResult = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permissionResult.granted) {
      Alert.alert('Permission Required', 'Please grant camera roll permissions');
      return;
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [4, 3],
      quality: 0.8,
    });

    if (!result.canceled && result.assets[0]) {
      setUploadingImage(true);
      try {
        const imageUrl = await retailerApi.uploadProductImage(result.assets[0]);
        if (imageUrl) {
          Alert.alert('Success', 'Image uploaded successfully');
        }
      } catch (error) {
        Alert.alert('Error', 'Failed to upload image');
      } finally {
        setUploadingImage(false);
      }
    }
    */
  };

  const handleAddProduct = () => {
    if (!selectedProduct || !productFormData.price) {
      Alert.alert('Error', 'Please select a product and enter a price');
      return;
    }

    addProductMutation.mutate({
      productId: selectedProduct.id,
      price: parseFloat(productFormData.price),
      compareAtPrice: productFormData.compareAtPrice
        ? parseFloat(productFormData.compareAtPrice)
        : undefined,
      costPrice: productFormData.costPrice
        ? parseFloat(productFormData.costPrice)
        : undefined,
      stockQuantity: productFormData.stockQuantity
        ? parseInt(productFormData.stockQuantity)
        : 0,
      lowStockThreshold: productFormData.lowStockThreshold
        ? parseInt(productFormData.lowStockThreshold)
        : 10,
      isActive: true,
    });
  };

  const handleUpdatePrice = (productRetailerId: number, newPrice: string) => {
    if (!newPrice || isNaN(parseFloat(newPrice))) {
      Alert.alert('Error', 'Please enter a valid price');
      return;
    }
    updateProductMutation.mutate({
      productRetailerId,
      data: { price: parseFloat(newPrice) },
    });
  };

  const handleUpdateStock = (productRetailerId: number, newStock: string) => {
    if (!newStock || isNaN(parseInt(newStock))) {
      Alert.alert('Error', 'Please enter a valid stock quantity');
      return;
    }
    updateProductMutation.mutate({
      productRetailerId,
      data: { stockQuantity: parseInt(newStock) },
    });
  };

  const handleToggleActive = (productRetailerId: number, isActive: boolean) => {
    updateProductMutation.mutate({
      productRetailerId,
      data: { isActive },
    });
  };

  if (isLoading) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color="#3b82f6" />
        <Text style={styles.loadingText}>Loading products...</Text>
      </View>
    );
  }

  const products = productsData?.content || [];

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Product Management</Text>
        <TouchableOpacity
          style={styles.addButton}
          onPress={() => setIsAddProductOpen(true)}
        >
          <Ionicons name="add" size={24} color="#fff" />
          <Text style={styles.addButtonText}>Add Product</Text>
        </TouchableOpacity>
      </View>

      {/* Search and Filter */}
      <View style={styles.filterContainer}>
        <View style={styles.searchContainer}>
          <Ionicons name="search" size={20} color="#6b7280" />
          <TextInput
            style={styles.searchInput}
            placeholder="Search products..."
            value={searchTerm}
            onChangeText={setSearchTerm}
          />
        </View>
        <View style={styles.filterButtons}>
          <TouchableOpacity
            style={[
              styles.filterButton,
              isActiveFilter === undefined && styles.filterButtonActive,
            ]}
            onPress={() => setIsActiveFilter(undefined)}
          >
            <Text
              style={[
                styles.filterButtonText,
                isActiveFilter === undefined && styles.filterButtonTextActive,
              ]}
            >
              All
            </Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[
              styles.filterButton,
              isActiveFilter === true && styles.filterButtonActive,
            ]}
            onPress={() => setIsActiveFilter(true)}
          >
            <Text
              style={[
                styles.filterButtonText,
                isActiveFilter === true && styles.filterButtonTextActive,
              ]}
            >
              Active
            </Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[
              styles.filterButton,
              isActiveFilter === false && styles.filterButtonActive,
            ]}
            onPress={() => setIsActiveFilter(false)}
          >
            <Text
              style={[
                styles.filterButtonText,
                isActiveFilter === false && styles.filterButtonTextActive,
              ]}
            >
              Inactive
            </Text>
          </TouchableOpacity>
        </View>
      </View>

      <FlatList
        data={products}
        keyExtractor={(item) => item.id.toString()}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <View style={styles.productCard}>
            <Image
              source={{
                uri: item.product?.images?.[0] || 'https://via.placeholder.com/200',
              }}
              style={styles.productImage}
            />
            <View style={styles.productInfo}>
              <Text style={styles.productName}>{item.product?.name || 'Unknown'}</Text>
              <Text style={styles.productSku}>SKU: {item.product?.sku || 'N/A'}</Text>

              <View style={styles.priceRow}>
                <Text style={styles.priceLabel}>Price:</Text>
                <TextInput
                  style={styles.priceInput}
                  value={item.price.toString()}
                  keyboardType="numeric"
                  onSubmitEditing={(e) =>
                    handleUpdatePrice(item.id, e.nativeEvent.text)
                  }
                />
              </View>

              <View style={styles.stockRow}>
                <Text style={styles.stockLabel}>Stock:</Text>
                <TextInput
                  style={styles.stockInput}
                  value={item.stockQuantity.toString()}
                  keyboardType="numeric"
                  onSubmitEditing={(e) =>
                    handleUpdateStock(item.id, e.nativeEvent.text)
                  }
                />
                {item.isLowStock && (
                  <Text style={styles.lowStockWarning}>Low Stock!</Text>
                )}
              </View>

              <View style={styles.actionRow}>
                <TouchableOpacity
                  style={[
                    styles.toggleButton,
                    item.isActive ? styles.toggleButtonActive : styles.toggleButtonInactive,
                  ]}
                  onPress={() => handleToggleActive(item.id, !item.isActive)}
                >
                  <Text
                    style={[
                      styles.toggleButtonText,
                      item.isActive && styles.toggleButtonTextActive,
                    ]}
                  >
                    {item.isActive ? 'Active' : 'Inactive'}
                  </Text>
                </TouchableOpacity>
              </View>
            </View>
          </View>
        )}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Ionicons name="cube-outline" size={64} color="#9ca3af" />
            <Text style={styles.emptyTitle}>No Products</Text>
            <Text style={styles.emptyText}>
              {searchTerm
                ? 'No products found matching your search'
                : 'Add your first product to get started'}
            </Text>
          </View>
        }
      />

      {/* Add Product Modal */}
      <Modal
        visible={isAddProductOpen}
        animationType="slide"
        presentationStyle="pageSheet"
      >
        <View style={styles.modalContainer}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Add Product</Text>
            <TouchableOpacity onPress={() => setIsAddProductOpen(false)}>
              <Ionicons name="close" size={24} color="#111827" />
            </TouchableOpacity>
          </View>

          <ScrollView style={styles.modalContent}>
            <Text style={styles.modalLabel}>Select Product</Text>
            <FlatList
              data={availableProducts}
              keyExtractor={(item) => item.id.toString()}
              renderItem={({ item }) => (
                <TouchableOpacity
                  style={[
                    styles.productOption,
                    selectedProduct?.id === item.id && styles.productOptionSelected,
                  ]}
                  onPress={() => setSelectedProduct(item)}
                >
                  <Text style={styles.productOptionText}>{item.name}</Text>
                  {selectedProduct?.id === item.id && (
                    <Ionicons name="checkmark" size={20} color="#3b82f6" />
                  )}
                </TouchableOpacity>
              )}
            />

            <Text style={styles.modalLabel}>Price *</Text>
            <TextInput
              style={styles.modalInput}
              placeholder="Enter price"
              value={productFormData.price}
              onChangeText={(text) =>
                setProductFormData({ ...productFormData, price: text })
              }
              keyboardType="numeric"
            />

            <Text style={styles.modalLabel}>Compare At Price</Text>
            <TextInput
              style={styles.modalInput}
              placeholder="Enter compare at price"
              value={productFormData.compareAtPrice}
              onChangeText={(text) =>
                setProductFormData({ ...productFormData, compareAtPrice: text })
              }
              keyboardType="numeric"
            />

            <Text style={styles.modalLabel}>Stock Quantity</Text>
            <TextInput
              style={styles.modalInput}
              placeholder="Enter stock quantity"
              value={productFormData.stockQuantity}
              onChangeText={(text) =>
                setProductFormData({ ...productFormData, stockQuantity: text })
              }
              keyboardType="numeric"
            />

            <Text style={styles.modalLabel}>Low Stock Threshold</Text>
            <TextInput
              style={styles.modalInput}
              placeholder="Enter low stock threshold"
              value={productFormData.lowStockThreshold}
              onChangeText={(text) =>
                setProductFormData({ ...productFormData, lowStockThreshold: text })
              }
              keyboardType="numeric"
            />

            <TouchableOpacity
              style={styles.uploadButton}
              onPress={pickImage}
              disabled={uploadingImage}
            >
              {uploadingImage ? (
                <ActivityIndicator color="#fff" />
              ) : (
                <>
                  <Ionicons name="image-outline" size={20} color="#fff" />
                  <Text style={styles.uploadButtonText}>Upload Product Image</Text>
                </>
              )}
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.submitButton}
              onPress={handleAddProduct}
              disabled={addProductMutation.isPending}
            >
              {addProductMutation.isPending ? (
                <ActivityIndicator color="#fff" />
              ) : (
                <Text style={styles.submitButtonText}>Add Product</Text>
              )}
            </TouchableOpacity>
          </ScrollView>
        </View>
      </Modal>
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
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#111827',
  },
  addButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#3b82f6',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8,
    gap: 8,
  },
  addButtonText: {
    color: '#fff',
    fontWeight: '600',
    fontSize: 14,
  },
  filterContainer: {
    padding: 20,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  searchContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f3f4f6',
    borderRadius: 8,
    paddingHorizontal: 12,
    marginBottom: 12,
    gap: 8,
  },
  searchInput: {
    flex: 1,
    paddingVertical: 10,
    fontSize: 14,
  },
  filterButtons: {
    flexDirection: 'row',
    gap: 8,
  },
  filterButton: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    backgroundColor: '#f3f4f6',
  },
  filterButtonActive: {
    backgroundColor: '#3b82f6',
  },
  filterButtonText: {
    fontSize: 14,
    color: '#6b7280',
    fontWeight: '500',
  },
  filterButtonTextActive: {
    color: '#fff',
  },
  list: {
    padding: 12,
  },
  productCard: {
    flexDirection: 'row',
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 12,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  productImage: {
    width: 100,
    height: 100,
    borderRadius: 8,
    backgroundColor: '#f3f4f6',
    marginRight: 12,
  },
  productInfo: {
    flex: 1,
  },
  productName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 4,
  },
  productSku: {
    fontSize: 12,
    color: '#6b7280',
    marginBottom: 8,
  },
  priceRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
    gap: 8,
  },
  priceLabel: {
    fontSize: 14,
    color: '#6b7280',
  },
  priceInput: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 6,
    padding: 8,
    fontSize: 14,
    backgroundColor: '#fff',
  },
  stockRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
    gap: 8,
  },
  stockLabel: {
    fontSize: 14,
    color: '#6b7280',
  },
  stockInput: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 6,
    padding: 8,
    fontSize: 14,
    backgroundColor: '#fff',
  },
  lowStockWarning: {
    fontSize: 12,
    color: '#ef4444',
    fontWeight: '600',
  },
  actionRow: {
    marginTop: 8,
  },
  toggleButton: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 6,
    alignSelf: 'flex-start',
    backgroundColor: '#e5e7eb',
  },
  toggleButtonActive: {
    backgroundColor: '#d1fae5',
  },
  toggleButtonInactive: {
    backgroundColor: '#fee2e2',
  },
  toggleButtonText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#6b7280',
  },
  toggleButtonTextActive: {
    color: '#10b981',
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 40,
    minHeight: 400,
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    marginTop: 16,
    color: '#111827',
  },
  emptyText: {
    fontSize: 14,
    color: '#6b7280',
    textAlign: 'center',
    marginTop: 8,
  },
  modalContainer: {
    flex: 1,
    backgroundColor: '#fff',
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  modalTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#111827',
  },
  modalContent: {
    flex: 1,
    padding: 20,
  },
  modalLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 8,
    marginTop: 16,
  },
  modalInput: {
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 8,
    padding: 12,
    fontSize: 14,
    backgroundColor: '#fff',
    marginBottom: 8,
  },
  productOption: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 12,
    borderRadius: 8,
    backgroundColor: '#f3f4f6',
    marginBottom: 8,
  },
  productOptionSelected: {
    backgroundColor: '#dbeafe',
  },
  productOptionText: {
    fontSize: 14,
    color: '#111827',
  },
  uploadButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#8b5cf6',
    padding: 12,
    borderRadius: 8,
    marginTop: 16,
    gap: 8,
  },
  uploadButtonText: {
    color: '#fff',
    fontWeight: '600',
    fontSize: 14,
  },
  submitButton: {
    backgroundColor: '#3b82f6',
    padding: 16,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 16,
  },
  submitButtonText: {
    color: '#fff',
    fontWeight: '600',
    fontSize: 16,
  },
});

