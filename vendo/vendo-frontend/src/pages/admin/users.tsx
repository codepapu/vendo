import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Edit, Trash2, Search, UserX, UserCheck, Shield } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { adminApi, type AdminUserResponse } from '@/services/admin-api';
import { formatDate } from '@/lib/utils';
import type { OrderResponse } from '@/types';

/**
 * Admin page for managing users
 */
export function AdminUsers() {
  const queryClient = useQueryClient();
  const [searchKeyword, setSearchKeyword] = useState('');
  const [userTypeFilter, setUserTypeFilter] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [viewingUser, setViewingUser] = useState<AdminUserResponse | null>(null);
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isRoleModalOpen, setIsRoleModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<AdminUserResponse | null>(null);
  const [userOrders, setUserOrders] = useState<OrderResponse[]>([]);

  // Form state
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phone: '',
    isActive: true,
  });

  const [selectedRoles, setSelectedRoles] = useState<string[]>([]);

  // Fetch users
  const { data: usersData, isLoading } = useQuery({
    queryKey: ['admin-users', userTypeFilter, statusFilter, dateFrom, dateTo, searchKeyword],
    queryFn: () => adminApi.getAllUsers({
      userType: userTypeFilter || undefined,
      status: statusFilter || undefined,
      dateFrom: dateFrom || undefined,
      dateTo: dateTo || undefined,
      search: searchKeyword || undefined,
      page: 0,
      size: 100,
    }),
  });

  // Fetch user details
  const { data: userDetails } = useQuery({
    queryKey: ['admin-user-details', viewingUser?.id],
    queryFn: () => adminApi.getUserById(viewingUser!.id),
    enabled: !!viewingUser,
  });

  // Fetch user orders
  const { data: ordersData } = useQuery({
    queryKey: ['admin-user-orders', viewingUser?.id],
    queryFn: () => adminApi.getUserOrderHistory(viewingUser!.id),
    enabled: !!viewingUser,
  });

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: ({ userId, data }: { userId: number; data: any }) =>
      adminApi.updateUser(userId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
      setIsEditModalOpen(false);
      setEditingUser(null);
    },
  });

  // Update roles mutation
  const updateRolesMutation = useMutation({
    mutationFn: ({ userId, roles }: { userId: number; roles: string[] }) =>
      adminApi.updateUserRoles(userId, roles),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
      setIsRoleModalOpen(false);
      setEditingUser(null);
    },
  });

  // Suspend mutation
  const suspendMutation = useMutation({
    mutationFn: (userId: number) => adminApi.suspendUser(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
    },
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (userId: number) => adminApi.deleteUser(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
    },
  });

  const handleViewDetails = async (user: AdminUserResponse) => {
    setViewingUser(user);
    setIsDetailsModalOpen(true);
  };

  const handleEdit = (user: AdminUserResponse) => {
    setEditingUser(user);
    setFormData({
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      phone: user.phone || '',
      isActive: user.isActive,
    });
    setIsEditModalOpen(true);
  };

  const handleUpdate = () => {
    if (!editingUser) return;
    updateMutation.mutate({
      userId: editingUser.id,
      data: formData,
    });
  };

  const handleEditRoles = (user: AdminUserResponse) => {
    setEditingUser(user);
    setSelectedRoles(user.roles || []);
    setIsRoleModalOpen(true);
  };

  const handleUpdateRoles = () => {
    if (!editingUser) return;
    updateRolesMutation.mutate({
      userId: editingUser.id,
      roles: selectedRoles,
    });
  };

  const handleSuspend = (userId: number) => {
    if (confirm('Are you sure you want to suspend this user?')) {
      suspendMutation.mutate(userId);
    }
  };

  const handleDelete = (userId: number) => {
    if (confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
      deleteMutation.mutate(userId);
    }
  };

  const users = usersData?.content || [];
  const orders = ordersData || [];

  return (
    <div className="container mx-auto py-8 px-4">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold">User Management</h1>
          <p className="text-muted-foreground mt-1">Manage all users in the system</p>
        </div>
      </div>

      {/* Filters */}
      <Card className="p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
          <div className="md:col-span-2">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search users..."
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>
          <Select value={userTypeFilter} onValueChange={setUserTypeFilter}>
            <SelectTrigger>
              <SelectValue placeholder="All Types" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="">All Types</SelectItem>
              <SelectItem value="CUSTOMER">Customer</SelectItem>
              <SelectItem value="BUSINESS">Business</SelectItem>
              <SelectItem value="RETAILER">Retailer</SelectItem>
            </SelectContent>
          </Select>
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger>
              <SelectValue placeholder="All Status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="">All Status</SelectItem>
              <SelectItem value="active">Active</SelectItem>
              <SelectItem value="inactive">Inactive</SelectItem>
              <SelectItem value="suspended">Suspended</SelectItem>
            </SelectContent>
          </Select>
          <div className="flex gap-2">
            <Input
              type="date"
              placeholder="From"
              value={dateFrom}
              onChange={(e) => setDateFrom(e.target.value)}
              className="flex-1"
            />
            <Input
              type="date"
              placeholder="To"
              value={dateTo}
              onChange={(e) => setDateTo(e.target.value)}
              className="flex-1"
            />
          </div>
        </div>
      </Card>

      {/* Users Table */}
      {isLoading ? (
        <div className="text-center py-12">Loading...</div>
      ) : users.length === 0 ? (
        <Card className="p-12 text-center">
          <p className="text-muted-foreground">No users found.</p>
        </Card>
      ) : (
        <Card>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b">
                  <th className="p-4 text-left">Name</th>
                  <th className="p-4 text-left">Email</th>
                  <th className="p-4 text-left">Type</th>
                  <th className="p-4 text-left">Status</th>
                  <th className="p-4 text-left">Roles</th>
                  <th className="p-4 text-left">Registration Date</th>
                  <th className="p-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id} className="border-b hover:bg-muted/50">
                    <td className="p-4 font-medium">
                      {user.firstName || user.lastName
                        ? `${user.firstName || ''} ${user.lastName || ''}`.trim()
                        : user.username}
                    </td>
                    <td className="p-4">{user.email}</td>
                    <td className="p-4">
                      <span className="px-2 py-1 rounded text-xs bg-blue-100 text-blue-800">
                        {user.userType}
                      </span>
                    </td>
                    <td className="p-4">
                      <span className={`px-2 py-1 rounded text-xs ${
                        user.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                      }`}>
                        {user.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="p-4">
                      <div className="flex gap-1 flex-wrap">
                        {user.roles?.map((role) => (
                          <span key={role} className="px-2 py-1 rounded text-xs bg-purple-100 text-purple-800">
                            {role}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="p-4 text-sm text-muted-foreground">
                      {formatDate(user.registrationDate)}
                    </td>
                    <td className="p-4">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleViewDetails(user)}
                        >
                          View
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleEdit(user)}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleEditRoles(user)}
                        >
                          <Shield className="h-4 w-4" />
                        </Button>
                        {user.isActive && (
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleSuspend(user.id)}
                            disabled={suspendMutation.isPending}
                          >
                            <UserX className="h-4 w-4" />
                          </Button>
                        )}
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleDelete(user.id)}
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

      {/* User Details Modal */}
      <Dialog open={isDetailsModalOpen} onOpenChange={setIsDetailsModalOpen}>
        <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>User Details</DialogTitle>
          </DialogHeader>
          {viewingUser && (
            <Tabs defaultValue="info" className="w-full">
              <TabsList>
                <TabsTrigger value="info">User Info</TabsTrigger>
                <TabsTrigger value="orders">Order History</TabsTrigger>
                <TabsTrigger value="activity">Activity</TabsTrigger>
              </TabsList>
              <TabsContent value="info" className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label className="text-muted-foreground">Name</Label>
                    <p className="font-semibold">
                      {viewingUser.firstName || viewingUser.lastName
                        ? `${viewingUser.firstName || ''} ${viewingUser.lastName || ''}`.trim()
                        : viewingUser.username}
                    </p>
                  </div>
                  <div>
                    <Label className="text-muted-foreground">Email</Label>
                    <p className="font-semibold">{viewingUser.email}</p>
                  </div>
                  <div>
                    <Label className="text-muted-foreground">Username</Label>
                    <p className="font-semibold">{viewingUser.username}</p>
                  </div>
                  <div>
                    <Label className="text-muted-foreground">Phone</Label>
                    <p className="font-semibold">{viewingUser.phone || 'N/A'}</p>
                  </div>
                  <div>
                    <Label className="text-muted-foreground">User Type</Label>
                    <p className="font-semibold">{viewingUser.userType}</p>
                  </div>
                  <div>
                    <Label className="text-muted-foreground">Status</Label>
                    <p className="font-semibold">{viewingUser.isActive ? 'Active' : 'Inactive'}</p>
                  </div>
                  <div>
                    <Label className="text-muted-foreground">Verified</Label>
                    <p className="font-semibold">{viewingUser.isVerified ? 'Yes' : 'No'}</p>
                  </div>
                  <div>
                    <Label className="text-muted-foreground">Registration Date</Label>
                    <p className="font-semibold">{formatDate(viewingUser.registrationDate)}</p>
                  </div>
                  {viewingUser.lastLogin && (
                    <div>
                      <Label className="text-muted-foreground">Last Login</Label>
                      <p className="font-semibold">{formatDate(viewingUser.lastLogin)}</p>
                    </div>
                  )}
                  {viewingUser.orderCount !== undefined && (
                    <div>
                      <Label className="text-muted-foreground">Total Orders</Label>
                      <p className="font-semibold">{viewingUser.orderCount}</p>
                    </div>
                  )}
                  {viewingUser.totalSpent !== undefined && (
                    <div>
                      <Label className="text-muted-foreground">Total Spent</Label>
                      <p className="font-semibold">${viewingUser.totalSpent.toFixed(2)}</p>
                    </div>
                  )}
                </div>
                <div>
                  <Label className="text-muted-foreground mb-2 block">Roles</Label>
                  <div className="flex gap-2 flex-wrap">
                    {viewingUser.roles?.map((role) => (
                      <span key={role} className="px-3 py-1 rounded-full text-sm bg-purple-100 text-purple-800">
                        {role}
                      </span>
                    ))}
                  </div>
                </div>
              </TabsContent>
              <TabsContent value="orders" className="space-y-4">
                {orders.length === 0 ? (
                  <p className="text-muted-foreground">No orders found.</p>
                ) : (
                  <div className="space-y-2">
                    {orders.map((order) => (
                      <Card key={order.id} className="p-4">
                        <div className="flex justify-between items-start">
                          <div>
                            <p className="font-semibold">Order #{order.code}</p>
                            <p className="text-sm text-muted-foreground">
                              {formatDate(order.createdAt)} • ${order.amount.toFixed(2)}
                            </p>
                          </div>
                          <span className={`px-2 py-1 rounded text-xs ${
                            order.status === 'DELIVERED' ? 'bg-green-100 text-green-800' :
                            order.status === 'CANCELLED' ? 'bg-red-100 text-red-800' :
                            'bg-gray-100 text-gray-800'
                          }`}>
                            {order.status}
                          </span>
                        </div>
                      </Card>
                    ))}
                  </div>
                )}
              </TabsContent>
              <TabsContent value="activity" className="space-y-4">
                <p className="text-muted-foreground">Activity log coming soon...</p>
              </TabsContent>
            </Tabs>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDetailsModalOpen(false)}>
              Close
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit User Modal */}
      <Dialog open={isEditModalOpen} onOpenChange={setIsEditModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit User</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="firstName">First Name</Label>
                <Input
                  id="firstName"
                  value={formData.firstName}
                  onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                />
              </div>
              <div>
                <Label htmlFor="lastName">Last Name</Label>
                <Input
                  id="lastName"
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                />
              </div>
            </div>
            <div>
              <Label htmlFor="phone">Phone</Label>
              <Input
                id="phone"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              />
            </div>
            <div>
              <Label htmlFor="isActive">Status</Label>
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
              Update User
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Roles Modal */}
      <Dialog open={isRoleModalOpen} onOpenChange={setIsRoleModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Manage User Roles</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label>Roles</Label>
              <div className="space-y-2 mt-2">
                {['ADMIN', 'USER', 'VENDOR', 'CUSTOMER'].map((role) => (
                  <label key={role} className="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      checked={selectedRoles.includes(role)}
                      onChange={(e) => {
                        if (e.target.checked) {
                          setSelectedRoles([...selectedRoles, role]);
                        } else {
                          setSelectedRoles(selectedRoles.filter(r => r !== role));
                        }
                      }}
                      className="rounded"
                    />
                    <span>{role}</span>
                  </label>
                ))}
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsRoleModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleUpdateRoles} disabled={updateRolesMutation.isPending}>
              Update Roles
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
