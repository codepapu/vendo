import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { retailerApi } from '@/services/retailer-api';
import { useRetailerStore } from '@/stores/retailer-store';
import { useAuthStore } from '@/stores/auth-store';

const registerSchema = z.object({
  storeName: z.string().min(1, 'Store name is required').max(255, 'Store name must not exceed 255 characters'),
  storeSlug: z.string()
    .min(1, 'Store slug is required')
    .max(255, 'Store slug must not exceed 255 characters')
    .regex(/^[a-z0-9-]+$/, 'Store slug must contain only lowercase letters, numbers, and hyphens'),
  description: z.string().optional(),
  commissionRate: z.number().min(0).max(100).optional(),
  bankAccount: z.object({
    accountHolderName: z.string().min(1, 'Account holder name is required'),
    accountNumber: z.string().min(1, 'Account number is required'),
    bankName: z.string().min(1, 'Bank name is required'),
    ifscCode: z.string().optional(),
    routingNumber: z.string().optional(),
    swiftCode: z.string().optional(),
  }).optional(),
  taxInformation: z.object({
    taxId: z.string().optional(),
    taxNumber: z.string().optional(),
    vatNumber: z.string().optional(),
    gstNumber: z.string().optional(),
  }).optional(),
});

type RegisterFormData = z.infer<typeof registerSchema>;

export default function RetailerRegister() {
  const navigate = useNavigate();
  const { setRetailer } = useRetailerStore();
  const { user } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [slugChecking, setSlugChecking] = useState(false);
  const [slugAvailable, setSlugAvailable] = useState<boolean | null>(null);
  const [logoFile, setLogoFile] = useState<File | null>(null);
  const [bannerFile, setBannerFile] = useState<File | null>(null);
  const [logoPreview, setLogoPreview] = useState<string | null>(null);
  const [bannerPreview, setBannerPreview] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  const storeSlug = watch('storeSlug');

  // Check slug availability
  const checkSlugAvailability = async (slug: string) => {
    if (!slug || slug.length < 3) {
      setSlugAvailable(null);
      return;
    }

    setSlugChecking(true);
    try {
      const available = await retailerApi.checkSlugAvailability(slug);
      setSlugAvailable(available);
    } catch (error) {
      setSlugAvailable(false);
    } finally {
      setSlugChecking(false);
    }
  };

  // Handle slug change
  const handleSlugChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const slug = e.target.value.toLowerCase().replace(/[^a-z0-9-]/g, '-');
    setValue('storeSlug', slug);
    if (slug.length >= 3) {
      checkSlugAvailability(slug);
    }
  };

  // Handle logo upload
  const handleLogoUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setLogoFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setLogoPreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  // Handle banner upload
  const handleBannerUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setBannerFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setBannerPreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const onSubmit = async (data: RegisterFormData) => {
    if (slugAvailable === false) {
      return;
    }

    setLoading(true);
    try {
      // Upload logo if provided
      let logoUrl = '';
      if (logoFile) {
        const uploadedUrl = await retailerApi.uploadStoreLogo(logoFile);
        if (uploadedUrl) {
          logoUrl = uploadedUrl;
        }
      }

      // Upload banner if provided
      let bannerUrl = '';
      if (bannerFile) {
        const uploadedUrl = await retailerApi.uploadStoreBanner(bannerFile);
        if (uploadedUrl) {
          bannerUrl = uploadedUrl;
        }
      }

      // Register retailer
      const retailer = await retailerApi.registerRetailer({
        ...data,
        logoUrl: logoUrl || undefined,
        bannerUrl: bannerUrl || undefined,
      });

      if (retailer) {
        setRetailer(retailer);
        navigate('/retailer/dashboard');
      }
    } catch (error) {
      console.error('Registration error:', error);
    } finally {
      setLoading(false);
    }
  };

  if (!user) {
    return (
      <div className="container mx-auto py-8">
        <Card>
          <CardContent className="pt-6">
            <p>Please log in to register as a retailer.</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-8 max-w-4xl">
      <Card>
        <CardHeader>
          <CardTitle>Register as Retailer</CardTitle>
          <CardDescription>
            Create your store and start selling products on our marketplace
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            {/* Store Information */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold">Store Information</h3>
              
              <div className="space-y-2">
                <Label htmlFor="storeName">Store Name *</Label>
                <Input
                  id="storeName"
                  {...register('storeName')}
                  placeholder="Enter store name"
                />
                {errors.storeName && (
                  <p className="text-sm text-red-500">{errors.storeName.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="storeSlug">Store Slug *</Label>
                <Input
                  id="storeSlug"
                  {...register('storeSlug')}
                  onChange={handleSlugChange}
                  placeholder="my-store"
                />
                {slugChecking && (
                  <p className="text-sm text-muted-foreground">Checking availability...</p>
                )}
                {slugAvailable === true && (
                  <p className="text-sm text-green-500">✓ Slug is available</p>
                )}
                {slugAvailable === false && (
                  <p className="text-sm text-red-500">✗ Slug is already taken</p>
                )}
                {errors.storeSlug && (
                  <p className="text-sm text-red-500">{errors.storeSlug.message}</p>
                )}
                <p className="text-sm text-muted-foreground">
                  This will be your store URL: /store/{storeSlug || 'your-slug'}
                </p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="description">Description</Label>
                <Textarea
                  id="description"
                  {...register('description')}
                  placeholder="Describe your store..."
                  rows={4}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="commissionRate">Commission Rate (%)</Label>
                <Input
                  id="commissionRate"
                  type="number"
                  step="0.01"
                  min="0"
                  max="100"
                  {...register('commissionRate', { valueAsNumber: true })}
                  placeholder="15.00"
                />
                <p className="text-sm text-muted-foreground">
                  Platform commission rate (optional, will be set by admin if not provided)
                </p>
              </div>
            </div>

            {/* Store Images */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold">Store Images</h3>
              
              <div className="space-y-2">
                <Label htmlFor="logo">Store Logo</Label>
                <Input
                  id="logo"
                  type="file"
                  accept="image/*"
                  onChange={handleLogoUpload}
                />
                {logoPreview && (
                  <div className="mt-2">
                    <img src={logoPreview} alt="Logo preview" className="h-24 w-24 object-cover rounded" />
                  </div>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="banner">Store Banner</Label>
                <Input
                  id="banner"
                  type="file"
                  accept="image/*"
                  onChange={handleBannerUpload}
                />
                {bannerPreview && (
                  <div className="mt-2">
                    <img src={bannerPreview} alt="Banner preview" className="h-32 w-full object-cover rounded" />
                  </div>
                )}
              </div>
            </div>

            {/* Bank Account Details */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold">Bank Account Details</h3>
              
              <div className="space-y-2">
                <Label htmlFor="accountHolderName">Account Holder Name</Label>
                <Input
                  id="accountHolderName"
                  {...register('bankAccount.accountHolderName')}
                  placeholder="Enter account holder name"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="accountNumber">Account Number</Label>
                <Input
                  id="accountNumber"
                  {...register('bankAccount.accountNumber')}
                  placeholder="Enter account number"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="bankName">Bank Name</Label>
                <Input
                  id="bankName"
                  {...register('bankAccount.bankName')}
                  placeholder="Enter bank name"
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="ifscCode">IFSC Code</Label>
                  <Input
                    id="ifscCode"
                    {...register('bankAccount.ifscCode')}
                    placeholder="IFSC code"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="routingNumber">Routing Number</Label>
                  <Input
                    id="routingNumber"
                    {...register('bankAccount.routingNumber')}
                    placeholder="Routing number"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="swiftCode">SWIFT Code</Label>
                  <Input
                    id="swiftCode"
                    {...register('bankAccount.swiftCode')}
                    placeholder="SWIFT code"
                  />
                </div>
              </div>
            </div>

            {/* Tax Information */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold">Tax Information</h3>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="taxId">Tax ID</Label>
                  <Input
                    id="taxId"
                    {...register('taxInformation.taxId')}
                    placeholder="Enter tax ID"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="taxNumber">Tax Number</Label>
                  <Input
                    id="taxNumber"
                    {...register('taxInformation.taxNumber')}
                    placeholder="Enter tax number"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="vatNumber">VAT Number</Label>
                  <Input
                    id="vatNumber"
                    {...register('taxInformation.vatNumber')}
                    placeholder="Enter VAT number"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="gstNumber">GST Number</Label>
                  <Input
                    id="gstNumber"
                    {...register('taxInformation.gstNumber')}
                    placeholder="Enter GST number"
                  />
                </div>
              </div>
            </div>

            <Button type="submit" className="w-full" disabled={loading || slugAvailable === false}>
              {loading ? 'Submitting...' : 'Submit Registration'}
            </Button>

            <p className="text-sm text-muted-foreground text-center">
              Your registration will be reviewed by our admin team. You'll be notified once approved.
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

