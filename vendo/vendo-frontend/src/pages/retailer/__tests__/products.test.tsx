import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@/test/utils/test-utils';
import userEvent from '@testing-library/user-event';
import { useRetailerStore } from '@/stores/retailer-store';
import { useAuthStore } from '@/stores/auth-store';
import { retailerApi } from '@/services/retailer-api';
import { productApi } from '@/services/product-api';
import { useQuery } from '@tanstack/react-query';
import RetailerProducts from '../products';

// Mock dependencies
vi.mock('@/stores/retailer-store', () => ({
  useRetailerStore: vi.fn(),
}));

vi.mock('@/stores/auth-store', () => ({
  useAuthStore: vi.fn(),
}));

vi.mock('@/services/retailer-api', () => ({
  retailerApi: {
    addProduct: vi.fn(),
  },
}));

vi.mock('@/services/product-api', () => ({
  productApi: {
    getAllProducts: vi.fn(),
  },
}));

vi.mock('@tanstack/react-query', () => ({
  useQuery: vi.fn(),
}));

describe('ProductManagement (RetailerProducts)', () => {
  const mockUpdateProduct = vi.fn();
  const mockLoadProducts = vi.fn();

  const mockUser = {
    id: 1,
    email: 'retailer@example.com',
  };

  const mockRetailer = {
    id: 1,
    storeName: 'Test Store',
    status: 'ACTIVE',
  };

  const mockProducts = [
    {
      id: 1,
      product: {
        id: 1,
        name: 'Test Product 1',
        sku: 'TEST-001',
      },
      price: 100,
      stockQuantity: 50,
      isActive: true,
      isLowStock: false,
      lowStockThreshold: 10,
    },
    {
      id: 2,
      product: {
        id: 2,
        name: 'Test Product 2',
        sku: 'TEST-002',
      },
      price: 200,
      stockQuantity: 5,
      isActive: true,
      isLowStock: true,
      lowStockThreshold: 10,
    },
  ];

  const mockAvailableProducts = {
    content: [
      {
        id: 1,
        name: 'Available Product 1',
        sku: 'AVAIL-001',
        price: 100,
        salePrice: 90,
        status: 'PUBLISHED',
      },
      {
        id: 2,
        name: 'Available Product 2',
        sku: 'AVAIL-002',
        price: 200,
        status: 'PUBLISHED',
      },
    ],
  };

  beforeEach(() => {
    vi.clearAllMocks();
    (useAuthStore as any).mockReturnValue({ user: mockUser });
    (useRetailerStore as any).mockReturnValue({
      currentRetailer: mockRetailer,
      products: mockProducts,
      loadProducts: mockLoadProducts,
      updateProduct: mockUpdateProduct,
      isLoadingProducts: false,
    });
    (useQuery as any).mockReturnValue({
      data: mockAvailableProducts,
    });
  });

  it('should render product management page', () => {
    render(<RetailerProducts />);

    expect(screen.getByText(/product management/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /add product/i })).toBeInTheDocument();
  });

  it('should show message when retailer is not registered', () => {
    (useRetailerStore as any).mockReturnValue({
      currentRetailer: null,
      products: [],
      loadProducts: mockLoadProducts,
      updateProduct: mockUpdateProduct,
      isLoadingProducts: false,
    });

    render(<RetailerProducts />);

    expect(screen.getByText(/you are not registered as a retailer yet/i)).toBeInTheDocument();
  });

  it('should display list of products', () => {
    render(<RetailerProducts />);

    expect(screen.getByText(/test product 1/i)).toBeInTheDocument();
    expect(screen.getByText(/test product 2/i)).toBeInTheDocument();
    expect(screen.getByText(/test-001/i)).toBeInTheDocument();
    expect(screen.getByText(/test-002/i)).toBeInTheDocument();
  });

  it('should show product details correctly', () => {
    render(<RetailerProducts />);

    expect(screen.getByText(/\$100.00/i)).toBeInTheDocument();
    expect(screen.getByText(/50 units/i)).toBeInTheDocument();
    expect(screen.getByText(/5 units/i)).toBeInTheDocument();
  });

  it('should show low stock badge for low stock products', () => {
    render(<RetailerProducts />);

    expect(screen.getByText(/low stock/i)).toBeInTheDocument();
  });

  it('should show active/inactive badges', () => {
    render(<RetailerProducts />);

    const activeBadges = screen.getAllByText(/active/i);
    expect(activeBadges.length).toBeGreaterThan(0);
  });

  it('should open add product dialog', async () => {
    const user = userEvent.setup();
    render(<RetailerProducts />);

    const addButton = screen.getByRole('button', { name: /add product/i });
    await user.click(addButton);

    await waitFor(() => {
      expect(screen.getByText(/add product to store/i)).toBeInTheDocument();
      expect(screen.getByText(/select product/i)).toBeInTheDocument();
    });
  });

  it('should display available products in dialog', async () => {
    const user = userEvent.setup();
    render(<RetailerProducts />);

    const addButton = screen.getByRole('button', { name: /add product/i });
    await user.click(addButton);

    await waitFor(() => {
      expect(screen.getByText(/available product 1/i)).toBeInTheDocument();
      expect(screen.getByText(/available product 2/i)).toBeInTheDocument();
    });
  });

  it('should allow selecting a product', async () => {
    const user = userEvent.setup();
    render(<RetailerProducts />);

    const addButton = screen.getByRole('button', { name: /add product/i });
    await user.click(addButton);

    await waitFor(() => {
      expect(screen.getByText(/available product 1/i)).toBeInTheDocument();
    });

    const productItem = screen.getByText(/available product 1/i).closest('div');
    if (productItem) {
      await user.click(productItem);
    }

    await waitFor(() => {
      expect(screen.getByLabelText(/price/i)).toBeInTheDocument();
    });
  });

  it('should allow adding a product with valid data', async () => {
    const user = userEvent.setup();
    (retailerApi.addProduct as any).mockResolvedValue({
      id: 3,
      productId: 1,
      retailerId: 1,
      price: 100,
      stockQuantity: 50,
      isActive: true,
    });

    render(<RetailerProducts />);

    const addButton = screen.getByRole('button', { name: /add product/i });
    await user.click(addButton);

    await waitFor(() => {
      expect(screen.getByText(/available product 1/i)).toBeInTheDocument();
    });

    const productItem = screen.getByText(/available product 1/i).closest('div');
    if (productItem) {
      await user.click(productItem);
    }

    await waitFor(() => {
      const priceInput = screen.getByLabelText(/price/i);
      await user.clear(priceInput);
      await user.type(priceInput, '150');
    });

    const addProductButton = screen.getByRole('button', { name: /add product/i });
    await user.click(addProductButton);

    await waitFor(() => {
      expect(retailerApi.addProduct).toHaveBeenCalledWith(
        1,
        expect.objectContaining({
          productId: 1,
          retailerId: 1,
          price: 150,
        })
      );
    });
  });

  it('should filter products by search term', async () => {
    const user = userEvent.setup();
    render(<RetailerProducts />);

    const searchInput = screen.getByPlaceholderText(/search products/i);
    await user.type(searchInput, 'Test Product 1');

    await waitFor(() => {
      expect(mockLoadProducts).toHaveBeenCalledWith(
        1,
        expect.objectContaining({
          searchTerm: 'Test Product 1',
        })
      );
    });
  });

  it('should filter products by active status', async () => {
    const user = userEvent.setup();
    render(<RetailerProducts />);

    const activeButton = screen.getByRole('button', { name: /active/i });
    await user.click(activeButton);

    await waitFor(() => {
      expect(mockLoadProducts).toHaveBeenCalledWith(
        1,
        expect.objectContaining({
          isActive: true,
        })
      );
    });
  });

  it('should allow updating product price', async () => {
    const user = userEvent.setup();
    window.prompt = vi.fn().mockReturnValue('120');

    render(<RetailerProducts />);

    const editPriceButtons = screen.getAllByRole('button', { name: /edit price/i });
    await user.click(editPriceButtons[0]);

    await waitFor(() => {
      expect(mockUpdateProduct).toHaveBeenCalledWith(
        1,
        1,
        expect.objectContaining({
          price: 120,
        })
      );
    });
  });

  it('should allow updating product stock', async () => {
    const user = userEvent.setup();
    window.prompt = vi.fn().mockReturnValue('75');

    render(<RetailerProducts />);

    const updateStockButtons = screen.getAllByRole('button', { name: /update stock/i });
    await user.click(updateStockButtons[0]);

    await waitFor(() => {
      expect(mockUpdateProduct).toHaveBeenCalledWith(
        1,
        1,
        expect.objectContaining({
          stockQuantity: 75,
        })
      );
    });
  });

  it('should allow toggling product active status', async () => {
    const user = userEvent.setup();
    render(<RetailerProducts />);

    const deactivateButtons = screen.getAllByRole('button', { name: /deactivate/i });
    await user.click(deactivateButtons[0]);

    await waitFor(() => {
      expect(mockUpdateProduct).toHaveBeenCalledWith(
        1,
        1,
        expect.objectContaining({
          isActive: false,
        })
      );
    });
  });

  it('should show loading state', () => {
    (useRetailerStore as any).mockReturnValue({
      currentRetailer: mockRetailer,
      products: [],
      loadProducts: mockLoadProducts,
      updateProduct: mockUpdateProduct,
      isLoadingProducts: true,
    });

    render(<RetailerProducts />);

    expect(screen.getByText(/loading products/i)).toBeInTheDocument();
  });

  it('should show empty state when no products', () => {
    (useRetailerStore as any).mockReturnValue({
      currentRetailer: mockRetailer,
      products: [],
      loadProducts: mockLoadProducts,
      updateProduct: mockUpdateProduct,
      isLoadingProducts: false,
    });

    render(<RetailerProducts />);

    expect(screen.getByText(/no products found/i)).toBeInTheDocument();
  });

  it('should validate required fields in add product form', async () => {
    const user = userEvent.setup();
    render(<RetailerProducts />);

    const addButton = screen.getByRole('button', { name: /add product/i });
    await user.click(addButton);

    await waitFor(() => {
      expect(screen.getByText(/available product 1/i)).toBeInTheDocument();
    });

    const productItem = screen.getByText(/available product 1/i).closest('div');
    if (productItem) {
      await user.click(productItem);
    }

    await waitFor(() => {
      const addProductButton = screen.getAllByRole('button', { name: /add product/i });
      // The submit button should be disabled if price is not set
      expect(addProductButton[addProductButton.length - 1]).toBeDisabled();
    });
  });

  it('should close dialog on cancel', async () => {
    const user = userEvent.setup();
    render(<RetailerProducts />);

    const addButton = screen.getByRole('button', { name: /add product/i });
    await user.click(addButton);

    await waitFor(() => {
      expect(screen.getByText(/add product to store/i)).toBeInTheDocument();
    });

    const cancelButton = screen.getByRole('button', { name: /cancel/i });
    await user.click(cancelButton);

    await waitFor(() => {
      expect(screen.queryByText(/add product to store/i)).not.toBeInTheDocument();
    });
  });
});

