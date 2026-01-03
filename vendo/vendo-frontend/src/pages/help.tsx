import { useState } from 'react';
import { motion } from 'framer-motion';
import { HelpCircle, Search, BookOpen, MessageCircle, FileText, ChevronRight } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Link } from 'react-router-dom';
import { useDebounce } from '@/hooks/use-debounce';

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.5 },
  },
};

const helpCategories = [
  {
    id: 'getting-started',
    title: 'Getting Started',
    icon: BookOpen,
    articles: [
      { id: 1, title: 'How to create an account', slug: 'create-account' },
      { id: 2, title: 'How to place an order', slug: 'place-order' },
      { id: 3, title: 'Understanding your dashboard', slug: 'dashboard' },
    ],
  },
  {
    id: 'orders',
    title: 'Orders & Shipping',
    icon: FileText,
    articles: [
      { id: 4, title: 'Track your order', slug: 'track-order' },
      { id: 5, title: 'Shipping information', slug: 'shipping' },
      { id: 6, title: 'Delivery times', slug: 'delivery-times' },
    ],
  },
  {
    id: 'returns',
    title: 'Returns & Refunds',
    icon: MessageCircle,
    articles: [
      { id: 7, title: 'How to return an item', slug: 'return-item' },
      { id: 8, title: 'Refund policy', slug: 'refund-policy' },
      { id: 9, title: 'Exchange items', slug: 'exchange' },
    ],
  },
];

const popularArticles = [
  { id: 1, title: 'How to create an account', category: 'Getting Started', views: 1250 },
  { id: 4, title: 'Track your order', category: 'Orders & Shipping', views: 980 },
  { id: 7, title: 'How to return an item', category: 'Returns & Refunds', views: 750 },
  { id: 2, title: 'How to place an order', category: 'Getting Started', views: 650 },
];

export function HelpCenter() {
  const [searchQuery, setSearchQuery] = useState('');
  const debouncedSearch = useDebounce(searchQuery, 500);

  // Filter categories and articles based on search
  const filteredCategories = helpCategories.map((category) => ({
    ...category,
    articles: category.articles.filter((article) =>
      article.title.toLowerCase().includes(debouncedSearch.toLowerCase())
    ),
  })).filter((category) => category.articles.length > 0 || debouncedSearch === '');

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
            <HelpCircle className="h-16 w-16 text-primary mx-auto" />
            <h1 className="text-4xl md:text-6xl font-bold tracking-tight">
              Help <span className="bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">Center</span>
            </h1>
            <p className="text-xl text-muted-foreground">
              Find answers to common questions and get support
            </p>
          </motion.div>
        </div>
      </section>

      {/* Main Content */}
      <section className="py-16 bg-background">
        <div className="container max-w-6xl">
          {/* Search Bar */}
          <div className="mb-12">
            <div className="relative max-w-2xl mx-auto">
              <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 h-5 w-5 text-muted-foreground" />
              <Input
                placeholder="Search for help articles..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-12 h-14 text-lg"
              />
            </div>
          </div>

          {/* Popular Articles */}
          {debouncedSearch === '' && (
            <motion.div
              initial="hidden"
              whileInView="visible"
              viewport={{ once: true }}
              variants={itemVariants}
              className="mb-12"
            >
              <h2 className="text-2xl font-bold mb-6">Popular Articles</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {popularArticles.map((article) => (
                  <Card
                    key={article.id}
                    className="hover:shadow-lg transition-shadow cursor-pointer"
                  >
                    <CardContent className="p-6">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <h3 className="font-semibold mb-2">{article.title}</h3>
                          <div className="flex items-center gap-2">
                            <Badge variant="secondary">{article.category}</Badge>
                            <span className="text-sm text-muted-foreground">
                              {article.views} views
                            </span>
                          </div>
                        </div>
                        <ChevronRight className="h-5 w-5 text-muted-foreground" />
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </motion.div>
          )}

          {/* Help Categories */}
          <div className="space-y-8">
            {filteredCategories.map((category) => (
              <motion.div
                key={category.id}
                initial="hidden"
                whileInView="visible"
                viewport={{ once: true }}
                variants={itemVariants}
              >
                <Card>
                  <CardHeader>
                    <div className="flex items-center gap-3">
                      <category.icon className="h-6 w-6 text-primary" />
                      <CardTitle>{category.title}</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                      {category.articles.map((article) => (
                        <Link
                          key={article.id}
                          to={`/help/${article.slug}`}
                          className="block p-4 rounded-lg border hover:bg-muted transition-colors"
                        >
                          <div className="flex items-center justify-between">
                            <span className="font-medium">{article.title}</span>
                            <ChevronRight className="h-4 w-4 text-muted-foreground" />
                          </div>
                        </Link>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              </motion.div>
            ))}
          </div>

          {/* Contact Support */}
          {debouncedSearch === '' && (
            <motion.div
              initial="hidden"
              whileInView="visible"
              viewport={{ once: true }}
              variants={itemVariants}
              className="mt-12"
            >
              <Card className="bg-primary/5 border-primary/20">
                <CardContent className="p-8 text-center">
                  <MessageCircle className="h-12 w-12 text-primary mx-auto mb-4" />
                  <h3 className="text-2xl font-bold mb-2">Still need help?</h3>
                  <p className="text-muted-foreground mb-6">
                    Can&apos;t find what you&apos;re looking for? Our support team is here to help.
                  </p>
                  <div className="flex flex-col sm:flex-row gap-4 justify-center">
                    <Button asChild>
                      <Link to="/contact">Contact Support</Link>
                    </Button>
                    <Button variant="outline" asChild>
                      <Link to="/faq">View FAQ</Link>
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          )}
        </div>
      </section>
    </div>
  );
}

