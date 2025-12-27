import { useEffect } from 'react';
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  TouchableOpacity,
  ActivityIndicator,
} from 'react-native';
import { useRouter } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { retailerApi } from '@/services/retailer-api';
import { useAuthStore } from '@/stores/auth-store';
import { Ionicons } from '@expo/vector-icons';

export default function RetailerDashboard() {
  const router = useRouter();
  const { user } = useAuthStore();

  const { data: retailer, isLoading: isLoadingRetailer } = useQuery({
    queryKey: ['retailer', user?.id],
    queryFn: () => (user?.id ? retailerApi.getRetailerByUserId(user.id) : null),
    enabled: !!user?.id,
  });

  const { data: dashboard, isLoading: isLoadingDashboard } = useQuery({
    queryKey: ['retailer-dashboard'],
    queryFn: () => retailerApi.getDashboard(),
    enabled: !!retailer && retailer.status === 'ACTIVE',
  });

  if (isLoadingRetailer || isLoadingDashboard) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color="#3b82f6" />
        <Text style={styles.loadingText}>Loading dashboard...</Text>
      </View>
    );
  }

  if (!retailer) {
    return (
      <View style={styles.centerContainer}>
        <Ionicons name="storefront-outline" size={64} color="#9ca3af" />
        <Text style={styles.emptyTitle}>Not a Retailer</Text>
        <Text style={styles.emptyText}>
          You need to register as a retailer to access this dashboard.
        </Text>
      </View>
    );
  }

  if (retailer.status === 'PENDING') {
    return (
      <View style={styles.centerContainer}>
        <Ionicons name="hourglass-outline" size={64} color="#f59e0b" />
        <Text style={styles.emptyTitle}>Registration Pending</Text>
        <Text style={styles.emptyText}>
          Your retailer registration is being reviewed. You'll be notified once
          it's approved.
        </Text>
        <Text style={styles.statusLabel}>Status: {retailer.status}</Text>
      </View>
    );
  }

  if (retailer.status === 'REJECTED') {
    return (
      <View style={styles.centerContainer}>
        <Ionicons name="close-circle" size={64} color="#ef4444" />
        <Text style={styles.emptyTitle}>Registration Rejected</Text>
        <Text style={styles.emptyText}>
          Your retailer registration has been rejected. Please contact support
          for more information.
        </Text>
      </View>
    );
  }

  if (!dashboard) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color="#3b82f6" />
        <Text style={styles.loadingText}>Loading dashboard data...</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>{dashboard.storeName} Dashboard</Text>
        <Text style={styles.subtitle}>Welcome back! Here's your store overview.</Text>
      </View>

      {/* Key Metrics */}
      <View style={styles.metricsContainer}>
        <View style={styles.metricCard}>
          <Ionicons name="cash" size={24} color="#10b981" />
          <Text style={styles.metricValue}>
            ${dashboard.totalSales.toLocaleString()}
          </Text>
          <Text style={styles.metricLabel}>Total Sales</Text>
          <Text style={styles.metricSubtext}>
            This month: ${dashboard.thisMonthSales.toLocaleString()}
          </Text>
        </View>

        <View style={styles.metricCard}>
          <Ionicons name="receipt" size={24} color="#3b82f6" />
          <Text style={styles.metricValue}>{dashboard.totalOrders}</Text>
          <Text style={styles.metricLabel}>Total Orders</Text>
          <Text style={styles.metricSubtext}>
            This month: {dashboard.thisMonthOrders}
          </Text>
        </View>

        <View style={styles.metricCard}>
          <Ionicons name="cube" size={24} color="#8b5cf6" />
          <Text style={styles.metricValue}>{dashboard.activeProducts}</Text>
          <Text style={styles.metricLabel}>Active Products</Text>
          {dashboard.lowStockProducts > 0 && (
            <Text style={styles.metricSubtextWarning}>
              {dashboard.lowStockProducts} low stock
            </Text>
          )}
        </View>

        <View style={styles.metricCard}>
          <Ionicons name="wallet" size={24} color="#f59e0b" />
          <Text style={styles.metricValue}>
            ${dashboard.totalCommissions.toLocaleString()}
          </Text>
          <Text style={styles.metricLabel}>Commissions</Text>
          <Text style={styles.metricSubtext}>
            Pending: ${dashboard.pendingCommissions.toLocaleString()}
          </Text>
        </View>
      </View>

      {/* Quick Actions */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Quick Actions</Text>
        <View style={styles.actionGrid}>
          <TouchableOpacity
            style={styles.actionCard}
            onPress={() => router.push('/retailer/products')}
          >
            <Ionicons name="bag" size={32} color="#3b82f6" />
            <Text style={styles.actionText}>Products</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.actionCard}
            onPress={() => router.push('/retailer/orders')}
          >
            <Ionicons name="list" size={32} color="#10b981" />
            <Text style={styles.actionText}>Orders</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.actionCard}
            onPress={() => router.push('/retailer/analytics')}
          >
            <Ionicons name="analytics" size={32} color="#8b5cf6" />
            <Text style={styles.actionText}>Analytics</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Top Products */}
      {dashboard.topProducts && dashboard.topProducts.length > 0 && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Top Products</Text>
          <View style={styles.listContainer}>
            {dashboard.topProducts.slice(0, 5).map((product) => (
              <View key={product.productId} style={styles.productCard}>
                <View style={styles.productInfo}>
                  <Text style={styles.productName} numberOfLines={1}>
                    {product.productName}
                  </Text>
                  <Text style={styles.productStats}>
                    {product.quantitySold} sold
                  </Text>
                </View>
                <Text style={styles.productRevenue}>
                  ${product.totalRevenue.toLocaleString()}
                </Text>
              </View>
            ))}
          </View>
        </View>
      )}

      {/* Recent Orders */}
      {dashboard.recentOrders && dashboard.recentOrders.length > 0 && (
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Recent Orders</Text>
            <TouchableOpacity onPress={() => router.push('/retailer/orders')}>
              <Text style={styles.viewAll}>View All</Text>
            </TouchableOpacity>
          </View>
          <View style={styles.listContainer}>
            {dashboard.recentOrders.slice(0, 5).map((order) => (
              <TouchableOpacity
                key={order.orderId}
                style={styles.orderCard}
                onPress={() => router.push(`/retailer/orders/${order.orderId}`)}
              >
                <View style={styles.orderInfo}>
                  <Text style={styles.orderNumber}>
                    Order #{order.orderNumber}
                  </Text>
                  <Text style={styles.orderDate}>
                    {new Date(order.createdAt).toLocaleDateString()}
                  </Text>
                </View>
                <View style={styles.orderAmount}>
                  <Text style={styles.orderAmountValue}>
                    ${order.totalAmount.toLocaleString()}
                  </Text>
                  <View
                    style={[
                      styles.statusBadge,
                      order.status === 'DELIVERED' && styles.statusDelivered,
                      order.status === 'PENDING' && styles.statusPending,
                    ]}
                  >
                    <Text style={styles.statusText}>{order.status}</Text>
                  </View>
                </View>
              </TouchableOpacity>
            ))}
          </View>
        </View>
      )}
    </ScrollView>
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
    backgroundColor: '#f9fafb',
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
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 4,
    color: '#111827',
  },
  subtitle: {
    fontSize: 16,
    color: '#6b7280',
  },
  metricsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    padding: 12,
    gap: 12,
  },
  metricCard: {
    flex: 1,
    minWidth: '45%',
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  metricValue: {
    fontSize: 20,
    fontWeight: 'bold',
    marginTop: 8,
    color: '#111827',
  },
  metricLabel: {
    fontSize: 12,
    color: '#6b7280',
    marginTop: 4,
    textAlign: 'center',
  },
  metricSubtext: {
    fontSize: 10,
    color: '#9ca3af',
    marginTop: 2,
  },
  metricSubtextWarning: {
    fontSize: 10,
    color: '#ef4444',
    marginTop: 2,
    fontWeight: '600',
  },
  section: {
    padding: 20,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: 16,
  },
  viewAll: {
    fontSize: 14,
    color: '#3b82f6',
    fontWeight: '600',
  },
  actionGrid: {
    flexDirection: 'row',
    gap: 12,
  },
  actionCard: {
    flex: 1,
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 20,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  actionText: {
    fontSize: 14,
    fontWeight: '600',
    marginTop: 8,
    color: '#111827',
  },
  listContainer: {
    gap: 12,
  },
  productCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
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
  productStats: {
    fontSize: 12,
    color: '#6b7280',
  },
  productRevenue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#3b82f6',
  },
  orderCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  orderInfo: {
    flex: 1,
  },
  orderNumber: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 4,
  },
  orderDate: {
    fontSize: 12,
    color: '#6b7280',
  },
  orderAmount: {
    alignItems: 'flex-end',
  },
  orderAmountValue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: 4,
  },
  statusBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
    backgroundColor: '#e5e7eb',
  },
  statusDelivered: {
    backgroundColor: '#d1fae5',
  },
  statusPending: {
    backgroundColor: '#fef3c7',
  },
  statusText: {
    fontSize: 10,
    fontWeight: '600',
    color: '#111827',
    textTransform: 'uppercase',
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
  statusLabel: {
    fontSize: 14,
    color: '#6b7280',
    marginTop: 8,
    fontWeight: '600',
  },
});

