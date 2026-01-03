import { lazy, Suspense, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ShoppingBag, TrendingUp, Shield, Zap, Star, Quote, ArrowRight, Mail } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { useQuery } from '@tanstack/react-query';
import { productApi } from '@/services/product-api';
import { Skeleton } from '@/components/ui/skeleton';
import { useToast } from '@/hooks/use-toast';

const ProductGrid = lazy(() => import('@/components/product-grid').then((m) => ({ default: m.ProductGrid })));

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1,
    },
  },
};

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

export function Home() {
  return (
    <div className="flex flex-col">
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-br from-primary/10 via-background to-background py-20 md:py-32">
        <div className="container">
          <motion.div
            initial="hidden"
            animate="visible"
            variants={containerVariants}
            className="max-w-3xl mx-auto text-center space-y-6"
          >
            <motion.h1
              variants={itemVariants}
              className="text-4xl md:text-6xl font-bold tracking-tight"
            >
              Welcome to{' '}
              <span className="bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">
                Vendo
              </span>
            </motion.h1>
            <motion.p
              variants={itemVariants}
              className="text-xl text-muted-foreground max-w-2xl mx-auto"
            >
              Discover amazing products at unbeatable prices. Shop the latest trends and find
              everything you need in one place.
            </motion.p>
            <motion.div variants={itemVariants} className="flex gap-4 justify-center">
              <Button size="lg" asChild>
                <Link to="/products">Shop Now</Link>
              </Button>
              <Button size="lg" variant="outline" asChild>
                <Link to="/products">Browse Products</Link>
              </Button>
            </motion.div>
          </motion.div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-muted/50">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={containerVariants}
            className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6"
          >
            {[
              {
                icon: ShoppingBag,
                title: 'Wide Selection',
                description: 'Thousands of products from trusted vendors',
              },
              {
                icon: Zap,
                title: 'Fast Delivery',
                description: 'Quick and reliable shipping to your door',
              },
              {
                icon: Shield,
                title: 'Secure Payment',
                description: 'Your transactions are safe and encrypted',
              },
              {
                icon: TrendingUp,
                title: 'Best Prices',
                description: 'Competitive pricing on all products',
              },
            ].map((feature, index) => (
              <motion.div key={index} variants={itemVariants}>
                <Card className="h-full">
                  <CardHeader>
                    <feature.icon className="h-8 w-8 text-primary mb-2" />
                    <CardTitle>{feature.title}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <CardDescription>{feature.description}</CardDescription>
                  </CardContent>
                </Card>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* Categories Grid */}
      <section className="py-20 bg-background">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={containerVariants}
          >
            <div className="text-center mb-12">
              <h2 className="text-3xl md:text-4xl font-bold mb-4">Shop by Category</h2>
              <p className="text-muted-foreground max-w-2xl mx-auto">
                Explore our wide range of product categories
              </p>
            </div>
            <Suspense fallback={
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                {[...Array(8)].map((_, i) => (
                  <Card key={i}>
                    <Skeleton className="h-48 w-full" />
                    <CardHeader>
                      <Skeleton className="h-6 w-3/4" />
                    </CardHeader>
                  </Card>
                ))}
              </div>
            }>
              <CategoriesGrid />
            </Suspense>
          </motion.div>
        </div>
      </section>

      {/* Featured Products */}
      <section className="py-20 bg-muted/50">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={containerVariants}
          >
            <div className="flex items-center justify-between mb-8">
              <div>
                <h2 className="text-3xl md:text-4xl font-bold mb-2">Featured Products</h2>
                <p className="text-muted-foreground">Handpicked products just for you</p>
              </div>
              <Button variant="outline" asChild>
                <Link to="/products">View All <ArrowRight className="ml-2 h-4 w-4" /></Link>
              </Button>
            </div>
            <Suspense fallback={<div className="text-center py-12">Loading products...</div>}>
              <ProductGrid limit={8} />
            </Suspense>
          </motion.div>
        </div>
      </section>

      {/* Trending Products */}
      <section className="py-20 bg-background">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={containerVariants}
          >
            <div className="flex items-center justify-between mb-8">
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <TrendingUp className="h-6 w-6 text-primary" />
                  <h2 className="text-3xl md:text-4xl font-bold">Trending Now</h2>
                </div>
                <p className="text-muted-foreground">Products everyone is talking about</p>
              </div>
              <Button variant="outline" asChild>
                <Link to="/products?sortBy=views&sortDir=DESC">View All <ArrowRight className="ml-2 h-4 w-4" /></Link>
              </Button>
            </div>
            <Suspense fallback={<div className="text-center py-12">Loading products...</div>}>
              <ProductGrid limit={8} featured={false} />
            </Suspense>
          </motion.div>
        </div>
      </section>

      {/* Testimonials */}
      <section className="py-20 bg-muted/50">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={containerVariants}
            className="text-center mb-12"
          >
            <h2 className="text-3xl md:text-4xl font-bold mb-4">What Our Customers Say</h2>
            <p className="text-muted-foreground max-w-2xl mx-auto">
              Don't just take our word for it - hear from our satisfied customers
            </p>
          </motion.div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[
              {
                name: 'Sarah Johnson',
                role: 'Regular Customer',
                rating: 5,
                text: 'Vendo has completely transformed my shopping experience. Fast delivery, great prices, and excellent customer service!',
                image: '👩',
              },
              {
                name: 'Michael Chen',
                role: 'Business Owner',
                rating: 5,
                text: 'As a business owner, I appreciate the B2B features. Bulk ordering and invoicing make procurement so much easier.',
                image: '👨',
              },
              {
                name: 'Emily Rodriguez',
                role: 'Fashion Enthusiast',
                rating: 5,
                text: 'The variety of products and stores is amazing. I can find everything I need in one place, and the quality is always top-notch.',
                image: '👩',
              },
            ].map((testimonial, index) => (
              <motion.div key={index} variants={itemVariants}>
                <Card className="h-full">
                  <CardHeader>
                    <div className="flex items-center gap-4 mb-4">
                      <div className="text-4xl">{testimonial.image}</div>
                      <div>
                        <CardTitle className="text-lg">{testimonial.name}</CardTitle>
                        <CardDescription>{testimonial.role}</CardDescription>
                      </div>
                    </div>
                    <div className="flex gap-1">
                      {[...Array(testimonial.rating)].map((_, i) => (
                        <Star key={i} className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                      ))}
                    </div>
                  </CardHeader>
                  <CardContent>
                    <Quote className="h-8 w-8 text-primary/20 mb-2" />
                    <p className="text-muted-foreground italic">{testimonial.text}</p>
                  </CardContent>
                </Card>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Newsletter Signup */}
      <section className="py-20 bg-background">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
            className="max-w-2xl mx-auto text-center"
          >
            <Card className="bg-gradient-to-br from-primary/10 to-primary/5 border-primary/20">
              <CardHeader>
                <Mail className="h-12 w-12 text-primary mx-auto mb-4" />
                <CardTitle className="text-2xl md:text-3xl">Stay Updated</CardTitle>
                <CardDescription className="text-base">
                  Subscribe to our newsletter for the latest products, deals, and updates
                </CardDescription>
              </CardHeader>
              <CardContent>
                <NewsletterForm />
              </CardContent>
            </Card>
          </motion.div>
        </div>
      </section>

      {/* Trust Badges */}
      <section className="py-12 bg-muted/30 border-t">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={containerVariants}
            className="grid grid-cols-2 md:grid-cols-4 gap-8 text-center"
          >
            {[
              { icon: Shield, label: 'Secure Payments', description: 'SSL Encrypted' },
              { icon: Zap, label: 'Fast Shipping', description: '2-5 Business Days' },
              { icon: ShoppingBag, label: '10K+ Products', description: 'Wide Selection' },
              { icon: TrendingUp, label: 'Best Prices', description: 'Price Match Guarantee' },
            ].map((badge, index) => (
              <motion.div key={index} variants={itemVariants} className="space-y-2">
                <badge.icon className="h-8 w-8 text-primary mx-auto" />
                <p className="font-semibold text-sm">{badge.label}</p>
                <p className="text-xs text-muted-foreground">{badge.description}</p>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>
    </div>
  );
}

// Categories Grid Component
function CategoriesGrid() {
  const { data: categories, isLoading } = useQuery({
    queryKey: ['categories', 'featured'],
    queryFn: async () => {
      // TODO: Implement featured categories API
      return [];
    },
  });

  if (isLoading) {
    return (
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        {[...Array(8)].map((_, i) => (
          <Card key={i}>
            <Skeleton className="h-48 w-full" />
            <CardHeader>
              <Skeleton className="h-6 w-3/4" />
            </CardHeader>
          </Card>
        ))}
      </div>
    );
  }

  // Fallback categories if API is not available
  const fallbackCategories = [
    { id: 1, name: 'Electronics', slug: 'electronics', imageUrl: null },
    { id: 2, name: 'Fashion', slug: 'fashion', imageUrl: null },
    { id: 3, name: 'Home & Garden', slug: 'home-garden', imageUrl: null },
    { id: 4, name: 'Sports', slug: 'sports', imageUrl: null },
    { id: 5, name: 'Books', slug: 'books', imageUrl: null },
    { id: 6, name: 'Toys', slug: 'toys', imageUrl: null },
    { id: 7, name: 'Health', slug: 'health', imageUrl: null },
    { id: 8, name: 'Beauty', slug: 'beauty', imageUrl: null },
  ];

  const displayCategories = categories && categories.length > 0 ? categories : fallbackCategories;

  return (
    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
      {displayCategories.slice(0, 8).map((category: any) => (
        <Link key={category.id} to={`/categories?category=${category.slug || category.id}`}>
          <Card className="h-full hover:shadow-lg transition-shadow cursor-pointer group">
            <div className="relative h-48 overflow-hidden rounded-t-lg">
              {category.imageUrl ? (
                <img
                  src={category.imageUrl}
                  alt={category.name}
                  className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
                />
              ) : (
                <div className="w-full h-full bg-gradient-to-br from-primary/20 to-primary/10 flex items-center justify-center">
                  <ShoppingBag className="h-16 w-16 text-primary/40" />
                </div>
              )}
              <div className="absolute inset-0 bg-black/0 group-hover:bg-black/10 transition-colors" />
            </div>
            <CardHeader>
              <CardTitle className="group-hover:text-primary transition-colors">
                {category.name}
              </CardTitle>
            </CardHeader>
          </Card>
        </Link>
      ))}
    </div>
  );
}

// Newsletter Form Component
function NewsletterForm() {
  const [email, setEmail] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { toast } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) return;

    setIsSubmitting(true);
    // TODO: Implement newsletter subscription API
    setTimeout(() => {
      setIsSubmitting(false);
      setEmail('');
      toast({
        title: 'Successfully subscribed!',
        description: 'You will receive our latest updates via email.',
      });
    }, 1000);
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col sm:flex-row gap-2">
      <Input
        type="email"
        placeholder="Enter your email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        className="flex-1"
        required
      />
      <Button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Subscribing...' : 'Subscribe'}
      </Button>
    </form>
  );
}
