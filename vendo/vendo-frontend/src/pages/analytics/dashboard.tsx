import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
Select,
SelectContent,
SelectItem,
SelectTrigger,
SelectValue,
} from '@/components/ui/select';
import { analyticsApi } from '@/services/analytics-api';
import { reportingApi } from '@/services/reporting-api';
import type {
AnalyticsDashboard,ConversionRateMetrics,
TopCustomer,
RevenueByCategory
} from '@/types';
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

export default function AnalyticsDashboard() {
  const [startDate, setStartDate] = useState(() => {
    const date = new Date();
    date.setMonth(date.getMonth() - 1);
    return date.toISOString().split('T')[0];
  });
  const [endDate, setEndDate] = useState(() => {
    return new Date().toISOString().split('T')[0];
  });
  const [reportFormat, setReportFormat] = useState<'PDF' | 'CSV' | 'EXCEL'>('PDF');
  const [isGeneratingReport, setIsGeneratingReport] = useState(false);

  const { data: dashboard, isLoading, refetch } = useQuery({
    queryKey: ['analytics-dashboard', startDate, endDate],
    queryFn: () => analyticsApi.getDashboard(),
  });

  // Calculate conversion rates from dashboard data
  const calculateConversionRates = (dashboard: AnalyticsDashboard | undefined): ConversionRateMetrics | null => {
    if (!dashboard) return null;

    const sales = dashboard.sales;
    const products = dashboard.products;

    // Mock conversion rate calculation (in production, this would come from backend)
    const totalViews = products.productViews.reduce((sum, pv) => sum + (pv.views || 0), 0);
    const totalPurchases = sales.totalOrders;
    const overallConversionRate = totalViews > 0 ? (totalPurchases / totalViews) * 100 : 0;

    return {
      overallConversionRate: Math.round(overallConversionRate * 100) / 100,
      cartToCheckoutRate: 75.5, // Mock data
      checkoutToPurchaseRate: 85.2, // Mock data
      productViewToCartRate: 12.3, // Mock data
      byCategory: [], // Would be calculated from backend
      byPeriod: sales.revenueByPeriod.map((period) => ({
        period: period.period,
        conversionRate: Math.random() * 20, // Mock data
        views: period.orders * 10, // Mock data
        purchases: period.orders,
      })),
    };
  };

  // Get top customers (mock data - would come from backend)
  const getTopCustomers = (): TopCustomer[] => {
    // In production, this would come from the backend
    return [
      {
        userId: 1,
        userName: 'John Doe',
        userEmail: 'john@example.com',
        totalSpent: 12500.00,
        orderCount: 15,
        averageOrderValue: 833.33,
        lastOrderDate: new Date().toISOString(),
      },
      {
        userId: 2,
        userName: 'Jane Smith',
        userEmail: 'jane@example.com',
        totalSpent: 9800.00,
        orderCount: 12,
        averageOrderValue: 816.67,
        lastOrderDate: new Date(Date.now() - 86400000).toISOString(),
      },
    ];
  };

  // Get revenue by category (mock data - would come from backend)
  const getRevenueByCategory = (): RevenueByCategory[] => {
    // In production, this would come from the backend
    return [
      {
        categoryId: 1,
        categoryName: 'Electronics',
        revenue: 45000.00,
        orderCount: 120,
        productCount: 45,
        averageOrderValue: 375.00,
      },
      {
        categoryId: 2,
        categoryName: 'Fashion',
        revenue: 32000.00,
        orderCount: 95,
        productCount: 38,
        averageOrderValue: 336.84,
      },
    ];
  };

  const handleExportReport = async () => {
    setIsGeneratingReport(true);
    try {
      const fileExtension = reportFormat === 'EXCEL' ? 'xlsx' : reportFormat.toLowerCase();
      const success = await reportingApi.generateAndDownloadReport(
        {
          reportType: 'SALES',
          startDate,
          endDate,
          format: reportFormat,
          includeCharts: true,
        },
        `analytics-report-${startDate}-${endDate}.${fileExtension}`
      );

      if (success) {
        // Success toast is shown in the API service
      }
    } catch (error) {
      console.error('Failed to export report:', error);
    } finally {
      setIsGeneratingReport(false);
    }
  };

  if (isLoading) {
    return (
      <div className="container mx-auto py-8">
        <p>Loading analytics dashboard...</p>
      </div>
    );
  }

  if (!dashboard) {
    return (
      <div className="container mx-auto py-8">
        <Card>
          <CardContent className="pt-6">
            <p>No analytics data available.</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  const conversionRates = calculateConversionRates(dashboard);
  const topCustomers = getTopCustomers();
  const revenueByCategory = getRevenueByCategory();

  return (
    <div className="container mx-auto py-8">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold">Analytics Dashboard</h1>
          <p className="text-muted-foreground">Comprehensive analytics and insights</p>
        </div>
        <div className="flex gap-2">
          <Select value={reportFormat} onValueChange={(value: any) => setReportFormat(value)}>
            <SelectTrigger className="w-[120px]">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="PDF">PDF</SelectItem>
              <SelectItem value="CSV">CSV</SelectItem>
              <SelectItem value="EXCEL">Excel (XLSX)</SelectItem>
            </SelectContent>
          </Select>
          <Button onClick={handleExportReport} disabled={isGeneratingReport}>
            {isGeneratingReport ? 'Generating...' : 'Export Report'}
          </Button>
        </div>
      </div>

      {/* Date Range Picker */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Date Range</CardTitle>
          <CardDescription>Select a date range for analytics</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex gap-4">
            <div className="space-y-2">
              <Label htmlFor="startDate">Start Date</Label>
              <Input
                id="startDate"
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="endDate">End Date</Label>
              <Input
                id="endDate"
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
              />
            </div>
            <div className="flex items-end">
              <Button onClick={() => refetch()}>Apply</Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Key Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <Card>
          <CardHeader>
            <CardTitle>Total Revenue</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">${dashboard.sales.totalRevenue.toLocaleString()}</p>
            <p className="text-sm text-muted-foreground">
              Today: ${dashboard.sales.todayRevenue.toLocaleString()}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Total Orders</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{dashboard.sales.totalOrders}</p>
            <p className="text-sm text-muted-foreground">
              Today: {dashboard.sales.todayOrders}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Average Order Value</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">${dashboard.sales.averageOrderValue.toFixed(2)}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Total Users</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{dashboard.users.totalUsers}</p>
            <p className="text-sm text-muted-foreground">
              Active: {dashboard.users.activeUsers} | New Today: {dashboard.users.newUsersToday}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Sales Over Time Chart */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Sales Over Time</CardTitle>
          <CardDescription>Revenue and order trends</CardDescription>
        </CardHeader>
        <CardContent>
          {dashboard.sales.revenueByPeriod && dashboard.sales.revenueByPeriod.length > 0 ? (
            <ResponsiveContainer width="100%" height={400}>
              <AreaChart data={dashboard.sales.revenueByPeriod}>
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
          ) : (
            <p className="text-center text-muted-foreground py-8">No sales data available</p>
          )}
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        {/* Top Products */}
        <Card>
          <CardHeader>
            <CardTitle>Top Products</CardTitle>
            <CardDescription>Best selling products</CardDescription>
          </CardHeader>
          <CardContent>
            {dashboard.products.productSales && dashboard.products.productSales.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={dashboard.products.productSales.slice(0, 10)}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="productName" angle={-45} textAnchor="end" height={100} />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="revenue" fill="#8884d8" name="Revenue ($)" />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <p className="text-center text-muted-foreground py-8">No product data available</p>
            )}
          </CardContent>
        </Card>

        {/* Revenue by Category */}
        <Card>
          <CardHeader>
            <CardTitle>Revenue by Category</CardTitle>
            <CardDescription>Sales breakdown by category</CardDescription>
          </CardHeader>
          <CardContent>
            {revenueByCategory.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={revenueByCategory}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ categoryName, revenue }) => `${categoryName}: $${revenue.toFixed(0)}`}
                    outerRadius={80}
                    fill="#8884d8"
                    dataKey="revenue"
                  >
                    {revenueByCategory.map((_entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <p className="text-center text-muted-foreground py-8">No category data available</p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Conversion Rates */}
      {conversionRates && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Conversion Rates</CardTitle>
            <CardDescription>Conversion metrics and trends</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
              <div>
                <p className="text-sm text-muted-foreground">Overall Conversion</p>
                <p className="text-2xl font-bold">{conversionRates.overallConversionRate.toFixed(2)}%</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Cart to Checkout</p>
                <p className="text-2xl font-bold">{conversionRates.cartToCheckoutRate.toFixed(2)}%</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Checkout to Purchase</p>
                <p className="text-2xl font-bold">{conversionRates.checkoutToPurchaseRate.toFixed(2)}%</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">View to Cart</p>
                <p className="text-2xl font-bold">{conversionRates.productViewToCartRate.toFixed(2)}%</p>
              </div>
            </div>

            {conversionRates.byPeriod && conversionRates.byPeriod.length > 0 && (
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={conversionRates.byPeriod}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="period" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line
                    type="monotone"
                    dataKey="conversionRate"
                    stroke="#8884d8"
                    name="Conversion Rate (%)"
                  />
                </LineChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>
      )}

      {/* Top Customers */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Top Customers</CardTitle>
          <CardDescription>Highest value customers</CardDescription>
        </CardHeader>
        <CardContent>
          {topCustomers.length > 0 ? (
            <div className="space-y-2">
              {topCustomers.map((customer) => (
                <div
                  key={customer.userId}
                  className="flex justify-between items-center p-4 border rounded"
                >
                  <div>
                    <p className="font-medium">{customer.userName}</p>
                    <p className="text-sm text-muted-foreground">{customer.userEmail}</p>
                    <p className="text-xs text-muted-foreground">
                      {customer.orderCount} orders
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold">${customer.totalSpent.toLocaleString()}</p>
                    <p className="text-sm text-muted-foreground">
                      Avg: ${customer.averageOrderValue.toFixed(2)}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-center text-muted-foreground py-8">No customer data available</p>
          )}
        </CardContent>
      </Card>

      {/* User Growth */}
      <Card>
        <CardHeader>
          <CardTitle>User Growth</CardTitle>
          <CardDescription>New user registration trends</CardDescription>
        </CardHeader>
        <CardContent>
          {dashboard.users.userGrowth && dashboard.users.userGrowth.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={dashboard.users.userGrowth}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="period" />
                <YAxis yAxisId="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip />
                <Legend />
                <Bar yAxisId="left" dataKey="newUsers" fill="#8884d8" name="New Users" />
                <Bar yAxisId="right" dataKey="activeUsers" fill="#82ca9d" name="Active Users" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-center text-muted-foreground py-8">No user growth data available</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

