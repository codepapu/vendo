import { useQuery } from '@tanstack/react-query';
import { useParams, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Store, Star, Package, MapPin, Verified, ArrowLeft } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Badge } from '@/components/ui/badge';
import { retailerApi } from '@/services/retailer-api';
import { ProductGrid } from '@/components/product-grid';

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.5,
    },
  },
};

interface StoreDetailData {
  id: number;
  storeName: string;
  storeSlug: string;
  description?: string;
  logoUrl?: string;
  rating?: number;
  reviewCount?: number;
  totalSales?: number;
  location?: string;
  verified?: boolean;
}

export function StoreDetail() {
  const { slug } = useParams<{ slug: string }>();

  const { data: store, isLoading: isLoadingStore } = useQuery<StoreDetailData | null>({
    queryKey: ['store', slug],
    queryFn: async () => {
      if (!slug) return null;
      // TODO: Implement getStoreBySlug API method
      // For now, return null
      return null;
    },
    enabled: !!slug,
  });

  const { data: products, isLoading: isLoadingProducts } = useQuery({
    queryKey: ['store-products', slug],
    queryFn: async () => {
      if (!slug) return null;
      // TODO: Implement getStoreProducts API method
      return null;
    },
    enabled: !!slug,
  });

  if (isLoadingStore) {
    return (
      <div className="flex flex-col min-h-screen">
        <div className="container py-8">
          <Skeleton className="h-64 w-full mb-8" />
          <div className="space-y-4">
            <Skeleton className="h-8 w-1/3" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-2/3" />
          </div>
        </div>
      </div>
    );
  }

  if (!store) {
    return (
      <div className="flex flex-col min-h-screen items-center justify-center">
        <Card className="max-w-md">
          <CardContent className="py-12 text-center">
            <Store className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <h2 className="text-2xl font-bold mb-2">Store Not Found</h2>
            <p className="text-muted-foreground mb-4">
              The store you're looking for doesn't exist or has been removed.
            </p>
            <Button asChild>
              <Link to="/stores">Browse All Stores</Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="flex flex-col min-h-screen">
      {/* Store Header */}
      <section className="relative overflow-hidden bg-gradient-to-br from-primary/10 via-background to-background py-12">
        <div className="container">
          <motion.div
            initial="hidden"
            animate="visible"
            variants={itemVariants}
            className="flex items-start gap-6"
          >
            <Button variant="ghost" size="sm" asChild className="absolute top-4 left-4">
              <Link to="/stores">
                <ArrowLeft className="mr-2 h-4 w-4" />
                Back to Stores
              </Link>
            </Button>

            <div className="w-24 h-24 md:w-32 md:h-32 rounded-lg bg-muted flex items-center justify-center flex-shrink-0 mt-8">
              {store.logoUrl ? (
                <img
                  src={store.logoUrl}
                  alt={store.storeName}
                  className="w-full h-full object-cover rounded-lg"
                />
              ) : (
                <Store className="h-12 w-12 text-muted-foreground" />
              )}
            </div>

            <div className="flex-1 pt-8">
              <div className="flex items-center gap-3 mb-2">
                <h1 className="text-3xl md:text-4xl font-bold">{store.storeName}</h1>
                {store.verified && (
                  <Badge variant="default" className="gap-1">
                    <Verified className="h-3 w-3" />
                    Verified
                  </Badge>
                )}
              </div>

              {store.rating && (
                <div className="flex items-center gap-2 mb-4">
                  <Star className="h-5 w-5 fill-yellow-400 text-yellow-400" />
                  <span className="font-medium">{store.rating.toFixed(1)}</span>
                  <span className="text-muted-foreground">({store.reviewCount || 0} reviews)</span>
                </div>
              )}

              {store.description && (
                <p className="text-muted-foreground mb-4 max-w-2xl">
                  {store.description}
                </p>
              )}

              <div className="flex flex-wrap gap-4 text-sm text-muted-foreground">
                {store.totalSales && (
                  <div className="flex items-center gap-2">
                    <Package className="h-4 w-4" />
                    <span>{store.totalSales.toLocaleString()} products sold</span>
                  </div>
                )}
                {store.location && (
                  <div className="flex items-center gap-2">
                    <MapPin className="h-4 w-4" />
                    <span>{store.location}</span>
                  </div>
                )}
              </div>
            </div>
          </motion.div>
        </div>
      </section>

      {/* Store Products */}
      <section className="py-16 bg-background">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
          >
            <h2 className="text-2xl font-bold mb-6">Products from {store.storeName}</h2>
            {isLoadingProducts ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {[...Array(8)].map((_, i) => (
                  <Card key={i}>
                    <Skeleton className="h-48 w-full" />
                    <CardHeader>
                      <Skeleton className="h-4 w-3/4" />
                      <Skeleton className="h-4 w-1/2" />
                    </CardHeader>
                  </Card>
                ))}
              </div>
            ) : products && products.length > 0 ? (
              <ProductGrid products={products} />
            ) : (
              <Card>
                <CardContent className="py-12 text-center">
                  <Package className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">
                    This store doesn't have any products available yet.
                  </p>
                </CardContent>
              </Card>
            )}
          </motion.div>
        </div>
      </section>
    </div>
  );
}

