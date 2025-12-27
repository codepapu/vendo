import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useSearchParams } from 'react-router-dom';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Slider } from '@/components/ui/slider';
import { searchApi } from '@/services/search-api';
import { ProductGrid } from '@/components/product-grid';

// Search analytics tracking
const trackSearchEvent = (event: string, data: any) => {
  // Track search analytics
  if (typeof window !== 'undefined' && (window as any).gtag) {
    (window as any).gtag('event', event, data);
  }
  // Also log to console for development
  console.log('Search Analytics:', event, data);
};

export default function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [query, setQuery] = useState(searchParams.get('q') || '');
  const [category, setCategory] = useState(searchParams.get('category') || '');
  const [priceRange, setPriceRange] = useState<[number, number]>([0, 1000]);
  const [suggestions, setSuggestions] = useState<string[]>([]);

  const { data: searchResults, isLoading } = useQuery({
    queryKey: ['search', query, category, priceRange],
    queryFn: () => searchApi.searchProducts({
      query,
      category: category || undefined,
      minPrice: priceRange[0],
      maxPrice: priceRange[1],
    }),
    enabled: query.length > 0,
  });

  useEffect(() => {
    if (query.length > 2) {
      searchApi.getSuggestions(query).then(setSuggestions);
    } else {
      setSuggestions([]);
    }
  }, [query]);

  const handleSearch = () => {
    setSearchParams({ q: query, category });
    // Track search analytics
    trackSearchEvent('search', {
      search_term: query,
      category: category || 'all',
      price_range: `${priceRange[0]}-${priceRange[1]}`,
    });
  };

  useEffect(() => {
    if (searchResults) {
      // Track search results view
      trackSearchEvent('search_results', {
        query,
        total_results: searchResults.totalResults,
        category: category || 'all',
      });
    }
  }, [searchResults, query, category]);

  return (
    <div className="container mx-auto py-8">
      <div className="mb-6">
        <div className="relative">
          <Input
            type="text"
            placeholder="Search products..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            className="pr-10"
          />
          <Button onClick={handleSearch} className="absolute right-2 top-1/2 -translate-y-1/2">
            Search
          </Button>
          
          {suggestions.length > 0 && (
            <div className="absolute z-10 w-full mt-1 bg-white border rounded shadow-lg">
              {suggestions.map((suggestion, idx) => (
                <div
                  key={idx}
                  className="p-2 hover:bg-gray-100 cursor-pointer"
                  onClick={() => {
                    setQuery(suggestion);
                    handleSearch();
                  }}
                >
                  {suggestion}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <div className="lg:col-span-1">
          <Card>
            <CardContent className="pt-6">
              <h3 className="font-semibold mb-4">Filters</h3>
              
              <div className="space-y-4">
                <div>
                  <label className="text-sm font-medium mb-2 block">Category</label>
                  <select
                    value={category}
                    onChange={(e) => setCategory(e.target.value)}
                    className="w-full p-2 border rounded"
                  >
                    <option value="">All Categories</option>
                    <option value="electronics">Electronics</option>
                    <option value="fashion">Fashion</option>
                    <option value="home">Home</option>
                  </select>
                </div>

                <div>
                  <label className="text-sm font-medium mb-2 block">
                    Price Range: ${priceRange[0]} - ${priceRange[1]}
                  </label>
                  <Slider
                    value={priceRange}
                    onValueChange={(value) => setPriceRange(value as [number, number])}
                    max={1000}
                    step={10}
                    className="w-full"
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="lg:col-span-3">
          {isLoading ? (
            <p>Loading...</p>
          ) : searchResults ? (
            <>
              <p className="mb-4">
                Found {searchResults.totalResults} results
              </p>
              <ProductGrid products={searchResults.products || []} />
            </>
          ) : (
            <p>Enter a search query to get started</p>
          )}
        </div>
      </div>
    </div>
  );
}

