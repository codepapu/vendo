import { View, Text, ScrollView, StyleSheet, TouchableOpacity, Switch, Alert, ActivityIndicator } from 'react-native';
import { useRouter } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '@/stores/auth-store';
import { ProtectedRoute } from '@/components/protected-route';
import { notificationApi } from '@/services/notification-api';
import { Ionicons } from '@expo/vector-icons';
import { useState, useEffect } from 'react';

function SettingsScreen() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [pushEnabled, setPushEnabled] = useState(true);
  const [orderUpdates, setOrderUpdates] = useState(true);
  const [promotions, setPromotions] = useState(true);
  const [lowStockAlerts, setLowStockAlerts] = useState(false);
  const [newProducts, setNewProducts] = useState(true);

  const { data: preferences, isLoading } = useQuery({
    queryKey: ['notificationPreferences'],
    queryFn: () => notificationApi.getNotificationPreferences(),
  });

  const updatePreferencesMutation = useMutation({
    mutationFn: (data: any) => notificationApi.updateNotificationPreferences(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notificationPreferences'] });
      Alert.alert('Success', 'Notification preferences updated');
    },
  });

  useEffect(() => {
    if (preferences) {
      setPushEnabled(preferences.pushEnabled);
      setOrderUpdates(preferences.orderUpdates);
      setPromotions(preferences.promotions);
      setLowStockAlerts(preferences.lowStockAlerts);
      setNewProducts(preferences.newProducts);
    }
  }, [preferences]);

  const handleToggle = (key: string, value: boolean) => {
    const updates: any = {};
    updates[key] = value;

    switch (key) {
      case 'pushEnabled':
        setPushEnabled(value);
        break;
      case 'orderUpdates':
        setOrderUpdates(value);
        break;
      case 'promotions':
        setPromotions(value);
        break;
      case 'lowStockAlerts':
        setLowStockAlerts(value);
        break;
      case 'newProducts':
        setNewProducts(value);
        break;
    }

    updatePreferencesMutation.mutate(updates);
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
          <Ionicons name="arrow-back" size={24} color="#000" />
        </TouchableOpacity>
        <Text style={styles.title}>Settings</Text>
        <View style={styles.placeholder} />
      </View>

      {/* Notification Preferences */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Notification Preferences</Text>
        {isLoading && (
          <View style={styles.loadingContainer}>
            <ActivityIndicator size="small" color="#3b82f6" />
          </View>
        )}
        
        <View style={styles.settingItem}>
          <View style={styles.settingInfo}>
            <Ionicons name="notifications-outline" size={24} color="#3b82f6" />
            <View style={styles.settingText}>
              <Text style={styles.settingLabel}>Push Notifications</Text>
              <Text style={styles.settingDescription}>Enable push notifications</Text>
            </View>
          </View>
          <Switch
            value={pushEnabled}
            onValueChange={(value) => handleToggle('pushEnabled', value)}
            trackColor={{ false: '#d1d5db', true: '#93c5fd' }}
            thumbColor={pushEnabled ? '#3b82f6' : '#f3f4f6'}
            disabled={isLoading || updatePreferencesMutation.isPending}
          />
        </View>

        <View style={styles.settingItem}>
          <View style={styles.settingInfo}>
            <Ionicons name="receipt-outline" size={24} color="#3b82f6" />
            <View style={styles.settingText}>
              <Text style={styles.settingLabel}>Order Updates</Text>
              <Text style={styles.settingDescription}>Get notified about order status changes</Text>
            </View>
          </View>
          <Switch
            value={orderUpdates}
            onValueChange={(value) => handleToggle('orderUpdates', value)}
            trackColor={{ false: '#d1d5db', true: '#93c5fd' }}
            thumbColor={orderUpdates ? '#3b82f6' : '#f3f4f6'}
            disabled={!pushEnabled || isLoading || updatePreferencesMutation.isPending}
          />
        </View>

        <View style={styles.settingItem}>
          <View style={styles.settingInfo}>
            <Ionicons name="pricetag-outline" size={24} color="#3b82f6" />
            <View style={styles.settingText}>
              <Text style={styles.settingLabel}>Promotions</Text>
              <Text style={styles.settingDescription}>Receive promotional offers and discounts</Text>
            </View>
          </View>
          <Switch
            value={promotions}
            onValueChange={(value) => handleToggle('promotions', value)}
            trackColor={{ false: '#d1d5db', true: '#93c5fd' }}
            thumbColor={promotions ? '#3b82f6' : '#f3f4f6'}
            disabled={!pushEnabled || isLoading || updatePreferencesMutation.isPending}
          />
        </View>

        <View style={styles.settingItem}>
          <View style={styles.settingInfo}>
            <Ionicons name="alert-circle-outline" size={24} color="#3b82f6" />
            <View style={styles.settingText}>
              <Text style={styles.settingLabel}>Low Stock Alerts</Text>
              <Text style={styles.settingDescription}>Get notified when items are running low</Text>
            </View>
          </View>
          <Switch
            value={lowStockAlerts}
            onValueChange={(value) => handleToggle('lowStockAlerts', value)}
            trackColor={{ false: '#d1d5db', true: '#93c5fd' }}
            thumbColor={lowStockAlerts ? '#3b82f6' : '#f3f4f6'}
            disabled={!pushEnabled || isLoading || updatePreferencesMutation.isPending}
          />
        </View>

        <View style={styles.settingItem}>
          <View style={styles.settingInfo}>
            <Ionicons name="cube-outline" size={24} color="#3b82f6" />
            <View style={styles.settingText}>
              <Text style={styles.settingLabel}>New Products</Text>
              <Text style={styles.settingDescription}>Be notified when new products are added</Text>
            </View>
          </View>
          <Switch
            value={newProducts}
            onValueChange={(value) => handleToggle('newProducts', value)}
            trackColor={{ false: '#d1d5db', true: '#93c5fd' }}
            thumbColor={newProducts ? '#3b82f6' : '#f3f4f6'}
            disabled={!pushEnabled || isLoading || updatePreferencesMutation.isPending}
          />
        </View>
      </View>

      {/* Account Settings */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Account</Text>
        
        <TouchableOpacity
          style={styles.menuItem}
          onPress={() => Alert.alert('Coming Soon', 'Address management coming soon')}
        >
          <View style={styles.menuItemLeft}>
            <Ionicons name="location-outline" size={24} color="#3b82f6" />
            <Text style={styles.menuItemText}>Manage Addresses</Text>
          </View>
          <Ionicons name="chevron-forward" size={20} color="#9ca3af" />
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.menuItem}
          onPress={() => Alert.alert('Coming Soon', 'Payment methods coming soon')}
        >
          <View style={styles.menuItemLeft}>
            <Ionicons name="card-outline" size={24} color="#3b82f6" />
            <Text style={styles.menuItemText}>Payment Methods</Text>
          </View>
          <Ionicons name="chevron-forward" size={20} color="#9ca3af" />
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}

export default function Settings() {
  return (
    <ProtectedRoute>
      <SettingsScreen />
    </ProtectedRoute>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
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
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#111827',
  },
  section: {
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 16,
    color: '#111827',
  },
  settingItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#f3f4f6',
  },
  settingInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    gap: 12,
  },
  settingText: {
    flex: 1,
  },
  settingLabel: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 4,
  },
  settingDescription: {
    fontSize: 14,
    color: '#6b7280',
  },
  menuItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    backgroundColor: '#f9fafb',
    borderRadius: 12,
    marginBottom: 12,
  },
  menuItemLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  menuItemText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  loadingContainer: {
    padding: 16,
    alignItems: 'center',
  },
});

