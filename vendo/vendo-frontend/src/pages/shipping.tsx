import { motion } from 'framer-motion';
import { Truck, Package, Clock, MapPin, CheckCircle } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.5 },
  },
};

const shippingMethods = [
  {
    name: 'Standard Shipping',
    price: 5.99,
    deliveryTime: '5-7 business days',
    description: 'Standard shipping for most items',
  },
  {
    name: 'Express Shipping',
    price: 12.99,
    deliveryTime: '2-3 business days',
    description: 'Faster delivery for urgent orders',
  },
  {
    name: 'Overnight Shipping',
    price: 24.99,
    deliveryTime: '1 business day',
    description: 'Next day delivery (order before 2 PM)',
  },
  {
    name: 'Free Shipping',
    price: 0,
    deliveryTime: '5-7 business days',
    description: 'Free shipping on orders over $50',
    badge: 'Popular',
  },
];

const shippingZones = [
  { zone: 'Domestic (US)', deliveryTime: '5-7 business days', price: 'From $5.99' },
  { zone: 'Canada', deliveryTime: '7-10 business days', price: 'From $12.99' },
  { zone: 'International', deliveryTime: '10-21 business days', price: 'From $19.99' },
];

export function Shipping() {
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
            <Truck className="h-16 w-16 text-primary mx-auto" />
            <h1 className="text-4xl md:text-6xl font-bold tracking-tight">
              Shipping <span className="bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">Information</span>
            </h1>
            <p className="text-xl text-muted-foreground">
              Everything you need to know about shipping and delivery
            </p>
          </motion.div>
        </div>
      </section>

      {/* Main Content */}
      <section className="py-16 bg-background">
        <div className="container max-w-6xl space-y-12">
          {/* Shipping Methods */}
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
          >
            <h2 className="text-3xl font-bold mb-6">Shipping Methods</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {shippingMethods.map((method, index) => (
                <Card key={index} className="relative">
                  {method.badge && (
                    <Badge className="absolute top-4 right-4">{method.badge}</Badge>
                  )}
                  <CardHeader>
                    <CardTitle className="text-lg">{method.name}</CardTitle>
                    <CardDescription>{method.description}</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-2">
                      <div className="flex items-baseline gap-2">
                        <span className="text-2xl font-bold">
                          {method.price === 0 ? 'Free' : `$${method.price.toFixed(2)}`}
                        </span>
                      </div>
                      <div className="flex items-center gap-2 text-sm text-muted-foreground">
                        <Clock className="h-4 w-4" />
                        <span>{method.deliveryTime}</span>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </motion.div>

          {/* Shipping Zones */}
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
          >
            <h2 className="text-3xl font-bold mb-6">Shipping Zones</h2>
            <Card>
              <CardContent className="p-0">
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead>
                      <tr className="border-b">
                        <th className="text-left p-4 font-semibold">Zone</th>
                        <th className="text-left p-4 font-semibold">Delivery Time</th>
                        <th className="text-left p-4 font-semibold">Price</th>
                      </tr>
                    </thead>
                    <tbody>
                      {shippingZones.map((zone, index) => (
                        <tr key={index} className="border-b">
                          <td className="p-4">{zone.zone}</td>
                          <td className="p-4">{zone.deliveryTime}</td>
                          <td className="p-4">{zone.price}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </CardContent>
            </Card>
          </motion.div>

          {/* How to Track */}
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
          >
            <h2 className="text-3xl font-bold mb-6">How to Track Your Order</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <Card>
                <CardHeader>
                  <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center mb-4">
                    <Package className="h-6 w-6 text-primary" />
                  </div>
                  <CardTitle>Step 1</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    After placing your order, you'll receive a confirmation email with your order number.
                  </p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center mb-4">
                    <Truck className="h-6 w-6 text-primary" />
                  </div>
                  <CardTitle>Step 2</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Once your order ships, you'll receive a shipping confirmation email with a tracking number.
                  </p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center mb-4">
                    <MapPin className="h-6 w-6 text-primary" />
                  </div>
                  <CardTitle>Step 3</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Use the tracking number to monitor your package's journey on our tracking page.
                  </p>
                </CardContent>
              </Card>
            </div>
          </motion.div>

          {/* Important Information */}
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            variants={itemVariants}
          >
            <Card className="bg-muted/50">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <CheckCircle className="h-5 w-5 text-primary" />
                  Important Information
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <h4 className="font-semibold mb-2">Processing Time</h4>
                  <p className="text-muted-foreground">
                    Most orders are processed within 1-2 business days. Orders placed on weekends or holidays
                    will be processed on the next business day.
                  </p>
                </div>
                <div>
                  <h4 className="font-semibold mb-2">Delivery Delays</h4>
                  <p className="text-muted-foreground">
                    During peak seasons or due to weather conditions, delivery times may be extended. We'll
                    notify you if there are any significant delays.
                  </p>
                </div>
                <div>
                  <h4 className="font-semibold mb-2">International Shipping</h4>
                  <p className="text-muted-foreground">
                    International orders may be subject to customs fees and import duties, which are the
                    responsibility of the customer. Delivery times vary by country.
                  </p>
                </div>
                <div>
                  <h4 className="font-semibold mb-2">Undeliverable Packages</h4>
                  <p className="text-muted-foreground">
                    If a package is returned to us as undeliverable, we'll contact you to arrange reshipment.
                    Additional shipping fees may apply.
                  </p>
                </div>
              </CardContent>
            </Card>
          </motion.div>
        </div>
      </section>
    </div>
  );
}

