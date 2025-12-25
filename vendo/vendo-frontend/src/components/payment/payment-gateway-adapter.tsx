import React from 'react';
import { PaymentGateway, PaymentMethod, PaymentResponse, ProcessPaymentRequest } from '@/types';
import { StripePaymentForm } from './stripe-payment-form';
import { PayPalPaymentButton } from './paypal-payment-button';
import { RazorpayCheckout } from './razorpay-checkout';
import { UPIPaymentForm } from './upi-payment-form';

/**
 * Payment Gateway Adapter Props
 * Common props for all payment gateway components
 */
export interface PaymentGatewayAdapterProps {
  orderId: number;
  amount: number;
  currency: string;
  customerEmail?: string;
  customerName?: string;
  customerPhone?: string;
  onSuccess: (payment: PaymentResponse) => void;
  onError: (error: string) => void;
  paymentMethod?: PaymentMethod;
}

/**
 * Payment Gateway Adapter
 * Factory pattern to render the appropriate payment gateway component
 */
export class PaymentGatewayAdapter {
  /**
   * Get the appropriate payment gateway component based on gateway type
   */
  static getGatewayComponent(
    gateway: PaymentGateway,
    props: PaymentGatewayAdapterProps
  ): React.ReactElement {
    const commonProps = {
      orderId: props.orderId,
      amount: props.amount,
      currency: props.currency,
      customerEmail: props.customerEmail,
      customerName: props.customerName,
      customerPhone: props.customerPhone,
      onSuccess: props.onSuccess,
      onError: props.onError,
    };

    switch (gateway) {
      case 'STRIPE':
        return <StripePaymentForm {...commonProps} />;

      case 'PAYPAL':
        return <PayPalPaymentButton {...commonProps} />;

      case 'RAZORPAY':
        return (
          <RazorpayCheckout
            {...commonProps}
            paymentMethod={
              (props.paymentMethod as 'CREDIT_CARD' | 'DEBIT_CARD' | 'NET_BANKING' | 'WALLET' | 'UPI') ||
              'CREDIT_CARD'
            }
          />
        );

      case 'GOOGLE_PAY':
      case 'PHONEPE':
      case 'UPI':
        return <UPIPaymentForm {...commonProps} gateway={gateway} />;

      default:
        throw new Error(`Unsupported payment gateway: ${gateway}`);
    }
  }

  /**
   * Check if a gateway supports a specific payment method
   */
  static supportsPaymentMethod(
    gateway: PaymentGateway,
    method: PaymentMethod
  ): boolean {
    const gatewayMethods: Record<PaymentGateway, PaymentMethod[]> = {
      STRIPE: ['CREDIT_CARD', 'DEBIT_CARD'],
      PAYPAL: ['PAYPAL'],
      RAZORPAY: ['CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING', 'WALLET', 'UPI'],
      GOOGLE_PAY: ['GOOGLE_PAY', 'UPI'],
      PHONEPE: ['PHONEPE', 'UPI'],
      UPI: ['UPI'],
      PAYSTACK: ['CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER'],
      SSLCOMMERZ: ['CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING'],
      MOLLIE: ['CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER'],
    };

    return gatewayMethods[gateway]?.includes(method) || false;
  }

  /**
   * Get available payment methods for a gateway
   */
  static getAvailableMethods(gateway: PaymentGateway): PaymentMethod[] {
    const gatewayMethods: Record<PaymentGateway, PaymentMethod[]> = {
      STRIPE: ['CREDIT_CARD', 'DEBIT_CARD'],
      PAYPAL: ['PAYPAL'],
      RAZORPAY: ['CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING', 'WALLET', 'UPI'],
      GOOGLE_PAY: ['GOOGLE_PAY', 'UPI'],
      PHONEPE: ['PHONEPE', 'UPI'],
      UPI: ['UPI'],
      PAYSTACK: ['CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER'],
      SSLCOMMERZ: ['CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING'],
      MOLLIE: ['CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER'],
    };

    return gatewayMethods[gateway] || [];
  }

  /**
   * Get gateway display name
   */
  static getGatewayName(gateway: PaymentGateway): string {
    const names: Record<PaymentGateway, string> = {
      STRIPE: 'Stripe',
      PAYPAL: 'PayPal',
      RAZORPAY: 'Razorpay',
      GOOGLE_PAY: 'Google Pay',
      PHONEPE: 'PhonePe',
      UPI: 'UPI',
      PAYSTACK: 'Paystack',
      SSLCOMMERZ: 'SSLCommerz',
      MOLLIE: 'Mollie',
    };

    return names[gateway] || gateway;
  }

  /**
   * Check if gateway requires redirect
   */
  static requiresRedirect(gateway: PaymentGateway): boolean {
    // Gateways that typically use redirect flows
    return ['PAYPAL', 'RAZORPAY'].includes(gateway);
  }

  /**
   * Check if gateway supports instant payment confirmation
   */
  static supportsInstantConfirmation(gateway: PaymentGateway): boolean {
    // Gateways that can confirm payment immediately without redirect
    return ['STRIPE', 'GOOGLE_PAY', 'PHONEPE', 'UPI'].includes(gateway);
  }
}

