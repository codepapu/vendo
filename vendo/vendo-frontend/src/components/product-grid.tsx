import { useQuery } from '@tanstack/react-query';
import type { ProductResponse, PaginatedResponse } from '@/types';
import { productApi } from '@/services/product-api';
import { ProductCard } from './product-card';
import { Card } from '@/components/ui/card';

interface ProductGridProps {
  searchParams?: {
    keyword?: string;
    status?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: 'ASC' | 'DESC';
    categoryId?: number;
    brandId?: number;
    minPrice?: number;
    maxPrice?: number;
  };
  onAddToCart?: (productId: number) => void;
  onAddToWishlist?: (productId: number) => void;
}

/**
 * ProductGrid component for displaying a grid of products
 * Uses TanStack Query for data fetching
 */
export function ProductGrid({ searchParams, onAddToCart, onAddToWishlist }: ProductGridProps) {
  const { data, isLoading, error } = useQuery<PaginatedResponse<ProductResponse> | null>({
    queryKey: ['products', searchParams],
    queryFn: async () => {
      if (searchParams?.keyword) {
        return await productApi.searchProducts({
          keyword: searchParams.keyword,
          status: searchParams.status,
          page: searchParams.page,
          size: searchParams.size,
        });
      }
      
      if (searchParams?.categoryId) {
        return await productApi.getProductsByCategory(
          searchParams.categoryId,
          {
            status: searchParams.status,
            page: searchParams.page,
            size: searchParams.size,
          }
        );
      }
      
      if (searchParams?.brandId) {
        return await productApi.getProductsByBrand(
          searchParams.brandId,
          {
            status: searchParams.status,
            page: searchParams.page,
            size: searchParams.size,
          }
        );
      }
      
      if (searchParams?.minPrice !== undefined || searchParams?.maxPrice !== undefined) {
        return await productApi.getProductsByPriceRange({
          minPrice: searchParams.minPrice,
          maxPrice: searchParams.maxPrice,
          status: searchParams.status,
          page: searchParams.page,
          size: searchParams.size,
        });
      }
      
      return await productApi.getAllProducts({
        status: searchParams?.status,
        page: searchParams?.page,
        size: searchParams?.size,
        sortBy: searchParams?.sortBy,
        sortDir: searchParams?.sortDir,
      });
    },
    enabled: true,
  });

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        {[...Array(8)].map((_, i) => (
          <Card key={i} className="animate-pulse">
            <div className="h-48 bg-muted" />
            <div className="p-4 space-y-2">
              <div className="h-4 bg-muted rounded w-3/4" />
              <div className="h-4 bg-muted rounded w-1/2" />
            </div>
          </Card>
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-12 text-destructive">
        Failed to load products. Please try again later.
      </div>
    );
  }

  if (!data || !data.content || data.content.length === 0) {
    return (
      <div className="text-center py-12 text-muted-foreground">
        No products found.
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      {data.content.map((product, index) => (
        <ProductCard
          key={product.id}
          product={product}
          index={index}
          onAddToCart={onAddToCart}
          onAddToWishlist={onAddToWishlist}
        />
      ))}
    </div>
  );
}
