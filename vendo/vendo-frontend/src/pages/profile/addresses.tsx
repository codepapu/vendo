import { useState, FormEvent } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { addressApi, type AddAddressRequest, type UpdateAddressRequest } from '@/services/address-api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/hooks/use-toast';
import { Plus, Edit, Trash2, MapPin, Loader2, Check } from 'lucide-react';
import type { AddressResponse } from '@/types';

export function Addresses() {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [isAdding, setIsAdding] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);

  // Fetch addresses
  const { data: addresses = [], isLoading } = useQuery({
    queryKey: ['addresses'],
    queryFn: () => addressApi.getAddresses(),
  });

  // Add address mutation
  const addAddressMutation = useMutation({
    mutationFn: (data: AddAddressRequest) => addressApi.addAddress(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['addresses'] });
      setIsAdding(false);
    },
  });

  // Update address mutation
  const updateAddressMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateAddressRequest }) =>
      addressApi.updateAddress(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['addresses'] });
      setEditingId(null);
    },
  });

  // Delete address mutation
  const deleteAddressMutation = useMutation({
    mutationFn: (id: number) => addressApi.deleteAddress(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['addresses'] });
    },
  });

  // Set default address mutation
  const setDefaultMutation = useMutation({
    mutationFn: (id: number) => addressApi.setDefaultAddress(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['addresses'] });
    },
  });

  const handleAddSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const data: AddAddressRequest = {
      addressType: (formData.get('addressType') as 'SHIPPING' | 'BILLING' | 'BOTH') || 'SHIPPING',
      contactName: formData.get('contactName') as string,
      phone: formData.get('phone') as string,
      addressLine1: formData.get('addressLine1') as string,
      addressLine2: formData.get('addressLine2') as string || undefined,
      city: formData.get('city') as string,
      state: formData.get('state') as string,
      postalCode: formData.get('postalCode') as string,
      country: formData.get('country') as string,
      isDefault: formData.get('isDefault') === 'on',
    };
    addAddressMutation.mutate(data);
  };

  const handleUpdateSubmit = async (e: FormEvent<HTMLFormElement>, id: number) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const data: UpdateAddressRequest = {
      addressType: (formData.get('addressType') as 'SHIPPING' | 'BILLING' | 'BOTH') || undefined,
      contactName: formData.get('contactName') as string || undefined,
      phone: formData.get('phone') as string || undefined,
      addressLine1: formData.get('addressLine1') as string || undefined,
      addressLine2: formData.get('addressLine2') as string || undefined,
      city: formData.get('city') as string || undefined,
      state: formData.get('state') as string || undefined,
      postalCode: formData.get('postalCode') as string || undefined,
      country: formData.get('country') as string || undefined,
      isDefault: formData.get('isDefault') === 'on',
    };
    updateAddressMutation.mutate({ id, data });
  };

  const handleDelete = (id: number) => {
    if (window.confirm('Are you sure you want to delete this address?')) {
      deleteAddressMutation.mutate(id);
    }
  };

  if (isLoading) {
    return (
      <div className="container py-8 flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin" />
      </div>
    );
  }

  return (
    <div className="container py-8 max-w-6xl">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Addresses</h1>
          <p className="text-muted-foreground mt-2">Manage your shipping and billing addresses</p>
        </div>
        <Button onClick={() => setIsAdding(true)} disabled={isAdding}>
          <Plus className="h-4 w-4 mr-2" />
          Add Address
        </Button>
      </div>

      {/* Add Address Form */}
      {isAdding && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Add New Address</CardTitle>
            <CardDescription>Enter your address information</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleAddSubmit} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="addressType">Address Type</Label>
                  <select
                    id="addressType"
                    name="addressType"
                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background"
                    defaultValue="SHIPPING"
                  >
                    <option value="SHIPPING">Shipping</option>
                    <option value="BILLING">Billing</option>
                    <option value="BOTH">Both</option>
                  </select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="contactName">Contact Name</Label>
                  <Input id="contactName" name="contactName" required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="phone">Phone</Label>
                  <Input id="phone" name="phone" type="tel" required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="country">Country</Label>
                  <Input id="country" name="country" required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="addressLine1">Address Line 1</Label>
                  <Input id="addressLine1" name="addressLine1" required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="addressLine2">Address Line 2 (Optional)</Label>
                  <Input id="addressLine2" name="addressLine2" />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="city">City</Label>
                  <Input id="city" name="city" required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="state">State/Province</Label>
                  <Input id="state" name="state" required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="postalCode">Postal Code</Label>
                  <Input id="postalCode" name="postalCode" required />
                </div>
                <div className="space-y-2 flex items-center pt-6">
                  <input
                    type="checkbox"
                    id="isDefault"
                    name="isDefault"
                    className="h-4 w-4 rounded border-gray-300"
                  />
                  <Label htmlFor="isDefault" className="ml-2 cursor-pointer">
                    Set as default address
                  </Label>
                </div>
              </div>
              <div className="flex gap-2 pt-4">
                <Button type="submit" disabled={addAddressMutation.isPending}>
                  {addAddressMutation.isPending ? (
                    <>
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      Adding...
                    </>
                  ) : (
                    'Add Address'
                  )}
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setIsAdding(false)}
                >
                  Cancel
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      {/* Addresses List */}
      {addresses.length === 0 && !isAdding ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <MapPin className="h-12 w-12 text-muted-foreground mb-4" />
            <p className="text-muted-foreground">No addresses found</p>
            <Button onClick={() => setIsAdding(true)} className="mt-4">
              <Plus className="h-4 w-4 mr-2" />
              Add Your First Address
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {addresses.map((address) => (
            <Card key={address.id} className={address.isDefault ? 'border-primary' : ''}>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle className="text-lg flex items-center gap-2">
                      {address.isDefault && (
                        <span className="text-xs bg-primary text-primary-foreground px-2 py-1 rounded">
                          Default
                        </span>
                      )}
                      {address.addressType || 'Shipping'} Address
                    </CardTitle>
                    <CardDescription>{address.contactName}</CardDescription>
                  </div>
                  <div className="flex gap-2">
                    {!address.isDefault && (
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => setDefaultMutation.mutate(address.id)}
                        title="Set as default"
                      >
                        <Check className="h-4 w-4" />
                      </Button>
                    )}
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => setEditingId(address.id)}
                      disabled={editingId === address.id}
                    >
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => handleDelete(address.id)}
                      disabled={deleteAddressMutation.isPending}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                {editingId === address.id ? (
                  <form
                    onSubmit={(e) => handleUpdateSubmit(e, address.id)}
                    className="space-y-4"
                  >
                    <div className="grid grid-cols-1 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor={`edit-type-${address.id}`}>Address Type</Label>
                        <select
                          id={`edit-type-${address.id}`}
                          name="addressType"
                          className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                          defaultValue={address.addressType || 'SHIPPING'}
                        >
                          <option value="SHIPPING">Shipping</option>
                          <option value="BILLING">Billing</option>
                          <option value="BOTH">Both</option>
                        </select>
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor={`edit-contact-${address.id}`}>Contact Name</Label>
                        <Input
                          id={`edit-contact-${address.id}`}
                          name="contactName"
                          defaultValue={address.contactName || ''}
                          required
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor={`edit-phone-${address.id}`}>Phone</Label>
                        <Input
                          id={`edit-phone-${address.id}`}
                          name="phone"
                          type="tel"
                          defaultValue={address.phone || ''}
                          required
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor={`edit-address1-${address.id}`}>Address Line 1</Label>
                        <Input
                          id={`edit-address1-${address.id}`}
                          name="addressLine1"
                          defaultValue={address.addressLine1}
                          required
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor={`edit-address2-${address.id}`}>Address Line 2</Label>
                        <Input
                          id={`edit-address2-${address.id}`}
                          name="addressLine2"
                          defaultValue={address.addressLine2 || ''}
                        />
                      </div>
                      <div className="grid grid-cols-2 gap-4">
                        <div className="space-y-2">
                          <Label htmlFor={`edit-city-${address.id}`}>City</Label>
                          <Input
                            id={`edit-city-${address.id}`}
                            name="city"
                            defaultValue={address.city}
                            required
                          />
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor={`edit-state-${address.id}`}>State</Label>
                          <Input
                            id={`edit-state-${address.id}`}
                            name="state"
                            defaultValue={address.state}
                            required
                          />
                        </div>
                      </div>
                      <div className="grid grid-cols-2 gap-4">
                        <div className="space-y-2">
                          <Label htmlFor={`edit-postal-${address.id}`}>Postal Code</Label>
                          <Input
                            id={`edit-postal-${address.id}`}
                            name="postalCode"
                            defaultValue={address.postalCode}
                            required
                          />
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor={`edit-country-${address.id}`}>Country</Label>
                          <Input
                            id={`edit-country-${address.id}`}
                            name="country"
                            defaultValue={address.country}
                            required
                          />
                        </div>
                      </div>
                      <div className="flex items-center space-x-2">
                        <input
                          type="checkbox"
                          id={`edit-default-${address.id}`}
                          name="isDefault"
                          defaultChecked={address.isDefault}
                          className="h-4 w-4 rounded border-gray-300"
                        />
                        <Label htmlFor={`edit-default-${address.id}`} className="cursor-pointer">
                          Set as default address
                        </Label>
                      </div>
                    </div>
                    <div className="flex gap-2 pt-4">
                      <Button
                        type="submit"
                        size="sm"
                        disabled={updateAddressMutation.isPending}
                      >
                        {updateAddressMutation.isPending ? (
                          <>
                            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                            Saving...
                          </>
                        ) : (
                          'Save'
                        )}
                      </Button>
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => setEditingId(null)}
                      >
                        Cancel
                      </Button>
                    </div>
                  </form>
                ) : (
                  <div className="space-y-2">
                    <p className="text-sm">{address.addressLine1}</p>
                    {address.addressLine2 && (
                      <p className="text-sm">{address.addressLine2}</p>
                    )}
                    <p className="text-sm">
                      {address.city}, {address.state} {address.postalCode}
                    </p>
                    <p className="text-sm">{address.country}</p>
                    {address.phone && (
                      <p className="text-sm text-muted-foreground mt-2">
                        Phone: {address.phone}
                      </p>
                    )}
                  </div>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
