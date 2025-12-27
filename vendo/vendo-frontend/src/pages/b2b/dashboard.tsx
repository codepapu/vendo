import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useB2BStore } from '@/stores/b2b-store';
import { b2bApi } from '@/services/b2b-api';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

export default function B2BDashboard() {
  const { currentBusinessAccount, loadBusinessAccount, loadPurchaseOrders, loadInvoices } = useB2BStore();

  useEffect(() => {
    loadBusinessAccount();
    loadPurchaseOrders();
    loadInvoices();
  }, []);

  const { data: ordersData } = useQuery({
    queryKey: ['b2b-orders'],
    queryFn: () => b2bApi.getPurchaseOrders(),
  });

  const { data: invoicesData } = useQuery({
    queryKey: ['b2b-invoices'],
    queryFn: () => b2bApi.getInvoices(),
  });

  // Mock chart data - in production, would fetch from analytics API
  const chartData = [
    { month: 'Jan', orders: 12, revenue: 12000 },
    { month: 'Feb', orders: 15, revenue: 15000 },
    { month: 'Mar', orders: 18, revenue: 18000 },
    { month: 'Apr', orders: 20, revenue: 20000 },
  ];

  if (!currentBusinessAccount) {
    return (
      <div className="container mx-auto py-8">
        <p>Loading...</p>
      </div>
    );
  }

  const availableCredit = currentBusinessAccount.creditLimit - (currentBusinessAccount.totalSpent || 0);
  const pendingInvoices = invoicesData?.content.filter((inv) => inv.status === 'PENDING') || [];
  const recentOrders = ordersData?.content.slice(0, 5) || [];

  return (
    <div className="container mx-auto py-8">
      <h1 className="text-3xl font-bold mb-6">B2B Dashboard</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <Card>
          <CardHeader>
            <CardTitle>Account Status</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{currentBusinessAccount.status}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Credit Limit</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">${currentBusinessAccount.creditLimit.toLocaleString()}</p>
            <p className="text-sm text-muted-foreground">
              Available: ${availableCredit.toLocaleString()}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Total Orders</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{currentBusinessAccount.totalOrders || 0}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Pending Invoices</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{pendingInvoices.length}</p>
            <p className="text-sm text-muted-foreground">
              ${pendingInvoices.reduce((sum, inv) => sum + inv.totalAmount, 0).toLocaleString()}
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <Card>
          <CardHeader>
            <CardTitle>Sales Trends</CardTitle>
            <CardDescription>Monthly order and revenue trends</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="orders" stroke="#8884d8" name="Orders" />
                <Line type="monotone" dataKey="revenue" stroke="#82ca9d" name="Revenue ($)" />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Recent Purchase Orders</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {recentOrders.length === 0 ? (
                <p className="text-muted-foreground">No recent orders</p>
              ) : (
                recentOrders.map((order) => (
                  <div key={order.id} className="flex justify-between items-center p-2 border rounded">
                    <div>
                      <p className="font-medium">{order.poNumber}</p>
                      <p className="text-sm text-muted-foreground">{order.status}</p>
                    </div>
                    <p className="text-sm">{new Date(order.requestedAt).toLocaleDateString()}</p>
                  </div>
                ))
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

