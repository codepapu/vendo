import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Search as SearchIcon, Filter, X, Package } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Slider } from '@/components/ui/slider';
import { Badge } from '@/components/ui/badge';
import { searchApi } from '@/services/search-api';
import { ProductGrid } from '@/components/product-grid';
import { Skeleton } from '@/components/ui/skeleton';

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
  const navigate = useNavigate();
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

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
      opacity: 1,
      y: 0,
      transition: { duration: 0.5 },
    },
  };

  return (
    <div className="flex flex-col min-h-screen">
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-br from-primary/10 via-background to-background py-20">
        <div className="container">
          <motion.div
            initial="hidden"
            animate="visible"
            variants={itemVariants}
            className="max-w-3xl mx-auto text-center space-y-6"
          >
            <SearchIcon className="h-16 w-16 text-primary mx-auto" />
            <h1 className="text-4xl md:text-6xl font-bold tracking-tight">
              Search <span className="bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">Products</span>
            </h1>
            <p className="text-xl text-muted-foreground">
              Find exactly what you're looking for
            </p>
          </motion.div>
        </div>
      </section>

      {/* Search Section */}
      <section className="py-8 bg-background border-b">
        <div className="container max-w-6xl">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
            className="space-y-4"
          >
            <div className="relative max-w-2xl mx-auto">
              <SearchIcon className="absolute left-4 top-1/2 transform -translate-y-1/2 h-5 w-5 text-muted-foreground" />
              <Input
                type="text"
                placeholder="Search products, brands, categories..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                className="pl-12 pr-24 h-14 text-lg"
              />
              <Button 
                onClick={handleSearch} 
                size="lg"
                className="absolute right-2 top-1/2 -translate-y-1/2"
              >
                Search
              </Button>
              
              {suggestions.length > 0 && (
                <Card className="absolute z-10 w-full mt-2 shadow-lg">
                  <CardContent className="p-2">
                    {suggestions.map((suggestion, idx) => (
                      <div
                        key={idx}
                        className="p-2 hover:bg-muted rounded cursor-pointer transition-colors"
                        onClick={() => {
                          setQuery(suggestion);
                          handleSearch();
                        }}
                      >
                        {suggestion}
                      </div>
                    ))}
                  </CardContent>
                </Card>
              )}
            </div>
          </motion.div>
        </div>
      </section>

      {/* Results Section */}
      <section className="py-16 bg-background">
        <div className="container max-w-6xl">
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
            {/* Filters Sidebar */}
            <div className="lg:col-span-1">
              <Card className="sticky top-24">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Filter className="h-5 w-5" />
                    Filters
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-6">
                  <div>
                    <label className="text-sm font-medium mb-2 block">Category</label>
                    <select
                      value={category}
                      onChange={(e) => setCategory(e.target.value)}
                      className="w-full p-2 border rounded bg-background"
                    >
                      <option value="">All Categories</option>
                      <option value="electronics">Electronics</option>
                      <option value="fashion">Fashion</option>
                      <option value="home">Home & Garden</option>
                      <option value="sports">Sports</option>
                      <option value="books">Books</option>
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

                  {(category || priceRange[0] > 0 || priceRange[1] < 1000) && (
                    <Button
                      variant="outline"
                      className="w-full"
                      onClick={() => {
                        setCategory('');
                        setPriceRange([0, 1000]);
                      }}
                    >
                      <X className="mr-2 h-4 w-4" />
                      Clear Filters
                    </Button>
                  )}
                </CardContent>
              </Card>
            </div>

            {/* Results */}
            <div className="lg:col-span-3">
              {isLoading ? (
                <div className="space-y-4">
                  <Skeleton className="h-8 w-48" />
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {[...Array(6)].map((_, i) => (
                      <Card key={i}>
                        <Skeleton className="h-48 w-full" />
                        <CardHeader>
                          <Skeleton className="h-6 w-3/4" />
                          <Skeleton className="h-4 w-full" />
                        </CardHeader>
                      </Card>
                    ))}
                  </div>
                </div>
              ) : searchResults && searchResults.totalResults > 0 ? (
                <motion.div
                  initial="hidden"
                  animate="visible"
                  variants={itemVariants}
                  className="space-y-6"
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <h2 className="text-2xl font-bold">
                        Search Results
                      </h2>
                      <p className="text-muted-foreground mt-1">
                        Found <strong>{searchResults.totalResults}</strong> {searchResults.totalResults === 1 ? 'result' : 'results'}
                        {query && (
                          <> for "<strong>{query}</strong>"</>
                        )}
                      </p>
                    </div>
                    {category && (
                      <Badge variant="secondary">
                        {category}
                      </Badge>
                    )}
                  </div>
                  <ProductGrid
                    products={(searchResults.products || []).map((p) => ({
                      id: p.id,
                      name: p.name,
                      description: p.description,
                      images: p.imageUrl,
                      price: p.price,
                      salePrice: p.salePrice,
                      averageRating: p.rating,
                      reviewCount: p.reviewCount,
                      brandName: p.brand,
                      categoryNames: p.category ? [p.category] : [],
                      status: 'PUBLISHED',
                      sku: p.slug,
                      isFeatured: false,
                      isVariation: false,
                      saleType: 0,
                      createdAt: '',
                      updatedAt: '',
                    }))}
                  />
                </motion.div>
              ) : query ? (
                <Card>
                  <CardContent className="py-12 text-center">
                    <Package className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <h3 className="text-xl font-semibold mb-2">No results found</h3>
                    <p className="text-muted-foreground mb-4">
                      We couldn't find any products matching "{query}"
                    </p>
                    <div className="flex flex-col sm:flex-row gap-2 justify-center">
                      <Button variant="outline" onClick={() => setQuery('')}>
                        Clear Search
                      </Button>
                      <Button onClick={() => navigate('/products')}>
                        Browse All Products
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ) : (
                <Card>
                  <CardContent className="py-12 text-center">
                    <SearchIcon className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <h3 className="text-xl font-semibold mb-2">Start Your Search</h3>
                    <p className="text-muted-foreground">
                      Enter a search query above to find products, brands, or categories
                    </p>
                  </CardContent>
                </Card>
              )}
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

