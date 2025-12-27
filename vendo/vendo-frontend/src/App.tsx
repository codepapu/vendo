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
const PaymentPage = lazy(() => import('@/pages/payment/index').then((m) => ({ default: m.default })));
const PaymentSuccess = lazy(() => import('@/pages/payment/success').then((m) => ({ default: m.PaymentSuccessPage })));
const PaymentCancel = lazy(() => import('@/pages/payment/cancel').then((m) => ({ default: m.PaymentCancelPage })));
const BlogIndex = lazy(() => import('@/pages/blog/index').then((m) => ({ default: m.BlogIndex })));
const BlogPostDetail = lazy(() => import('@/pages/blog/[slug]').then((m) => ({ default: m.BlogPostDetail })));
const DynamicPage = lazy(() => import('@/pages/pages/[slug]').then((m) => ({ default: m.DynamicPage })));
const AdminBlogPosts = lazy(() => import('@/pages/admin/content/blog').then((m) => ({ default: m.AdminBlogPosts })));
const AdminBlogPostEditor = lazy(() => import('@/pages/admin/content/blog/[id]').then((m) => ({ default: m.AdminBlogPostEditor })));
const AdminPages = lazy(() => import('@/pages/admin/content/pages').then((m) => ({ default: m.AdminPages })));
const AdminPageEditor = lazy(() => import('@/pages/admin/content/pages/[id]').then((m) => ({ default: m.AdminPageEditor })));
const AdminProducts = lazy(() => import('@/pages/admin/products').then((m) => ({ default: m.AdminProducts })));
const AdminOrders = lazy(() => import('@/pages/admin/orders').then((m) => ({ default: m.AdminOrders })));
const AdminUsers = lazy(() => import('@/pages/admin/users').then((m) => ({ default: m.AdminUsers })));
const AdminSettings = lazy(() => import('@/pages/admin/settings').then((m) => ({ default: m.AdminSettings })));
const AdminCoupons = lazy(() => import('@/pages/admin/coupons').then((m) => ({ default: m.AdminCoupons })));
const AdminBusinessAccounts = lazy(() => import('@/pages/admin/business-accounts').then((m) => ({ default: m.AdminBusinessAccounts })));
const AdminRetailers = lazy(() => import('@/pages/admin/retailers').then((m) => ({ default: m.AdminRetailers })));
const AdminDashboard = lazy(() => import('@/pages/admin/dashboard').then((m) => ({ default: m.AdminDashboard })));
const RetailerRegister = lazy(() => import('@/pages/retailer/register').then((m) => ({ default: m.default })));
const RetailerDashboard = lazy(() => import('@/pages/retailer/dashboard').then((m) => ({ default: m.default })));
const RetailerProducts = lazy(() => import('@/pages/retailer/products').then((m) => ({ default: m.default })));
const RetailerOrders = lazy(() => import('@/pages/retailer/orders').then((m) => ({ default: m.default })));
const RetailerAnalytics = lazy(() => import('@/pages/retailer/analytics').then((m) => ({ default: m.default })));
const AnalyticsDashboard = lazy(() => import('@/pages/analytics/dashboard').then((m) => ({ default: m.default })));
// Public pages
const About = lazy(() => import('@/pages/about').then((m) => ({ default: m.About })));
const Contact = lazy(() => import('@/pages/contact').then((m) => ({ default: m.Contact })));
const Privacy = lazy(() => import('@/pages/privacy').then((m) => ({ default: m.Privacy })));
const Terms = lazy(() => import('@/pages/terms').then((m) => ({ default: m.Terms })));
const FAQ = lazy(() => import('@/pages/faq').then((m) => ({ default: m.FAQ })));
const Categories = lazy(() => import('@/pages/categories').then((m) => ({ default: m.Categories })));
const Stores = lazy(() => import('@/pages/stores').then((m) => ({ default: m.Stores })));
const StoreDetail = lazy(() => import('@/pages/stores/[slug]').then((m) => ({ default: m.StoreDetail })));

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
              {/* Payment Routes */}
              <Route
                path="/payment"
                element={
                  <ProtectedRoute>
                    <PaymentPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/payment/success"
                element={<PaymentSuccess />}
              />
              <Route
                path="/payment/cancel"
                element={<PaymentCancel />}
              />
              {/* Public Pages */}
              <Route path="/about" element={<About />} />
              <Route path="/contact" element={<Contact />} />
              <Route path="/privacy" element={<Privacy />} />
              <Route path="/terms" element={<Terms />} />
              <Route path="/faq" element={<FAQ />} />
              <Route path="/categories" element={<Categories />} />
              <Route path="/stores" element={<Stores />} />
              <Route path="/stores/:slug" element={<StoreDetail />} />
              {/* Content Management Routes */}
              <Route path="/blog" element={<BlogIndex />} />
              <Route path="/blog/:slug" element={<BlogPostDetail />} />
              <Route path="/pages/:slug" element={<DynamicPage />} />
              {/* Admin Content Routes */}
              <Route
                path="/admin/content/blog"
                element={
                  <ProtectedRoute>
                    <AdminBlogPosts />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/content/blog/:id"
                element={
                  <ProtectedRoute>
                    <AdminBlogPostEditor />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/content/pages"
                element={
                  <ProtectedRoute>
                    <AdminPages />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/content/pages/:id"
                element={
                  <ProtectedRoute>
                    <AdminPageEditor />
                  </ProtectedRoute>
                }
              />
              {/* Admin Management Routes */}
              <Route
                path="/admin/products"
                element={
                  <ProtectedRoute>
                    <AdminProducts />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/orders"
                element={
                  <ProtectedRoute>
                    <AdminOrders />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/users"
                element={
                  <ProtectedRoute>
                    <AdminUsers />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/settings"
                element={
                  <ProtectedRoute>
                    <AdminSettings />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/coupons"
                element={
                  <ProtectedRoute>
                    <AdminCoupons />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/business-accounts"
                element={
                  <ProtectedRoute>
                    <AdminBusinessAccounts />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/retailers"
                element={
                  <ProtectedRoute>
                    <AdminRetailers />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/dashboard"
                element={
                  <ProtectedRoute>
                    <AdminDashboard />
                  </ProtectedRoute>
                }
              />
              {/* Retailer Routes */}
              <Route
                path="/retailer/register"
                element={
                  <ProtectedRoute>
                    <RetailerRegister />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/retailer/dashboard"
                element={
                  <ProtectedRoute>
                    <RetailerDashboard />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/retailer/products"
                element={
                  <ProtectedRoute>
                    <RetailerProducts />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/retailer/orders"
                element={
                  <ProtectedRoute>
                    <RetailerOrders />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/retailer/analytics"
                element={
                  <ProtectedRoute>
                    <RetailerAnalytics />
                  </ProtectedRoute>
                }
              />
              {/* Analytics & Reporting Routes */}
              <Route
                path="/analytics/dashboard"
                element={
                  <ProtectedRoute>
                    <AnalyticsDashboard />
                  </ProtectedRoute>
                }
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
