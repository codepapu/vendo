import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { Search, Filter, X, ChevronLeft, ChevronRight } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { ProductGrid } from '@/components/product-grid';
import { useDebounce } from '@/hooks/use-debounce';
import { useAuthStore } from '@/stores/auth-store';
import { wishlistApi } from '@/services/wishlist-api';
import { showSuccessToast } from '@/lib/api';
import { productApi } from '@/services/product-api';
import type { PaginatedResponse, ProductResponse } from '@/types';

/**
 * Products catalog page with search, filters, sorting, and pagination
 */
export function Products() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [showFilters, setShowFilters] = useState(false);
  const { isAuthenticated } = useAuthStore();

  // Get current search parameters
  const keyword = searchParams.get('keyword') || '';
  const status = searchParams.get('status') || '';
  const categoryId = searchParams.get('categoryId') ? Number(searchParams.get('categoryId')) : undefined;
  const brandId = searchParams.get('brandId') ? Number(searchParams.get('brandId')) : undefined;
  const minPrice = searchParams.get('minPrice') ? Number(searchParams.get('minPrice')) : undefined;
  const maxPrice = searchParams.get('maxPrice') ? Number(searchParams.get('maxPrice')) : undefined;
  const sortBy = searchParams.get('sortBy') || 'createdAt';
  const sortDir = (searchParams.get('sortDir') || 'DESC') as 'ASC' | 'DESC';
  const page = Number(searchParams.get('page') || '0');
  const size = Number(searchParams.get('size') || '20');

  const [searchInput, setSearchInput] = useState(keyword);
  const debouncedSearch = useDebounce(searchInput, 500);

  // Update URL when debounced search changes
  useEffect(() => {
    if (debouncedSearch !== keyword) {
      const newParams = new URLSearchParams(searchParams);
      if (debouncedSearch) {
        newParams.set('keyword', debouncedSearch);
      } else {
        newParams.delete('keyword');
      }
      newParams.set('page', '0'); // Reset to first page on search
      setSearchParams(newParams);
    }
  }, [debouncedSearch, keyword, searchParams, setSearchParams]);

  const handleSearch = (value: string) => {
    setSearchInput(value);
  };

  const handleFilterChange = (key: string, value: string | number | undefined) => {
    const newParams = new URLSearchParams(searchParams);
    if (value === undefined || value === '') {
      newParams.delete(key);
    } else {
      newParams.set(key, String(value));
    }
    newParams.set('page', '0'); // Reset to first page on filter change
    setSearchParams(newParams);
  };

  const handleSortChange = (newSortBy: string) => {
    const newParams = new URLSearchParams(searchParams);
    if (newSortBy === sortBy) {
      // Toggle sort direction
      newParams.set('sortDir', sortDir === 'ASC' ? 'DESC' : 'ASC');
    } else {
      newParams.set('sortBy', newSortBy);
      newParams.set('sortDir', 'DESC');
    }
    setSearchParams(newParams);
  };

  const handlePageChange = (newPage: number) => {
    const newParams = new URLSearchParams(searchParams);
    newParams.set('page', String(newPage));
    setSearchParams(newParams);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleAddToWishlist = async (productId: number) => {
    if (!isAuthenticated) {
      showSuccessToast('Please login to add items to wishlist');
      return;
    }
    await wishlistApi.addToWishlist(productId);
  };

  const handleAddToCart = (productId: number) => {
    // TODO: Implement cart functionality
    showSuccessToast('Product added to cart');
  };

  const clearFilters = () => {
    setSearchParams({});
    setSearchInput('');
  };

  const hasActiveFilters = status || categoryId || brandId || minPrice || maxPrice;

  // Fetch products for pagination info
  const searchParamsObj = {
    keyword: debouncedSearch || undefined,
    status: status || undefined,
    page,
    size,
    sortBy,
    sortDir,
    categoryId,
    brandId,
    minPrice,
    maxPrice,
  };

  const { data: paginationData } = useQuery<PaginatedResponse<ProductResponse> | null>({
    queryKey: ['products', 'pagination', searchParamsObj],
    queryFn: async () => {
      if (searchParamsObj.keyword) {
        return await productApi.searchProducts({
          keyword: searchParamsObj.keyword,
          status: searchParamsObj.status,
          page: searchParamsObj.page,
          size: searchParamsObj.size,
        });
      }
      
      if (searchParamsObj.categoryId) {
        return await productApi.getProductsByCategory(
          searchParamsObj.categoryId,
          {
            status: searchParamsObj.status,
            page: searchParamsObj.page,
            size: searchParamsObj.size,
          }
        );
      }
      
      return await productApi.getAllProducts({
        status: searchParamsObj.status,
        page: searchParamsObj.page,
        size: searchParamsObj.size,
        sortBy: searchParamsObj.sortBy,
        sortDir: searchParamsObj.sortDir,
      });
    },
  });

  const totalPages = paginationData?.totalPages || 0;
  const currentPage = paginationData?.number || 0;

  return (
    <div className="container py-8">
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        className="space-y-8"
      >
        {/* Header */}
        <div>
          <h1 className="text-4xl font-bold mb-2">All Products</h1>
          <p className="text-muted-foreground">Discover our complete product catalog</p>
        </div>

        {/* Search and Filter Bar */}
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search products..."
              value={searchInput}
              onChange={(e) => handleSearch(e.target.value)}
              className="pl-10"
            />
          </div>
          <Button
            variant={showFilters ? 'default' : 'outline'}
            onClick={() => setShowFilters(!showFilters)}
          >
            <Filter className="h-4 w-4 mr-2" />
            Filters
            {hasActiveFilters && (
              <span className="ml-2 bg-primary-foreground text-primary rounded-full px-2 py-0.5 text-xs">
                {[status, categoryId, brandId, minPrice, maxPrice].filter(Boolean).length}
              </span>
            )}
          </Button>
          {hasActiveFilters && (
            <Button variant="outline" onClick={clearFilters}>
              <X className="h-4 w-4 mr-2" />
              Clear
            </Button>
          )}
        </div>

        {/* Filters Panel */}
        {showFilters && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="border rounded-lg p-4 space-y-4 bg-card"
          >
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="text-sm font-medium mb-2 block">Status</label>
                <select
                  value={status}
                  onChange={(e) => handleFilterChange('status', e.target.value)}
                  className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                >
                  <option value="">All</option>
                  <option value="published">Published</option>
                  <option value="draft">Draft</option>
                </select>
              </div>
              <div>
                <label className="text-sm font-medium mb-2 block">Min Price</label>
                <Input
                  type="number"
                  placeholder="0"
                  value={minPrice || ''}
                  onChange={(e) => handleFilterChange('minPrice', e.target.value ? Number(e.target.value) : undefined)}
                />
              </div>
              <div>
                <label className="text-sm font-medium mb-2 block">Max Price</label>
                <Input
                  type="number"
                  placeholder="1000"
                  value={maxPrice || ''}
                  onChange={(e) => handleFilterChange('maxPrice', e.target.value ? Number(e.target.value) : undefined)}
                />
              </div>
            </div>
          </motion.div>
        )}

        {/* Sort Bar */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="text-sm text-muted-foreground">Sort by:</span>
            <Button
              variant={sortBy === 'createdAt' ? 'default' : 'ghost'}
              size="sm"
              onClick={() => handleSortChange('createdAt')}
            >
              Newest {sortBy === 'createdAt' && (sortDir === 'ASC' ? '↑' : '↓')}
            </Button>
            <Button
              variant={sortBy === 'price' ? 'default' : 'ghost'}
              size="sm"
              onClick={() => handleSortChange('price')}
            >
              Price {sortBy === 'price' && (sortDir === 'ASC' ? '↑' : '↓')}
            </Button>
            <Button
              variant={sortBy === 'name' ? 'default' : 'ghost'}
              size="sm"
              onClick={() => handleSortChange('name')}
            >
              Name {sortBy === 'name' && (sortDir === 'ASC' ? '↑' : '↓')}
            </Button>
          </div>
          {paginationData && (
            <span className="text-sm text-muted-foreground">
              Showing {paginationData.numberOfElements} of {paginationData.totalElements} products
            </span>
          )}
        </div>

        {/* Product Grid */}
        <ProductGrid
          searchParams={searchParamsObj}
          onAddToCart={handleAddToCart}
          onAddToWishlist={handleAddToWishlist}
        />

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-center gap-2 mt-8">
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 0}
            >
              <ChevronLeft className="h-4 w-4 mr-1" />
              Previous
            </Button>
            
            <div className="flex items-center gap-1">
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                const startPage = Math.max(0, Math.min(totalPages - 5, currentPage - 2));
                const pageNum = startPage + i;
                if (pageNum < 0 || pageNum >= totalPages) return null;
                return (
                  <Button
                    key={pageNum}
                    variant={pageNum === currentPage ? 'default' : 'outline'}
                    size="sm"
                    onClick={() => handlePageChange(pageNum)}
                  >
                    {pageNum + 1}
                  </Button>
                );
              })}
            </div>
            
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage >= totalPages - 1}
            >
              Next
              <ChevronRight className="h-4 w-4 ml-1" />
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
