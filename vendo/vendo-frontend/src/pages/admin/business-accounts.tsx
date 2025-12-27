import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Search, Filter, Eye, CheckCircle, XCircle, DollarSign, Building2 } from 'lucide-react';
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

/**
 * Business Account Status
 */
type BusinessAccountStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUSPENDED';

/**
 * Business Account Interface
 */
interface BusinessAccount {
  id: number;
  userId: number;
  companyName: string;
  businessRegistrationNumber?: string;
  taxId?: string;
  businessType: 'CORPORATE' | 'SME' | 'ENTERPRISE';
  creditLimit: number;
  creditTermsDays: number;
  status: BusinessAccountStatus;
  approvedAt?: string;
  approvedBy?: number;
  createdAt: string;
  totalOrders?: number;
  totalSpent?: number;
  availableCredit?: number;
  user?: {
    email: string;
    firstName?: string;
    lastName?: string;
  };
}

/**
 * Admin page for managing business accounts
 */
export function AdminBusinessAccounts() {
  const queryClient = useQueryClient();
  const [searchKeyword, setSearchKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [viewingAccount, setViewingAccount] = useState<BusinessAccount | null>(null);
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
  const [isApprovalModalOpen, setIsApprovalModalOpen] = useState(false);
  const [isCreditLimitModalOpen, setIsCreditLimitModalOpen] = useState(false);
  const [approvalStatus, setApprovalStatus] = useState<'APPROVED' | 'REJECTED'>('APPROVED');
  const [approvalNotes, setApprovalNotes] = useState('');
  const [newCreditLimit, setNewCreditLimit] = useState('');
  const [creditLimitNotes, setCreditLimitNotes] = useState('');

  // Fetch business accounts
  const { data: accountsData, isLoading } = useQuery({
    queryKey: ['admin-business-accounts', statusFilter, searchKeyword],
    queryFn: () => adminApi.getAllBusinessAccounts({
      status: statusFilter || undefined,
      search: searchKeyword || undefined,
      page: 0,
      size: 100,
    }),
  });

  // Fetch account details
  const { data: accountDetails } = useQuery({
    queryKey: ['admin-business-account', viewingAccount?.id],
    queryFn: () => adminApi.getBusinessAccountById(viewingAccount!.id),
    enabled: !!viewingAccount && isDetailsModalOpen,
  });

  // Approval mutation
  const approvalMutation = useMutation({
    mutationFn: ({ accountId, status, notes }: { accountId: number; status: 'APPROVED' | 'REJECTED'; notes?: string }) =>
      adminApi.approveBusinessAccount(accountId, status, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-business-accounts'] });
      setIsApprovalModalOpen(false);
      setApprovalNotes('');
      setViewingAccount(null);
    },
  });

  // Credit limit mutation
  const creditLimitMutation = useMutation({
    mutationFn: ({ accountId, creditLimit, notes }: { accountId: number; creditLimit: number; notes?: string }) =>
      adminApi.updateCreditLimit(accountId, creditLimit, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-business-accounts'] });
      queryClient.invalidateQueries({ queryKey: ['admin-business-account', viewingAccount?.id] });
      setIsCreditLimitModalOpen(false);
      setNewCreditLimit('');
      setCreditLimitNotes('');
    },
  });

  const accounts = accountsData?.content || [];

  const handleViewDetails = (account: BusinessAccount) => {
    setViewingAccount(account);
    setIsDetailsModalOpen(true);
  };

  const handleApprove = (account: BusinessAccount, status: 'APPROVED' | 'REJECTED') => {
    setViewingAccount(account);
    setApprovalStatus(status);
    setIsApprovalModalOpen(true);
  };

  const handleUpdateCreditLimit = (account: BusinessAccount) => {
    setViewingAccount(account);
    setNewCreditLimit(account.creditLimit.toString());
    setIsCreditLimitModalOpen(true);
  };

  const handleSubmitApproval = () => {
    if (!viewingAccount) return;
    approvalMutation.mutate({
      accountId: viewingAccount.id,
      status: approvalStatus,
      notes: approvalNotes || undefined,
    });
  };

  const handleSubmitCreditLimit = () => {
    if (!viewingAccount || !newCreditLimit) return;
    const limit = parseFloat(newCreditLimit);
    if (isNaN(limit) || limit < 0) {
      alert('Please enter a valid credit limit');
      return;
    }
    creditLimitMutation.mutate({
      accountId: viewingAccount.id,
      creditLimit: limit,
      notes: creditLimitNotes || undefined,
    });
  };

  const getStatusBadge = (status: BusinessAccountStatus) => {
    const variants: Record<BusinessAccountStatus, { variant: 'default' | 'secondary' | 'destructive' | 'outline'; label: string }> = {
      PENDING: { variant: 'outline', label: 'Pending' },
      APPROVED: { variant: 'default', label: 'Approved' },
      REJECTED: { variant: 'destructive', label: 'Rejected' },
      SUSPENDED: { variant: 'secondary', label: 'Suspended' },
    };
    const config = variants[status];
    return <Badge variant={config.variant}>{config.label}</Badge>;
  };

  const account = accountDetails || viewingAccount;

  return (
    <div className="container mx-auto py-8 px-4">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold">Business Accounts</h1>
          <p className="text-muted-foreground mt-1">Manage business account approvals and credit limits</p>
        </div>
      </div>

      {/* Filters */}
      <Card className="p-4 mb-6">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
              <Input
                placeholder="Search by company name..."
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
              <SelectItem value="APPROVED">Approved</SelectItem>
              <SelectItem value="REJECTED">Rejected</SelectItem>
              <SelectItem value="SUSPENDED">Suspended</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </Card>

      {/* Accounts Table */}
      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
        </div>
      ) : accounts.length === 0 ? (
        <Card className="p-12 text-center">
          <Building2 className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <p className="text-muted-foreground">No business accounts found</p>
        </Card>
      ) : (
        <Card>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b">
                  <th className="text-left p-4 font-semibold">Company Name</th>
                  <th className="text-left p-4 font-semibold">Status</th>
                  <th className="text-left p-4 font-semibold">Credit Limit</th>
                  <th className="text-left p-4 font-semibold">Total Orders</th>
                  <th className="text-left p-4 font-semibold">Created</th>
                  <th className="text-right p-4 font-semibold">Actions</th>
                </tr>
              </thead>
              <tbody>
                {accounts.map((account: BusinessAccount) => (
                  <tr key={account.id} className="border-b hover:bg-muted/50">
                    <td className="p-4">
                      <div className="font-medium">{account.companyName}</div>
                      {account.businessType && (
                        <div className="text-sm text-muted-foreground">{account.businessType}</div>
                      )}
                    </td>
                    <td className="p-4">{getStatusBadge(account.status)}</td>
                    <td className="p-4">
                      <div className="flex items-center gap-2">
                        <DollarSign className="h-4 w-4 text-muted-foreground" />
                        {formatCurrency(account.creditLimit)}
                      </div>
                    </td>
                    <td className="p-4">{account.totalOrders || 0}</td>
                    <td className="p-4 text-sm text-muted-foreground">
                      {formatDate(account.createdAt)}
                    </td>
                    <td className="p-4">
                      <div className="flex items-center justify-end gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleViewDetails(account)}
                        >
                          <Eye className="h-4 w-4" />
                        </Button>
                        {account.status === 'PENDING' && (
                          <>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleApprove(account, 'APPROVED')}
                              className="text-green-600 hover:text-green-700"
                            >
                              <CheckCircle className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleApprove(account, 'REJECTED')}
                              className="text-red-600 hover:text-red-700"
                            >
                              <XCircle className="h-4 w-4" />
                            </Button>
                          </>
                        )}
                        {account.status === 'APPROVED' && (
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleUpdateCreditLimit(account)}
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

      {/* Account Details Modal */}
      <Dialog open={isDetailsModalOpen} onOpenChange={setIsDetailsModalOpen}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Business Account Details</DialogTitle>
            <DialogDescription>View complete information about this business account</DialogDescription>
          </DialogHeader>
          {account && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label>Company Name</Label>
                  <p className="font-medium">{account.companyName}</p>
                </div>
                <div>
                  <Label>Status</Label>
                  <div>{getStatusBadge(account.status)}</div>
                </div>
                <div>
                  <Label>Business Type</Label>
                  <p>{account.businessType}</p>
                </div>
                <div>
                  <Label>Credit Limit</Label>
                  <p className="font-medium">{formatCurrency(account.creditLimit)}</p>
                </div>
                {account.availableCredit !== undefined && (
                  <div>
                    <Label>Available Credit</Label>
                    <p className="font-medium">{formatCurrency(account.availableCredit)}</p>
                  </div>
                )}
                <div>
                  <Label>Credit Terms</Label>
                  <p>{account.creditTermsDays} days</p>
                </div>
                <div>
                  <Label>Total Orders</Label>
                  <p className="font-medium">{account.totalOrders || 0}</p>
                </div>
                {account.totalSpent !== undefined && (
                  <div>
                    <Label>Total Spent</Label>
                    <p className="font-medium">{formatCurrency(account.totalSpent)}</p>
                  </div>
                )}
                {account.businessRegistrationNumber && (
                  <div>
                    <Label>Registration Number</Label>
                    <p>{account.businessRegistrationNumber}</p>
                  </div>
                )}
                {account.taxId && (
                  <div>
                    <Label>Tax ID</Label>
                    <p>{account.taxId}</p>
                  </div>
                )}
                <div>
                  <Label>Created At</Label>
                  <p>{formatDate(account.createdAt)}</p>
                </div>
                {account.approvedAt && (
                  <div>
                    <Label>Approved At</Label>
                    <p>{formatDate(account.approvedAt)}</p>
                  </div>
                )}
                {account.user && (
                  <>
                    <div>
                      <Label>Contact Email</Label>
                      <p>{account.user.email}</p>
                    </div>
                    {(account.user.firstName || account.user.lastName) && (
                      <div>
                        <Label>Contact Name</Label>
                        <p>{[account.user.firstName, account.user.lastName].filter(Boolean).join(' ')}</p>
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
            <DialogTitle>
              {approvalStatus === 'APPROVED' ? 'Approve' : 'Reject'} Business Account
            </DialogTitle>
            <DialogDescription>
              {approvalStatus === 'APPROVED'
                ? 'Approve this business account and grant access to B2B features.'
                : 'Reject this business account application.'}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            {viewingAccount && (
              <div>
                <Label>Company Name</Label>
                <p className="font-medium">{viewingAccount.companyName}</p>
              </div>
            )}
            <div>
              <Label htmlFor="approval-notes">Notes (Optional)</Label>
              <Textarea
                id="approval-notes"
                value={approvalNotes}
                onChange={(e) => setApprovalNotes(e.target.value)}
                placeholder="Add any notes about this decision..."
                rows={4}
              />
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => setIsApprovalModalOpen(false)}>
                Cancel
              </Button>
              <Button
                onClick={handleSubmitApproval}
                disabled={approvalMutation.isPending}
                variant={approvalStatus === 'APPROVED' ? 'default' : 'destructive'}
              >
                {approvalMutation.isPending
                  ? 'Processing...'
                  : approvalStatus === 'APPROVED'
                  ? 'Approve Account'
                  : 'Reject Account'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Credit Limit Modal */}
      <Dialog open={isCreditLimitModalOpen} onOpenChange={setIsCreditLimitModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Update Credit Limit</DialogTitle>
            <DialogDescription>
              Adjust the credit limit for this business account.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            {viewingAccount && (
              <div>
                <Label>Company Name</Label>
                <p className="font-medium">{viewingAccount.companyName}</p>
                <p className="text-sm text-muted-foreground mt-1">
                  Current Credit Limit: {formatCurrency(viewingAccount.creditLimit)}
                </p>
              </div>
            )}
            <div>
              <Label htmlFor="credit-limit">New Credit Limit</Label>
              <Input
                id="credit-limit"
                type="number"
                value={newCreditLimit}
                onChange={(e) => setNewCreditLimit(e.target.value)}
                placeholder="Enter new credit limit"
                min="0"
                step="0.01"
              />
            </div>
            <div>
              <Label htmlFor="credit-notes">Notes (Optional)</Label>
              <Textarea
                id="credit-notes"
                value={creditLimitNotes}
                onChange={(e) => setCreditLimitNotes(e.target.value)}
                placeholder="Add any notes about this change..."
                rows={3}
              />
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => setIsCreditLimitModalOpen(false)}>
                Cancel
              </Button>
              <Button
                onClick={handleSubmitCreditLimit}
                disabled={creditLimitMutation.isPending}
              >
                {creditLimitMutation.isPending ? 'Updating...' : 'Update Credit Limit'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

