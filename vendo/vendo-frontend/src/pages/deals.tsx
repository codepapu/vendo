import { useState } from 'react';
import { motion } from 'framer-motion';
import { Tag, Percent, Clock, TrendingDown, Sparkles } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { ProductGrid } from '@/components/product-grid';
import { Link } from 'react-router-dom';

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.5 },
  },
};

const featuredDeals = [
  {
    id: 1,
    title: 'Flash Sale',
    discount: 'Up to 50% OFF',
    description: 'Limited time offers on selected items',
    icon: Sparkles,
    color: 'text-red-500',
    bgColor: 'bg-red-50 dark:bg-red-950',
  },
  {
    id: 2,
    title: 'New Arrivals',
    discount: '20% OFF',
    description: 'Get the latest products at special prices',
    icon: TrendingDown,
    color: 'text-blue-500',
    bgColor: 'bg-blue-50 dark:bg-blue-950',
  },
  {
    id: 3,
    title: 'Clearance',
    discount: 'Up to 70% OFF',
    description: 'Last chance to grab amazing deals',
    icon: Tag,
    color: 'text-green-500',
    bgColor: 'bg-green-50 dark:bg-green-950',
  },
];

export function Deals() {
  const [selectedDeal, setSelectedDeal] = useState<string>('all');

  const searchParams = {
    status: 'published',
    page: 0,
    size: 20,
    sortBy: 'price',
    sortDir: 'ASC' as const,
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
            <Tag className="h-16 w-16 text-primary mx-auto" />
            <h1 className="text-4xl md:text-6xl font-bold tracking-tight">
              Exclusive <span className="bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">Deals</span>
            </h1>
            <p className="text-xl text-muted-foreground">
              Discover amazing discounts and special offers on quality products
            </p>
          </motion.div>
        </div>
      </section>

      {/* Featured Deals */}
      <section className="py-16 bg-background">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
            className="text-center mb-12"
          >
            <h2 className="text-3xl font-bold mb-4">Featured Deals</h2>
            <p className="text-muted-foreground">
              Don't miss out on these limited-time offers
            </p>
          </motion.div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
            {featuredDeals.map((deal, index) => (
              <motion.div
                key={deal.id}
                variants={itemVariants}
                whileHover={{ y: -5 }}
                transition={{ duration: 0.2 }}
              >
                <Card className={`h-full hover:shadow-lg transition-shadow ${deal.bgColor}`}>
                  <CardHeader>
                    <div className="flex items-center justify-between mb-4">
                      <deal.icon className={`h-12 w-12 ${deal.color}`} />
                      <Badge variant="destructive" className="text-lg px-3 py-1">
                        {deal.discount}
                      </Badge>
                    </div>
                    <CardTitle>{deal.title}</CardTitle>
                    <CardDescription className="text-base">
                      {deal.description}
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <Button asChild className="w-full">
                      <Link to="/products">Shop Now</Link>
                    </Button>
                  </CardContent>
                </Card>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* All Deals Products */}
      <section className="py-16 bg-muted/50">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
            className="mb-8"
          >
            <div className="flex items-center justify-between mb-6">
              <div>
                <h2 className="text-3xl font-bold mb-2">All Deals</h2>
                <p className="text-muted-foreground">
                  Browse all discounted products
                </p>
              </div>
              <div className="flex items-center gap-2">
                <Clock className="h-5 w-5 text-muted-foreground" />
                <span className="text-sm text-muted-foreground">
                  Deals refresh daily
                </span>
              </div>
            </div>

            <ProductGrid
              searchParams={searchParams}
              onAddToCart={(productId) => {
                // Handle add to cart
                console.log('Add to cart:', productId);
              }}
              onAddToWishlist={(productId) => {
                // Handle add to wishlist
                console.log('Add to wishlist:', productId);
              }}
            />
          </motion.div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 bg-background">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
            className="max-w-2xl mx-auto text-center"
          >
            <Card>
              <CardHeader>
                <CardTitle className="text-2xl flex items-center justify-center gap-2">
                  <Percent className="h-6 w-6 text-primary" />
                  Never Miss a Deal
                </CardTitle>
                <CardDescription className="text-base">
                  Subscribe to our newsletter and be the first to know about new deals, flash sales, and exclusive offers.
                </CardDescription>
              </CardHeader>
              <CardContent>
                <Button asChild size="lg">
                  <Link to="/contact">Subscribe Now</Link>
                </Button>
              </CardContent>
            </Card>
          </motion.div>
        </div>
      </section>
    </div>
  );
}

