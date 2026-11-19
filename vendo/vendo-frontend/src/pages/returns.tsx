import { motion } from 'framer-motion';
import { RotateCcw, CheckCircle, Clock, Package, AlertCircle, FileText } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Link } from 'react-router-dom';

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.5 },
  },
};

const returnSteps = [
  {
    step: 1,
    title: 'Initiate Return',
    description: 'Log into your account and go to your orders page. Click "Return Item" next to the order you want to return.',
    icon: FileText,
  },
  {
    step: 2,
    title: 'Get Return Label',
    description: 'We\'ll provide you with a prepaid return shipping label. Print it and attach it to your package.',
    icon: Package,
  },
  {
    step: 3,
    title: 'Ship Item Back',
    description: 'Pack the item securely in its original packaging and drop it off at any authorized shipping location.',
    icon: RotateCcw,
  },
  {
    step: 4,
    title: 'Receive Refund',
    description: 'Once we receive and inspect the item, we\'ll process your refund to your original payment method within 5-7 business days.',
    icon: CheckCircle,
  },
];

const returnPolicy = [
  {
    title: 'Return Window',
    description: 'Items must be returned within 30 days of delivery for a full refund.',
    icon: Clock,
  },
  {
    title: 'Condition Requirements',
    description: 'Items must be unused, unwashed, and in original packaging with all tags attached.',
    icon: CheckCircle,
  },
  {
    title: 'Non-Returnable Items',
    description: 'Personalized items, gift cards, and items marked as final sale cannot be returned.',
    icon: AlertCircle,
  },
  {
    title: 'Refund Processing',
    description: 'Refunds are processed to the original payment method within 5-7 business days after we receive the item.',
    icon: Package,
  },
];

export function Returns() {
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
            <RotateCcw className="h-16 w-16 text-primary mx-auto" />
            <h1 className="text-4xl md:text-6xl font-bold tracking-tight">
              Returns & <span className="bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">Refunds</span>
            </h1>
            <p className="text-xl text-muted-foreground">
              Easy returns and hassle-free refunds
            </p>
          </motion.div>
        </div>
      </section>

      {/* Main Content */}
      <section className="py-16 bg-background">
        <div className="container max-w-6xl space-y-12">
          {/* Return Process */}
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
          >
            <h2 className="text-3xl font-bold mb-6">How to Return an Item</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {returnSteps.map((step) => (
                <Card key={step.step} className="relative">
                  <div className="absolute -top-4 -left-4 h-8 w-8 rounded-full bg-primary text-primary-foreground flex items-center justify-center font-bold">
                    {step.step}
                  </div>
                  <CardHeader>
                    <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center mb-4">
                      <step.icon className="h-6 w-6 text-primary" />
                    </div>
                    <CardTitle className="text-lg">{step.title}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-muted-foreground">{step.description}</p>
                  </CardContent>
                </Card>
              ))}
            </div>
          </motion.div>

          {/* Return Policy */}
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
          >
            <h2 className="text-3xl font-bold mb-6">Return Policy</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {returnPolicy.map((policy, index) => (
                <Card key={index}>
                  <CardHeader>
                    <div className="flex items-center gap-3">
                      <policy.icon className="h-5 w-5 text-primary" />
                      <CardTitle className="text-lg">{policy.title}</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground">{policy.description}</p>
                  </CardContent>
                </Card>
              ))}
            </div>
          </motion.div>

          {/* Refund Information */}
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
          >
            <Card className="bg-muted/50">
              <CardHeader>
                <CardTitle>Refund Information</CardTitle>
                <CardDescription>What to expect when processing your refund</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <h4 className="font-semibold mb-2">Refund Timeline</h4>
                  <p className="text-muted-foreground">
                    Once we receive your returned item and verify its condition, we'll process your refund
                    within 5-7 business days. The refund will be issued to your original payment method.
                  </p>
                </div>
                <div>
                  <h4 className="font-semibold mb-2">Shipping Costs</h4>
                  <p className="text-muted-foreground">
                    Original shipping costs are non-refundable unless the item was defective or we made an error.
                    Return shipping is free for eligible returns.
                  </p>
                </div>
                <div>
                  <h4 className="font-semibold mb-2">Partial Refunds</h4>
                  <p className="text-muted-foreground">
                    If an item is returned in a condition that doesn't meet our return policy, we may issue
                    a partial refund or refuse the return.
                  </p>
                </div>
              </CardContent>
            </Card>
          </motion.div>

          {/* Exchange Information */}
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
          >
            <Card>
              <CardHeader>
                <CardTitle>Exchanges</CardTitle>
                <CardDescription>Need a different size or color?</CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground mb-4">
                  We currently don't offer direct exchanges. To exchange an item, please return the original
                  item for a refund and place a new order for the item you'd like.
                </p>
                <Button asChild>
                  <Link to="/returns/history">Start a Return</Link>
                </Button>
              </CardContent>
            </Card>
          </motion.div>

          {/* FAQ Section */}
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
          >
            <h2 className="text-3xl font-bold mb-6">Frequently Asked Questions</h2>
            <div className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">How long do I have to return an item?</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    You have 30 days from the delivery date to initiate a return. Items must be postmarked
                    within this timeframe.
                  </p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Do I need to pay for return shipping?</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    No, we provide a prepaid return shipping label for all eligible returns. Simply print
                    the label and attach it to your package.
                  </p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">What if my item arrives damaged or defective?</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    If your item arrives damaged or defective, please contact our support team immediately.
                    We'll arrange a free return and full refund, including original shipping costs.
                  </p>
                </CardContent>
              </Card>
            </div>
          </motion.div>
        </div>
      </section>
    </div>
  );
}

