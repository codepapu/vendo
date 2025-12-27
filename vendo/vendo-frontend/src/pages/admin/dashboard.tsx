import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import {
  DollarSign,
  ShoppingCart,
  Users,
  Store,
  Clock,
  TrendingUp,
  FileText,
  CheckCircle,
  XCircle,
  AlertCircle,
} from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { adminApi } from '@/services/admin-api';
import { formatCurrency, formatDate } from '@/lib/utils';
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  AreaChart,
  Area,
} from 'recharts';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8', '#82ca9d'];

/**
 * Admin Dashboard Interface
 */
interface AdminDashboard {
  stats?: {
    totalSales?: number;
    totalOrders?: number;
    activeUsers?: number;
    activeRetailers?: number;
    pendingApprovals?: number;
    totalRevenue?: number;
  };
  salesOverTime?: Array<{
    period: string;
    revenue: number;
    orders: number;
  }>;
  revenueByType?: Array<{
    type: string;
    revenue: number;
  }>;
  topProducts?: Array<{
    productId: number;
    productName: string;
    revenue: number;
    orders: number;
  }>;
  recentActivity?: Array<{
    id: number;
    type: string;
    description: string;
    timestamp: string;
    status?: string;
  }>;
  pendingBusinessAccounts?: number;
  pendingRetailers?: number;
}

/**
 * Admin Dashboard page
 */
export function AdminDashboard() {
  const navigate = useNavigate();

  // Fetch dashboard data
  const { data: dashboard, isLoading } = useQuery({
    queryKey: ['admin-dashboard'],
    queryFn: () => adminApi.getAdminDashboard(),
  });

  // Fetch pending business accounts
  const { data: pendingAccountsData } = useQuery({
    queryKey: ['admin-business-accounts', 'PENDING'],
    queryFn: () => adminApi.getAllBusinessAccounts({ status: 'PENDING', page: 0, size: 10 }),
  });

  // Fetch pending retailers
  const { data: pendingRetailersData } = useQuery({
    queryKey: ['admin-retailers', 'PENDING'],
    queryFn: () => adminApi.getAllRetailers({ status: 'PENDING', page: 0, size: 10 }),
  });

  const stats = dashboard?.stats || {};
  const pendingBusinessAccounts = pendingAccountsData?.content?.length || 0;
  const pendingRetailers = pendingRetailersData?.content?.length || 0;

  // Mock chart data - in production, this would come from the backend
  const salesOverTimeData = dashboard?.salesOverTime || [
    { period: 'Jan', revenue: 50000, orders: 120 },
    { period: 'Feb', revenue: 75000, orders: 150 },
    { period: 'Mar', revenue: 60000, orders: 130 },
    { period: 'Apr', revenue: 90000, orders: 180 },
    { period: 'May', revenue: 85000, orders: 170 },
    { period: 'Jun', revenue: 100000, orders: 200 },
  ];

  const revenueByTypeData = dashboard?.revenueByType || [
    { type: 'B2C', revenue: 400000 },
    { type: 'B2B', revenue: 250000 },
    { type: 'Retailer', revenue: 150000 },
  ];

  const topProductsData = dashboard?.topProducts || [
    { productId: 1, productName: 'Product A', revenue: 50000, orders: 100 },
    { productId: 2, productName: 'Product B', revenue: 45000, orders: 90 },
    { productId: 3, productName: 'Product C', revenue: 40000, orders: 80 },
    { productId: 4, productName: 'Product D', revenue: 35000, orders: 70 },
    { productId: 5, productName: 'Product E', revenue: 30000, orders: 60 },
  ];

  const recentActivity = dashboard?.recentActivity || [
    {
      id: 1,
      type: 'ORDER',
      description: 'New order #12345 placed',
      timestamp: new Date().toISOString(),
      status: 'PENDING',
    },
    {
      id: 2,
      type: 'BUSINESS_ACCOUNT',
      description: 'Business account "ABC Corp" approved',
      timestamp: new Date(Date.now() - 3600000).toISOString(),
      status: 'APPROVED',
    },
    {
      id: 3,
      type: 'RETAILER',
      description: 'Retailer "Store XYZ" registered',
      timestamp: new Date(Date.now() - 7200000).toISOString(),
      status: 'PENDING',
    },
  ];

  if (isLoading) {
    return (
      <div className="container mx-auto py-8 px-4">
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-8 px-4">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold">Admin Dashboard</h1>
          <p className="text-muted-foreground mt-1">Overview of your e-commerce platform</p>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Sales</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatCurrency(stats.totalSales || stats.totalRevenue || 0)}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              <TrendingUp className="inline h-3 w-3 mr-1" />
              +12.5% from last month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Orders</CardTitle>
            <ShoppingCart className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.totalOrders || 0}</div>
            <p className="text-xs text-muted-foreground mt-1">
              <TrendingUp className="inline h-3 w-3 mr-1" />
              +8.2% from last month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Users</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.activeUsers || 0}</div>
            <p className="text-xs text-muted-foreground mt-1">
              <TrendingUp className="inline h-3 w-3 mr-1" />
              +5.1% from last month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Retailers</CardTitle>
            <Store className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.activeRetailers || 0}</div>
            <p className="text-xs text-muted-foreground mt-1">
              <TrendingUp className="inline h-3 w-3 mr-1" />
              +2.3% from last month
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Quick Actions */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
          <CardDescription>Common administrative tasks</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-wrap gap-4">
            <Button
              variant="outline"
              onClick={() => navigate('/admin/business-accounts')}
              className="relative"
            >
              <CheckCircle className="h-4 w-4 mr-2" />
              Approve Business Accounts
              {pendingBusinessAccounts > 0 && (
                <Badge variant="destructive" className="ml-2">
                  {pendingBusinessAccounts}
                </Badge>
              )}
            </Button>
            <Button
              variant="outline"
              onClick={() => navigate('/admin/retailers')}
              className="relative"
            >
              <CheckCircle className="h-4 w-4 mr-2" />
              Approve Retailers
              {pendingRetailers > 0 && (
                <Badge variant="destructive" className="ml-2">
                  {pendingRetailers}
                </Badge>
              )}
            </Button>
            <Button variant="outline" onClick={() => navigate('/admin/orders')}>
              <FileText className="h-4 w-4 mr-2" />
              View Reports
            </Button>
            <Button variant="outline" onClick={() => navigate('/admin/products')}>
              <ShoppingCart className="h-4 w-4 mr-2" />
              Manage Products
            </Button>
            <Button variant="outline" onClick={() => navigate('/admin/users')}>
              <Users className="h-4 w-4 mr-2" />
              Manage Users
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        {/* Sales Over Time */}
        <Card>
          <CardHeader>
            <CardTitle>Sales Over Time</CardTitle>
            <CardDescription>Revenue and orders trend</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={salesOverTimeData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="period" />
                <YAxis yAxisId="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip />
                <Legend />
                <Area
                  yAxisId="left"
                  type="monotone"
                  dataKey="revenue"
                  stroke="#8884d8"
                  fill="#8884d8"
                  fillOpacity={0.6}
                  name="Revenue ($)"
                />
                <Line
                  yAxisId="right"
                  type="monotone"
                  dataKey="orders"
                  stroke="#82ca9d"
                  name="Orders"
                />
              </AreaChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Revenue by Type */}
        <Card>
          <CardHeader>
            <CardTitle>Revenue by Type</CardTitle>
            <CardDescription>B2C, B2B, and Retailer revenue breakdown</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={revenueByTypeData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ type, percent }) => `${type}: ${(percent * 100).toFixed(0)}%`}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="revenue"
                >
                  {revenueByTypeData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(value: number) => formatCurrency(value)} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Top Products */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Top Products</CardTitle>
          <CardDescription>Best selling products by revenue</CardDescription>
        </CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={topProductsData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="productName" angle={-45} textAnchor="end" height={100} />
              <YAxis />
              <Tooltip formatter={(value: number) => formatCurrency(value)} />
              <Legend />
              <Bar dataKey="revenue" fill="#8884d8" name="Revenue ($)" />
            </BarChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>

      {/* Recent Activity */}
      <Card>
        <CardHeader>
          <CardTitle>Recent Activity</CardTitle>
          <CardDescription>Latest system events and actions</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {recentActivity.length === 0 ? (
              <p className="text-center text-muted-foreground py-8">No recent activity</p>
            ) : (
              recentActivity.map((activity) => (
                <div
                  key={activity.id}
                  className="flex items-start gap-4 p-4 border rounded-lg hover:bg-muted/50"
                >
                  <div className="mt-1">
                    {activity.status === 'APPROVED' || activity.type === 'ORDER' ? (
                      <CheckCircle className="h-5 w-5 text-green-600" />
                    ) : activity.status === 'PENDING' ? (
                      <Clock className="h-5 w-5 text-yellow-600" />
                    ) : (
                      <AlertCircle className="h-5 w-5 text-red-600" />
                    )}
                  </div>
                  <div className="flex-1">
                    <p className="font-medium">{activity.description}</p>
                    <p className="text-sm text-muted-foreground mt-1">
                      {formatDate(activity.timestamp)}
                    </p>
                  </div>
                  {activity.status && (
                    <Badge
                      variant={
                        activity.status === 'APPROVED'
                          ? 'default'
                          : activity.status === 'PENDING'
                          ? 'outline'
                          : 'destructive'
                      }
                    >
                      {activity.status}
                    </Badge>
                  )}
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

