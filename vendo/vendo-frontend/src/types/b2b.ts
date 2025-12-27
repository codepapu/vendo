/**
 * B2B Type Definitions
 * Types for Business-to-Business features
 */

/**
 * Business Account
 */
export interface BusinessAccount {
  id: number;
  userId: number;
  companyName: string;
  businessRegistrationNumber?: string;
  taxId?: string;
  businessType: 'CORPORATE' | 'SME' | 'ENTERPRISE';
  creditLimit: number;
  creditTermsDays: number;
  paymentTerms?: string;
  billingAddressId?: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUSPENDED';
  approvedAt?: string;
  approvedBy?: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * Business Contact
 */
export interface BusinessContact {
  id: number;
  businessAccountId: number;
  userId: number;
  contactType: 'PRIMARY' | 'BILLING' | 'SHIPPING' | 'PURCHASER';
  isPrimary: boolean;
  createdAt: string;
}

/**
 * B2B Pricing
 */
export interface B2BPricing {
  productId: number;
  quantity: number;
  unitPrice: number;
  discount: number;
  totalPrice: number;
  priceTier?: PriceTier;
}

/**
 * Price Tier
 */
export interface PriceTier {
  id: number;
  productId: number;
  businessType?: string;
  minQuantity: number;
  maxQuantity?: number;
  price: number;
  discountPercentage?: number;
  isActive: boolean;
}

/**
 * Purchase Order
 */
export interface PurchaseOrder {
  id: number;
  poNumber: string;
  businessAccountId: number;
  orderId?: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'COMPLETED';
  requestedBy: number;
  approvedBy?: number;
  approvalNotes?: string;
  requestedAt: string;
  approvedAt?: string;
  createdAt: string;
}

/**
 * Invoice
 */
export interface Invoice {
  id: number;
  invoiceNumber: string;
  orderId?: number;
  businessAccountId: number;
  issueDate: string;
  dueDate: string;
  status: 'PENDING' | 'PAID' | 'OVERDUE' | 'CANCELLED';
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  paidAmount: number;
  paidAt?: string;
  createdAt: string;
}

/**
 * Request Types
 */
export interface BusinessAccountRequest {
  companyName: string;
  businessRegistrationNumber?: string;
  taxId?: string;
  businessType: 'CORPORATE' | 'SME' | 'ENTERPRISE';
  creditLimit?: number;
  creditTermsDays?: number;
  paymentTerms?: string;
  billingAddressId?: number;
}

export interface PurchaseOrderRequest {
  orderId: number;
  notes?: string;
}

/**
 * Response Types
 */
export interface BusinessAccountResponse extends BusinessAccount {
  availableCredit: number;
  totalOrders: number;
  totalSpent: number;
}

export interface PurchaseOrderResponse extends PurchaseOrder {
  businessAccount?: BusinessAccount;
  order?: {
    id: number;
    orderNumber: string;
    totalAmount: number;
  };
}

export interface InvoiceResponse extends Invoice {
  businessAccount?: BusinessAccount;
  order?: {
    id: number;
    orderNumber: string;
  };
  lineItems?: InvoiceLineItem[];
  paymentHistory?: PaymentHistory[];
}

export interface InvoiceLineItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface PaymentHistory {
  id: number;
  amount: number;
  paymentMethod: string;
  paidAt: string;
}

