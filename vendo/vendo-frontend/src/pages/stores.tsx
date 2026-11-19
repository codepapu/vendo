import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import { Store, Star, ArrowRight, Search } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { useState } from 'react';

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

interface StoreItem {
  id: number;
  storeName: string;
  storeSlug: string;
  description?: string;
  logoUrl?: string;
  rating?: number;
  totalSales?: number;
  verified?: boolean;
}

export function Stores() {
  const [searchQuery, setSearchQuery] = useState('');

  const { data: stores, isLoading } = useQuery({
    queryKey: ['stores', 'public'],
    queryFn: async () => {
      // TODO: Implement getPublicStores API method
      // For now, return empty array
      return [] as StoreItem[];
    },
  });

  const filteredStores = stores?.filter((store) =>
    store.storeName.toLowerCase().includes(searchQuery.toLowerCase()) ||
    store.description?.toLowerCase().includes(searchQuery.toLowerCase())
  ) || [];

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
            <Store className="h-16 w-16 text-primary mx-auto" />
            <h1 className="text-4xl md:text-6xl font-bold tracking-tight">
              Our <span className="bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">Stores</span>
            </h1>
            <p className="text-xl text-muted-foreground">
              Discover trusted retailers and shop from verified stores
            </p>
          </motion.div>
        </div>
      </section>

      {/* Search */}
      <section className="py-8 bg-background border-b">
        <div className="container max-w-4xl">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-muted-foreground" />
            <Input
              type="text"
              placeholder="Search stores..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10"
            />
          </div>
        </div>
      </section>

      {/* Stores Grid */}
      <section className="py-16 bg-background">
        <div className="container">
          {isLoading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {[...Array(6)].map((_, i) => (
                <Card key={i}>
                  <CardHeader>
                    <Skeleton className="h-32 w-full rounded-lg mb-4" />
                    <Skeleton className="h-6 w-3/4" />
                    <Skeleton className="h-4 w-full" />
                  </CardHeader>
                  <CardContent>
                    <Skeleton className="h-4 w-1/2" />
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : filteredStores.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredStores.map((store) => (
                <motion.div
                  key={store.id}
                  initial="hidden"
                  whileInView="visible"
                  viewport={{ once: true }}
                  variants={itemVariants}
                  whileHover={{ y: -5 }}
                  transition={{ duration: 0.2 }}
                >
                  <Card className="h-full hover:shadow-lg transition-shadow">
                    <CardHeader>
                      <div className="flex items-start gap-4 mb-4">
                        <div className="w-16 h-16 rounded-lg bg-muted flex items-center justify-center flex-shrink-0">
                          {store.logoUrl ? (
                            <img
                              src={store.logoUrl}
                              alt={store.storeName}
                              className="w-full h-full object-cover rounded-lg"
                            />
                          ) : (
                            <Store className="h-8 w-8 text-muted-foreground" />
                          )}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-1">
                            <CardTitle className="text-lg truncate">{store.storeName}</CardTitle>
                            {store.verified && (
                              <span className="text-primary text-xs font-medium">✓ Verified</span>
                            )}
                          </div>
                          {store.rating && (
                            <div className="flex items-center gap-1">
                              <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                              <span className="text-sm font-medium">{store.rating.toFixed(1)}</span>
                            </div>
                          )}
                        </div>
                      </div>
                      {store.description && (
                        <CardDescription className="line-clamp-2">
                          {store.description}
                        </CardDescription>
                      )}
                    </CardHeader>
                    <CardContent>
                      <div className="flex items-center justify-between">
                        {store.totalSales && (
                          <span className="text-sm text-muted-foreground">
                            {store.totalSales.toLocaleString()} sales
                          </span>
                        )}
                        <Button asChild variant="ghost" size="sm">
                          <Link to={`/stores/${store.storeSlug}`}>
                            Visit Store
                            <ArrowRight className="ml-2 h-4 w-4" />
                          </Link>
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                </motion.div>
              ))}
            </div>
          ) : (
            <Card>
              <CardContent className="py-12 text-center">
                <Store className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-muted-foreground">
                  {searchQuery
                    ? 'No stores found matching your search.'
                    : 'No stores available at the moment.'}
                </p>
              </CardContent>
            </Card>
          )}
        </div>
      </section>
    </div>
  );
}

