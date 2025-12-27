import { http, HttpResponse } from 'msw';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export const handlers = [
  // B2B API handlers
  http.post(`${API_BASE_URL}/b2b/accounts`, () => {
    return HttpResponse.json({
      success: true,
      data: {
        id: 1,
        userId: 1,
        companyName: 'Test Company',
        businessRegistrationNumber: 'REG123',
        taxId: 'TAX123',
        businessType: 'CORPORATE',
        creditLimit: 10000,
        creditTermsDays: 30,
        paymentTerms: 'Net 30',
        billingAddressId: 1,
        status: 'PENDING',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      },
    });
  }),

  http.get(`${API_BASE_URL}/b2b/accounts`, () => {
    return HttpResponse.json({
      success: true,
      data: {
        id: 1,
        userId: 1,
        companyName: 'Test Company',
        status: 'APPROVED',
      },
    });
  }),

  // Retailer API handlers
  http.post(`${API_BASE_URL}/retailers/register`, () => {
    return HttpResponse.json({
      success: true,
      message: 'Retailer registered successfully',
      data: {
        id: 1,
        userId: 1,
        storeName: 'Test Store',
        storeSlug: 'test-store',
        description: 'A test store',
        commissionRate: 10.0,
        status: 'PENDING',
        verified: false,
        createdAt: '2024-01-01T00:00:00Z',
      },
    });
  }),

  http.get(`${API_BASE_URL}/retailers/dashboard`, () => {
    return HttpResponse.json({
      success: true,
      data: {
        retailerId: 1,
        storeName: 'Test Store',
        status: 'ACTIVE',
        totalSales: 50000,
        totalOrders: 100,
        activeProducts: 25,
        lowStockProducts: 3,
        totalCommissions: 5000,
        pendingCommissions: 1000,
        thisMonthSales: 5000,
        thisMonthOrders: 10,
        recentOrders: [
          {
            orderId: 1,
            orderNumber: 'ORD-001',
            totalAmount: 500,
            status: 'DELIVERED',
            createdAt: '2024-01-15T00:00:00Z',
          },
        ],
        topProducts: [
          {
            productId: 1,
            productName: 'Test Product',
            quantitySold: 50,
            totalRevenue: 5000,
          },
        ],
      },
    });
  }),

  http.get(`${API_BASE_URL}/retailers/:id/products`, () => {
    return HttpResponse.json({
      success: true,
      data: [
        {
          id: 1,
          product: {
            id: 1,
            name: 'Test Product',
            sku: 'TEST-001',
          },
          price: 100,
          stockQuantity: 50,
          isActive: true,
          isLowStock: false,
        },
      ],
    });
  }),

  http.get(`${API_BASE_URL}/products`, () => {
    return HttpResponse.json({
      success: true,
      data: {
        content: [
          {
            id: 1,
            name: 'Test Product',
            sku: 'TEST-001',
            price: 100,
            salePrice: 90,
            status: 'PUBLISHED',
          },
        ],
        totalElements: 1,
      },
    });
  }),

  http.post(`${API_BASE_URL}/retailers/:id/products`, () => {
    return HttpResponse.json({
      success: true,
      data: {
        id: 1,
        productId: 1,
        retailerId: 1,
        price: 100,
        stockQuantity: 50,
        isActive: true,
      },
    });
  }),
];

