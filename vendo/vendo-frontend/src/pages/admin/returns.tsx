import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useToast } from '@/hooks/use-toast';
import { returnApi } from '@/services/return-api';

export default function AdminReturns() {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [statusFilter, setStatusFilter] = useState<string>('all');

  const { data: returns } = useQuery({
    queryKey: ['admin-returns', statusFilter],
    queryFn: () => returnApi.getReturnsByStatus(statusFilter !== 'all' ? statusFilter : undefined),
  });

  const approveMutation = useMutation({
    mutationFn: (id: number) => returnApi.approveReturn(id),
    onSuccess: () => {
      toast({ title: 'Success', description: 'Return approved' });
      queryClient.invalidateQueries({ queryKey: ['admin-returns'] });
    },
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) => returnApi.rejectReturn(id, reason),
    onSuccess: () => {
      toast({ title: 'Success', description: 'Return rejected' });
      queryClient.invalidateQueries({ queryKey: ['admin-returns'] });
    },
  });

  const processRefundMutation = useMutation({
    mutationFn: (id: number) => returnApi.processRefund(id),
    onSuccess: () => {
      toast({ title: 'Success', description: 'Refund processed' });
      queryClient.invalidateQueries({ queryKey: ['admin-returns'] });
    },
  });

  return (
    <div className="container mx-auto py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Return Management</h1>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-40">
            <SelectValue placeholder="Filter by status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All</SelectItem>
            <SelectItem value="PENDING">Pending</SelectItem>
            <SelectItem value="APPROVED">Approved</SelectItem>
            <SelectItem value="REJECTED">Rejected</SelectItem>
            <SelectItem value="COMPLETED">Completed</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="space-y-4">
        {returns?.map((returnRequest) => (
          <Card key={returnRequest.id}>
            <CardHeader>
              <div className="flex justify-between items-start">
                <div>
                  <CardTitle>Return #{returnRequest.code}</CardTitle>
                  <p className="text-sm text-muted-foreground">
                    Order: {returnRequest.orderId} | Status: {returnRequest.status}
                  </p>
                </div>
                <span
                  className={`px-3 py-1 rounded-full text-sm ${
                    returnRequest.status === 'APPROVED'
                      ? 'bg-green-100 text-green-800'
                      : returnRequest.status === 'REJECTED'
                      ? 'bg-red-100 text-red-800'
                      : 'bg-yellow-100 text-yellow-800'
                  }`}
                >
                  {returnRequest.status}
                </span>
              </div>
            </CardHeader>
            <CardContent>
              <p className="mb-2">Reason: {returnRequest.reason}</p>
              <p className="mb-2">Refund Amount: ${returnRequest.refundAmount?.toFixed(2)}</p>
              {returnRequest.items && returnRequest.items.length > 0 && (
                <div className="mb-4">
                  <p className="font-medium mb-2">Items:</p>
                  {returnRequest.items.map((item) => (
                    <p key={item.id} className="text-sm">
                      {item.productName} x {item.quantity} - ${item.refundAmount.toFixed(2)}
                    </p>
                  ))}
                </div>
              )}
              {returnRequest.history && returnRequest.history.length > 0 && (
                <div className="mb-4">
                  <p className="font-medium mb-2">Status History:</p>
                  {returnRequest.history.map((h) => (
                    <p key={h.id} className="text-sm text-muted-foreground">
                      {h.status}: {h.description} ({new Date(h.createdAt).toLocaleDateString()})
                    </p>
                  ))}
                </div>
              )}
              
              <div className="flex gap-2">
                {returnRequest.status === 'PENDING' && (
                  <>
                    <Button
                      onClick={() => approveMutation.mutate(returnRequest.id)}
                      disabled={approveMutation.isPending}
                    >
                      Approve
                    </Button>
                    <Button
                      variant="destructive"
                      onClick={() => rejectMutation.mutate({ id: returnRequest.id, reason: 'Rejected by admin' })}
                      disabled={rejectMutation.isPending}
                    >
                      Reject
                    </Button>
                  </>
                )}
                {returnRequest.status === 'APPROVED' && (
                  <Button
                    onClick={() => processRefundMutation.mutate(returnRequest.id)}
                    disabled={processRefundMutation.isPending}
                  >
                    Process Refund
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}

