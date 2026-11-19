import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useRetailerStore } from '@/stores/retailer-store';
import { useAuthStore } from '@/stores/auth-store';
import { retailerApi } from '@/services/retailer-api';
import { XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, BarChart, Bar } from 'recharts';

export default function RetailerDashboard() {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { currentRetailer, loadRetailer, loadDashboard, dashboardData, isLoadingDashboard } = useRetailerStore();

  useEffect(() => {
    if (user?.id) {
      loadRetailer(user.id);
      loadDashboard();
    }
  }, [user?.id]);

  const { data: dashboard } = useQuery({
    queryKey: ['retailer-dashboard'],
    queryFn: () => retailerApi.getDashboard(),
    enabled: !!user?.id,
  });

  const dashboardInfo = dashboard || dashboardData;

  if (!user) {
    return (
      <div className="container mx-auto py-8">
        <Card>
          <CardContent className="pt-6">
            <p>Please log in to view your dashboard.</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!currentRetailer) {
    return (
      <div className="container mx-auto py-8">
        <Card>
          <CardContent className="pt-6">
            <p>You are not registered as a retailer yet.</p>
            <Button onClick={() => navigate('/retailer/register')} className="mt-4">
              Register as Retailer
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (currentRetailer.status === 'PENDING') {
    return (
      <div className="container mx-auto py-8">
        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <h2 className="text-2xl font-bold mb-4">Registration Pending</h2>
              <p className="text-muted-foreground mb-4">
                Your retailer registration is being reviewed. You'll be notified once it's approved.
              </p>
              <p className="text-sm text-muted-foreground">
                Status: <span className="font-semibold">{currentRetailer.status}</span>
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (currentRetailer.status === 'REJECTED') {
    return (
      <div className="container mx-auto py-8">
        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <h2 className="text-2xl font-bold mb-4 text-red-500">Registration Rejected</h2>
              <p className="text-muted-foreground mb-4">
                Your retailer registration has been rejected. Please contact support for more information.
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (isLoadingDashboard || !dashboardInfo) {
    return (
      <div className="container mx-auto py-8">
        <p>Loading dashboard...</p>
      </div>
    );
  }

  // Prepare chart data from recent orders or sales data
  const salesChartData = dashboardInfo.recentOrders?.slice(0, 7).map((order) => ({
    date: new Date(order.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
    sales: order.totalAmount,
    orders: 1,
  })) || [];

  return (
    <div className="container mx-auto py-8">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold">{dashboardInfo.storeName} Dashboard</h1>
          <p className="text-muted-foreground">Welcome back! Here's your store overview.</p>
        </div>
        <div className="flex gap-2">
          <Button onClick={() => navigate('/retailer/products')} variant="outline">
            Manage Products
          </Button>
          <Button onClick={() => navigate('/retailer/orders')}>
            View Orders
          </Button>
        </div>
      </div>

      {/* Key Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <Card>
          <CardHeader>
            <CardTitle>Total Sales</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">${dashboardInfo.totalSales.toLocaleString()}</p>
            <p className="text-sm text-muted-foreground">
              This month: ${dashboardInfo.thisMonthSales.toLocaleString()}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Total Orders</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{dashboardInfo.totalOrders}</p>
            <p className="text-sm text-muted-foreground">
              This month: {dashboardInfo.thisMonthOrders}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Active Products</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{dashboardInfo.activeProducts}</p>
            {dashboardInfo.lowStockProducts > 0 && (
              <p className="text-sm text-red-500">
                {dashboardInfo.lowStockProducts} low stock
              </p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Commissions</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">${dashboardInfo.totalCommissions.toLocaleString()}</p>
            <p className="text-sm text-muted-foreground">
              Pending: ${dashboardInfo.pendingCommissions.toLocaleString()}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Charts and Recent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <Card>
          <CardHeader>
            <CardTitle>Sales Overview</CardTitle>
            <CardDescription>Recent sales and orders</CardDescription>
          </CardHeader>
          <CardContent>
            {salesChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={salesChartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="sales" fill="#8884d8" name="Sales ($)" />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <p className="text-muted-foreground text-center py-8">No sales data available</p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Top Products</CardTitle>
            <CardDescription>Best selling products</CardDescription>
          </CardHeader>
          <CardContent>
            {dashboardInfo.topProducts && dashboardInfo.topProducts.length > 0 ? (
              <div className="space-y-3">
                {dashboardInfo.topProducts.slice(0, 5).map((product) => (
                  <div key={product.productId} className="flex justify-between items-center p-2 border rounded">
                    <div>
                      <p className="font-medium">{product.productName}</p>
                      <p className="text-sm text-muted-foreground">
                        {product.quantitySold} sold
                      </p>
                    </div>
                    <p className="text-sm font-semibold">
                      ${product.totalRevenue.toLocaleString()}
                    </p>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground text-center py-8">No product data available</p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Recent Orders */}
      <Card>
        <CardHeader>
          <div className="flex justify-between items-center">
            <div>
              <CardTitle>Recent Orders</CardTitle>
              <CardDescription>Latest orders from your store</CardDescription>
            </div>
            <Button onClick={() => navigate('/retailer/orders')} variant="outline">
              View All
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {dashboardInfo.recentOrders && dashboardInfo.recentOrders.length > 0 ? (
            <div className="space-y-2">
              {dashboardInfo.recentOrders.map((order) => (
                <div
                  key={order.orderId}
                  className="flex justify-between items-center p-4 border rounded hover:bg-muted cursor-pointer"
                  onClick={() => navigate(`/retailer/orders/${order.orderId}`)}
                >
                  <div>
                    <p className="font-medium">Order #{order.orderNumber}</p>
                    <p className="text-sm text-muted-foreground">
                      {new Date(order.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold">${order.totalAmount.toLocaleString()}</p>
                    <p className={`text-sm ${
                      order.status === 'DELIVERED' ? 'text-green-500' :
                      order.status === 'PENDING' ? 'text-yellow-500' :
                      'text-blue-500'
                    }`}>
                      {order.status}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-muted-foreground text-center py-8">No recent orders</p>
          )}
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <div className="mt-6 flex gap-4">
        <Button onClick={() => navigate('/retailer/products')} variant="outline">
          Add Product
        </Button>
        <Button onClick={() => navigate('/retailer/analytics')} variant="outline">
          View Analytics
        </Button>
      </div>
    </div>
  );
}

