import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Save, CreditCard, Truck, Receipt, Mail, Settings as SettingsIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { adminApi, type SystemConfigResponse } from '@/services/admin-api';

/**
 * Admin page for system configuration
 */
export function AdminSettings() {
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState('payment');
  const [config, setConfig] = useState<SystemConfigResponse>({
    paymentGateways: {},
    shippingMethods: {},
    taxSettings: {},
    emailTemplates: {},
    systemSettings: {},
  });

  // Fetch configuration
  const { data: configData, isLoading } = useQuery({
    queryKey: ['admin-config'],
    queryFn: () => adminApi.getSystemConfiguration(),
  });

  // Update configuration mutation
  const updateMutation = useMutation({
    mutationFn: (config: SystemConfigResponse) => adminApi.updateSystemConfiguration(config),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-config'] });
    },
  });

  useEffect(() => {
    if (configData) {
      setConfig(configData);
    }
  }, [configData]);

  const handleSave = () => {
    // Basic validation
    if (config.paymentGateways?.stripe?.enabled && !config.paymentGateways?.stripe?.apiKey) {
      alert('Stripe API Key is required when Stripe is enabled');
      return;
    }
    if (config.paymentGateways?.paypal?.enabled && !config.paymentGateways?.paypal?.clientId) {
      alert('PayPal Client ID is required when PayPal is enabled');
      return;
    }
    if (config.taxSettings?.enabled && !config.taxSettings?.defaultRate) {
      alert('Default Tax Rate is required when tax calculation is enabled');
      return;
    }
    updateMutation.mutate(config);
  };

  const updatePaymentGateway = (gateway: string, field: string, value: any) => {
    setConfig({
      ...config,
      paymentGateways: {
        ...config.paymentGateways,
        [gateway]: {
          ...(config.paymentGateways?.[gateway] || {}),
          [field]: value,
        },
      },
    });
  };

  const updateShippingMethod = (method: string, field: string, value: any) => {
    setConfig({
      ...config,
      shippingMethods: {
        ...config.shippingMethods,
        [method]: {
          ...(config.shippingMethods?.[method] || {}),
          [field]: value,
        },
      },
    });
  };

  const updateTaxSetting = (field: string, value: any) => {
    setConfig({
      ...config,
      taxSettings: {
        ...config.taxSettings,
        [field]: value,
      },
    });
  };

  const updateEmailTemplate = (template: string, field: string, value: any) => {
    setConfig({
      ...config,
      emailTemplates: {
        ...config.emailTemplates,
        [template]: {
          ...(config.emailTemplates?.[template] || {}),
          [field]: value,
        },
      },
    });
  };

  const updateSystemSetting = (field: string, value: any) => {
    setConfig({
      ...config,
      systemSettings: {
        ...config.systemSettings,
        [field]: value,
      },
    });
  };

  if (isLoading) {
    return <div className="container mx-auto py-8 px-4 text-center">Loading...</div>;
  }

  return (
    <div className="container mx-auto py-8 px-4">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold">System Configuration</h1>
          <p className="text-muted-foreground mt-1">Configure system settings and integrations</p>
        </div>
        <Button onClick={handleSave} disabled={updateMutation.isPending}>
          <Save className="mr-2 h-4 w-4" />
          Save All Changes
        </Button>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
        <TabsList className="grid w-full grid-cols-5">
          <TabsTrigger value="payment">
            <CreditCard className="mr-2 h-4 w-4" />
            Payment
          </TabsTrigger>
          <TabsTrigger value="shipping">
            <Truck className="mr-2 h-4 w-4" />
            Shipping
          </TabsTrigger>
          <TabsTrigger value="tax">
            <Receipt className="mr-2 h-4 w-4" />
            Tax
          </TabsTrigger>
          <TabsTrigger value="email">
            <Mail className="mr-2 h-4 w-4" />
            Email
          </TabsTrigger>
          <TabsTrigger value="system">
            <SettingsIcon className="mr-2 h-4 w-4" />
            System
          </TabsTrigger>
        </TabsList>

        {/* Payment Gateways */}
        <TabsContent value="payment" className="space-y-6">
          <Card className="p-6">
            <h2 className="text-xl font-semibold mb-4">Stripe</h2>
            <div className="space-y-4">
              <div>
                <Label htmlFor="stripe-enabled">Enabled</Label>
                <Select
                  value={config.paymentGateways?.stripe?.enabled ? 'true' : 'false'}
                  onValueChange={(v) => updatePaymentGateway('stripe', 'enabled', v === 'true')}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="true">Enabled</SelectItem>
                    <SelectItem value="false">Disabled</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="stripe-api-key">API Key</Label>
                <Input
                  id="stripe-api-key"
                  type="password"
                  value={config.paymentGateways?.stripe?.apiKey || ''}
                  onChange={(e) => updatePaymentGateway('stripe', 'apiKey', e.target.value)}
                  placeholder="sk_live_..."
                />
              </div>
              <div>
                <Label htmlFor="stripe-public-key">Public Key</Label>
                <Input
                  id="stripe-public-key"
                  value={config.paymentGateways?.stripe?.publicKey || ''}
                  onChange={(e) => updatePaymentGateway('stripe', 'publicKey', e.target.value)}
                  placeholder="pk_live_..."
                />
              </div>
              <div>
                <Label htmlFor="stripe-webhook-secret">Webhook Secret</Label>
                <Input
                  id="stripe-webhook-secret"
                  type="password"
                  value={config.paymentGateways?.stripe?.webhookSecret || ''}
                  onChange={(e) => updatePaymentGateway('stripe', 'webhookSecret', e.target.value)}
                  placeholder="whsec_..."
                />
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <h2 className="text-xl font-semibold mb-4">PayPal</h2>
            <div className="space-y-4">
              <div>
                <Label htmlFor="paypal-enabled">Enabled</Label>
                <Select
                  value={config.paymentGateways?.paypal?.enabled ? 'true' : 'false'}
                  onValueChange={(v) => updatePaymentGateway('paypal', 'enabled', v === 'true')}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="true">Enabled</SelectItem>
                    <SelectItem value="false">Disabled</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="paypal-client-id">Client ID</Label>
                <Input
                  id="paypal-client-id"
                  value={config.paymentGateways?.paypal?.clientId || ''}
                  onChange={(e) => updatePaymentGateway('paypal', 'clientId', e.target.value)}
                  placeholder="Client ID"
                />
              </div>
              <div>
                <Label htmlFor="paypal-secret">Secret</Label>
                <Input
                  id="paypal-secret"
                  type="password"
                  value={config.paymentGateways?.paypal?.secret || ''}
                  onChange={(e) => updatePaymentGateway('paypal', 'secret', e.target.value)}
                  placeholder="Secret"
                />
              </div>
              <div>
                <Label htmlFor="paypal-mode">Mode</Label>
                <Select
                  value={config.paymentGateways?.paypal?.mode || 'sandbox'}
                  onValueChange={(v) => updatePaymentGateway('paypal', 'mode', v)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="sandbox">Sandbox</SelectItem>
                    <SelectItem value="live">Live</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </Card>
        </TabsContent>

        {/* Shipping Methods */}
        <TabsContent value="shipping" className="space-y-6">
          <Card className="p-6">
            <h2 className="text-xl font-semibold mb-4">Standard Shipping</h2>
            <div className="space-y-4">
              <div>
                <Label htmlFor="standard-enabled">Enabled</Label>
                <Select
                  value={config.shippingMethods?.standard?.enabled ? 'true' : 'false'}
                  onValueChange={(v) => updateShippingMethod('standard', 'enabled', v === 'true')}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="true">Enabled</SelectItem>
                    <SelectItem value="false">Disabled</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="standard-cost">Base Cost ($)</Label>
                <Input
                  id="standard-cost"
                  type="number"
                  step="0.01"
                  value={config.shippingMethods?.standard?.baseCost || ''}
                  onChange={(e) => updateShippingMethod('standard', 'baseCost', parseFloat(e.target.value))}
                  placeholder="0.00"
                />
              </div>
              <div>
                <Label htmlFor="standard-days">Estimated Days</Label>
                <Input
                  id="standard-days"
                  type="number"
                  value={config.shippingMethods?.standard?.estimatedDays || ''}
                  onChange={(e) => updateShippingMethod('standard', 'estimatedDays', parseInt(e.target.value))}
                  placeholder="5-7"
                />
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <h2 className="text-xl font-semibold mb-4">Express Shipping</h2>
            <div className="space-y-4">
              <div>
                <Label htmlFor="express-enabled">Enabled</Label>
                <Select
                  value={config.shippingMethods?.express?.enabled ? 'true' : 'false'}
                  onValueChange={(v) => updateShippingMethod('express', 'enabled', v === 'true')}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="true">Enabled</SelectItem>
                    <SelectItem value="false">Disabled</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="express-cost">Base Cost ($)</Label>
                <Input
                  id="express-cost"
                  type="number"
                  step="0.01"
                  value={config.shippingMethods?.express?.baseCost || ''}
                  onChange={(e) => updateShippingMethod('express', 'baseCost', parseFloat(e.target.value))}
                  placeholder="0.00"
                />
              </div>
              <div>
                <Label htmlFor="express-days">Estimated Days</Label>
                <Input
                  id="express-days"
                  type="number"
                  value={config.shippingMethods?.express?.estimatedDays || ''}
                  onChange={(e) => updateShippingMethod('express', 'estimatedDays', parseInt(e.target.value))}
                  placeholder="1-2"
                />
              </div>
            </div>
          </Card>
        </TabsContent>

        {/* Tax Settings */}
        <TabsContent value="tax" className="space-y-6">
          <Card className="p-6">
            <h2 className="text-xl font-semibold mb-4">Tax Configuration</h2>
            <div className="space-y-4">
              <div>
                <Label htmlFor="tax-enabled">Tax Calculation Enabled</Label>
                <Select
                  value={config.taxSettings?.enabled ? 'true' : 'false'}
                  onValueChange={(v) => updateTaxSetting('enabled', v === 'true')}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="true">Enabled</SelectItem>
                    <SelectItem value="false">Disabled</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="default-tax-rate">Default Tax Rate (%)</Label>
                <Input
                  id="default-tax-rate"
                  type="number"
                  step="0.01"
                  value={config.taxSettings?.defaultRate || ''}
                  onChange={(e) => updateTaxSetting('defaultRate', parseFloat(e.target.value))}
                  placeholder="0.00"
                />
              </div>
              <div>
                <Label htmlFor="tax-inclusive">Prices Include Tax</Label>
                <Select
                  value={config.taxSettings?.inclusive ? 'true' : 'false'}
                  onValueChange={(v) => updateTaxSetting('inclusive', v === 'true')}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="true">Yes</SelectItem>
                    <SelectItem value="false">No</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </Card>
        </TabsContent>

        {/* Email Templates */}
        <TabsContent value="email" className="space-y-6">
          <Card className="p-6">
            <h2 className="text-xl font-semibold mb-4">Order Confirmation Email</h2>
            <div className="space-y-4">
              <div>
                <Label htmlFor="order-confirmation-subject">Subject</Label>
                <Input
                  id="order-confirmation-subject"
                  value={config.emailTemplates?.orderConfirmation?.subject || ''}
                  onChange={(e) => updateEmailTemplate('orderConfirmation', 'subject', e.target.value)}
                  placeholder="Order Confirmation"
                />
              </div>
              <div>
                <Label htmlFor="order-confirmation-body">Body</Label>
                <Textarea
                  id="order-confirmation-body"
                  value={config.emailTemplates?.orderConfirmation?.body || ''}
                  onChange={(e) => updateEmailTemplate('orderConfirmation', 'body', e.target.value)}
                  rows={10}
                  placeholder="Email template body..."
                />
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <h2 className="text-xl font-semibold mb-4">Shipping Notification Email</h2>
            <div className="space-y-4">
              <div>
                <Label htmlFor="shipping-subject">Subject</Label>
                <Input
                  id="shipping-subject"
                  value={config.emailTemplates?.shippingNotification?.subject || ''}
                  onChange={(e) => updateEmailTemplate('shippingNotification', 'subject', e.target.value)}
                  placeholder="Your Order Has Shipped"
                />
              </div>
              <div>
                <Label htmlFor="shipping-body">Body</Label>
                <Textarea
                  id="shipping-body"
                  value={config.emailTemplates?.shippingNotification?.body || ''}
                  onChange={(e) => updateEmailTemplate('shippingNotification', 'body', e.target.value)}
                  rows={10}
                  placeholder="Email template body..."
                />
              </div>
            </div>
          </Card>
        </TabsContent>

        {/* System Settings */}
        <TabsContent value="system" className="space-y-6">
          <Card className="p-6">
            <h2 className="text-xl font-semibold mb-4">General Settings</h2>
            <div className="space-y-4">
              <div>
                <Label htmlFor="site-name">Site Name</Label>
                <Input
                  id="site-name"
                  value={config.systemSettings?.siteName || ''}
                  onChange={(e) => updateSystemSetting('siteName', e.target.value)}
                  placeholder="Vendo"
                />
              </div>
              <div>
                <Label htmlFor="site-email">Site Email</Label>
                <Input
                  id="site-email"
                  type="email"
                  value={config.systemSettings?.siteEmail || ''}
                  onChange={(e) => updateSystemSetting('siteEmail', e.target.value)}
                  placeholder="admin@example.com"
                />
              </div>
              <div>
                <Label htmlFor="currency">Currency</Label>
                <Select
                  value={config.systemSettings?.currency || 'USD'}
                  onValueChange={(v) => updateSystemSetting('currency', v)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="USD">USD ($)</SelectItem>
                    <SelectItem value="EUR">EUR (€)</SelectItem>
                    <SelectItem value="GBP">GBP (£)</SelectItem>
                    <SelectItem value="INR">INR (₹)</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="timezone">Timezone</Label>
                <Input
                  id="timezone"
                  value={config.systemSettings?.timezone || ''}
                  onChange={(e) => updateSystemSetting('timezone', e.target.value)}
                  placeholder="UTC"
                />
              </div>
            </div>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
