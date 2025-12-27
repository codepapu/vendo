import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@/test/utils/test-utils';
import userEvent from '@testing-library/user-event';
import { useNavigate } from 'react-router-dom';
import { useRetailerStore } from '@/stores/retailer-store';
import { useAuthStore } from '@/stores/auth-store';
import { retailerApi } from '@/services/retailer-api';
import { useQuery } from '@tanstack/react-query';
import RetailerDashboard from '../dashboard';

// Mock dependencies
vi.mock('react-router-dom', () => ({
  useNavigate: vi.fn(),
}));

vi.mock('@/stores/retailer-store', () => ({
  useRetailerStore: vi.fn(),
}));

vi.mock('@/stores/auth-store', () => ({
  useAuthStore: vi.fn(),
}));

vi.mock('@/services/retailer-api', () => ({
  retailerApi: {
    getDashboard: vi.fn(),
  },
}));

vi.mock('@tanstack/react-query', () => ({
  useQuery: vi.fn(),
}));

describe('RetailerDashboard', () => {
  const mockNavigate = vi.fn();
  const mockLoadRetailer = vi.fn();
  const mockLoadDashboard = vi.fn();

  const mockUser = {
    id: 1,
    email: 'retailer@example.com',
    username: 'retailer',
  };

  const mockRetailer = {
    id: 1,
    storeName: 'Test Store',
    status: 'ACTIVE',
    verified: true,
  };

  const mockDashboardData = {
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
  };

  beforeEach(() => {
    vi.clearAllMocks();
    (useNavigate as any).mockReturnValue(mockNavigate);
    (useAuthStore as any).mockReturnValue({ user: mockUser });
    (useRetailerStore as any).mockReturnValue({
      currentRetailer: mockRetailer,
      loadRetailer: mockLoadRetailer,
      loadDashboard: mockLoadDashboard,
      dashboardData: null,
      isLoadingDashboard: false,
    });
    (useQuery as any).mockReturnValue({
      data: mockDashboardData,
      refetch: vi.fn(),
    });
  });

  it('should render dashboard when user is logged in and retailer is active', async () => {
    render(<RetailerDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/test store dashboard/i)).toBeInTheDocument();
    });

    expect(screen.getByText(/total sales/i)).toBeInTheDocument();
    expect(screen.getByText(/total orders/i)).toBeInTheDocument();
    expect(screen.getByText(/active products/i)).toBeInTheDocument();
    expect(screen.getByText(/commissions/i)).toBeInTheDocument();
  });

  it('should show login message when user is not logged in', () => {
    (useAuthStore as any).mockReturnValue({ user: null });

    render(<RetailerDashboard />);

    expect(screen.getByText(/please log in to view your dashboard/i)).toBeInTheDocument();
  });

  it('should show registration prompt when user has no retailer account', () => {
    (useRetailerStore as any).mockReturnValue({
      currentRetailer: null,
      loadRetailer: mockLoadRetailer,
      loadDashboard: mockLoadDashboard,
      dashboardData: null,
      isLoadingDashboard: false,
    });

    render(<RetailerDashboard />);

    expect(screen.getByText(/you are not registered as a retailer yet/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /register as retailer/i })).toBeInTheDocument();
  });

  it('should show pending status when retailer is pending', () => {
    (useRetailerStore as any).mockReturnValue({
      currentRetailer: { ...mockRetailer, status: 'PENDING' },
      loadRetailer: mockLoadRetailer,
      loadDashboard: mockLoadDashboard,
      dashboardData: null,
      isLoadingDashboard: false,
    });

    render(<RetailerDashboard />);

    expect(screen.getByText(/registration pending/i)).toBeInTheDocument();
    expect(screen.getByText(/your retailer registration is being reviewed/i)).toBeInTheDocument();
  });

  it('should show rejected status when retailer is rejected', () => {
    (useRetailerStore as any).mockReturnValue({
      currentRetailer: { ...mockRetailer, status: 'REJECTED' },
      loadRetailer: mockLoadRetailer,
      loadDashboard: mockLoadDashboard,
      dashboardData: null,
      isLoadingDashboard: false,
    });

    render(<RetailerDashboard />);

    expect(screen.getByText(/registration rejected/i)).toBeInTheDocument();
    expect(screen.getByText(/your retailer registration has been rejected/i)).toBeInTheDocument();
  });

  it('should show loading state when dashboard is loading', () => {
    (useRetailerStore as any).mockReturnValue({
      currentRetailer: mockRetailer,
      loadRetailer: mockLoadRetailer,
      loadDashboard: mockLoadDashboard,
      dashboardData: null,
      isLoadingDashboard: true,
    });
    (useQuery as any).mockReturnValue({
      data: null,
      refetch: vi.fn(),
    });

    render(<RetailerDashboard />);

    expect(screen.getByText(/loading dashboard/i)).toBeInTheDocument();
  });

  it('should display dashboard metrics correctly', async () => {
    render(<RetailerDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/\$50,000/i)).toBeInTheDocument(); // Total sales
      expect(screen.getByText(/100/i)).toBeInTheDocument(); // Total orders
      expect(screen.getByText(/25/i)).toBeInTheDocument(); // Active products
      expect(screen.getByText(/\$5,000/i)).toBeInTheDocument(); // Total commissions
    });
  });

  it('should display recent orders', async () => {
    render(<RetailerDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/recent orders/i)).toBeInTheDocument();
      expect(screen.getByText(/ord-001/i)).toBeInTheDocument();
    });
  });

  it('should display top products', async () => {
    render(<RetailerDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/top products/i)).toBeInTheDocument();
      expect(screen.getByText(/test product/i)).toBeInTheDocument();
    });
  });

  it('should navigate to products page when button is clicked', async () => {
    const user = userEvent.setup();
    render(<RetailerDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/test store dashboard/i)).toBeInTheDocument();
    });

    const manageProductsButton = screen.getByRole('button', { name: /manage products/i });
    await user.click(manageProductsButton);

    expect(mockNavigate).toHaveBeenCalledWith('/retailer/products');
  });

  it('should navigate to orders page when button is clicked', async () => {
    const user = userEvent.setup();
    render(<RetailerDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/test store dashboard/i)).toBeInTheDocument();
    });

    const viewOrdersButton = screen.getByRole('button', { name: /view orders/i });
    await user.click(viewOrdersButton);

    expect(mockNavigate).toHaveBeenCalledWith('/retailer/orders');
  });

  it('should show low stock warning when applicable', async () => {
    render(<RetailerDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/3 low stock/i)).toBeInTheDocument();
    });
  });

  it('should display this month sales and orders', async () => {
    render(<RetailerDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/this month: \$5,000/i)).toBeInTheDocument();
      expect(screen.getByText(/this month: 10/i)).toBeInTheDocument();
    });
  });

  it('should handle empty dashboard data gracefully', () => {
    (useRetailerStore as any).mockReturnValue({
      currentRetailer: mockRetailer,
      loadRetailer: mockLoadRetailer,
      loadDashboard: mockLoadDashboard,
      dashboardData: null,
      isLoadingDashboard: false,
    });
    (useQuery as any).mockReturnValue({
      data: null,
      refetch: vi.fn(),
    });

    render(<RetailerDashboard />);

    expect(screen.getByText(/loading dashboard/i)).toBeInTheDocument();
  });
});

