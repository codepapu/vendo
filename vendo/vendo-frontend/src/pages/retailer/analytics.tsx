import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { useRetailerStore } from '@/stores/retailer-store';
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
} from 'recharts';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8', '#82ca9d'];

export default function RetailerAnalytics() {
  const { currentRetailer, analytics, loadAnalytics, isLoadingAnalytics } = useRetailerStore();
  const [startDate, setStartDate] = useState(() => {
    const date = new Date();
    date.setMonth(date.getMonth() - 3);
    return date.toISOString().split('T')[0];
  });
  const [endDate, setEndDate] = useState(() => {
    return new Date().toISOString().split('T')[0];
  });

  useEffect(() => {
    if (currentRetailer?.id) {
      loadAnalytics(currentRetailer.id, {
        startDate,
        endDate,
      });
    }
  }, [currentRetailer?.id, startDate, endDate]);

  const handleExportCSV = () => {
    if (!analytics) return;

    // Create CSV content
    let csvContent = 'Analytics Report\n\n';
    csvContent += `Period: ${startDate} to ${endDate}\n\n`;
    csvContent += 'Sales Over Time\n';
    csvContent += 'Date,Sales,Orders,Revenue\n';
    
    analytics.salesOverTime?.forEach((point) => {
      csvContent += `${point.date},${point.sales},${point.orders},${point.revenue}\n`;
    });

    csvContent += '\nTop Products\n';
    csvContent += 'Product Name,Quantity Sold,Total Revenue\n';
    analytics.topProducts?.forEach((product) => {
      csvContent += `${product.productName},${product.quantitySold},${product.totalRevenue}\n`;
    });

    // Create and download file
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `retailer-analytics-${startDate}-${endDate}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  };

  const handleExportPDF = () => {
    // In a real implementation, you would use a library like jsPDF or pdfmake
    alert('PDF export functionality would be implemented with a PDF library');
  };

  if (!currentRetailer) {
    return (
      <div className="container mx-auto py-8">
        <Card>
          <CardContent className="pt-6">
            <p>You are not registered as a retailer yet.</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (isLoadingAnalytics) {
    return (
      <div className="container mx-auto py-8">
        <p>Loading analytics...</p>
      </div>
    );
  }

  if (!analytics) {
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

  return (
    <div className="container mx-auto py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Analytics & Reports</h1>
        <div className="flex gap-2">
          <Button onClick={handleExportCSV} variant="outline">
            Export CSV
          </Button>
          <Button onClick={handleExportPDF} variant="outline">
            Export PDF
          </Button>
        </div>
      </div>

      {/* Date Range Picker */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Date Range</CardTitle>
          <CardDescription>Select a date range for custom reports</CardDescription>
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
          </div>
        </CardContent>
      </Card>

      {/* Sales Over Time Chart */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Sales Over Time</CardTitle>
          <CardDescription>Revenue and order trends</CardDescription>
        </CardHeader>
        <CardContent>
          {analytics.salesOverTime && analytics.salesOverTime.length > 0 ? (
            <ResponsiveContainer width="100%" height={400}>
              <LineChart data={analytics.salesOverTime}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis yAxisId="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip />
                <Legend />
                <Line
                  yAxisId="left"
                  type="monotone"
                  dataKey="revenue"
                  stroke="#8884d8"
                  name="Revenue ($)"
                  strokeWidth={2}
                />
                <Line
                  yAxisId="right"
                  type="monotone"
                  dataKey="orders"
                  stroke="#82ca9d"
                  name="Orders"
                  strokeWidth={2}
                />
              </LineChart>
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
            <CardDescription>Best selling products by revenue</CardDescription>
          </CardHeader>
          <CardContent>
            {analytics.topProducts && analytics.topProducts.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={analytics.topProducts.slice(0, 10)}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="productName" angle={-45} textAnchor="end" height={100} />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="totalRevenue" fill="#8884d8" name="Revenue ($)" />
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
            <CardDescription>Sales breakdown by product category</CardDescription>
          </CardHeader>
          <CardContent>
            {analytics.revenueByCategory && analytics.revenueByCategory.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={analytics.revenueByCategory}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ categoryName, revenue }) => `${categoryName}: $${revenue.toFixed(0)}`}
                    outerRadius={80}
                    fill="#8884d8"
                    dataKey="revenue"
                  >
                    {analytics.revenueByCategory.map((_entry, index) => (
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

      {/* Customer Demographics */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Customer Demographics</CardTitle>
          <CardDescription>Customer segment analysis</CardDescription>
        </CardHeader>
        <CardContent>
          {analytics.customerDemographics && analytics.customerDemographics.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={analytics.customerDemographics}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="segment" />
                <YAxis yAxisId="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip />
                <Legend />
                <Bar yAxisId="left" dataKey="count" fill="#8884d8" name="Customer Count" />
                <Bar yAxisId="right" dataKey="totalSpent" fill="#82ca9d" name="Total Spent ($)" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-center text-muted-foreground py-8">No demographic data available</p>
          )}
        </CardContent>
      </Card>

      {/* Commission Breakdown */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Commission Breakdown</CardTitle>
          <CardDescription>Commission tracking and payout history</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
            <div>
              <p className="text-sm text-muted-foreground">Total Commissions</p>
              <p className="text-2xl font-bold">
                ${analytics.commissionBreakdown?.totalCommissions.toFixed(2) || '0.00'}
              </p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Pending</p>
              <p className="text-2xl font-bold text-yellow-500">
                ${analytics.commissionBreakdown?.pendingCommissions.toFixed(2) || '0.00'}
              </p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Paid</p>
              <p className="text-2xl font-bold text-green-500">
                ${analytics.commissionBreakdown?.paidCommissions.toFixed(2) || '0.00'}
              </p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Avg Rate</p>
              <p className="text-2xl font-bold">
                {analytics.commissionBreakdown?.averageCommissionRate.toFixed(2) || '0.00'}%
              </p>
            </div>
          </div>

          {analytics.commissionBreakdown?.byPeriod && analytics.commissionBreakdown.byPeriod.length > 0 && (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={analytics.commissionBreakdown.byPeriod}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="period" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="amount" fill="#8884d8" name="Commission ($)" />
              </BarChart>
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>

      {/* Payout History */}
      <Card>
        <CardHeader>
          <CardTitle>Payout History</CardTitle>
          <CardDescription>Commission payout transactions</CardDescription>
        </CardHeader>
        <CardContent>
          {analytics.payoutHistory && analytics.payoutHistory.length > 0 ? (
            <div className="space-y-2">
              {analytics.payoutHistory.map((payout) => (
                <div
                  key={payout.id}
                  className="flex justify-between items-center p-4 border rounded"
                >
                  <div>
                    <p className="font-medium">${payout.amount.toFixed(2)}</p>
                    <p className="text-sm text-muted-foreground">
                      {payout.description || 'Commission payout'}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {new Date(payout.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                  <div className="text-right">
                    <Badge
                      variant={
                        payout.status === 'PROCESSED'
                          ? 'default'
                          : payout.status === 'PENDING'
                          ? 'secondary'
                          : 'destructive'
                      }
                    >
                      {payout.status}
                    </Badge>
                    {payout.processedAt && (
                      <p className="text-xs text-muted-foreground mt-1">
                        Processed: {new Date(payout.processedAt).toLocaleDateString()}
                      </p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-center text-muted-foreground py-8">No payout history available</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

