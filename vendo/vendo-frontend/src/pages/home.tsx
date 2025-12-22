import { lazy, Suspense } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ShoppingBag, TrendingUp, Shield, Zap } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

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

      {/* Featured Products */}
      <section className="py-20">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={containerVariants}
          >
            <div className="flex items-center justify-between mb-8">
              <h2 className="text-3xl font-bold">Featured Products</h2>
              <Button variant="outline" asChild>
                <Link to="/products">View All</Link>
              </Button>
            </div>
            <Suspense fallback={<div className="text-center py-12">Loading products...</div>}>
              <ProductGrid limit={8} />
            </Suspense>
          </motion.div>
        </div>
      </section>
    </div>
  );
}
