import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import type { ProductResponse } from '@/types';
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { formatPrice } from '@/lib/utils';
import { ShoppingCart, Heart } from 'lucide-react';
import { useState } from 'react';

interface ProductCardProps {
  product: ProductResponse;
  index?: number;
  onAddToCart?: (productId: number) => void;
  onAddToWishlist?: (productId: number) => void;
}

/**
 * ProductCard component for displaying individual product cards
 */
export function ProductCard({ product, index = 0, onAddToCart, onAddToWishlist }: ProductCardProps) {
  const [isWishlisted, setIsWishlisted] = useState(false);

  // Parse images if it's a string (JSON or comma-separated)
  const parseImages = (images?: string): string[] => {
    if (!images) return [];
    try {
      const parsed = JSON.parse(images);
      return Array.isArray(parsed) ? parsed : [parsed];
    } catch {
      // If not JSON, treat as comma-separated string
      return images.split(',').map(img => img.trim()).filter(Boolean);
    }
  };

  const images = parseImages(product.images);
  const mainImage = images[0] || '/placeholder.png';
  const price = product.salePrice || product.price || 0;
  const originalPrice = product.salePrice ? product.price : undefined;

  const handleAddToCart = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    onAddToCart?.(product.id);
  };

  const handleAddToWishlist = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsWishlisted(!isWishlisted);
    onAddToWishlist?.(product.id);
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true }}
      transition={{ delay: index * 0.05 }}
    >
      <Card className="group overflow-hidden hover:shadow-lg transition-shadow h-full flex flex-col">
        <Link to={`/products/${product.id}`} className="flex-1 flex flex-col">
          <div className="relative aspect-square overflow-hidden bg-muted">
            <img
              src={mainImage}
              alt={product.name}
              className="object-cover w-full h-full group-hover:scale-105 transition-transform duration-300"
              loading="lazy"
            />
            {product.isFeatured && (
              <div className="absolute top-2 left-2 bg-primary text-primary-foreground text-xs font-semibold px-2 py-1 rounded">
                Featured
              </div>
            )}
            {originalPrice && (
              <div className="absolute top-2 right-2 bg-destructive text-destructive-foreground text-xs font-semibold px-2 py-1 rounded">
                Sale
              </div>
            )}
          </div>
        </Link>
        
        <CardHeader className="flex-1">
          <Link to={`/products/${product.id}`}>
            <h3 className="font-semibold line-clamp-2 group-hover:text-primary transition-colors">
              {product.name}
            </h3>
          </Link>
          {product.brandName && (
            <p className="text-sm text-muted-foreground">{product.brandName}</p>
          )}
        </CardHeader>
        
        <CardContent className="flex-1">
          <div className="flex items-center gap-2 mb-2">
            <span className="text-2xl font-bold">
              {formatPrice(price)}
            </span>
            {originalPrice && (
              <span className="text-sm text-muted-foreground line-through">
                {formatPrice(originalPrice)}
              </span>
            )}
          </div>
          
          {product.averageRating && (
            <div className="flex items-center gap-1 text-sm mb-2">
              <span className="text-yellow-500">★</span>
              <span className="font-medium">{product.averageRating.toFixed(1)}</span>
              {product.reviewCount && (
                <span className="text-muted-foreground">({product.reviewCount})</span>
              )}
            </div>
          )}
          
          {product.quantity !== undefined && product.quantity <= 10 && product.quantity > 0 && (
            <p className="text-sm text-destructive">Only {product.quantity} left!</p>
          )}
        </CardContent>
        
        <CardFooter className="flex gap-2 pt-4">
          <Button className="flex-1" asChild>
            <Link to={`/products/${product.id}`}>View Details</Link>
          </Button>
          <Button 
            variant="outline" 
            size="icon"
            onClick={handleAddToCart}
            title="Add to cart"
          >
            <ShoppingCart className="h-4 w-4" />
          </Button>
          <Button 
            variant="outline" 
            size="icon"
            onClick={handleAddToWishlist}
            title="Add to wishlist"
            className={isWishlisted ? 'text-destructive' : ''}
          >
            <Heart className={`h-4 w-4 ${isWishlisted ? 'fill-current' : ''}`} />
          </Button>
        </CardFooter>
      </Card>
    </motion.div>
  );
}
