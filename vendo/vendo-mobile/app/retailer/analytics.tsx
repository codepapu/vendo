import { useState } from 'react';
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  ActivityIndicator,
  TouchableOpacity,
} from 'react-native';
import { useQuery } from '@tanstack/react-query';
import { retailerApi } from '@/services/retailer-api';
import { useAuthStore } from '@/stores/auth-store';
import { Ionicons } from '@expo/vector-icons';

// Simple bar chart component
const SimpleBarChart = ({ data, maxValue }: { data: any[]; maxValue: number }) => {
  return (
    <View style={styles.chartContainer}>
      {data.map((item, index) => {
        const height = (item.revenue / maxValue) * 150;
        return (
          <View key={index} style={styles.barWrapper}>
            <View style={styles.barContainer}>
              <View
                style={[
                  styles.bar,
                  {
                    height: Math.max(height, 4),
                    backgroundColor: '#3b82f6',
                  },
                ]}
              />
            </View>
            <Text style={styles.barLabel} numberOfLines={1}>
              {item.date}
            </Text>
          </View>
        );
      })}
    </View>
  );
};

export default function RetailerAnalytics() {
  const { user } = useAuthStore();
  const [startDate, setStartDate] = useState(() => {
    const date = new Date();
    date.setMonth(date.getMonth() - 3);
    return date.toISOString().split('T')[0];
  });
  const [endDate, setEndDate] = useState(() => {
    return new Date().toISOString().split('T')[0];
  });

  const { data: retailer } = useQuery({
    queryKey: ['retailer', user?.id],
    queryFn: () => (user?.id ? retailerApi.getRetailerByUserId(user.id) : null),
    enabled: !!user?.id,
  });

  const { data: analytics, isLoading } = useQuery({
    queryKey: ['retailer-analytics', retailer?.id, startDate, endDate],
    queryFn: () =>
      retailer?.id
        ? retailerApi.getAnalytics(retailer.id, { startDate, endDate })
        : null,
    enabled: !!retailer?.id,
  });

  if (isLoading) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color="#3b82f6" />
        <Text style={styles.loadingText}>Loading analytics...</Text>
      </View>
    );
  }

  if (!analytics) {
    return (
      <View style={styles.centerContainer}>
        <Ionicons name="analytics-outline" size={64} color="#9ca3af" />
        <Text style={styles.emptyTitle}>No Analytics Data</Text>
        <Text style={styles.emptyText}>
          Analytics data will appear here once you start receiving orders.
        </Text>
      </View>
    );
  }

  const maxRevenue =
    analytics.salesOverTime?.reduce(
      (max, item) => Math.max(max, item.revenue),
      0
    ) || 1;

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Analytics</Text>
        <Text style={styles.subtitle}>
          {new Date(startDate).toLocaleDateString()} -{' '}
          {new Date(endDate).toLocaleDateString()}
        </Text>
      </View>

      {/* Sales Over Time */}
      {analytics.salesOverTime && analytics.salesOverTime.length > 0 && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Sales Over Time</Text>
          <View style={styles.chartCard}>
            <SimpleBarChart
              data={analytics.salesOverTime}
              maxValue={maxRevenue}
            />
          </View>
        </View>
      )}

      {/* Top Products */}
      {analytics.topProducts && analytics.topProducts.length > 0 && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Top Products</Text>
          <View style={styles.listContainer}>
            {analytics.topProducts.map((product, index) => (
              <View key={product.productId} style={styles.productCard}>
                <View style={styles.productRank}>
                  <Text style={styles.rankNumber}>#{index + 1}</Text>
                </View>
                <View style={styles.productInfo}>
                  <Text style={styles.productName} numberOfLines={1}>
                    {product.productName}
                  </Text>
                  <Text style={styles.productStats}>
                    {product.quantitySold} sold
                  </Text>
                </View>
                <View style={styles.productRevenue}>
                  <Text style={styles.revenueValue}>
                    ${product.totalRevenue.toLocaleString()}
                  </Text>
                </View>
              </View>
            ))}
          </View>
        </View>
      )}

      {/* Revenue by Category */}
      {analytics.revenueByCategory && analytics.revenueByCategory.length > 0 && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Revenue by Category</Text>
          <View style={styles.listContainer}>
            {analytics.revenueByCategory.map((category) => (
              <View key={category.categoryId} style={styles.categoryCard}>
                <View style={styles.categoryInfo}>
                  <Text style={styles.categoryName}>{category.categoryName}</Text>
                  <Text style={styles.categoryOrders}>
                    {category.orderCount} orders
                  </Text>
                </View>
                <Text style={styles.categoryRevenue}>
                  ${category.revenue.toLocaleString()}
                </Text>
              </View>
            ))}
          </View>
        </View>
      )}

      {/* Commission Breakdown */}
      {analytics.commissionBreakdown && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Commission Breakdown</Text>
          <View style={styles.statsGrid}>
            <View style={styles.statCard}>
              <Text style={styles.statLabel}>Total Commissions</Text>
              <Text style={styles.statValue}>
                ${analytics.commissionBreakdown.totalCommissions.toLocaleString()}
              </Text>
            </View>
            <View style={styles.statCard}>
              <Text style={styles.statLabel}>Pending</Text>
              <Text style={styles.statValue}>
                ${analytics.commissionBreakdown.pendingCommissions.toLocaleString()}
              </Text>
            </View>
            <View style={styles.statCard}>
              <Text style={styles.statLabel}>Paid</Text>
              <Text style={styles.statValue}>
                ${analytics.commissionBreakdown.paidCommissions.toLocaleString()}
              </Text>
            </View>
            <View style={styles.statCard}>
              <Text style={styles.statLabel}>Avg Rate</Text>
              <Text style={styles.statValue}>
                {analytics.commissionBreakdown.averageCommissionRate.toFixed(1)}%
              </Text>
            </View>
          </View>
        </View>
      )}

      {/* Payout History */}
      {analytics.payoutHistory && analytics.payoutHistory.length > 0 && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Payout History</Text>
          <View style={styles.listContainer}>
            {analytics.payoutHistory.map((payout) => (
              <View key={payout.id} style={styles.payoutCard}>
                <View style={styles.payoutInfo}>
                  <Text style={styles.payoutAmount}>
                    ${payout.amount.toLocaleString()}
                  </Text>
                  {payout.description && (
                    <Text style={styles.payoutDescription}>
                      {payout.description}
                    </Text>
                  )}
                  <Text style={styles.payoutDate}>
                    {new Date(payout.createdAt).toLocaleDateString()}
                  </Text>
                </View>
                <View
                  style={[
                    styles.payoutStatus,
                    payout.status === 'PROCESSED' && styles.payoutStatusProcessed,
                    payout.status === 'PENDING' && styles.payoutStatusPending,
                    payout.status === 'FAILED' && styles.payoutStatusFailed,
                  ]}
                >
                  <Text style={styles.payoutStatusText}>{payout.status}</Text>
                </View>
              </View>
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
  section: {
    padding: 20,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: 16,
  },
  chartCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  chartContainer: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'space-around',
    height: 200,
    paddingVertical: 16,
  },
  barWrapper: {
    flex: 1,
    alignItems: 'center',
  },
  barContainer: {
    flex: 1,
    justifyContent: 'flex-end',
    width: '100%',
    maxWidth: 40,
  },
  bar: {
    width: '100%',
    borderRadius: 4,
  },
  barLabel: {
    fontSize: 10,
    color: '#6b7280',
    marginTop: 8,
    textAlign: 'center',
  },
  listContainer: {
    gap: 12,
  },
  productCard: {
    flexDirection: 'row',
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
  productRank: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#f3f4f6',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  rankNumber: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#111827',
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
    alignItems: 'flex-end',
  },
  revenueValue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#3b82f6',
  },
  categoryCard: {
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
  categoryInfo: {
    flex: 1,
  },
  categoryName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 4,
  },
  categoryOrders: {
    fontSize: 12,
    color: '#6b7280',
  },
  categoryRevenue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#3b82f6',
  },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  statCard: {
    flex: 1,
    minWidth: '45%',
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  statLabel: {
    fontSize: 12,
    color: '#6b7280',
    marginBottom: 8,
  },
  statValue: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#111827',
  },
  payoutCard: {
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
  payoutInfo: {
    flex: 1,
  },
  payoutAmount: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: 4,
  },
  payoutDescription: {
    fontSize: 12,
    color: '#6b7280',
    marginBottom: 4,
  },
  payoutDate: {
    fontSize: 12,
    color: '#9ca3af',
  },
  payoutStatus: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 12,
    backgroundColor: '#e5e7eb',
  },
  payoutStatusProcessed: {
    backgroundColor: '#d1fae5',
  },
  payoutStatusPending: {
    backgroundColor: '#fef3c7',
  },
  payoutStatusFailed: {
    backgroundColor: '#fee2e2',
  },
  payoutStatusText: {
    fontSize: 12,
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
});

