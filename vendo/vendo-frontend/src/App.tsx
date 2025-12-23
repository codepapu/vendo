import { lazy, Suspense } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Layout } from '@/components/layout/layout';
import { Toaster } from '@/components/ui/toaster';
import { ProtectedRoute } from '@/components/protected-route';

// Lazy load pages for code splitting
const Home = lazy(() => import('@/pages/home').then((m) => ({ default: m.Home })));
const Products = lazy(() => import('@/pages/products').then((m) => ({ default: m.Products })));
const ProductDetail = lazy(() =>
  import('@/pages/product-detail').then((m) => ({ default: m.ProductDetail }))
);
const Cart = lazy(() => import('@/pages/cart').then((m) => ({ default: m.Cart })));
const Checkout = lazy(() => import('@/pages/checkout').then((m) => ({ default: m.Checkout })));
const Login = lazy(() => import('@/pages/login').then((m) => ({ default: m.Login })));
const Register = lazy(() => import('@/pages/register').then((m) => ({ default: m.Register })));
const Profile = lazy(() => import('@/pages/profile').then((m) => ({ default: m.Profile })));
const Addresses = lazy(() => import('@/pages/profile/addresses').then((m) => ({ default: m.Addresses })));
const Orders = lazy(() => import('@/pages/profile/orders').then((m) => ({ default: m.Orders })));
const Settings = lazy(() => import('@/pages/profile/settings').then((m) => ({ default: m.Settings })));
const OrderConfirmation = lazy(() => import('@/pages/checkout/confirmation').then((m) => ({ default: m.OrderConfirmation })));
const OrderDetail = lazy(() => import('@/pages/orders/[id]').then((m) => ({ default: m.OrderDetail })));
const GuestCheckout = lazy(() => import('@/pages/checkout/guest').then((m) => ({ default: m.GuestCheckout })));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5 minutes
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router>
        <Layout>
          <Suspense
            fallback={
              <div className="flex items-center justify-center min-h-screen">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
              </div>
            }
          >
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/products" element={<Products />} />
              <Route path="/products/:id" element={<ProductDetail />} />
              <Route
                path="/cart"
                element={
                  <ProtectedRoute>
                    <Cart />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/checkout"
                element={
                  <ProtectedRoute>
                    <Checkout />
                  </ProtectedRoute>
                }
              />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route
                path="/profile"
                element={
                  <ProtectedRoute>
                    <Profile />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/profile/addresses"
                element={
                  <ProtectedRoute>
                    <Addresses />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/profile/orders"
                element={
                  <ProtectedRoute>
                    <Orders />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/profile/settings"
                element={
                  <ProtectedRoute>
                    <Settings />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/checkout/confirmation"
                element={<OrderConfirmation />}
              />
              <Route
                path="/orders/:id"
                element={
                  <ProtectedRoute>
                    <OrderDetail />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/checkout/guest"
                element={<GuestCheckout />}
              />
            </Routes>
          </Suspense>
        </Layout>
        <Toaster />
      </Router>
    </QueryClientProvider>
  );
}

export default App;
