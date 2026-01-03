import { motion } from 'framer-motion';
import { Shield, Lock, Eye, FileText, List } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useState } from 'react';

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

export function Privacy() {
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
            <Shield className="h-16 w-16 text-primary mx-auto" />
            <h1 className="text-4xl md:text-6xl font-bold tracking-tight">
              Privacy <span className="bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">Policy</span>
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
                  <a href="#introduction" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Introduction
                  </a>
                  <a href="#information-we-collect" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Information We Collect
                  </a>
                  <a href="#how-we-use" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    How We Use Your Information
                  </a>
                  <a href="#information-sharing" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Information Sharing
                  </a>
                  <a href="#data-security" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Data Security
                  </a>
                  <a href="#your-rights" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Your Rights
                  </a>
                  <a href="#cookies" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Cookies and Tracking
                  </a>
                  <a href="#changes" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Changes to Policy
                  </a>
                  <a href="#contact" className="block text-sm text-muted-foreground hover:text-foreground transition-colors">
                    Contact Us
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
                className="prose prose-lg dark:prose-invert max-w-none space-y-8"
              >
                <Card id="introduction">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <FileText className="h-5 w-5" />
                      Introduction
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <p className="text-muted-foreground">
                      At Vendo, we take your privacy seriously. This Privacy Policy explains how we
                      collect, use, disclose, and safeguard your information when you visit our
                      website and use our services.
                    </p>
                    <p className="text-muted-foreground">
                      Please read this privacy policy carefully. If you do not agree with the terms
                      of this privacy policy, please do not access the site.
                    </p>
                  </CardContent>
                </Card>

                <Card id="information-we-collect">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Eye className="h-5 w-5" />
                      Information We Collect
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                <div>
                  <h3 className="font-semibold mb-2">Personal Information</h3>
                  <p className="text-muted-foreground">
                    We may collect personal information that you voluntarily provide to us when
                    you register, make a purchase, or use our services, including:
                  </p>
                  <ul className="list-disc list-inside text-muted-foreground space-y-1 ml-4">
                    <li>Name and contact information</li>
                    <li>Email address</li>
                    <li>Billing and shipping addresses</li>
                    <li>Payment information</li>
                    <li>Phone number</li>
                  </ul>
                </div>
                <div>
                  <h3 className="font-semibold mb-2">Automatically Collected Information</h3>
                  <p className="text-muted-foreground">
                    When you visit our website, we automatically collect certain information
                    about your device, including:
                  </p>
                  <ul className="list-disc list-inside text-muted-foreground space-y-1 ml-4">
                    <li>IP address</li>
                    <li>Browser type and version</li>
                    <li>Pages you visit and time spent</li>
                    <li>Referring website addresses</li>
                  </ul>
                </div>
                  </CardContent>
                </Card>

                <Card id="how-we-use">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Lock className="h-5 w-5" />
                      How We Use Your Information
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                <p className="text-muted-foreground mb-4">
                  We use the information we collect to:
                </p>
                <ul className="list-disc list-inside text-muted-foreground space-y-1 ml-4">
                  <li>Process and fulfill your orders</li>
                  <li>Send you order confirmations and updates</li>
                  <li>Respond to your inquiries and provide customer support</li>
                  <li>Send you marketing communications (with your consent)</li>
                  <li>Improve our website and services</li>
                  <li>Detect and prevent fraud</li>
                  <li>Comply with legal obligations</li>
                </ul>
                  </CardContent>
                </Card>

                <Card id="information-sharing">
                  <CardHeader>
                    <CardTitle>Information Sharing and Disclosure</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                <p className="text-muted-foreground">
                  We do not sell your personal information. We may share your information only in
                  the following circumstances:
                </p>
                <ul className="list-disc list-inside text-muted-foreground space-y-1 ml-4">
                  <li>With service providers who assist us in operating our website</li>
                  <li>With payment processors to complete transactions</li>
                  <li>With shipping companies to deliver your orders</li>
                  <li>When required by law or to protect our rights</li>
                  <li>In connection with a business transfer or merger</li>
                </ul>
                  </CardContent>
                </Card>

                <Card id="data-security">
                  <CardHeader>
                    <CardTitle>Data Security</CardTitle>
                  </CardHeader>
                  <CardContent>
                <p className="text-muted-foreground">
                  We implement appropriate technical and organizational security measures to
                  protect your personal information. However, no method of transmission over
                  the Internet is 100% secure, and we cannot guarantee absolute security.
                </p>
                  </CardContent>
                </Card>

                <Card id="your-rights">
                  <CardHeader>
                    <CardTitle>Your Rights</CardTitle>
                  </CardHeader>
                  <CardContent>
                <p className="text-muted-foreground mb-4">
                  Depending on your location, you may have the following rights:
                </p>
                <ul className="list-disc list-inside text-muted-foreground space-y-1 ml-4">
                  <li>Access your personal information</li>
                  <li>Correct inaccurate information</li>
                  <li>Request deletion of your information</li>
                  <li>Object to processing of your information</li>
                  <li>Request data portability</li>
                  <li>Withdraw consent for data processing</li>
                </ul>
                  </CardContent>
                </Card>

                <Card id="cookies">
                  <CardHeader>
                    <CardTitle>Cookies and Tracking Technologies</CardTitle>
                  </CardHeader>
                  <CardContent>
                <p className="text-muted-foreground">
                  We use cookies and similar tracking technologies to track activity on our
                  website and store certain information. You can instruct your browser to refuse
                  all cookies or to indicate when a cookie is being sent.
                </p>
                  </CardContent>
                </Card>

                <Card id="changes">
                  <CardHeader>
                    <CardTitle>Changes to This Privacy Policy</CardTitle>
                  </CardHeader>
                  <CardContent>
                <p className="text-muted-foreground">
                  We may update our Privacy Policy from time to time. We will notify you of any
                  changes by posting the new Privacy Policy on this page and updating the
                  "Last updated" date.
                </p>
                  </CardContent>
                </Card>

                <Card id="contact">
                  <CardHeader>
                    <CardTitle>Contact Us</CardTitle>
                  </CardHeader>
                  <CardContent>
                <p className="text-muted-foreground">
                  If you have any questions about this Privacy Policy, please contact us at:
                </p>
                <p className="mt-2">
                  <strong>Email:</strong> privacy@vendo.com<br />
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

