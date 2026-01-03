import { motion } from 'framer-motion';
import { Newspaper, Mail, FileText, Download, Calendar, User } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Link } from 'react-router-dom';

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.5 },
  },
};

const pressReleases = [
  {
    date: '2024-01-15',
    title: 'Vendo Launches New Marketplace Platform',
    description: 'Vendo announces the launch of its revolutionary e-commerce marketplace connecting customers with verified retailers.',
    category: 'Product Launch',
  },
  {
    date: '2024-02-20',
    title: 'Vendo Raises $50M in Series B Funding',
    description: 'Leading investors back Vendo\'s mission to transform online shopping with innovative technology and exceptional service.',
    category: 'Funding',
  },
  {
    date: '2024-03-10',
    title: 'Vendo Partners with Top Retailers',
    description: 'Vendo expands its network with partnerships from leading brands, offering customers more choices and better prices.',
    category: 'Partnership',
  },
  {
    date: '2024-04-05',
    title: 'Vendo Achieves 1 Million Active Users',
    description: 'Vendo celebrates a major milestone as it reaches one million active users, demonstrating strong market adoption.',
    category: 'Milestone',
  },
];

const mediaKit = [
  {
    title: 'Company Logo',
    description: 'High-resolution Vendo logos in various formats',
    icon: Download,
  },
  {
    title: 'Brand Guidelines',
    description: 'Complete brand identity and style guide',
    icon: FileText,
  },
  {
    title: 'Product Screenshots',
    description: 'Latest product screenshots and images',
    icon: Download,
  },
  {
    title: 'Company Fact Sheet',
    description: 'Key facts and statistics about Vendo',
    icon: FileText,
  },
];

export function Press() {
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
            <Newspaper className="h-16 w-16 text-primary mx-auto" />
            <h1 className="text-4xl md:text-6xl font-bold tracking-tight">
              Press & <span className="bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">Media</span>
            </h1>
            <p className="text-xl text-muted-foreground">
              Latest news, press releases, and media resources from Vendo
            </p>
          </motion.div>
        </div>
      </section>

      {/* Press Contact */}
      <section className="py-16 bg-background">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
            className="max-w-4xl mx-auto"
          >
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Mail className="h-5 w-5 text-primary" />
                  Press Inquiries
                </CardTitle>
                <CardDescription>
                  For media inquiries, interview requests, or press-related questions, please contact our press team.
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <p className="text-sm font-medium mb-1">Email</p>
                  <a href="mailto:press@vendo.com" className="text-primary hover:underline">
                    press@vendo.com
                  </a>
                </div>
                <div>
                  <p className="text-sm font-medium mb-1">Phone</p>
                  <a href="tel:+1234567890" className="text-primary hover:underline">
                    +1 (234) 567-890
                  </a>
                </div>
                <Button asChild className="mt-4">
                  <Link to="/contact">Contact Press Team</Link>
                </Button>
              </CardContent>
            </Card>
          </motion.div>
        </div>
      </section>

      {/* Press Releases */}
      <section className="py-16 bg-muted/50">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
            className="text-center mb-12"
          >
            <h2 className="text-3xl font-bold mb-4">Press Releases</h2>
            <p className="text-muted-foreground">
              Stay updated with our latest announcements and company news
            </p>
          </motion.div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-4xl mx-auto">
            {pressReleases.map((release, index) => (
              <motion.div
                key={index}
                variants={itemVariants}
                whileHover={{ y: -5 }}
                transition={{ duration: 0.2 }}
              >
                <Card className="h-full hover:shadow-lg transition-shadow">
                  <CardHeader>
                    <div className="flex items-start justify-between mb-2">
                      <Badge variant="secondary">{release.category}</Badge>
                      <div className="flex items-center gap-1 text-sm text-muted-foreground">
                        <Calendar className="h-4 w-4" />
                        {new Date(release.date).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}
                      </div>
                    </div>
                    <CardTitle>{release.title}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <CardDescription className="text-base">
                      {release.description}
                    </CardDescription>
                  </CardContent>
                </Card>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Media Kit */}
      <section className="py-16 bg-background">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
            className="text-center mb-12"
          >
            <h2 className="text-3xl font-bold mb-4">Media Kit</h2>
            <p className="text-muted-foreground">
              Download assets and resources for media use
            </p>
          </motion.div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 max-w-4xl mx-auto">
            {mediaKit.map((item, index) => (
              <motion.div
                key={index}
                variants={itemVariants}
                whileHover={{ y: -5 }}
                transition={{ duration: 0.2 }}
              >
                <Card className="h-full hover:shadow-lg transition-shadow">
                  <CardHeader>
                    <item.icon className="h-10 w-10 text-primary mb-4" />
                    <CardTitle className="text-lg">{item.title}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <CardDescription className="text-sm mb-4">
                      {item.description}
                    </CardDescription>
                    <Button variant="outline" size="sm" className="w-full">
                      Download
                    </Button>
                  </CardContent>
                </Card>
              </motion.div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}

