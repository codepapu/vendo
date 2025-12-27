import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Edit, Trash2, Search, Eye, TrendingUp, Power, PowerOff } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { adminApi } from '@/services/admin-api';
import { formatDate } from '@/lib/utils';
import type { CouponResponse } from '@/types';

/**
 * Admin page for managing coupons
 */
export function AdminCoupons() {
  const queryClient = useQueryClient();
  const [searchKeyword, setSearchKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isAnalyticsModalOpen, setIsAnalyticsModalOpen] = useState(false);
  const [editingCoupon, setEditingCoupon] = useState<CouponResponse | null>(null);
  const [viewingCoupon, setViewingCoupon] = useState<CouponResponse | null>(null);
  const [analyticsData, setAnalyticsData] = useState<any>(null);

  // Form state
  const [formData, setFormData] = useState({
    code: '',
    name: '',
    description: '',
    discountType: 'PERCENTAGE' as 'PERCENTAGE' | 'FIXED_AMOUNT' | 'FREE_SHIPPING',
    discountValue: '',
    minimumPurchase: '',
    maximumDiscount: '',
    usageLimit: '',
    validFrom: '',
    validUntil: '',
    applicableTo: 'ALL',
    isActive: true,
  });

  // Fetch coupons
  const { data: couponsData, isLoading } = useQuery({
    queryKey: ['admin-coupons', statusFilter, searchKeyword],
    queryFn: () => adminApi.getAllCoupons({
      status: statusFilter || undefined,
      search: searchKeyword || undefined,
      page: 0,
      size: 100,
    }),
  });

  // Fetch analytics
  const { data: analytics } = useQuery({
    queryKey: ['coupon-analytics', viewingCoupon?.id],
    queryFn: () => adminApi.getCouponAnalytics(viewingCoupon!.id),
    enabled: !!viewingCoupon && isAnalyticsModalOpen,
  });

  // Create mutation
  const createMutation = useMutation({
    mutationFn: (data: any) => adminApi.createCoupon(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-coupons'] });
      setIsCreateModalOpen(false);
      resetForm();
    },
  });

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: ({ couponId, data }: { couponId: number; data: any }) =>
      adminApi.updateCoupon(couponId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-coupons'] });
      setIsEditModalOpen(false);
      setEditingCoupon(null);
      resetForm();
    },
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (couponId: number) => adminApi.deleteCoupon(couponId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-coupons'] });
    },
  });

  // Toggle active status mutation
  const toggleActiveMutation = useMutation({
    mutationFn: ({ couponId, isActive }: { couponId: number; isActive: boolean }) =>
      adminApi.updateCoupon(couponId, { isActive }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-coupons'] });
    },
  });

  useEffect(() => {
    if (analytics) {
      setAnalyticsData(analytics);
    }
  }, [analytics]);

  const resetForm = () => {
    setFormData({
      code: '',
      name: '',
      description: '',
      discountType: 'PERCENTAGE',
      discountValue: '',
      minimumPurchase: '',
      maximumDiscount: '',
      usageLimit: '',
      validFrom: '',
      validUntil: '',
      applicableTo: 'ALL',
      isActive: true,
    });
  };

  const handleCreate = () => {
    if (!formData.code || !formData.name || !formData.discountValue || !formData.validFrom || !formData.validUntil) {
      alert('Please fill in all required fields (Code, Name, Discount Value, Valid From, Valid Until)');
      return;
    }
    if (new Date(formData.validFrom) >= new Date(formData.validUntil)) {
      alert('Valid Until date must be after Valid From date');
      return;
    }
    createMutation.mutate({
      code: formData.code,
      name: formData.name,
      description: formData.description,
      discountType: formData.discountType,
      discountValue: parseFloat(formData.discountValue) || 0,
      minimumPurchase: formData.minimumPurchase ? parseFloat(formData.minimumPurchase) : undefined,
      maximumDiscount: formData.maximumDiscount ? parseFloat(formData.maximumDiscount) : undefined,
      usageLimit: formData.usageLimit ? parseInt(formData.usageLimit) : undefined,
      validFrom: formData.validFrom,
      validUntil: formData.validUntil,
      applicableTo: formData.applicableTo,
    });
  };

  const handleEdit = (coupon: CouponResponse) => {
    setEditingCoupon(coupon);
    setFormData({
      code: coupon.code,
      name: coupon.name || '',
      description: coupon.description || '',
      discountType: (coupon.discountType as any) || 'PERCENTAGE',
      discountValue: coupon.discountValue.toString(),
      minimumPurchase: coupon.minimumPurchase?.toString() || '',
      maximumDiscount: coupon.maximumDiscount?.toString() || '',
      usageLimit: coupon.usageLimit?.toString() || '',
      validFrom: coupon.validFrom,
      validUntil: coupon.validUntil,
      applicableTo: coupon.applicableTo || 'ALL',
      isActive: coupon.isActive,
    });
    setIsEditModalOpen(true);
  };

  const handleUpdate = () => {
    if (!editingCoupon) return;
    if (!formData.name || !formData.discountValue || !formData.validFrom || !formData.validUntil) {
      alert('Please fill in all required fields (Name, Discount Value, Valid From, Valid Until)');
      return;
    }
    if (new Date(formData.validFrom) >= new Date(formData.validUntil)) {
      alert('Valid Until date must be after Valid From date');
      return;
    }
    updateMutation.mutate({
      couponId: editingCoupon.id,
      data: {
        name: formData.name,
        description: formData.description,
        discountType: formData.discountType,
        discountValue: parseFloat(formData.discountValue) || 0,
        minimumPurchase: formData.minimumPurchase ? parseFloat(formData.minimumPurchase) : undefined,
        maximumDiscount: formData.maximumDiscount ? parseFloat(formData.maximumDiscount) : undefined,
        usageLimit: formData.usageLimit ? parseInt(formData.usageLimit) : undefined,
        validFrom: formData.validFrom,
        validUntil: formData.validUntil,
        applicableTo: formData.applicableTo,
        isActive: formData.isActive,
      },
    });
  };

  const handleDelete = (id: number) => {
    if (confirm('Are you sure you want to delete this coupon? This action cannot be undone.')) {
      deleteMutation.mutate(id);
    }
  };

  const handleViewAnalytics = (coupon: CouponResponse) => {
    setViewingCoupon(coupon);
    setIsAnalyticsModalOpen(true);
  };

  const handleToggleActive = (coupon: CouponResponse) => {
    toggleActiveMutation.mutate({ couponId: coupon.id, isActive: !coupon.isActive });
  };

  const coupons = couponsData?.content || [];

  return (
    <div className="container mx-auto py-8 px-4">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold">Coupon Management</h1>
          <p className="text-muted-foreground mt-1">Manage coupons and promotions</p>
        </div>
        <Button onClick={() => { resetForm(); setIsCreateModalOpen(true); }}>
          <Plus className="mr-2 h-4 w-4" />
          Create Coupon
        </Button>
      </div>

      {/* Filters */}
      <Card className="p-4 mb-6">
        <div className="flex gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search coupons..."
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="All Status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="">All Status</SelectItem>
              <SelectItem value="active">Active</SelectItem>
              <SelectItem value="inactive">Inactive</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </Card>

      {/* Coupons Table */}
      {isLoading ? (
        <div className="text-center py-12">Loading...</div>
      ) : coupons.length === 0 ? (
        <Card className="p-12 text-center">
          <p className="text-muted-foreground">No coupons found.</p>
        </Card>
      ) : (
        <Card>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b">
                  <th className="p-4 text-left">Code</th>
                  <th className="p-4 text-left">Name</th>
                  <th className="p-4 text-left">Discount</th>
                  <th className="p-4 text-left">Valid Dates</th>
                  <th className="p-4 text-left">Usage</th>
                  <th className="p-4 text-left">Status</th>
                  <th className="p-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {coupons.map((coupon) => (
                  <tr key={coupon.id} className="border-b hover:bg-muted/50">
                    <td className="p-4 font-medium font-mono">{coupon.code}</td>
                    <td className="p-4">{coupon.name}</td>
                    <td className="p-4">
                      {coupon.discountType === 'PERCENTAGE' && (
                        <span className="font-semibold">{coupon.discountValue}%</span>
                      )}
                      {coupon.discountType === 'FIXED_AMOUNT' && (
                        <span className="font-semibold">${coupon.discountValue}</span>
                      )}
                      {coupon.discountType === 'FREE_SHIPPING' && (
                        <span className="font-semibold">Free Shipping</span>
                      )}
                    </td>
                    <td className="p-4 text-sm text-muted-foreground">
                      <div>{formatDate(coupon.validFrom)}</div>
                      <div>to {formatDate(coupon.validUntil)}</div>
                    </td>
                    <td className="p-4">
                      {coupon.usedCount} / {coupon.usageLimit || '∞'}
                    </td>
                    <td className="p-4">
                      <span className={`px-2 py-1 rounded text-xs ${
                        coupon.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                      }`}>
                        {coupon.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="p-4">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleViewAnalytics(coupon)}
                        >
                          <TrendingUp className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleEdit(coupon)}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleToggleActive(coupon)}
                          disabled={toggleActiveMutation.isPending}
                          title={coupon.isActive ? 'Deactivate' : 'Activate'}
                        >
                          {coupon.isActive ? (
                            <PowerOff className="h-4 w-4" />
                          ) : (
                            <Power className="h-4 w-4" />
                          )}
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleDelete(coupon.id)}
                          disabled={deleteMutation.isPending}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}

      {/* Create Coupon Modal */}
      <Dialog open={isCreateModalOpen} onOpenChange={setIsCreateModalOpen}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Create Coupon</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="code">Coupon Code *</Label>
                <Input
                  id="code"
                  value={formData.code}
                  onChange={(e) => setFormData({ ...formData, code: e.target.value.toUpperCase() })}
                  placeholder="SAVE20"
                />
              </div>
              <div>
                <Label htmlFor="name">Coupon Name *</Label>
                <Input
                  id="name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="20% Off Sale"
                />
              </div>
            </div>
            <div>
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Coupon description"
                rows={3}
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="discount-type">Discount Type *</Label>
                <Select
                  value={formData.discountType}
                  onValueChange={(v: any) => setFormData({ ...formData, discountType: v })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="PERCENTAGE">Percentage</SelectItem>
                    <SelectItem value="FIXED_AMOUNT">Fixed Amount</SelectItem>
                    <SelectItem value="FREE_SHIPPING">Free Shipping</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="discount-value">Discount Value *</Label>
                <Input
                  id="discount-value"
                  type="number"
                  step="0.01"
                  value={formData.discountValue}
                  onChange={(e) => setFormData({ ...formData, discountValue: e.target.value })}
                  placeholder={formData.discountType === 'PERCENTAGE' ? '20' : '10.00'}
                />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="minimum-purchase">Minimum Purchase ($)</Label>
                <Input
                  id="minimum-purchase"
                  type="number"
                  step="0.01"
                  value={formData.minimumPurchase}
                  onChange={(e) => setFormData({ ...formData, minimumPurchase: e.target.value })}
                  placeholder="0.00"
                />
              </div>
              <div>
                <Label htmlFor="maximum-discount">Maximum Discount ($)</Label>
                <Input
                  id="maximum-discount"
                  type="number"
                  step="0.01"
                  value={formData.maximumDiscount}
                  onChange={(e) => setFormData({ ...formData, maximumDiscount: e.target.value })}
                  placeholder="0.00"
                />
              </div>
            </div>
            <div>
              <Label htmlFor="usage-limit">Usage Limit</Label>
              <Input
                id="usage-limit"
                type="number"
                value={formData.usageLimit}
                onChange={(e) => setFormData({ ...formData, usageLimit: e.target.value })}
                placeholder="Leave empty for unlimited"
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="valid-from">Valid From *</Label>
                <Input
                  id="valid-from"
                  type="datetime-local"
                  value={formData.validFrom}
                  onChange={(e) => setFormData({ ...formData, validFrom: e.target.value })}
                />
              </div>
              <div>
                <Label htmlFor="valid-until">Valid Until *</Label>
                <Input
                  id="valid-until"
                  type="datetime-local"
                  value={formData.validUntil}
                  onChange={(e) => setFormData({ ...formData, validUntil: e.target.value })}
                />
              </div>
            </div>
            <div>
              <Label htmlFor="applicable-to">Applicable To</Label>
              <Select
                value={formData.applicableTo}
                onValueChange={(v) => setFormData({ ...formData, applicableTo: v })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All Orders</SelectItem>
                  <SelectItem value="B2C">B2C Only</SelectItem>
                  <SelectItem value="B2B">B2B Only</SelectItem>
                  <SelectItem value="SPECIFIC_PRODUCTS">Specific Products</SelectItem>
                  <SelectItem value="SPECIFIC_CATEGORIES">Specific Categories</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsCreateModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleCreate} disabled={createMutation.isPending}>
              Create Coupon
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Coupon Modal */}
      <Dialog open={isEditModalOpen} onOpenChange={setIsEditModalOpen}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Edit Coupon</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="edit-code">Coupon Code *</Label>
                <Input
                  id="edit-code"
                  value={formData.code}
                  onChange={(e) => setFormData({ ...formData, code: e.target.value.toUpperCase() })}
                  disabled
                />
              </div>
              <div>
                <Label htmlFor="edit-name">Coupon Name *</Label>
                <Input
                  id="edit-name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                />
              </div>
            </div>
            <div>
              <Label htmlFor="edit-description">Description</Label>
              <Textarea
                id="edit-description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                rows={3}
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="edit-discount-type">Discount Type *</Label>
                <Select
                  value={formData.discountType}
                  onValueChange={(v: any) => setFormData({ ...formData, discountType: v })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="PERCENTAGE">Percentage</SelectItem>
                    <SelectItem value="FIXED_AMOUNT">Fixed Amount</SelectItem>
                    <SelectItem value="FREE_SHIPPING">Free Shipping</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="edit-discount-value">Discount Value *</Label>
                <Input
                  id="edit-discount-value"
                  type="number"
                  step="0.01"
                  value={formData.discountValue}
                  onChange={(e) => setFormData({ ...formData, discountValue: e.target.value })}
                />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="edit-minimum-purchase">Minimum Purchase ($)</Label>
                <Input
                  id="edit-minimum-purchase"
                  type="number"
                  step="0.01"
                  value={formData.minimumPurchase}
                  onChange={(e) => setFormData({ ...formData, minimumPurchase: e.target.value })}
                />
              </div>
              <div>
                <Label htmlFor="edit-maximum-discount">Maximum Discount ($)</Label>
                <Input
                  id="edit-maximum-discount"
                  type="number"
                  step="0.01"
                  value={formData.maximumDiscount}
                  onChange={(e) => setFormData({ ...formData, maximumDiscount: e.target.value })}
                />
              </div>
            </div>
            <div>
              <Label htmlFor="edit-usage-limit">Usage Limit</Label>
              <Input
                id="edit-usage-limit"
                type="number"
                value={formData.usageLimit}
                onChange={(e) => setFormData({ ...formData, usageLimit: e.target.value })}
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="edit-valid-from">Valid From *</Label>
                <Input
                  id="edit-valid-from"
                  type="datetime-local"
                  value={formData.validFrom}
                  onChange={(e) => setFormData({ ...formData, validFrom: e.target.value })}
                />
              </div>
              <div>
                <Label htmlFor="edit-valid-until">Valid Until *</Label>
                <Input
                  id="edit-valid-until"
                  type="datetime-local"
                  value={formData.validUntil}
                  onChange={(e) => setFormData({ ...formData, validUntil: e.target.value })}
                />
              </div>
            </div>
            <div>
              <Label htmlFor="edit-applicable-to">Applicable To</Label>
              <Select
                value={formData.applicableTo}
                onValueChange={(v) => setFormData({ ...formData, applicableTo: v })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All Orders</SelectItem>
                  <SelectItem value="B2C">B2C Only</SelectItem>
                  <SelectItem value="B2B">B2B Only</SelectItem>
                  <SelectItem value="SPECIFIC_PRODUCTS">Specific Products</SelectItem>
                  <SelectItem value="SPECIFIC_CATEGORIES">Specific Categories</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="edit-active">Status</Label>
              <Select
                value={formData.isActive ? 'active' : 'inactive'}
                onValueChange={(v) => setFormData({ ...formData, isActive: v === 'active' })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="active">Active</SelectItem>
                  <SelectItem value="inactive">Inactive</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleUpdate} disabled={updateMutation.isPending}>
              Update Coupon
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Analytics Modal */}
      <Dialog open={isAnalyticsModalOpen} onOpenChange={setIsAnalyticsModalOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Coupon Analytics - {viewingCoupon?.code}</DialogTitle>
          </DialogHeader>
          {analyticsData ? (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <Card className="p-4">
                  <Label className="text-muted-foreground">Total Usage</Label>
                  <p className="text-2xl font-bold">{analyticsData.totalUsage || 0}</p>
                </Card>
                <Card className="p-4">
                  <Label className="text-muted-foreground">Total Discount</Label>
                  <p className="text-2xl font-bold">${(analyticsData.totalDiscount || 0).toFixed(2)}</p>
                </Card>
                <Card className="p-4">
                  <Label className="text-muted-foreground">Orders Using Coupon</Label>
                  <p className="text-2xl font-bold">{analyticsData.orderCount || 0}</p>
                </Card>
                <Card className="p-4">
                  <Label className="text-muted-foreground">Average Order Value</Label>
                  <p className="text-2xl font-bold">${(analyticsData.avgOrderValue || 0).toFixed(2)}</p>
                </Card>
              </div>
            </div>
          ) : (
            <div className="text-center py-8">Loading analytics...</div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsAnalyticsModalOpen(false)}>
              Close
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
