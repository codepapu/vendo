export interface User {
  id: number;
  email: string;
  username: string;
  firstName: string;
  lastName: string;
  superUser: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresAt: string;
  user: User;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  discountPrice?: number;
  sku: string;
  stock: number;
  images: string[];
  category: Category;
  vendor?: Vendor;
  attributes?: ProductAttribute[];
  variations?: ProductVariation[];
  rating?: number;
  reviewCount?: number;
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  description?: string;
  image?: string;
  parentId?: number;
}

export interface Vendor {
  id: number;
  name: string;
  slug: string;
  description?: string;
  logo?: string;
}

export interface ProductAttribute {
  id: number;
  name: string;
  value: string;
}

export interface ProductVariation {
  id: number;
  name: string;
  price: number;
  stock: number;
  sku: string;
}

export interface CartItem {
  id: number;
  product: Product;
  quantity: number;
  variationId?: number;
}

export interface Cart {
  id: number;
  items: CartItem[];
  total: number;
  subtotal: number;
  tax: number;
  shipping: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  items: OrderItem[];
  total: number;
  status: OrderStatus;
  shippingAddress: Address;
  billingAddress: Address;
  paymentMethod: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItem {
  id: number;
  product: Product;
  quantity: number;
  price: number;
  total: number;
}

export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED'
  | 'REFUNDED';

export interface Address {
  id?: number;
  firstName: string;
  lastName: string;
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  phone?: string;
}

export interface LoginRequest {
  emailOrUsername: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
  firstName: string;
  lastName: string;
}
