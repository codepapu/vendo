import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { CreditCard, Wallet, Smartphone, Building2 } from 'lucide-react';
import { PaymentGateway, PaymentMethod } from '@/types';

interface PaymentMethodSelectorProps {
  selectedGateway: PaymentGateway | null;
  selectedMethod: PaymentMethod | null;
  onGatewaySelect: (gateway: PaymentGateway) => void;
  onMethodSelect: (method: PaymentMethod) => void;
  disabled?: boolean;
}

const gatewayConfig: Record<PaymentGateway, { name: string; icon: React.ReactNode; methods: PaymentMethod[] }> = {
  STRIPE: {
    name: 'Stripe',
    icon: <CreditCard className="h-5 w-5" />,
    methods: ['CREDIT_CARD', 'DEBIT_CARD'],
  },
  PAYPAL: {
    name: 'PayPal',
    icon: <Wallet className="h-5 w-5" />,
    methods: ['PAYPAL'],
  },
  RAZORPAY: {
    name: 'Razorpay',
    icon: <Building2 className="h-5 w-5" />,
    methods: ['CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING', 'WALLET', 'UPI'],
  },
  GOOGLE_PAY: {
    name: 'Google Pay',
    icon: <Smartphone className="h-5 w-5" />,
    methods: ['GOOGLE_PAY', 'UPI'],
  },
  PHONEPE: {
    name: 'PhonePe',
    icon: <Smartphone className="h-5 w-5" />,
    methods: ['PHONEPE', 'UPI'],
  },
  UPI: {
    name: 'UPI',
    icon: <Smartphone className="h-5 w-5" />,
    methods: ['UPI'],
  },
  PAYSTACK: {
    name: 'Paystack',
    icon: <CreditCard className="h-5 w-5" />,
    methods: ['CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER'],
  },
  SSLCOMMERZ: {
    name: 'SSLCommerz',
    icon: <CreditCard className="h-5 w-5" />,
    methods: ['CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING'],
  },
  MOLLIE: {
    name: 'Mollie',
    icon: <CreditCard className="h-5 w-5" />,
    methods: ['CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER'],
  },
};

const methodLabels: Record<PaymentMethod, string> = {
  CREDIT_CARD: 'Credit Card',
  DEBIT_CARD: 'Debit Card',
  PAYPAL: 'PayPal',
  STRIPE: 'Stripe',
  CASH_ON_DELIVERY: 'Cash on Delivery',
  BANK_TRANSFER: 'Bank Transfer',
  UPI: 'UPI',
  GOOGLE_PAY: 'Google Pay',
  PHONEPE: 'PhonePe',
  NET_BANKING: 'Net Banking',
  WALLET: 'Wallet',
};

export function PaymentMethodSelector({
  selectedGateway,
  selectedMethod,
  onGatewaySelect,
  onMethodSelect,
  disabled = false,
}: PaymentMethodSelectorProps) {
  const availableGateways: PaymentGateway[] = [
    'STRIPE',
    'PAYPAL',
    'RAZORPAY',
    'GOOGLE_PAY',
    'PHONEPE',
    'UPI',
  ];

  const availableMethods = selectedGateway
    ? gatewayConfig[selectedGateway].methods
    : [];

  return (
    <div className="space-y-4">
      {/* Gateway Selection */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <CreditCard className="h-5 w-5" />
            Select Payment Gateway
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
            {availableGateways.map((gateway) => {
              const config = gatewayConfig[gateway];
              const isSelected = selectedGateway === gateway;
              return (
                <button
                  key={gateway}
                  type="button"
                  onClick={() => {
                    onGatewaySelect(gateway);
                    // Reset method when gateway changes
                    if (selectedMethod && !config.methods.includes(selectedMethod)) {
                      onMethodSelect(config.methods[0]);
                    }
                  }}
                  disabled={disabled}
                  className={`
                    flex flex-col items-center gap-2 p-4 border rounded-lg transition-all
                    ${isSelected ? 'border-primary bg-primary/5 ring-2 ring-primary/20' : 'border-gray-200 hover:border-primary/50'}
                    ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer hover:bg-muted'}
                  `}
                >
                  <div className={isSelected ? 'text-primary' : 'text-gray-600'}>
                    {config.icon}
                  </div>
                  <span className="text-sm font-medium">{config.name}</span>
                </button>
              );
            })}
          </div>
        </CardContent>
      </Card>

      {/* Method Selection */}
      {selectedGateway && availableMethods.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Select Payment Method</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {availableMethods.map((method) => {
                const isSelected = selectedMethod === method;
                return (
                  <label
                    key={method}
                    className={`
                      flex items-center gap-3 p-4 border rounded-lg cursor-pointer transition-colors
                      ${isSelected ? 'border-primary bg-primary/5' : 'border-gray-200 hover:bg-muted'}
                      ${disabled ? 'opacity-50 cursor-not-allowed' : ''}
                    `}
                  >
                    <input
                      type="radio"
                      name="paymentMethod"
                      value={method}
                      checked={isSelected}
                      onChange={() => onMethodSelect(method)}
                      disabled={disabled}
                      className="w-4 h-4 text-primary"
                    />
                    <span className="font-medium">{methodLabels[method]}</span>
                  </label>
                );
              })}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

