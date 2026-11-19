import { motion } from 'framer-motion';
import { Network as SitemapIcon, ExternalLink } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Link } from 'react-router-dom';

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.5 },
  },
};

const sitemapSections = [
  {
    title: 'Shop',
    links: [
      { label: 'All Products', href: '/products' },
      { label: 'Categories', href: '/categories' },
      { label: 'Stores', href: '/stores' },
      { label: 'Search', href: '/search' },
    ],
  },
  {
    title: 'Company',
    links: [
      { label: 'About Us', href: '/about' },
      { label: 'Contact', href: '/contact' },
      { label: 'Blog', href: '/blog' },
    ],
  },
  {
    title: 'Legal',
    links: [
      { label: 'Privacy Policy', href: '/privacy' },
      { label: 'Terms of Service', href: '/terms' },
      { label: 'Cookie Policy', href: '/cookie-policy' },
      { label: 'Refund Policy', href: '/refund-policy' },
    ],
  },
  {
    title: 'Support',
    links: [
      { label: 'Help Center', href: '/help' },
      { label: 'FAQ', href: '/faq' },
      { label: 'Shipping Information', href: '/shipping' },
      { label: 'Returns & Refunds', href: '/returns' },
    ],
  },
  {
    title: 'Account',
    links: [
      { label: 'Login', href: '/login' },
      { label: 'Register', href: '/register' },
      { label: 'Profile', href: '/profile' },
      { label: 'Orders', href: '/orders' },
      { label: 'Cart', href: '/cart' },
    ],
  },
  {
    title: 'Business',
    links: [
      { label: 'B2B Portal', href: '/b2b/dashboard' },
      { label: 'Retailer Dashboard', href: '/retailer/dashboard' },
      { label: 'B2B Register', href: '/b2b/register' },
      { label: 'Retailer Register', href: '/retailer/register' },
    ],
  },
];

export function Sitemap() {
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
            <SitemapIcon className="h-16 w-16 text-primary mx-auto" />
            <h1 className="text-4xl md:text-6xl font-bold tracking-tight">
              Site <span className="bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">Map</span>
            </h1>
            <p className="text-xl text-muted-foreground">
              Find all pages and sections of our website
            </p>
          </motion.div>
        </div>
      </section>

      {/* Main Content */}
      <section className="py-16 bg-background">
        <div className="container max-w-6xl">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
            className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-12"
          >
            {sitemapSections.map((section, index) => (
              <Card key={index}>
                <CardHeader>
                  <CardTitle>{section.title}</CardTitle>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-2">
                    {section.links.map((link, linkIndex) => (
                      <li key={linkIndex}>
                        <Link
                          to={link.href}
                          className="text-sm text-muted-foreground hover:text-foreground transition-colors flex items-center gap-2"
                        >
                          {link.label}
                          <ExternalLink className="h-3 w-3 opacity-0 group-hover:opacity-100" />
                        </Link>
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            ))}
          </motion.div>

          {/* XML Sitemap Link */}
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
          >
            <Card className="bg-muted/50">
              <CardHeader>
                <CardTitle>XML Sitemap</CardTitle>
                <CardDescription>
                  For search engines and web crawlers
                </CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground mb-4">
                  Our XML sitemap is available for search engines to better index our website.
                </p>
                <a
                  href="/sitemap.xml"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-primary hover:underline flex items-center gap-2"
                >
                  View XML Sitemap
                  <ExternalLink className="h-4 w-4" />
                </a>
              </CardContent>
            </Card>
          </motion.div>
        </div>
      </section>
    </div>
  );
}

