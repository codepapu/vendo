import { Stack } from 'expo-router';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { StatusBar } from 'expo-status-bar';
import { usePushNotifications } from '@/hooks/use-push-notifications';
import '../src/lib/api';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 5 * 60 * 1000,
    },
  },
});

export default function RootLayout() {
  // Initialize push notifications
  usePushNotifications();

  return (
    <QueryClientProvider client={queryClient}>
      <StatusBar style="auto" />
      <Stack
        screenOptions={{
          headerShown: false,
        }}
      >
        <Stack.Screen name="(tabs)" />
        <Stack.Screen name="login" />
        <Stack.Screen name="register" />
        <Stack.Screen name="product/[id]" />
        <Stack.Screen name="checkout" />
        <Stack.Screen name="orders" />
        <Stack.Screen name="orders/[id]" />
        <Stack.Screen name="settings" />
        {/* Public Pages */}
        <Stack.Screen name="about" />
        <Stack.Screen name="contact" />
        <Stack.Screen name="privacy" />
        <Stack.Screen name="terms" />
        <Stack.Screen name="faq" />
        <Stack.Screen name="categories" />
        <Stack.Screen name="stores" />
        <Stack.Screen name="stores/[slug]" />
        <Stack.Screen name="b2b" />
        <Stack.Screen name="b2b/products" />
        <Stack.Screen name="b2b/orders" />
        <Stack.Screen name="b2b/orders/[id]" />
        <Stack.Screen name="b2b/invoices" />
        <Stack.Screen name="b2b/invoices/[id]" />
        <Stack.Screen name="retailer" />
        <Stack.Screen name="retailer/dashboard" />
        <Stack.Screen name="retailer/products" />
        <Stack.Screen name="retailer/orders" />
        <Stack.Screen name="retailer/orders/[id]" />
        <Stack.Screen name="retailer/analytics" />
      </Stack>
    </QueryClientProvider>
  );
}
