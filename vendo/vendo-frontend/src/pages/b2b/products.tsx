import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { useB2BStore } from '@/stores/b2b-store';
import { productApi } from '@/services/product-api';
import { B2BPricing } from '@/types/b2b';

export default function B2BProducts() {
  const { getPricing } = useB2BStore();
  const [selectedProduct, setSelectedProduct] = useState<number | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [pricing, setPricing] = useState<B2BPricing | null>(null);

  const { data: products } = useQuery({
    queryKey: ['products'],
    queryFn: () => productApi.getProducts(),
  });

  const handleGetPricing = async (productId: number) => {
    try {
      const price = await getPricing(productId, quantity);
      setPricing(price);
      setSelectedProduct(productId);
    } catch (error) {
      console.error('Failed to get pricing:', error);
    }
  };

  return (
    <div className="container mx-auto py-8">
      <h1 className="text-3xl font-bold mb-6">B2B Product Catalog</h1>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="md:col-span-2">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {products?.content.map((product) => {
              const productQuantity = selectedProduct === product.id ? quantity : 1;
              const hasPricing = selectedProduct === product.id && pricing;
              return (
                <Card key={product.id}>
                  <CardHeader>
                    <CardTitle className="text-lg">{product.name}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-2xl font-bold">${product.price}</p>
                    <div className="mt-4 space-y-2">
                      <Input
                        type="number"
                        min="1"
                        value={productQuantity}
                        onChange={(e) => {
                          const qty = parseInt(e.target.value) || 1;
                          if (selectedProduct === product.id) {
                            setQuantity(qty);
                          } else {
                            setSelectedProduct(product.id);
                            setQuantity(qty);
                          }
                        }}
                        placeholder="Quantity"
                      />
                      <Button
                        className="w-full"
                        onClick={() => handleGetPricing(product.id)}
                      >
                        Get B2B Pricing
                      </Button>
                      {hasPricing && (
                        <Button
                          className="w-full"
                          variant="default"
                          onClick={() => {
                            // Add to cart with B2B pricing
                            console.log('Add to cart with B2B pricing:', {
                              productId: product.id,
                              quantity: productQuantity,
                              price: pricing.unitPrice,
                              totalPrice: pricing.totalPrice,
                            });
                          }}
                        >
                          Add to Cart (B2B Pricing)
                        </Button>
                      )}
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </div>

        <div>
          {pricing && selectedProduct && (
            <Card>
              <CardHeader>
                <CardTitle>Volume Pricing</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  <p>Quantity: {quantity}</p>
                  <p>Unit Price: ${pricing.unitPrice.toFixed(2)}</p>
                  <p>Discount: {pricing.discount.toFixed(2)}%</p>
                  <p className="text-2xl font-bold">
                    Total: ${pricing.totalPrice.toFixed(2)}
                  </p>
                  {pricing.priceTier && (
                    <div className="mt-4 p-2 bg-muted rounded">
                      <p className="text-sm">
                        Tier: {pricing.priceTier.minQuantity} - {pricing.priceTier.maxQuantity || '∞'}
                      </p>
                    </div>
                  )}
                  <Button
                    className="w-full mt-4"
                    onClick={() => {
                      // Add to cart with B2B pricing
                      // TODO: Implement cart API call with B2B pricing
                      console.log('Add to cart with B2B pricing:', pricing);
                    }}
                  >
                    Add to Cart (B2B Pricing)
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}

