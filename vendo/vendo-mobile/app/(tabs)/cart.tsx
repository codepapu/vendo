import { View, Text, StyleSheet } from 'react-native';
import { useAuthStore } from '@/stores/auth-store';
import { ProtectedRoute } from '@/components/protected-route';

function CartScreen() {
  const { isAuthenticated } = useAuthStore();

  if (!isAuthenticated) {
    return (
      <View style={styles.container}>
        <Text style={styles.text}>Please login to view your cart</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Shopping Cart</Text>
      <Text style={styles.text}>Your cart is empty</Text>
    </View>
  );
}

export default function Cart() {
  return (
    <ProtectedRoute>
      <CartScreen />
    </ProtectedRoute>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 16,
  },
  text: {
    fontSize: 16,
    color: '#6b7280',
  },
});
