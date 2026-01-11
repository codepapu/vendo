import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@/test/utils/test-utils';
import userEvent from '@testing-library/user-event';
import { ProductCard } from '../product-card';
import type { ProductResponse } from '@/types';

const mockProduct: ProductResponse = {
  id: 1,
  name: 'Test Product',
  description: 'Test Description',
  price: 99.99,
  salePrice: 79.99,
  sku: 'TEST-001',
  status: 'published',
  quantity: 10,
  images: JSON.stringify(['/test-image.jpg']),
  averageRating: 4.5,
  reviewCount: 100,
  brandName: 'Test Brand',
  categoryNames: ['Test Category'],
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
};

describe('ProductCard', () => {
  const mockAddToCart = vi.fn();
  const mockAddToWishlist = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render product card with all information', () => {
    render(
      <ProductCard
        product={mockProduct}
        onAddToCart={mockAddToCart}
        onAddToWishlist={mockAddToWishlist}
      />
    );

    expect(screen.getByText('Test Product')).toBeInTheDocument();
    expect(screen.getByText('Test Brand')).toBeInTheDocument();
    expect(screen.getByText(/79\.99/)).toBeInTheDocument(); // Sale price
    expect(screen.getByText(/99\.99/)).toBeInTheDocument(); // Original price (strikethrough)
    expect(screen.getByText(/4\.5/)).toBeInTheDocument(); // Rating
    expect(screen.getByText(/100/)).toBeInTheDocument(); // Review count
  });

  it('should display sale price when available', () => {
    render(<ProductCard product={mockProduct} />);

    const salePrice = screen.getByText(/79\.99/);
    expect(salePrice).toBeInTheDocument();
  });

  it('should display original price with strikethrough when sale price exists', () => {
    render(<ProductCard product={mockProduct} />);

    const originalPrice = screen.getByText(/99\.99/);
    expect(originalPrice).toHaveClass('line-through');
  });

  it('should display low stock warning when quantity is low', () => {
    const lowStockProduct = { ...mockProduct, quantity: 5 };
    render(<ProductCard product={lowStockProduct} />);

    expect(screen.getByText(/Only 5 left!/)).toBeInTheDocument();
  });

  it('should call onAddToCart when add to cart button is clicked', async () => {
    const user = userEvent.setup();
    render(
      <ProductCard
        product={mockProduct}
        onAddToCart={mockAddToCart}
        onAddToWishlist={mockAddToWishlist}
      />
    );

    const addToCartButton = screen.getByTitle('Add to cart');
    await user.click(addToCartButton);

    expect(mockAddToCart).toHaveBeenCalledTimes(1);
    expect(mockAddToCart).toHaveBeenCalledWith(mockProduct.id);
  });

  it('should call onAddToWishlist when wishlist button is clicked', async () => {
    const user = userEvent.setup();
    render(
      <ProductCard
        product={mockProduct}
        onAddToCart={mockAddToCart}
        onAddToWishlist={mockAddToWishlist}
      />
    );

    const wishlistButton = screen.getByTitle('Add to wishlist');
    await user.click(wishlistButton);

    expect(mockAddToWishlist).toHaveBeenCalledTimes(1);
    expect(mockAddToWishlist).toHaveBeenCalledWith(mockProduct.id);
  });

  it('should render link to product detail page', () => {
    render(<ProductCard product={mockProduct} />);

    const viewDetailsLink = screen.getByRole('link', { name: /view details/i });
    expect(viewDetailsLink).toHaveAttribute('href', '/products/1');
  });

  it('should handle product without images', () => {
    const productWithoutImages = { ...mockProduct, images: undefined };
    render(<ProductCard product={productWithoutImages} />);

    const image = screen.getByRole('img');
    expect(image).toHaveAttribute('src', '/placeholder.png');
  });

  it('should handle product without sale price', () => {
    const productWithoutSale = { ...mockProduct, salePrice: undefined };
    render(<ProductCard product={productWithoutSale} />);

    expect(screen.getByText(/99\.99/)).toBeInTheDocument();
    expect(screen.queryByText(/79\.99/)).not.toBeInTheDocument();
  });

  it('should handle product without rating', () => {
    const productWithoutRating = {
      ...mockProduct,
      averageRating: undefined,
      reviewCount: undefined,
    };
    render(<ProductCard product={productWithoutRating} />);

    expect(screen.queryByText(/4\.5/)).not.toBeInTheDocument();
  });
});

