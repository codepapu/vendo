import { motion } from 'framer-motion';
import { Scale, FileCheck, AlertCircle, CheckCircle, List } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

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

export function Terms() {
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
            <Scale className="h-16 w-16 text-primary mx-auto" />
            <h1 className="text-4xl md:text-6xl font-bold tracking-tight">
              Terms of <span className="bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">Service</span>
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
                  <a href="#agreement" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Agreement to Terms
                  </a>
                  <a href="#use-license" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Use License
                  </a>
                  <a href="#account" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Account Registration
                  </a>
                  <a href="#products" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Products and Pricing
                  </a>
                  <a href="#payment" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Payment Terms
                  </a>
                  <a href="#returns" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Returns and Refunds
                  </a>
                  <a href="#prohibited" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Prohibited Uses
                  </a>
                  <a href="#ip" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Intellectual Property
                  </a>
                  <a href="#liability" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Limitation of Liability
                  </a>
                  <a href="#governing" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Governing Law
                  </a>
                  <a href="#changes" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Changes to Terms
                  </a>
                  <a href="#contact" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Contact Information
                  </a>
                </CardContent>
              </Card>
            </div>

            {/* Main Content */}
            <div className="lg:col-span-3">
              <motion.div
                initial="hidden"
                whileInView="visible"
                viewport={{ once: true }}
                variants={itemVariants}
                className="space-y-8"
              >
                <Card id="agreement">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <FileCheck className="h-5 w-5" />
                      Agreement to Terms
                    </CardTitle>
                  </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-muted-foreground">
                  By accessing or using Vendo's website and services, you agree to be bound by
                  these Terms of Service and all applicable laws and regulations. If you do not
                  agree with any of these terms, you are prohibited from using or accessing this site.
                </p>
              </CardContent>
            </Card>

                <Card id="use-license">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <CheckCircle className="h-5 w-5" />
                      Use License
                    </CardTitle>
                  </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-muted-foreground">
                  Permission is granted to temporarily download one copy of the materials on
                  Vendo's website for personal, non-commercial transitory viewing only. This is
                  the grant of a license, not a transfer of title, and under this license you may not:
                </p>
                <ul className="list-disc list-inside text-muted-foreground space-y-1 ml-4">
                  <li>Modify or copy the materials</li>
                  <li>Use the materials for any commercial purpose</li>
                  <li>Attempt to reverse engineer any software contained on the website</li>
                  <li>Remove any copyright or other proprietary notations from the materials</li>
                </ul>
              </CardContent>
            </Card>

                <Card id="account">
                  <CardHeader>
                    <CardTitle>Account Registration</CardTitle>
                  </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-muted-foreground">
                  To access certain features of our service, you may be required to register for
                  an account. You agree to:
                </p>
                <ul className="list-disc list-inside text-muted-foreground space-y-1 ml-4">
                  <li>Provide accurate, current, and complete information</li>
                  <li>Maintain and update your information to keep it accurate</li>
                  <li>Maintain the security of your password and identification</li>
                  <li>Accept all responsibility for activities that occur under your account</li>
                  <li>Notify us immediately of any unauthorized use of your account</li>
                </ul>
              </CardContent>
            </Card>

                <Card id="products">
                  <CardHeader>
                    <CardTitle>Products and Pricing</CardTitle>
                  </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-muted-foreground">
                  We strive to provide accurate product descriptions and pricing. However:
                </p>
                <ul className="list-disc list-inside text-muted-foreground space-y-1 ml-4">
                  <li>We reserve the right to correct any errors in pricing or product information</li>
                  <li>We reserve the right to refuse or cancel any order at any time</li>
                  <li>Product availability is subject to change without notice</li>
                  <li>Prices are subject to change without notice</li>
                </ul>
              </CardContent>
            </Card>

                <Card id="payment">
                  <CardHeader>
                    <CardTitle>Payment Terms</CardTitle>
                  </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-muted-foreground">
                  All payments must be made through our approved payment methods. By making a
                  purchase, you agree to:
                </p>
                <ul className="list-disc list-inside text-muted-foreground space-y-1 ml-4">
                  <li>Provide valid payment information</li>
                  <li>Authorize us to charge your payment method for the total amount</li>
                  <li>Pay all applicable taxes and fees</li>
                  <li>Accept that all sales are final unless otherwise stated</li>
                </ul>
              </CardContent>
            </Card>

                <Card id="returns">
                  <CardHeader>
                    <CardTitle>Returns and Refunds</CardTitle>
                  </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-muted-foreground">
                  Our return and refund policy is as follows:
                </p>
                <ul className="list-disc list-inside text-muted-foreground space-y-1 ml-4">
                  <li>Items must be returned within 30 days of purchase</li>
                  <li>Items must be in original condition with tags attached</li>
                  <li>Refunds will be processed to the original payment method</li>
                  <li>Shipping costs are non-refundable unless the item was defective</li>
                  <li>Some items may be non-refundable (e.g., personalized items)</li>
                </ul>
              </CardContent>
            </Card>

                <Card id="prohibited">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <AlertCircle className="h-5 w-5" />
                      Prohibited Uses
                    </CardTitle>
                  </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-muted-foreground">
                  You may not use our service:
                </p>
                <ul className="list-disc list-inside text-muted-foreground space-y-1 ml-4">
                  <li>For any unlawful purpose or to solicit others to perform unlawful acts</li>
                  <li>To violate any international, federal, provincial, or state regulations</li>
                  <li>To infringe upon or violate our intellectual property rights</li>
                  <li>To harass, abuse, insult, harm, or discriminate against others</li>
                  <li>To submit false or misleading information</li>
                  <li>To upload or transmit viruses or any other type of malicious code</li>
                </ul>
              </CardContent>
            </Card>

                <Card id="ip">
                  <CardHeader>
                    <CardTitle>Intellectual Property</CardTitle>
                  </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  The service and its original content, features, and functionality are and will
                  remain the exclusive property of Vendo and its licensors. The service is
                  protected by copyright, trademark, and other laws.
                </p>
              </CardContent>
            </Card>

                <Card id="liability">
                  <CardHeader>
                    <CardTitle>Limitation of Liability</CardTitle>
                  </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  In no event shall Vendo, nor its directors, employees, partners, agents,
                  suppliers, or affiliates, be liable for any indirect, incidental, special,
                  consequential, or punitive damages, including without limitation, loss of
                  profits, data, use, goodwill, or other intangible losses, resulting from your
                  use of the service.
                </p>
              </CardContent>
            </Card>

                <Card id="governing">
                  <CardHeader>
                    <CardTitle>Governing Law</CardTitle>
                  </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  These Terms shall be interpreted and governed by the laws of the jurisdiction
                  in which Vendo operates, without regard to its conflict of law provisions.
                </p>
              </CardContent>
            </Card>

                <Card id="changes">
                  <CardHeader>
                    <CardTitle>Changes to Terms</CardTitle>
                  </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  We reserve the right, at our sole discretion, to modify or replace these Terms
                  at any time. If a revision is material, we will provide at least 30 days notice
                  prior to any new terms taking effect.
                </p>
              </CardContent>
            </Card>

                <Card id="contact">
                  <CardHeader>
                    <CardTitle>Contact Information</CardTitle>
                  </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  If you have any questions about these Terms of Service, please contact us at:
                </p>
                <p className="mt-2">
                  <strong>Email:</strong> legal@vendo.com<br />
                  <strong>Address:</strong> 123 Commerce Street, Business City, BC 12345
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

