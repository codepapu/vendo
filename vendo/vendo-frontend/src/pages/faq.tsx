import { useState } from 'react';
import { motion } from 'framer-motion';
import { HelpCircle, ChevronDown, ChevronUp, Search } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';

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

interface FAQItem {
  question: string;
  answer: string;
  category: string;
}

const faqData: FAQItem[] = [
  {
    category: 'General',
    question: 'What is Vendo?',
    answer: 'Vendo is an online marketplace that connects customers with quality products from trusted retailers. We provide a secure, easy-to-use platform for shopping and selling.',
  },
  {
    category: 'General',
    question: 'How do I create an account?',
    answer: 'Click on the "Register" button in the top right corner, fill in your information, and verify your email address. It only takes a few minutes!',
  },
  {
    category: 'Orders',
    question: 'How do I place an order?',
    answer: 'Browse our products, add items to your cart, and proceed to checkout. You\'ll need to provide shipping and payment information to complete your order.',
  },
  {
    category: 'Orders',
    question: 'Can I modify or cancel my order?',
    answer: 'You can modify or cancel your order within 24 hours of placing it, as long as it hasn\'t been shipped. Contact our customer support for assistance.',
  },
  {
    category: 'Orders',
    question: 'How long does shipping take?',
    answer: 'Shipping times vary depending on your location and the retailer. Standard shipping typically takes 3-7 business days. Express shipping options are available at checkout.',
  },
  {
    category: 'Orders',
    question: 'How can I track my order?',
    answer: 'Once your order ships, you\'ll receive a tracking number via email. You can also view your order status in your account dashboard.',
  },
  {
    category: 'Payments',
    question: 'What payment methods do you accept?',
    answer: 'We accept all major credit cards, debit cards, PayPal, and other secure payment methods. All transactions are encrypted and secure.',
  },
  {
    category: 'Payments',
    question: 'Is my payment information secure?',
    answer: 'Yes, we use industry-standard encryption to protect your payment information. We never store your full credit card details on our servers.',
  },
  {
    category: 'Returns',
    question: 'What is your return policy?',
    answer: 'You can return most items within 30 days of purchase, provided they are in original condition with tags attached. Some items may be non-refundable.',
  },
  {
    category: 'Returns',
    question: 'How do I return an item?',
    answer: 'Go to your order history, select the item you want to return, and follow the return instructions. You\'ll receive a return label and instructions.',
  },
  {
    category: 'Account',
    question: 'How do I update my account information?',
    answer: 'Log in to your account and go to the Profile section. You can update your personal information, addresses, and preferences there.',
  },
  {
    category: 'Account',
    question: 'I forgot my password. How do I reset it?',
    answer: 'Click on "Forgot Password" on the login page, enter your email address, and we\'ll send you a link to reset your password.',
  },
  {
    category: 'Retailers',
    question: 'How do I become a retailer?',
    answer: 'Click on "Become a Retailer" in the footer or go to the retailer registration page. Fill out the application form and our team will review it.',
  },
  {
    category: 'Retailers',
    question: 'What are the fees for retailers?',
    answer: 'Our fee structure is competitive and transparent. Contact our sales team for detailed information about commission rates and fees.',
  },
];

export function FAQ() {
  const [searchQuery, setSearchQuery] = useState('');
  const [openItems, setOpenItems] = useState<number[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('All');

  const categories = ['All', ...Array.from(new Set(faqData.map((item) => item.category)))];

  const filteredFAQs = faqData.filter((item) => {
    const matchesSearch =
      item.question.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.answer.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = selectedCategory === 'All' || item.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const toggleItem = (index: number) => {
    setOpenItems((prev) =>
      prev.includes(index) ? prev.filter((i) => i !== index) : [...prev, index]
    );
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
            <HelpCircle className="h-16 w-16 text-primary mx-auto" />
            <h1 className="text-4xl md:text-6xl font-bold tracking-tight">
              Frequently Asked <span className="bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">Questions</span>
            </h1>
            <p className="text-xl text-muted-foreground">
              Find answers to common questions about Vendo
            </p>
          </motion.div>
        </div>
      </section>

      {/* Search and Filters */}
      <section className="py-8 bg-background border-b">
        <div className="container max-w-4xl">
          <div className="space-y-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-muted-foreground" />
              <Input
                type="text"
                placeholder="Search for answers..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10"
              />
            </div>
            <div className="flex flex-wrap gap-2">
              {categories.map((category) => (
                <Button
                  key={category}
                  variant={selectedCategory === category ? 'default' : 'outline'}
                  onClick={() => setSelectedCategory(category)}
                  size="sm"
                >
                  {category}
                </Button>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* FAQ Items */}
      <section className="py-16 bg-background">
        <div className="container max-w-4xl">
          <div className="space-y-4">
            {filteredFAQs.length > 0 ? (
              filteredFAQs.map((faq, index) => {
                const isOpen = openItems.includes(index);
                return (
                  <motion.div
                    key={index}
                    initial="hidden"
                    whileInView="visible"
                    viewport={{ once: true }}
                    variants={itemVariants}
                  >
                    <Card className="cursor-pointer hover:shadow-md transition-shadow" onClick={() => toggleItem(index)}>
                      <CardHeader>
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-2">
                              <span className="text-xs font-medium text-primary bg-primary/10 px-2 py-1 rounded">
                                {faq.category}
                              </span>
                            </div>
                            <CardTitle className="text-left">{faq.question}</CardTitle>
                          </div>
                          <Button variant="ghost" size="sm" className="ml-4">
                            {isOpen ? (
                              <ChevronUp className="h-5 w-5" />
                            ) : (
                              <ChevronDown className="h-5 w-5" />
                            )}
                          </Button>
                        </div>
                      </CardHeader>
                      {isOpen && (
                        <CardContent>
                          <p className="text-muted-foreground leading-relaxed">{faq.answer}</p>
                        </CardContent>
                      )}
                    </Card>
                  </motion.div>
                );
              })
            ) : (
              <Card>
                <CardContent className="py-12 text-center">
                  <p className="text-muted-foreground">
                    No FAQs found matching your search. Try different keywords or categories.
                  </p>
                </CardContent>
              </Card>
            )}
          </div>

          {/* Still Need Help */}
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
            className="mt-12"
          >
            <Card>
              <CardHeader>
                <CardTitle>Still Need Help?</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground mb-4">
                  Can't find the answer you're looking for? Our support team is here to help.
                </p>
                <div className="flex flex-wrap gap-4">
                  <Button asChild>
                    <a href="/contact">Contact Us</a>
                  </Button>
                  <Button variant="outline" asChild>
                    <a href="mailto:support@vendo.com">Email Support</a>
                  </Button>
                </div>
              </CardContent>
            </Card>
          </motion.div>
        </div>
      </section>
    </div>
  );
}

