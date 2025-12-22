import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { productsApi } from '@/lib/api';
import { Product } from '@vendo/shared';
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { formatPrice } from '@/lib/utils';
import { ShoppingCart } from 'lucide-react';

interface ProductGridProps {
  limit?: number;
  categoryId?: number;
}

export function ProductGrid({ limit, categoryId }: ProductGridProps) {
  const { data, isLoading, error } = useQuery({
    queryKey: ['products', { limit, categoryId }],
    queryFn: async () => {
      const response = await productsApi.getProducts({
        categoryId,
        size: limit || 20,
      });
      return response.data;
    },
  });

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        {[...Array(8)].map((_, i) => (
          <Card key={i} className="animate-pulse">
            <div className="h-48 bg-muted" />
            <CardHeader>
              <div className="h-4 bg-muted rounded w-3/4" />
            </CardHeader>
            <CardContent>
              <div className="h-4 bg-muted rounded w-1/2" />
            </CardContent>
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

  if (!data || data.length === 0) {
    return <div className="text-center py-12 text-muted-foreground">No products found.</div>;
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      {data.map((product: Product, index: number) => (
        <motion.div
          key={product.id}
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ delay: index * 0.1 }}
        >
          <Card className="group overflow-hidden hover:shadow-lg transition-shadow">
            <Link to={`/products/${product.id}`}>
              <div className="relative aspect-square overflow-hidden bg-muted">
                {product.images && product.images.length > 0 ? (
                  <img
                    src={product.images[0]}
                    alt={product.name}
                    className="object-cover w-full h-full group-hover:scale-105 transition-transform duration-300"
                    loading="lazy"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-muted-foreground">
                    No Image
                  </div>
                )}
              </div>
            </Link>
            <CardHeader>
              <Link to={`/products/${product.id}`}>
                <h3 className="font-semibold line-clamp-2 group-hover:text-primary transition-colors">
                  {product.name}
                </h3>
              </Link>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <span className="text-2xl font-bold">
                  {formatPrice(product.discountPrice || product.price)}
                </span>
                {product.discountPrice && (
                  <span className="text-sm text-muted-foreground line-through">
                    {formatPrice(product.price)}
                  </span>
                )}
              </div>
              {product.stock <= 10 && product.stock > 0 && (
                <p className="text-sm text-destructive mt-2">Only {product.stock} left!</p>
              )}
            </CardContent>
            <CardFooter className="flex gap-2">
              <Button className="flex-1" asChild>
                <Link to={`/products/${product.id}`}>View Details</Link>
              </Button>
              <Button variant="outline" size="icon">
                <ShoppingCart className="h-4 w-4" />
              </Button>
            </CardFooter>
          </Card>
        </motion.div>
      ))}
    </div>
  );
}
