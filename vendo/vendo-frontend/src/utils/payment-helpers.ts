import { PaymentResponse, PaymentStatus, PaymentGateway } from '@/types';

/**
 * Payment Helper Utilities
 */

/**
 * Check if payment is in a final state (completed or failed)
 */
export function isPaymentFinal(payment: PaymentResponse): boolean {
  const status = payment.status?.toUpperCase();
  return (
    status === 'COMPLETED' ||
    status === 'completed' ||
    status === 'FAILED' ||
    status === 'failed' ||
    status === 'CANCELLED' ||
    status === 'cancelled' ||
    status === 'REFUNDED' ||
    status === 'refunded'
  );
}

/**
 * Check if payment is successful
 */
export function isPaymentSuccessful(payment: PaymentResponse): boolean {
  const status = payment.status?.toUpperCase();
  return status === 'COMPLETED' || status === 'completed';
}

/**
 * Check if payment is pending/processing
 */
export function isPaymentPending(payment: PaymentResponse): boolean {
  const status = payment.status?.toUpperCase();
  return (
    status === 'PENDING' ||
    status === 'pending' ||
    status === 'PROCESSING' ||
    status === 'processing'
  );
}

/**
 * Check if payment can be refunded
 */
export function canRefundPayment(payment: PaymentResponse): boolean {
  if (!isPaymentSuccessful(payment)) {
    return false;
  }

  const refundableAmount = payment.amount - (payment.refundedAmount || 0);
  return refundableAmount > 0;
}

/**
 * Get refundable amount
 */
export function getRefundableAmount(payment: PaymentResponse): number {
  if (!isPaymentSuccessful(payment)) {
    return 0;
  }

  return payment.amount - (payment.refundedAmount || 0);
}

/**
 * Format payment status for display
 */
export function formatPaymentStatus(status: PaymentStatus | string): string {
  return status
    .toString()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join(' ');
}

/**
 * Get payment status color class
 */
export function getPaymentStatusColor(status: PaymentStatus | string): string {
  const statusUpper = status.toString().toUpperCase();
  
  if (statusUpper === 'COMPLETED' || statusUpper === 'completed') {
    return 'text-green-600';
  }
  if (statusUpper === 'FAILED' || statusUpper === 'failed') {
    return 'text-red-600';
  }
  if (statusUpper === 'PENDING' || statusUpper === 'pending' || 
      statusUpper === 'PROCESSING' || statusUpper === 'processing') {
    return 'text-yellow-600';
  }
  if (statusUpper === 'REFUNDED' || statusUpper === 'refunded' ||
      statusUpper === 'PARTIALLY_REFUNDED' || statusUpper === 'partially_refunded') {
    return 'text-blue-600';
  }
  
  return 'text-gray-600';
}

/**
 * Validate payment amount
 */
export function validatePaymentAmount(amount: number, currency: string = 'USD'): {
  valid: boolean;
  error?: string;
} {
  if (amount <= 0) {
    return {
      valid: false,
      error: 'Payment amount must be greater than zero',
    };
  }

  // Minimum amount validation based on currency
  const minimumAmounts: Record<string, number> = {
    USD: 0.5,
    EUR: 0.5,
    GBP: 0.5,
    INR: 1,
    JPY: 50,
  };

  const minimum = minimumAmounts[currency] || 0.01;
  if (amount < minimum) {
    return {
      valid: false,
      error: `Minimum payment amount is ${minimum} ${currency}`,
    };
  }

  return { valid: true };
}

/**
 * Get gateway-specific configuration
 */
export function getGatewayConfig(gateway: PaymentGateway) {
  const configs: Record<PaymentGateway, { name: string; supports3DS: boolean; supportsRecurring: boolean }> = {
    STRIPE: {
      name: 'Stripe',
      supports3DS: true,
      supportsRecurring: true,
    },
    PAYPAL: {
      name: 'PayPal',
      supports3DS: false,
      supportsRecurring: true,
    },
    RAZORPAY: {
      name: 'Razorpay',
      supports3DS: true,
      supportsRecurring: true,
    },
    GOOGLE_PAY: {
      name: 'Google Pay',
      supports3DS: true,
      supportsRecurring: false,
    },
    PHONEPE: {
      name: 'PhonePe',
      supports3DS: false,
      supportsRecurring: false,
    },
    UPI: {
      name: 'UPI',
      supports3DS: false,
      supportsRecurring: false,
    },
    PAYSTACK: {
      name: 'Paystack',
      supports3DS: true,
      supportsRecurring: true,
    },
    SSLCOMMERZ: {
      name: 'SSLCommerz',
      supports3DS: true,
      supportsRecurring: false,
    },
    MOLLIE: {
      name: 'Mollie',
      supports3DS: true,
      supportsRecurring: true,
    },
  };

  return configs[gateway] || { name: gateway, supports3DS: false, supportsRecurring: false };
}

