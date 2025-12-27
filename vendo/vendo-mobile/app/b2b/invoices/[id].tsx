import { View, Text, ScrollView, StyleSheet, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { b2bApi } from '@/services/b2b-api';
import { Ionicons } from '@expo/vector-icons';

export default function B2BInvoiceDetail() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const router = useRouter();

  const { data: invoice, isLoading, error } = useQuery({
    queryKey: ['b2b-invoice', id],
    queryFn: async () => {
      if (!id) return null;
      return await b2bApi.getInvoice(Number(id));
    },
    enabled: !!id,
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PAID':
        return '#10b981';
      case 'PENDING':
        return '#f59e0b';
      case 'OVERDUE':
        return '#ef4444';
      case 'CANCELLED':
        return '#6b7280';
      default:
        return '#6b7280';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PAID':
        return 'checkmark-circle';
      case 'PENDING':
        return 'time-outline';
      case 'OVERDUE':
        return 'alert-circle';
      case 'CANCELLED':
        return 'close-circle';
      default:
        return 'document-text';
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const isOverdue = (dueDate: string, status: string) => {
    if (status === 'PAID' || status === 'CANCELLED') return false;
    return new Date(dueDate) < new Date();
  };

  if (isLoading) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color="#3b82f6" />
        <Text style={styles.loadingText}>Loading invoice...</Text>
      </View>
    );
  }

  if (error || !invoice) {
    return (
      <View style={styles.centerContainer}>
        <Ionicons name="alert-circle-outline" size={64} color="#ef4444" />
        <Text style={styles.errorTitle}>Invoice not found</Text>
        <Text style={styles.errorText}>
          The invoice you're looking for doesn't exist.
        </Text>
        <TouchableOpacity style={styles.button} onPress={() => router.back()}>
          <Text style={styles.buttonText}>Go Back</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const overdue = isOverdue(invoice.dueDate, invoice.status);

  return (
    <ScrollView style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
          <Ionicons name="arrow-back" size={24} color="#000" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Invoice</Text>
        <View style={styles.placeholder} />
      </View>

      {/* Invoice Status */}
      <View style={styles.statusSection}>
        <View
          style={[
            styles.statusBadge,
            { backgroundColor: getStatusColor(invoice.status) + '20' },
          ]}
        >
          <Ionicons
            name={getStatusIcon(invoice.status) as any}
            size={24}
            color={getStatusColor(invoice.status)}
          />
          <Text
            style={[styles.statusText, { color: getStatusColor(invoice.status) }]}
          >
            {invoice.status}
          </Text>
        </View>
        <Text style={styles.invoiceNumber}>Invoice #{invoice.invoiceNumber}</Text>
        {overdue && (
          <View style={styles.overdueBadge}>
            <Ionicons name="warning" size={16} color="#ef4444" />
            <Text style={styles.overdueText}>OVERDUE</Text>
          </View>
        )}
      </View>

      {/* Invoice Amount */}
      <View style={styles.amountSection}>
        <Text style={styles.amountLabel}>Total Amount</Text>
        <Text style={styles.amountValue}>
          ${invoice.totalAmount.toLocaleString()}
        </Text>
        {invoice.paidAmount > 0 && (
          <Text style={styles.paidAmount}>
            Paid: ${invoice.paidAmount.toLocaleString()}
          </Text>
        )}
      </View>

      {/* Invoice Dates */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Invoice Dates</Text>
        <View style={styles.infoCard}>
          <View style={styles.infoRow}>
            <Ionicons name="calendar-outline" size={20} color="#3b82f6" />
            <View style={styles.infoContent}>
              <Text style={styles.infoLabel}>Issue Date</Text>
              <Text style={styles.infoValue}>{formatDate(invoice.issueDate)}</Text>
            </View>
          </View>
          <View style={styles.infoRow}>
            <Ionicons name="time-outline" size={20} color={overdue ? '#ef4444' : '#f59e0b'} />
            <View style={styles.infoContent}>
              <Text style={styles.infoLabel}>Due Date</Text>
              <Text
                style={[
                  styles.infoValue,
                  overdue && styles.overdueDate,
                ]}
              >
                {formatDate(invoice.dueDate)}
              </Text>
            </View>
          </View>
          {invoice.paidAt && (
            <View style={styles.infoRow}>
              <Ionicons name="checkmark-circle" size={20} color="#10b981" />
              <View style={styles.infoContent}>
                <Text style={styles.infoLabel}>Paid On</Text>
                <Text style={styles.infoValue}>
                  {formatDate(invoice.paidAt)}
                </Text>
              </View>
            </View>
          )}
        </View>
      </View>

      {/* Related Order */}
      {invoice.order && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Related Order</Text>
          <View style={styles.infoCard}>
            <View style={styles.infoRow}>
              <Ionicons name="receipt-outline" size={20} color="#3b82f6" />
              <View style={styles.infoContent}>
                <Text style={styles.infoLabel}>Order Number</Text>
                <Text style={styles.infoValue}>{invoice.order.orderNumber}</Text>
              </View>
            </View>
          </View>
        </View>
      )}

      {/* Invoice Breakdown */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Invoice Breakdown</Text>
        <View style={styles.breakdownCard}>
          <View style={styles.breakdownRow}>
            <Text style={styles.breakdownLabel}>Subtotal</Text>
            <Text style={styles.breakdownValue}>
              ${invoice.subtotal.toLocaleString()}
            </Text>
          </View>
          <View style={styles.breakdownRow}>
            <Text style={styles.breakdownLabel}>Tax</Text>
            <Text style={styles.breakdownValue}>
              ${invoice.taxAmount.toLocaleString()}
            </Text>
          </View>
          <View style={[styles.breakdownRow, styles.totalRow]}>
            <Text style={styles.totalLabel}>Total</Text>
            <Text style={styles.totalValue}>
              ${invoice.totalAmount.toLocaleString()}
            </Text>
          </View>
          {invoice.paidAmount > 0 && (
            <View style={[styles.breakdownRow, styles.paidRow]}>
              <Text style={styles.paidLabel}>Paid Amount</Text>
              <Text style={styles.paidValue}>
                ${invoice.paidAmount.toLocaleString()}
              </Text>
            </View>
          )}
          {invoice.totalAmount > invoice.paidAmount && (
            <View style={[styles.breakdownRow, styles.balanceRow]}>
              <Text style={styles.balanceLabel}>Balance Due</Text>
              <Text style={styles.balanceValue}>
                ${(invoice.totalAmount - invoice.paidAmount).toLocaleString()}
              </Text>
            </View>
          )}
        </View>
      </View>
    </ScrollView>
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
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  backButton: {
    padding: 8,
  },
  placeholder: {
    width: 40,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#111827',
  },
  statusSection: {
    padding: 20,
    alignItems: 'center',
    backgroundColor: '#f9fafb',
  },
  statusBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 24,
    marginBottom: 12,
  },
  statusText: {
    fontSize: 16,
    fontWeight: '600',
    textTransform: 'uppercase',
  },
  invoiceNumber: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: 8,
  },
  overdueBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: '#fee2e2',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 12,
    marginTop: 8,
  },
  overdueText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#ef4444',
  },
  amountSection: {
    padding: 20,
    backgroundColor: '#f0f9ff',
    alignItems: 'center',
  },
  amountLabel: {
    fontSize: 14,
    color: '#6b7280',
    marginBottom: 8,
  },
  amountValue: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#3b82f6',
  },
  paidAmount: {
    fontSize: 14,
    color: '#10b981',
    marginTop: 8,
    fontWeight: '600',
  },
  section: {
    padding: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 16,
    color: '#111827',
  },
  infoCard: {
    backgroundColor: '#f9fafb',
    borderRadius: 12,
    padding: 16,
  },
  infoRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 16,
    gap: 12,
  },
  infoContent: {
    flex: 1,
  },
  infoLabel: {
    fontSize: 12,
    color: '#6b7280',
    marginBottom: 4,
  },
  infoValue: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  overdueDate: {
    color: '#ef4444',
  },
  breakdownCard: {
    backgroundColor: '#f9fafb',
    borderRadius: 12,
    padding: 16,
  },
  breakdownRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  breakdownLabel: {
    fontSize: 16,
    color: '#6b7280',
  },
  breakdownValue: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
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
  paidRow: {
    marginTop: 8,
  },
  paidLabel: {
    fontSize: 16,
    color: '#10b981',
    fontWeight: '600',
  },
  paidValue: {
    fontSize: 16,
    fontWeight: '600',
    color: '#10b981',
  },
  balanceRow: {
    marginTop: 8,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#e5e7eb',
  },
  balanceLabel: {
    fontSize: 18,
    fontWeight: '600',
    color: '#111827',
  },
  balanceValue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ef4444',
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: '#6b7280',
  },
  errorTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    marginTop: 16,
    marginBottom: 8,
    color: '#111827',
  },
  errorText: {
    fontSize: 16,
    color: '#6b7280',
    textAlign: 'center',
    marginBottom: 24,
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
});

