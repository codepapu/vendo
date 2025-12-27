import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/hooks/use-toast';
import { b2bApi } from '@/services/b2b-api';
import { useB2BStore } from '@/stores/b2b-store';

const registerSchema = z.object({
  companyName: z.string().min(1, 'Company name is required'),
  businessRegistrationNumber: z.string().optional(),
  taxId: z.string().optional(),
  businessType: z.enum(['CORPORATE', 'SME', 'ENTERPRISE']),
  creditLimit: z.number().min(0).optional(),
  billingAddressId: z.number().optional(),
});

type RegisterFormData = z.infer<typeof registerSchema>;

export default function B2BRegister() {
  const navigate = useNavigate();
  const { toast } = useToast();
  const { setBusinessAccount } = useB2BStore();
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  const businessType = watch('businessType');

  const onSubmit = async (data: RegisterFormData) => {
    setLoading(true);
    try {
      const account = await b2bApi.createAccount(data);
      setBusinessAccount(account);
      toast({
        title: 'Success',
        description: 'Business account created successfully',
      });
      navigate('/b2b/dashboard');
    } catch (error: any) {
      toast({
        title: 'Error',
        description: error.response?.data?.message || 'Failed to create business account',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mx-auto py-8 max-w-2xl">
      <Card>
        <CardHeader>
          <CardTitle>Register Business Account</CardTitle>
          <CardDescription>
            Create a B2B account to access volume pricing and business features
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="companyName">Company Name *</Label>
              <Input
                id="companyName"
                {...register('companyName')}
                placeholder="Enter company name"
              />
              {errors.companyName && (
                <p className="text-sm text-red-500">{errors.companyName.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="businessRegistrationNumber">Business Registration Number</Label>
              <Input
                id="businessRegistrationNumber"
                {...register('businessRegistrationNumber')}
                placeholder="Enter registration number"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="taxId">Tax ID</Label>
              <Input
                id="taxId"
                {...register('taxId')}
                placeholder="Enter tax ID"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="businessType">Business Type *</Label>
              <Select
                value={businessType}
                onValueChange={(value) => setValue('businessType', value as any)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select business type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="CORPORATE">Corporate</SelectItem>
                  <SelectItem value="SME">SME</SelectItem>
                  <SelectItem value="ENTERPRISE">Enterprise</SelectItem>
                </SelectContent>
              </Select>
              {errors.businessType && (
                <p className="text-sm text-red-500">{errors.businessType.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="creditLimit">Credit Limit</Label>
              <Input
                id="creditLimit"
                type="number"
                {...register('creditLimit', { valueAsNumber: true })}
                placeholder="Enter credit limit"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="billingAddressId">Billing Address ID (Optional)</Label>
              <Input
                id="billingAddressId"
                type="number"
                {...register('billingAddressId', { valueAsNumber: true })}
                placeholder="Enter billing address ID"
              />
              <p className="text-sm text-muted-foreground">
                If you have an existing address, enter its ID. Otherwise, leave blank.
              </p>
            </div>

            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? 'Creating...' : 'Create Business Account'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

