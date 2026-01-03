import { motion } from 'framer-motion';
import { Cookie, Shield, Settings, Eye, List } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.5 },
  },
};

const cookieTypes = [
  {
    name: 'Essential Cookies',
    description: 'These cookies are necessary for the website to function properly. They enable basic functions like page navigation and access to secure areas.',
    examples: ['Session management', 'Authentication', 'Security'],
    required: true,
  },
  {
    name: 'Analytics Cookies',
    description: 'These cookies help us understand how visitors interact with our website by collecting and reporting information anonymously.',
    examples: ['Page views', 'User behavior', 'Traffic sources'],
    required: false,
  },
  {
    name: 'Functional Cookies',
    description: 'These cookies enable enhanced functionality and personalization, such as remembering your preferences and settings.',
    examples: ['Language preferences', 'Region settings', 'User preferences'],
    required: false,
  },
  {
    name: 'Advertising Cookies',
    description: 'These cookies are used to deliver relevant advertisements and track campaign performance.',
    examples: ['Ad targeting', 'Campaign tracking', 'Conversion measurement'],
    required: false,
  },
];

export function CookiePolicy() {
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
            <Cookie className="h-16 w-16 text-primary mx-auto" />
            <h1 className="text-4xl md:text-6xl font-bold tracking-tight">
              Cookie <span className="bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">Policy</span>
            </h1>
            <p className="text-xl text-muted-foreground">
              Last updated: {new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}
            </p>
          </motion.div>
        </div>
      </section>

      {/* Content */}
      <section className="py-16 bg-background">
        <div className="container max-w-6xl">
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
            {/* Table of Contents */}
            <div className="lg:col-span-1">
              <Card className="sticky top-24">
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    <List className="h-5 w-5" />
                    Table of Contents
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-2">
                  <a href="#what-are-cookies" className="text-sm text-muted-foreground hover:text-foreground block">
                    What Are Cookies?
                  </a>
                  <a href="#types-of-cookies" className="text-sm text-muted-foreground hover:text-foreground block">
                    Types of Cookies
                  </a>
                  <a href="#how-we-use" className="text-sm text-muted-foreground hover:text-foreground block">
                    How We Use Cookies
                  </a>
                  <a href="#manage-cookies" className="text-sm text-muted-foreground hover:text-foreground block">
                    Managing Cookies
                  </a>
                  <a href="#third-party" className="text-sm text-muted-foreground hover:text-foreground block">
                    Third-Party Cookies
                  </a>
                  <a href="#updates" className="text-sm text-muted-foreground hover:text-foreground block">
                    Policy Updates
                  </a>
                </CardContent>
              </Card>
            </div>

            {/* Main Content */}
            <div className="lg:col-span-3 space-y-8">
              {/* What Are Cookies */}
              <motion.div
                id="what-are-cookies"
                initial="hidden"
                whileInView="visible"
                viewport={{ once: true }}
                variants={itemVariants}
              >
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Shield className="h-5 w-5 text-primary" />
                      What Are Cookies?
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <p className="text-muted-foreground">
                      Cookies are small text files that are placed on your device when you visit a website. They are widely used to make websites work more efficiently and provide information to website owners.
                    </p>
                    <p className="text-muted-foreground">
                      Cookies help us provide you with a better experience by allowing us to remember your preferences, understand how you use our site, and improve our services.
                    </p>
                  </CardContent>
                </Card>
              </motion.div>

              {/* Types of Cookies */}
              <motion.div
                id="types-of-cookies"
                initial="hidden"
                whileInView="visible"
                viewport={{ once: true }}
                variants={itemVariants}
              >
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Settings className="h-5 w-5 text-primary" />
                      Types of Cookies We Use
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-6">
                    {cookieTypes.map((type, index) => (
                      <div key={index} className="border-l-4 border-primary pl-4 space-y-2">
                        <div className="flex items-center gap-2">
                          <h3 className="font-semibold">{type.name}</h3>
                          {type.required && (
                            <span className="text-xs bg-primary/10 text-primary px-2 py-1 rounded">
                              Required
                            </span>
                          )}
                        </div>
                        <p className="text-sm text-muted-foreground">{type.description}</p>
                        <div className="text-sm">
                          <p className="font-medium mb-1">Examples:</p>
                          <ul className="list-disc list-inside text-muted-foreground space-y-1">
                            {type.examples.map((example, i) => (
                              <li key={i}>{example}</li>
                            ))}
                          </ul>
                        </div>
                      </div>
                    ))}
                  </CardContent>
                </Card>
              </motion.div>

              {/* How We Use Cookies */}
              <motion.div
                id="how-we-use"
                initial="hidden"
                whileInView="visible"
                viewport={{ once: true }}
                variants={itemVariants}
              >
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Eye className="h-5 w-5 text-primary" />
                      How We Use Cookies
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <p className="text-muted-foreground">
                      We use cookies for the following purposes:
                    </p>
                    <ul className="list-disc list-inside space-y-2 text-muted-foreground">
                      <li>To enable essential website functionality</li>
                      <li>To remember your preferences and settings</li>
                      <li>To analyze website traffic and user behavior</li>
                      <li>To improve our services and user experience</li>
                      <li>To deliver personalized content and advertisements</li>
                      <li>To ensure website security and prevent fraud</li>
                    </ul>
                  </CardContent>
                </Card>
              </motion.div>

              {/* Managing Cookies */}
              <motion.div
                id="manage-cookies"
                initial="hidden"
                whileInView="visible"
                viewport={{ once: true }}
                variants={itemVariants}
              >
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Settings className="h-5 w-5 text-primary" />
                      Managing Cookies
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <p className="text-muted-foreground">
                      You have the right to accept or reject cookies. Most web browsers automatically accept cookies, but you can usually modify your browser settings to decline cookies if you prefer.
                    </p>
                    <p className="text-muted-foreground">
                      However, please note that disabling cookies may affect the functionality of our website and limit your ability to access certain features.
                    </p>
                    <div className="bg-muted p-4 rounded-lg">
                      <p className="text-sm font-medium mb-2">Browser Settings:</p>
                      <ul className="text-sm text-muted-foreground space-y-1">
                        <li>• Chrome: Settings → Privacy and security → Cookies</li>
                        <li>• Firefox: Options → Privacy & Security → Cookies</li>
                        <li>• Safari: Preferences → Privacy → Cookies</li>
                        <li>• Edge: Settings → Privacy, search, and services → Cookies</li>
                      </ul>
                    </div>
                  </CardContent>
                </Card>
              </motion.div>

              {/* Third-Party Cookies */}
              <motion.div
                id="third-party"
                initial="hidden"
                whileInView="visible"
                viewport={{ once: true }}
                variants={itemVariants}
              >
                <Card>
                  <CardHeader>
                    <CardTitle>Third-Party Cookies</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <p className="text-muted-foreground">
                      In addition to our own cookies, we may also use various third-party cookies to report usage statistics, deliver advertisements, and provide other services.
                    </p>
                    <p className="text-muted-foreground">
                      These third-party cookies are subject to the respective privacy policies of those third parties. We do not control these cookies.
                    </p>
                  </CardContent>
                </Card>
              </motion.div>

              {/* Policy Updates */}
              <motion.div
                id="updates"
                initial="hidden"
                whileInView="visible"
                viewport={{ once: true }}
                variants={itemVariants}
              >
                <Card>
                  <CardHeader>
                    <CardTitle>Policy Updates</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground">
                      We may update this Cookie Policy from time to time to reflect changes in our practices or for other operational, legal, or regulatory reasons. We will notify you of any material changes by posting the new Cookie Policy on this page.
                    </p>
                  </CardContent>
                </Card>
              </motion.div>

              {/* Contact */}
              <motion.div
                initial="hidden"
                whileInView="visible"
                viewport={{ once: true }}
                variants={itemVariants}
              >
                <Card>
                  <CardHeader>
                    <CardTitle>Questions About Cookies?</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground mb-4">
                      If you have any questions about our use of cookies, please contact us at{' '}
                      <a href="mailto:privacy@vendo.com" className="text-primary hover:underline">
                        privacy@vendo.com
                      </a>
                    </p>
                  </CardContent>
                </Card>
              </motion.div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

