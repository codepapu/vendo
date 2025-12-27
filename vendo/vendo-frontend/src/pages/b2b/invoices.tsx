import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useToast } from '@/hooks/use-toast';
import { b2bApi } from '@/services/b2b-api';

export default function Invoices() {
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const { toast } = useToast();

  const { data: invoicesData, refetch } = useQuery({
    queryKey: ['b2b-invoices', statusFilter],
    queryFn: () => b2bApi.getInvoices(statusFilter !== 'all' ? statusFilter : undefined),
  });

  const invoices = invoicesData?.content || [];

  const handleMarkPaid = async (invoiceId: number) => {
    try {
      await b2bApi.markInvoicePaid(invoiceId, 'MANUAL');
      toast({
        title: 'Success',
        description: 'Invoice marked as paid',
      });
      refetch();
    } catch (error: any) {
      toast({
        title: 'Error',
        description: error.response?.data?.message || 'Failed to mark invoice as paid',
        variant: 'destructive',
      });
    }
  };

  return (
    <div className="container mx-auto py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Invoices</h1>
        <div className="flex gap-4">
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-40">
              <SelectValue placeholder="Filter by status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All</SelectItem>
              <SelectItem value="PENDING">Pending</SelectItem>
              <SelectItem value="PAID">Paid</SelectItem>
              <SelectItem value="OVERDUE">Overdue</SelectItem>
            </SelectContent>
          </Select>
          <Button onClick={() => refetch()}>Refresh</Button>
        </div>
      </div>

      <div className="space-y-4">
        {invoices.length === 0 ? (
          <Card>
            <CardContent className="py-8 text-center">
              <p className="text-muted-foreground">No invoices found</p>
            </CardContent>
          </Card>
        ) : (
          invoices.map((invoice) => (
            <Card key={invoice.id}>
              <CardHeader>
                <div className="flex justify-between items-start">
                  <div>
                    <CardTitle>{invoice.invoiceNumber}</CardTitle>
                    <p className="text-sm text-muted-foreground">
                      Issue Date: {new Date(invoice.issueDate).toLocaleDateString()} | Due Date:{' '}
                      {new Date(invoice.dueDate).toLocaleDateString()}
                    </p>
                  </div>
                  <span
                    className={`px-3 py-1 rounded-full text-sm ${
                      invoice.status === 'PAID'
                        ? 'bg-green-100 text-green-800'
                        : invoice.status === 'OVERDUE'
                        ? 'bg-red-100 text-red-800'
                        : 'bg-yellow-100 text-yellow-800'
                    }`}
                  >
                    {invoice.status}
                  </span>
                </div>
              </CardHeader>
              <CardContent>
                <div className="flex justify-between items-center">
                  <div>
                    <p className="text-2xl font-bold">${invoice.totalAmount.toFixed(2)}</p>
                    {invoice.status === 'PENDING' && (
                      <p className="text-sm text-muted-foreground">
                        Due: {new Date(invoice.dueDate).toLocaleDateString()}
                      </p>
                    )}
                  </div>
                  {invoice.status === 'PENDING' && (
                    <Button onClick={() => handleMarkPaid(invoice.id)}>Mark as Paid</Button>
                  )}
                </div>
                {invoice.lineItems && invoice.lineItems.length > 0 && (
                  <div className="mt-4">
                    <p className="font-medium mb-2">Line Items:</p>
                    <div className="space-y-1">
                      {invoice.lineItems.map((item) => (
                        <div key={item.id} className="flex justify-between text-sm">
                          <span>
                            {item.productName} x {item.quantity}
                          </span>
                          <span>${item.totalPrice.toFixed(2)}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
                {invoice.paymentHistory && invoice.paymentHistory.length > 0 && (
                  <div className="mt-4">
                    <p className="font-medium mb-2">Payment History:</p>
                    <div className="space-y-1">
                      {invoice.paymentHistory.map((payment) => (
                        <div key={payment.id} className="flex justify-between text-sm">
                          <span>
                            {new Date(payment.paidAt).toLocaleDateString()} - {payment.paymentMethod}
                          </span>
                          <span>${payment.amount.toFixed(2)}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
  );
}

