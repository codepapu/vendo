import { View, Text, ScrollView, StyleSheet, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useRouter } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { b2bApi } from '@/services/b2b-api';
import { useAuthStore } from '@/stores/auth-store';
import { Ionicons } from '@expo/vector-icons';

export default function B2BDashboard() {
  const router = useRouter();
  const { user } = useAuthStore();

  const { data: account, isLoading: isLoadingAccount } = useQuery({
    queryKey: ['b2b-account'],
    queryFn: () => b2bApi.getAccount(),
    enabled: !!user,
  });

  const { data: ordersData, isLoading: isLoadingOrders } = useQuery({
    queryKey: ['b2b-orders'],
    queryFn: () => b2bApi.getPurchaseOrders(undefined, 0, 5),
    enabled: !!account,
  });

  const { data: invoicesData, isLoading: isLoadingInvoices } = useQuery({
    queryKey: ['b2b-invoices'],
    queryFn: () => b2bApi.getInvoices('PENDING', 0, 5),
    enabled: !!account,
  });

  if (isLoadingAccount) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color="#3b82f6" />
        <Text style={styles.loadingText}>Loading dashboard...</Text>
      </View>
    );
  }

  if (!account) {
    return (
      <View style={styles.centerContainer}>
        <Ionicons name="business-outline" size={64} color="#9ca3af" />
        <Text style={styles.emptyTitle}>No Business Account</Text>
        <Text style={styles.emptyText}>
          You need to create a business account to access B2B features.
        </Text>
      </View>
    );
  }

  const availableCredit = account.creditLimit - (account.totalSpent || 0);
  const pendingInvoices = invoicesData?.content || [];
  const recentOrders = ordersData?.content || [];

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>B2B Dashboard</Text>
        <Text style={styles.subtitle}>{account.companyName}</Text>
      </View>

      {/* Key Metrics */}
      <View style={styles.metricsContainer}>
        <View style={styles.metricCard}>
          <Ionicons name="checkmark-circle" size={24} color="#10b981" />
          <Text style={styles.metricValue}>{account.status}</Text>
          <Text style={styles.metricLabel}>Account Status</Text>
        </View>

        <View style={styles.metricCard}>
          <Ionicons name="card" size={24} color="#3b82f6" />
          <Text style={styles.metricValue}>
            ${account.creditLimit.toLocaleString()}
          </Text>
          <Text style={styles.metricLabel}>Credit Limit</Text>
          <Text style={styles.metricSubtext}>
            Available: ${availableCredit.toLocaleString()}
          </Text>
        </View>

        <View style={styles.metricCard}>
          <Ionicons name="receipt" size={24} color="#8b5cf6" />
          <Text style={styles.metricValue}>{account.totalOrders || 0}</Text>
          <Text style={styles.metricLabel}>Total Orders</Text>
        </View>

        <View style={styles.metricCard}>
          <Ionicons name="document-text" size={24} color="#f59e0b" />
          <Text style={styles.metricValue}>
            {isLoadingInvoices ? '...' : pendingInvoices.length}
          </Text>
          <Text style={styles.metricLabel}>Pending Invoices</Text>
        </View>
      </View>

      {/* Quick Actions */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Quick Actions</Text>
        <View style={styles.actionGrid}>
          <TouchableOpacity
            style={styles.actionCard}
            onPress={() => router.push('/b2b/products')}
          >
            <Ionicons name="bag" size={32} color="#3b82f6" />
            <Text style={styles.actionText}>Products</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.actionCard}
            onPress={() => router.push('/b2b/orders')}
          >
            <Ionicons name="list" size={32} color="#10b981" />
            <Text style={styles.actionText}>Orders</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.actionCard}
            onPress={() => router.push('/b2b/invoices')}
          >
            <Ionicons name="document" size={32} color="#f59e0b" />
            <Text style={styles.actionText}>Invoices</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Recent Purchase Orders */}
      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Recent Purchase Orders</Text>
          <TouchableOpacity onPress={() => router.push('/b2b/orders')}>
            <Text style={styles.viewAll}>View All</Text>
          </TouchableOpacity>
        </View>
        {isLoadingOrders ? (
          <ActivityIndicator size="small" color="#3b82f6" />
        ) : recentOrders.length === 0 ? (
          <Text style={styles.emptyText}>No recent orders</Text>
        ) : (
          <View style={styles.listContainer}>
            {recentOrders.map((order) => (
              <TouchableOpacity
                key={order.id}
                style={styles.orderCard}
                onPress={() => router.push(`/b2b/orders/${order.id}`)}
              >
                <View style={styles.orderHeader}>
                  <Text style={styles.orderNumber}>{order.poNumber}</Text>
                  <View
                    style={[
                      styles.statusBadge,
                      order.status === 'APPROVED' && styles.statusApproved,
                      order.status === 'PENDING' && styles.statusPending,
                      order.status === 'REJECTED' && styles.statusRejected,
                    ]}
                  >
                    <Text style={styles.statusText}>{order.status}</Text>
                  </View>
                </View>
                <Text style={styles.orderDate}>
                  {new Date(order.requestedAt).toLocaleDateString()}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        )}
      </View>
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
  orderCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  orderHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  orderNumber: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  statusBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
    backgroundColor: '#e5e7eb',
  },
  statusApproved: {
    backgroundColor: '#d1fae5',
  },
  statusPending: {
    backgroundColor: '#fef3c7',
  },
  statusRejected: {
    backgroundColor: '#fee2e2',
  },
  statusText: {
    fontSize: 10,
    fontWeight: '600',
    color: '#111827',
    textTransform: 'uppercase',
  },
  orderDate: {
    fontSize: 12,
    color: '#6b7280',
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
});

