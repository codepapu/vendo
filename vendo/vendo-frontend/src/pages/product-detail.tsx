import { useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { motion } from 'framer-motion';
import { ShoppingCart, Heart, Star, Truck, Shield, RotateCcw, ChevronLeft, ChevronRight } from 'lucide-react';
import { productApi } from '@/services/product-api';
import { reviewApi } from '@/services/review-api';
import { wishlistApi } from '@/services/wishlist-api';
import { useAuthStore } from '@/stores/auth-store';
import type { ProductResponse, ReviewResponse, ReviewRequest } from '@/types';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { formatPrice } from '@/lib/utils';
import { showSuccessToast } from '@/lib/api';
import { Link } from 'react-router-dom';

/**
 * Product detail page with images, description, reviews, and add to cart
 */
export function ProductDetail() {
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const { isAuthenticated, user } = useAuthStore();
  const [quantity, setQuantity] = useState(1);
  const [selectedImage, setSelectedImage] = useState(0);
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [reviewData, setReviewData] = useState<ReviewRequest>({
    productId: Number(id) || 0,
    rating: 5,
    title: '',
    reviewText: '',
  });

  // Fetch product details
  const { data: product, isLoading, error } = useQuery<ProductResponse | null>({
    queryKey: ['product', id],
    queryFn: async () => {
      if (!id) return null;
      return await productApi.getProductById(Number(id));
    },
    enabled: !!id,
  });

  // Fetch product reviews
  const { data: reviewsData } = useQuery({
    queryKey: ['product-reviews', id],
    queryFn: async () => {
      if (!id) return null;
      return await reviewApi.getProductReviews(Number(id), {
        page: 0,
        size: 10,
      });
    },
    enabled: !!id,
  });

  // Fetch review stats
  const { data: reviewStats } = useQuery({
    queryKey: ['product-review-stats', id],
    queryFn: async () => {
      if (!id) return null;
      return await reviewApi.getReviewStats(Number(id));
    },
    enabled: !!id,
  });

  // Add review mutation
  const addReviewMutation = useMutation({
    mutationFn: (data: ReviewRequest) => reviewApi.addReview(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['product-reviews', id] });
      queryClient.invalidateQueries({ queryKey: ['product-review-stats', id] });
      setShowReviewForm(false);
      setReviewData({
        productId: Number(id) || 0,
        rating: 5,
        title: '',
        reviewText: '',
      });
    },
  });

  const handleAddToCart = () => {
    if (!product) return;
    // TODO: Implement cart functionality
    showSuccessToast(`Added ${quantity} ${product.name} to cart`);
  };

  const handleAddToWishlist = async () => {
    if (!product || !isAuthenticated) {
      showSuccessToast('Please login to add items to wishlist');
      return;
    }
    await wishlistApi.addToWishlist(product.id);
  };

  const handleSubmitReview = () => {
    if (!reviewData.title || !reviewData.reviewText) {
      showSuccessToast('Please fill in all review fields');
      return;
    }
    addReviewMutation.mutate(reviewData);
  };

  if (isLoading) {
    return (
      <div className="container py-8">
        <div className="animate-pulse space-y-8">
          <div className="grid md:grid-cols-2 gap-8">
            <div className="h-96 bg-muted rounded" />
            <div className="space-y-4">
              <div className="h-8 bg-muted rounded w-3/4" />
              <div className="h-4 bg-muted rounded w-1/2" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="container py-8">
        <div className="text-center">
          <h2 className="text-2xl font-bold mb-4">Product not found</h2>
          <p className="text-muted-foreground mb-4">The product you're looking for doesn't exist.</p>
          <Button asChild>
            <Link to="/products">Browse Products</Link>
          </Button>
        </div>
      </div>
    );
  }

  // Parse images
  const parseImages = (images?: string): string[] => {
    if (!images) return ['/placeholder.png'];
    try {
      const parsed = JSON.parse(images);
      return Array.isArray(parsed) ? parsed : [parsed];
    } catch {
      return images.split(',').map(img => img.trim()).filter(Boolean);
    }
  };

  const images = parseImages(product.images);
  const price = product.salePrice || product.price || 0;
  const originalPrice = product.salePrice ? product.price : undefined;
  const reviews = reviewsData?.content || [];
  const averageRating = reviewStats?.averageRating || product.averageRating || 0;
  const reviewCount = reviewStats?.totalReviews || product.reviewCount || 0;

  return (
    <div className="container py-8">
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="space-y-8"
      >
        {/* Back Button */}
        <Button variant="ghost" asChild>
          <Link to="/products">
            <ChevronLeft className="h-4 w-4 mr-2" />
            Back to Products
          </Link>
        </Button>

        <div className="grid md:grid-cols-2 gap-8 lg:gap-12">
          {/* Product Images */}
          <div className="space-y-4">
            <div className="aspect-square overflow-hidden rounded-lg bg-muted relative">
              <img
                src={images[selectedImage] || '/placeholder.png'}
                alt={product.name}
                className="w-full h-full object-cover"
              />
              {images.length > 1 && (
                <>
                  <Button
                    variant="outline"
                    size="icon"
                    className="absolute left-2 top-1/2 -translate-y-1/2"
                    onClick={() => setSelectedImage((prev) => (prev > 0 ? prev - 1 : images.length - 1))}
                  >
                    <ChevronLeft className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="outline"
                    size="icon"
                    className="absolute right-2 top-1/2 -translate-y-1/2"
                    onClick={() => setSelectedImage((prev) => (prev < images.length - 1 ? prev + 1 : 0))}
                  >
                    <ChevronRight className="h-4 w-4" />
                  </Button>
                </>
              )}
            </div>
            {images.length > 1 && (
              <div className="grid grid-cols-4 gap-4">
                {images.map((image, index) => (
                  <button
                    key={index}
                    onClick={() => setSelectedImage(index)}
                    className={`aspect-square overflow-hidden rounded-lg border-2 transition-colors ${
                      selectedImage === index ? 'border-primary' : 'border-transparent'
                    }`}
                  >
                    <img
                      src={image || '/placeholder.png'}
                      alt={`${product.name} ${index + 1}`}
                      className="w-full h-full object-cover"
                    />
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Product Info */}
          <div className="space-y-6">
            <div>
              <h1 className="text-4xl font-bold mb-2">{product.name}</h1>
              {product.brandName && (
                <p className="text-muted-foreground">By {product.brandName}</p>
              )}
            </div>

            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2">
                <span className="text-4xl font-bold">
                  {formatPrice(price)}
                </span>
                {originalPrice && (
                  <span className="text-xl text-muted-foreground line-through">
                    {formatPrice(originalPrice)}
                  </span>
                )}
              </div>
              {averageRating > 0 && (
                <div className="flex items-center gap-1">
                  <Star className="h-5 w-5 fill-yellow-400 text-yellow-400" />
                  <span className="font-medium">{averageRating.toFixed(1)}</span>
                  {reviewCount > 0 && (
                    <span className="text-muted-foreground">({reviewCount})</span>
                  )}
                </div>
              )}
            </div>

            <div className="space-y-2">
              {product.description && (
                <p className="text-muted-foreground">{product.description}</p>
              )}
              {product.content && (
                <div 
                  className="text-muted-foreground prose prose-sm max-w-none"
                  dangerouslySetInnerHTML={{ __html: product.content }}
                />
              )}
              <div className="flex items-center gap-2">
                <span className="font-medium">Stock:</span>
                <span className={product.quantity && product.quantity > 0 ? 'text-green-600' : 'text-destructive'}>
                  {product.quantity && product.quantity > 0 ? `${product.quantity} available` : 'Out of stock'}
                </span>
              </div>
            </div>

            {/* Quantity Selector */}
            <div className="flex items-center gap-4">
              <label className="font-medium">Quantity:</label>
              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="icon"
                  onClick={() => setQuantity(Math.max(1, quantity - 1))}
                >
                  -
                </Button>
                <span className="w-12 text-center font-medium">{quantity}</span>
                <Button
                  variant="outline"
                  size="icon"
                  onClick={() => setQuantity(Math.min(product.quantity || 1, quantity + 1))}
                  disabled={quantity >= (product.quantity || 0)}
                >
                  +
                </Button>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex gap-4">
              <Button 
                size="lg" 
                className="flex-1" 
                disabled={!product.quantity || product.quantity === 0}
                onClick={handleAddToCart}
              >
                <ShoppingCart className="h-5 w-5 mr-2" />
                Add to Cart
              </Button>
              <Button variant="outline" size="lg" onClick={handleAddToWishlist}>
                <Heart className="h-5 w-5" />
              </Button>
            </div>

            {/* Features */}
            <div className="grid grid-cols-3 gap-4 pt-6 border-t">
              <Card>
                <CardContent className="pt-6">
                  <Truck className="h-6 w-6 text-primary mb-2" />
                  <p className="text-sm font-medium">Free Shipping</p>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="pt-6">
                  <Shield className="h-6 w-6 text-primary mb-2" />
                  <p className="text-sm font-medium">Secure Payment</p>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="pt-6">
                  <RotateCcw className="h-6 w-6 text-primary mb-2" />
                  <p className="text-sm font-medium">Easy Returns</p>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>

        {/* Reviews Section */}
        <div className="space-y-6 pt-8 border-t">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold mb-2">Customer Reviews</h2>
              {averageRating > 0 && (
                <div className="flex items-center gap-2">
                  <div className="flex items-center gap-1">
                    <Star className="h-5 w-5 fill-yellow-400 text-yellow-400" />
                    <span className="text-2xl font-bold">{averageRating.toFixed(1)}</span>
                  </div>
                  <span className="text-muted-foreground">({reviewCount} reviews)</span>
                </div>
              )}
            </div>
            {isAuthenticated && (
              <Button onClick={() => setShowReviewForm(!showReviewForm)}>
                Write a Review
              </Button>
            )}
          </div>

          {/* Review Form */}
          {showReviewForm && isAuthenticated && (
            <Card>
              <CardHeader>
                <CardTitle>Write a Review</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <label className="text-sm font-medium mb-2 block">Rating</label>
                  <div className="flex gap-1">
                    {[1, 2, 3, 4, 5].map((rating) => (
                      <button
                        key={rating}
                        onClick={() => setReviewData({ ...reviewData, rating })}
                        className="focus:outline-none"
                      >
                        <Star
                          className={`h-6 w-6 ${
                            rating <= reviewData.rating
                              ? 'fill-yellow-400 text-yellow-400'
                              : 'text-muted-foreground'
                          }`}
                        />
                      </button>
                    ))}
                  </div>
                </div>
                <div>
                  <label className="text-sm font-medium mb-2 block">Title</label>
                  <Input
                    value={reviewData.title}
                    onChange={(e) => setReviewData({ ...reviewData, title: e.target.value })}
                    placeholder="Review title"
                  />
                </div>
                <div>
                  <label className="text-sm font-medium mb-2 block">Review</label>
                  <Textarea
                    value={reviewData.reviewText}
                    onChange={(e) => setReviewData({ ...reviewData, reviewText: e.target.value })}
                    placeholder="Write your review..."
                    rows={4}
                  />
                </div>
                <div className="flex gap-2">
                  <Button onClick={handleSubmitReview} disabled={addReviewMutation.isPending}>
                    {addReviewMutation.isPending ? 'Submitting...' : 'Submit Review'}
                  </Button>
                  <Button variant="outline" onClick={() => setShowReviewForm(false)}>
                    Cancel
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Reviews List */}
          {reviews.length > 0 ? (
            <div className="space-y-4">
              {reviews.map((review: ReviewResponse) => (
                <Card key={review.id}>
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div>
                        <CardTitle className="text-lg">{review.title || 'No Title'}</CardTitle>
                        <div className="flex items-center gap-2 mt-1">
                          <div className="flex items-center gap-1">
                            {[...Array(5)].map((_, i) => (
                              <Star
                                key={i}
                                className={`h-4 w-4 ${
                                  i < review.rating
                                    ? 'fill-yellow-400 text-yellow-400'
                                    : 'text-muted-foreground'
                                }`}
                              />
                            ))}
                          </div>
                          <span className="text-sm text-muted-foreground">
                            by {review.userName || 'Anonymous'}
                          </span>
                          {review.isVerifiedPurchase && (
                            <span className="text-xs bg-green-100 text-green-800 px-2 py-0.5 rounded">
                              Verified Purchase
                            </span>
                          )}
                        </div>
                      </div>
                      <span className="text-sm text-muted-foreground">
                        {new Date(review.createdAt).toLocaleDateString()}
                      </span>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground">{review.reviewText}</p>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <div className="text-center py-8 text-muted-foreground">
              No reviews yet. Be the first to review this product!
            </div>
          )}
        </div>
      </motion.div>
    </div>
  );
}
