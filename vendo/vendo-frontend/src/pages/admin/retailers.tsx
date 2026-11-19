import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Search, Filter, Eye, CheckCircle, Percent, DollarSign, Store, Wallet } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { adminApi } from '@/services/admin-api';
import { formatDate, formatCurrency } from '@/lib/utils';
import type { Retailer } from '@/types/retailer';

/**
 * Admin page for managing retailers
 */
export function AdminRetailers() {
  const queryClient = useQueryClient();
  const [searchKeyword, setSearchKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [viewingRetailer, setViewingRetailer] = useState<Retailer | null>(null);
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
  const [isApprovalModalOpen, setIsApprovalModalOpen] = useState(false);
  const [isCommissionModalOpen, setIsCommissionModalOpen] = useState(false);
  const [isStatusModalOpen, setIsStatusModalOpen] = useState(false);
  const [isPayoutModalOpen, setIsPayoutModalOpen] = useState(false);
  const [isPayoutHistoryModalOpen, setIsPayoutHistoryModalOpen] = useState(false);
  const [newCommissionRate, setNewCommissionRate] = useState('');
  const [commissionNotes, setCommissionNotes] = useState('');
  const [newStatus, setNewStatus] = useState<'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'REJECTED'>('ACTIVE');
  const [statusNotes, setStatusNotes] = useState('');
  const [payoutStartDate, setPayoutStartDate] = useState('');
  const [payoutEndDate, setPayoutEndDate] = useState('');
  const [calculatedPayout, setCalculatedPayout] = useState<any | null>(null);

  // Fetch retailers
  const { data: retailersData, isLoading } = useQuery({
    queryKey: ['admin-retailers', statusFilter, searchKeyword],
    queryFn: () => adminApi.getAllRetailers({
      status: statusFilter || undefined,
      search: searchKeyword || undefined,
      page: 0,
      size: 100,
      sortBy: 'createdAt',
      sortDir: 'DESC',
    }),
  });

  // Fetch retailer details
  const { data: retailerDetails } = useQuery({
    queryKey: ['admin-retailer', viewingRetailer?.id],
    queryFn: () => adminApi.getRetailerById(viewingRetailer!.id),
    enabled: !!viewingRetailer && isDetailsModalOpen,
  });

  // Fetch payout history
  const { data: payoutHistoryData } = useQuery({
    queryKey: ['admin-retailer-payouts', viewingRetailer?.id],
    queryFn: () => adminApi.getRetailerPayoutHistory(viewingRetailer!.id, { page: 0, size: 20 }),
    enabled: !!viewingRetailer && isPayoutHistoryModalOpen,
  });

  // Fetch pending payouts
  const { data: pendingPayouts } = useQuery({
    queryKey: ['admin-retailer-pending-payouts', viewingRetailer?.id],
    queryFn: () => adminApi.getPendingPayouts(viewingRetailer!.id),
    enabled: !!viewingRetailer && isPayoutModalOpen,
  });

  // Approval mutation
  const approvalMutation = useMutation({
    mutationFn: (retailerId: number) => adminApi.approveRetailer(retailerId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-retailers'] });
      setIsApprovalModalOpen(false);
      setViewingRetailer(null);
    },
  });

  // Status update mutation
  const statusMutation = useMutation({
    mutationFn: ({ retailerId, status }: { retailerId: number; status: 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'REJECTED' }) =>
      adminApi.updateRetailerStatus(retailerId, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-retailers'] });
      queryClient.invalidateQueries({ queryKey: ['admin-retailer', viewingRetailer?.id] });
      setIsStatusModalOpen(false);
      setStatusNotes('');
    },
  });

  // Commission rate mutation
  const commissionMutation = useMutation({
    mutationFn: ({ retailerId, commissionRate }: { retailerId: number; commissionRate: number }) =>
      adminApi.updateRetailerCommissionRate(retailerId, commissionRate),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-retailers'] });
      queryClient.invalidateQueries({ queryKey: ['admin-retailer', viewingRetailer?.id] });
      setIsCommissionModalOpen(false);
      setNewCommissionRate('');
      setCommissionNotes('');
    },
  });

  const retailers = retailersData?.content || [];

  const handleViewDetails = (retailer: Retailer) => {
    setViewingRetailer(retailer);
    setIsDetailsModalOpen(true);
  };

  const handleApprove = (retailer: Retailer) => {
    setViewingRetailer(retailer);
    setIsApprovalModalOpen(true);
  };

  const handleUpdateCommission = (retailer: Retailer) => {
    setViewingRetailer(retailer);
    setNewCommissionRate(retailer.commissionRate.toString());
    setIsCommissionModalOpen(true);
  };

  const handleUpdateStatus = (retailer: Retailer) => {
    setViewingRetailer(retailer);
    setNewStatus(retailer.status === 'PENDING' ? 'ACTIVE' : retailer.status);
    setIsStatusModalOpen(true);
  };

  const handleManagePayouts = (retailer: Retailer) => {
    setViewingRetailer(retailer);
    setIsPayoutHistoryModalOpen(true);
  };

  const handleProcessPayout = (retailer: Retailer) => {
    setViewingRetailer(retailer);
    const endDate = new Date();
    const startDate = new Date();
    startDate.setMonth(startDate.getMonth() - 1);
    setPayoutStartDate(startDate.toISOString().split('T')[0]);
    setPayoutEndDate(endDate.toISOString().split('T')[0]);
    setIsPayoutModalOpen(true);
    setCalculatedPayout(null);
  };

  // Calculate payout mutation
  const calculatePayoutMutation = useMutation({
    mutationFn: ({ retailerId, startDate, endDate }: { retailerId: number; startDate: string; endDate: string }) =>
      adminApi.calculatePayout(retailerId, startDate, endDate),
    onSuccess: (data) => {
      setCalculatedPayout(data);
    },
  });

  // Process payout mutation
  const processPayoutMutation = useMutation({
    mutationFn: ({ retailerId, startDate, endDate, bankAccountId }: { retailerId: number; startDate: string; endDate: string; bankAccountId?: number }) =>
      adminApi.processPayout(retailerId, startDate, endDate, bankAccountId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-retailer-payouts', viewingRetailer?.id] });
      queryClient.invalidateQueries({ queryKey: ['admin-retailer-pending-payouts', viewingRetailer?.id] });
      setIsPayoutModalOpen(false);
      setCalculatedPayout(null);
      setPayoutStartDate('');
      setPayoutEndDate('');
    },
  });

  const handleCalculatePayout = () => {
    if (!viewingRetailer || !payoutStartDate || !payoutEndDate) {
      alert('Please select both start and end dates');
      return;
    }
    calculatePayoutMutation.mutate({
      retailerId: viewingRetailer.id,
      startDate: new Date(payoutStartDate).toISOString(),
      endDate: new Date(payoutEndDate).toISOString(),
    });
  };

  const handleSubmitPayout = () => {
    if (!viewingRetailer || !payoutStartDate || !payoutEndDate || !calculatedPayout) {
      alert('Please calculate payout first');
      return;
    }
    processPayoutMutation.mutate({
      retailerId: viewingRetailer.id,
      startDate: new Date(payoutStartDate).toISOString(),
      endDate: new Date(payoutEndDate).toISOString(),
    });
  };

  const handleSubmitApproval = () => {
    if (!viewingRetailer) return;
    approvalMutation.mutate(viewingRetailer.id);
  };

  const handleSubmitCommission = () => {
    if (!viewingRetailer || !newCommissionRate) return;
    const rate = parseFloat(newCommissionRate);
    if (isNaN(rate) || rate < 0 || rate > 100) {
      alert('Please enter a valid commission rate between 0 and 100');
      return;
    }
    commissionMutation.mutate({
      retailerId: viewingRetailer.id,
      commissionRate: rate,
    });
  };

  const handleSubmitStatus = () => {
    if (!viewingRetailer) return;
    statusMutation.mutate({
      retailerId: viewingRetailer.id,
      status: newStatus,
    });
  };

  const getStatusBadge = (status: string) => {
    const variants: Record<string, { variant: 'default' | 'secondary' | 'destructive' | 'outline'; label: string }> = {
      PENDING: { variant: 'outline', label: 'Pending' },
      ACTIVE: { variant: 'default', label: 'Active' },
      SUSPENDED: { variant: 'secondary', label: 'Suspended' },
      REJECTED: { variant: 'destructive', label: 'Rejected' },
    };
    const config = variants[status] || { variant: 'outline' as const, label: status };
    return <Badge variant={config.variant}>{config.label}</Badge>;
  };

  const retailer = retailerDetails || viewingRetailer;

  return (
    <div className="container mx-auto py-8 px-4">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold">Retailers</h1>
          <p className="text-muted-foreground mt-1">Manage retailer accounts, approvals, and commission rates</p>
        </div>
      </div>

      {/* Filters */}
      <Card className="p-4 mb-6">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
              <Input
                placeholder="Search by store name..."
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-full md:w-[200px]">
              <Filter className="mr-2 h-4 w-4" />
              <SelectValue placeholder="All Statuses" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="">All Statuses</SelectItem>
              <SelectItem value="PENDING">Pending</SelectItem>
              <SelectItem value="ACTIVE">Active</SelectItem>
              <SelectItem value="SUSPENDED">Suspended</SelectItem>
              <SelectItem value="REJECTED">Rejected</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </Card>

      {/* Retailers Table */}
      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
        </div>
      ) : retailers.length === 0 ? (
        <Card className="p-12 text-center">
          <Store className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <p className="text-muted-foreground">No retailers found</p>
        </Card>
      ) : (
        <Card>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b">
                  <th className="text-left p-4 font-semibold">Store Name</th>
                  <th className="text-left p-4 font-semibold">Status</th>
                  <th className="text-left p-4 font-semibold">Total Sales</th>
                  <th className="text-left p-4 font-semibold">Commission Rate</th>
                  <th className="text-left p-4 font-semibold">Created</th>
                  <th className="text-right p-4 font-semibold">Actions</th>
                </tr>
              </thead>
              <tbody>
                {retailers.map((retailer: Retailer) => (
                  <tr key={retailer.id} className="border-b hover:bg-muted/50">
                    <td className="p-4">
                      <div className="font-medium">{retailer.storeName}</div>
                      {retailer.storeSlug && (
                        <div className="text-sm text-muted-foreground">/{retailer.storeSlug}</div>
                      )}
                    </td>
                    <td className="p-4">{getStatusBadge(retailer.status)}</td>
                    <td className="p-4">
                      <div className="flex items-center gap-2">
                        <DollarSign className="h-4 w-4 text-muted-foreground" />
                        {formatCurrency(retailer.totalSales || 0)}
                      </div>
                    </td>
                    <td className="p-4">
                      <div className="flex items-center gap-2">
                        <Percent className="h-4 w-4 text-muted-foreground" />
                        {retailer.commissionRate}%
                      </div>
                    </td>
                    <td className="p-4 text-sm text-muted-foreground">
                      {formatDate(retailer.createdAt)}
                    </td>
                    <td className="p-4">
                      <div className="flex items-center justify-end gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleViewDetails(retailer)}
                        >
                          <Eye className="h-4 w-4" />
                        </Button>
                        {retailer.status === 'PENDING' && (
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleApprove(retailer)}
                            className="text-green-600 hover:text-green-700"
                          >
                            <CheckCircle className="h-4 w-4" />
                          </Button>
                        )}
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleUpdateCommission(retailer)}
                        >
                          <Percent className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleUpdateStatus(retailer)}
                        >
                          <Filter className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleManagePayouts(retailer)}
                          title="View Payout History"
                        >
                          <Wallet className="h-4 w-4" />
                        </Button>
                        {retailer.status === 'ACTIVE' && (
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleProcessPayout(retailer)}
                            title="Process Payout"
                            className="text-green-600 hover:text-green-700"
                          >
                            <DollarSign className="h-4 w-4" />
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}

      {/* Retailer Details Modal */}
      <Dialog open={isDetailsModalOpen} onOpenChange={setIsDetailsModalOpen}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Retailer Details</DialogTitle>
            <DialogDescription>View complete information about this retailer</DialogDescription>
          </DialogHeader>
          {retailer && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label>Store Name</Label>
                  <p className="font-medium">{retailer.storeName}</p>
                </div>
                <div>
                  <Label>Status</Label>
                  <div>{getStatusBadge(retailer.status)}</div>
                </div>
                <div>
                  <Label>Store Slug</Label>
                  <p>/{retailer.storeSlug}</p>
                </div>
                <div>
                  <Label>Commission Rate</Label>
                  <p className="font-medium">{retailer.commissionRate}%</p>
                </div>
                <div>
                  <Label>Total Sales</Label>
                  <p className="font-medium">{formatCurrency(retailer.totalSales || 0)}</p>
                </div>
                <div>
                  <Label>Total Orders</Label>
                  <p className="font-medium">{retailer.totalOrders || 0}</p>
                </div>
                {retailer.rating !== undefined && (
                  <div>
                    <Label>Rating</Label>
                    <p className="font-medium">{retailer.rating.toFixed(1)} ⭐</p>
                  </div>
                )}
                {retailer.verified !== undefined && (
                  <div>
                    <Label>Verified</Label>
                    <p>{retailer.verified ? 'Yes' : 'No'}</p>
                  </div>
                )}
                {retailer.description && (
                  <div className="col-span-2">
                    <Label>Description</Label>
                    <p className="text-sm">{retailer.description}</p>
                  </div>
                )}
                <div>
                  <Label>Created At</Label>
                  <p>{formatDate(retailer.createdAt)}</p>
                </div>
                <div>
                  <Label>Updated At</Label>
                  <p>{formatDate(retailer.updatedAt)}</p>
                </div>
                {retailer.user && (
                  <>
                    <div>
                      <Label>Contact Email</Label>
                      <p>{retailer.user.email}</p>
                    </div>
                    <div>
                      <Label>Username</Label>
                      <p>{retailer.user.username}</p>
                    </div>
                    {(retailer.user.firstName || retailer.user.lastName) && (
                      <div className="col-span-2">
                        <Label>Contact Name</Label>
                        <p>{[retailer.user.firstName, retailer.user.lastName].filter(Boolean).join(' ')}</p>
                      </div>
                    )}
                  </>
                )}
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Approval Modal */}
      <Dialog open={isApprovalModalOpen} onOpenChange={setIsApprovalModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Approve Retailer</DialogTitle>
            <DialogDescription>
              Approve this retailer and activate their account.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            {viewingRetailer && (
              <div>
                <Label>Store Name</Label>
                <p className="font-medium">{viewingRetailer.storeName}</p>
              </div>
            )}
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => setIsApprovalModalOpen(false)}>
                Cancel
              </Button>
              <Button
                onClick={handleSubmitApproval}
                disabled={approvalMutation.isPending}
              >
                {approvalMutation.isPending ? 'Processing...' : 'Approve Retailer'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Commission Rate Modal */}
      <Dialog open={isCommissionModalOpen} onOpenChange={setIsCommissionModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Update Commission Rate</DialogTitle>
            <DialogDescription>
              Adjust the commission rate for this retailer.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            {viewingRetailer && (
              <div>
                <Label>Store Name</Label>
                <p className="font-medium">{viewingRetailer.storeName}</p>
                <p className="text-sm text-muted-foreground mt-1">
                  Current Commission Rate: {viewingRetailer.commissionRate}%
                </p>
              </div>
            )}
            <div>
              <Label htmlFor="commission-rate">New Commission Rate (%)</Label>
              <Input
                id="commission-rate"
                type="number"
                value={newCommissionRate}
                onChange={(e) => setNewCommissionRate(e.target.value)}
                placeholder="Enter commission rate (0-100)"
                min="0"
                max="100"
                step="0.1"
              />
            </div>
            <div>
              <Label htmlFor="commission-notes">Notes (Optional)</Label>
              <Textarea
                id="commission-notes"
                value={commissionNotes}
                onChange={(e) => setCommissionNotes(e.target.value)}
                placeholder="Add any notes about this change..."
                rows={3}
              />
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => setIsCommissionModalOpen(false)}>
                Cancel
              </Button>
              <Button
                onClick={handleSubmitCommission}
                disabled={commissionMutation.isPending}
              >
                {commissionMutation.isPending ? 'Updating...' : 'Update Commission Rate'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Status Update Modal */}
      <Dialog open={isStatusModalOpen} onOpenChange={setIsStatusModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Update Retailer Status</DialogTitle>
            <DialogDescription>
              Change the status of this retailer account.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            {viewingRetailer && (
              <div>
                <Label>Store Name</Label>
                <p className="font-medium">{viewingRetailer.storeName}</p>
                <p className="text-sm text-muted-foreground mt-1">
                  Current Status: {getStatusBadge(viewingRetailer.status)}
                </p>
              </div>
            )}
            <div>
              <Label htmlFor="status">New Status</Label>
              <Select value={newStatus} onValueChange={(value: any) => setNewStatus(value)}>
                <SelectTrigger id="status">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="PENDING">Pending</SelectItem>
                  <SelectItem value="ACTIVE">Active</SelectItem>
                  <SelectItem value="SUSPENDED">Suspended</SelectItem>
                  <SelectItem value="REJECTED">Rejected</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="status-notes">Notes (Optional)</Label>
              <Textarea
                id="status-notes"
                value={statusNotes}
                onChange={(e) => setStatusNotes(e.target.value)}
                placeholder="Add any notes about this status change..."
                rows={3}
              />
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => setIsStatusModalOpen(false)}>
                Cancel
              </Button>
              <Button
                onClick={handleSubmitStatus}
                disabled={statusMutation.isPending}
                variant={newStatus === 'SUSPENDED' || newStatus === 'REJECTED' ? 'destructive' : 'default'}
              >
                {statusMutation.isPending ? 'Updating...' : 'Update Status'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Payout History Modal */}
      <Dialog open={isPayoutHistoryModalOpen} onOpenChange={setIsPayoutHistoryModalOpen}>
        <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Payout History</DialogTitle>
            <DialogDescription>
              View payout history for {viewingRetailer?.storeName}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            {payoutHistoryData?.content && payoutHistoryData.content.length > 0 ? (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b">
                      <th className="text-left p-2 font-semibold">Period</th>
                      <th className="text-left p-2 font-semibold">Payout Amount</th>
                      <th className="text-left p-2 font-semibold">Total Sales</th>
                      <th className="text-left p-2 font-semibold">Commission</th>
                      <th className="text-left p-2 font-semibold">Status</th>
                      <th className="text-left p-2 font-semibold">Processed At</th>
                    </tr>
                  </thead>
                  <tbody>
                    {payoutHistoryData.content.map((payout: any) => (
                      <tr key={payout.id} className="border-b hover:bg-muted/50">
                        <td className="p-2">
                          <div className="text-sm">
                            {formatDate(payout.periodStart)} - {formatDate(payout.periodEnd)}
                          </div>
                        </td>
                        <td className="p-2 font-medium">{formatCurrency(payout.payoutAmount || 0)}</td>
                        <td className="p-2">{formatCurrency(payout.totalSales || 0)}</td>
                        <td className="p-2">{formatCurrency(payout.totalCommission || 0)}</td>
                        <td className="p-2">{getStatusBadge(payout.status)}</td>
                        <td className="p-2 text-sm text-muted-foreground">
                          {payout.processedAt ? formatDate(payout.processedAt) : '-'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p className="text-center text-muted-foreground py-8">No payout history found</p>
            )}
            {pendingPayouts && pendingPayouts.length > 0 && (
              <div className="mt-6">
                <h3 className="font-semibold mb-2">Pending Payouts</h3>
                <div className="space-y-2">
                  {pendingPayouts.map((payout: any) => (
                    <div key={payout.id} className="p-3 border rounded-lg">
                      <div className="flex justify-between items-center">
                        <div>
                          <p className="font-medium">{formatCurrency(payout.payoutAmount || 0)}</p>
                          <p className="text-sm text-muted-foreground">
                            {formatDate(payout.periodStart)} - {formatDate(payout.periodEnd)}
                          </p>
                        </div>
                        <Badge variant="outline">{payout.status}</Badge>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>

      {/* Process Payout Modal */}
      <Dialog open={isPayoutModalOpen} onOpenChange={setIsPayoutModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Process Payout</DialogTitle>
            <DialogDescription>
              Calculate and process payout for {viewingRetailer?.storeName}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            {viewingRetailer && (
              <div>
                <Label>Store Name</Label>
                <p className="font-medium">{viewingRetailer.storeName}</p>
              </div>
            )}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="payout-start-date">Start Date</Label>
                <Input
                  id="payout-start-date"
                  type="date"
                  value={payoutStartDate}
                  onChange={(e) => setPayoutStartDate(e.target.value)}
                />
              </div>
              <div>
                <Label htmlFor="payout-end-date">End Date</Label>
                <Input
                  id="payout-end-date"
                  type="date"
                  value={payoutEndDate}
                  onChange={(e) => setPayoutEndDate(e.target.value)}
                />
              </div>
            </div>
            {calculatedPayout && (
              <div className="p-4 border rounded-lg bg-muted/50">
                <h4 className="font-semibold mb-2">Payout Calculation</h4>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span>Total Sales:</span>
                    <span className="font-medium">{formatCurrency(calculatedPayout.totalSales || 0)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Total Commission:</span>
                    <span className="font-medium">{formatCurrency(calculatedPayout.totalCommission || 0)}</span>
                  </div>
                  <div className="flex justify-between pt-2 border-t">
                    <span className="font-semibold">Payout Amount:</span>
                    <span className="font-bold text-lg">{formatCurrency(calculatedPayout.payoutAmount || 0)}</span>
                  </div>
                </div>
              </div>
            )}
            <div className="flex justify-end gap-2">
              <Button
                variant="outline"
                onClick={handleCalculatePayout}
                disabled={calculatePayoutMutation.isPending || !payoutStartDate || !payoutEndDate}
              >
                {calculatePayoutMutation.isPending ? 'Calculating...' : 'Calculate Payout'}
              </Button>
            </div>
            {calculatedPayout && (
              <div className="flex justify-end gap-2 pt-2 border-t">
                <Button variant="outline" onClick={() => setIsPayoutModalOpen(false)}>
                  Cancel
                </Button>
                <Button
                  onClick={handleSubmitPayout}
                  disabled={processPayoutMutation.isPending}
                >
                  {processPayoutMutation.isPending ? 'Processing...' : 'Process Payout'}
                </Button>
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

