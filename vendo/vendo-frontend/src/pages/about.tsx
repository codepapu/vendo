import { motion } from 'framer-motion';
import { Users, Target, Award, Heart, Shield } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

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

export function About() {
  return (
    <div className="flex flex-col min-h-screen">
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-br from-primary/10 via-background to-background py-20">
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
              About <span className="bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">Vendo</span>
            </motion.h1>
            <motion.p
              variants={itemVariants}
              className="text-xl text-muted-foreground"
            >
              Your trusted marketplace for quality products and exceptional service
            </motion.p>
          </motion.div>
        </div>
      </section>

      {/* Our Story */}
      <section className="py-16 bg-background">
        <div className="container">
          <div className="max-w-4xl mx-auto space-y-8">
            <motion.div
              initial="hidden"
              whileInView="visible"
              viewport={{ once: true }}
              variants={itemVariants}
              className="space-y-4"
            >
              <h2 className="text-3xl font-bold">Our Story</h2>
              <p className="text-lg text-muted-foreground leading-relaxed">
                Vendo was founded with a simple mission: to create a marketplace that connects
                customers with quality products from trusted retailers. We believe in making
                shopping easy, secure, and enjoyable for everyone.
              </p>
              <p className="text-lg text-muted-foreground leading-relaxed">
                Since our inception, we've grown into a platform that serves thousands of
                customers and hundreds of retailers, all while maintaining our commitment to
                excellence, transparency, and customer satisfaction.
              </p>
            </motion.div>
          </div>
        </div>
      </section>

      {/* Values */}
      <section className="py-16 bg-muted/50">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={containerVariants}
            className="text-center mb-12"
          >
            <h2 className="text-3xl font-bold mb-4">Our Values</h2>
            <p className="text-muted-foreground">The principles that guide everything we do</p>
          </motion.div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {[
              {
                icon: Target,
                title: 'Customer First',
                description: 'Every decision we make prioritizes our customers\' needs and satisfaction.',
              },
              {
                icon: Shield,
                title: 'Trust & Security',
                description: 'We ensure secure transactions and protect your personal information.',
              },
              {
                icon: Award,
                title: 'Quality',
                description: 'We partner only with verified retailers who meet our quality standards.',
              },
              {
                icon: Heart,
                title: 'Community',
                description: 'We build lasting relationships with customers, retailers, and partners.',
              },
            ].map((value, index) => (
              <motion.div
                key={index}
                variants={itemVariants}
                whileHover={{ y: -5 }}
                transition={{ duration: 0.2 }}
              >
                <Card className="h-full">
                  <CardHeader>
                    <value.icon className="h-10 w-10 text-primary mb-4" />
                    <CardTitle>{value.title}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <CardDescription className="text-base">
                      {value.description}
                    </CardDescription>
                  </CardContent>
                </Card>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Team */}
      <section className="py-16 bg-background">
        <div className="container">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={containerVariants}
            className="text-center mb-12"
          >
            <h2 className="text-3xl font-bold mb-4">Our Mission</h2>
            <p className="text-muted-foreground max-w-2xl mx-auto">
              To revolutionize online shopping by providing a seamless, secure, and enjoyable
              marketplace experience that benefits both customers and retailers.
            </p>
          </motion.div>

          <motion.div
            variants={itemVariants}
            className="max-w-4xl mx-auto"
          >
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Users className="h-6 w-6 text-primary" />
                  Join Our Community
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground mb-4">
                  Whether you're a customer looking for great products or a retailer wanting to
                  expand your reach, we welcome you to be part of the Vendo community.
                </p>
                <div className="flex flex-wrap gap-4">
                  <a
                    href="/register"
                    className="text-primary hover:underline font-medium"
                  >
                    Start Shopping →
                  </a>
                  <a
                    href="/retailer/register"
                    className="text-primary hover:underline font-medium"
                  >
                    Become a Retailer →
                  </a>
                </div>
              </CardContent>
            </Card>
          </motion.div>
        </div>
      </section>
    </div>
  );
}

