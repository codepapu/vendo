import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
Dialog,
DialogContent,
DialogDescription,
DialogFooter,
DialogHeader,
DialogTitle,
DialogTrigger,
} from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { useRetailerStore } from '@/stores/retailer-store';
import { retailerApi } from '@/services/retailer-api';
import { productApi } from '@/services/product-api';
import type { ProductResponse } from '@/types';
import type { ProductListingRequest } from '@/types/retailer';

export default function RetailerProducts() {
  const { currentRetailer, products, loadProducts, updateProduct, isLoadingProducts } = useRetailerStore();
  const [searchTerm, setSearchTerm] = useState('');
  const [isActiveFilter, setIsActiveFilter] = useState<boolean | undefined>(undefined);
  const [isAddProductOpen, setIsAddProductOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<ProductResponse | null>(null);
  const [productFormData, setProductFormData] = useState<Partial<ProductListingRequest>>({});

  useEffect(() => {
    if (currentRetailer?.id) {
      loadProducts(currentRetailer.id, {
        searchTerm: searchTerm || undefined,
        isActive: isActiveFilter,
      });
    }
  }, [currentRetailer?.id, searchTerm, isActiveFilter]);

  // Fetch available products for adding
  const { data: availableProducts } = useQuery({
    queryKey: ['available-products'],
    queryFn: () => productApi.getAllProducts({ status: 'PUBLISHED' }),
    enabled: isAddProductOpen,
  });

  const handleAddProduct = async () => {
    if (!currentRetailer?.id || !selectedProduct || !productFormData.price) {
      return;
    }

    const listingData: ProductListingRequest = {
      productId: selectedProduct.id,
      retailerId: currentRetailer.id,
      price: productFormData.price,
      compareAtPrice: productFormData.compareAtPrice,
      costPrice: productFormData.costPrice,
      stockQuantity: productFormData.stockQuantity || 0,
      lowStockThreshold: productFormData.lowStockThreshold || 10,
      isActive: productFormData.isActive !== false,
    };

    const result = await retailerApi.addProduct(currentRetailer.id, listingData);
    if (result) {
      setIsAddProductOpen(false);
      setSelectedProduct(null);
      setProductFormData({});
      loadProducts(currentRetailer.id);
    }
  };

  const handleUpdatePrice = async (productRetailerId: number, newPrice: number) => {
    if (!currentRetailer?.id) return;
    await updateProduct(currentRetailer.id, productRetailerId, { price: newPrice });
  };

  const handleUpdateStock = async (productRetailerId: number, newStock: number) => {
    if (!currentRetailer?.id) return;
    await updateProduct(currentRetailer.id, productRetailerId, { stockQuantity: newStock });
  };

  const handleToggleActive = async (productRetailerId: number, isActive: boolean) => {
    if (!currentRetailer?.id) return;
    await updateProduct(currentRetailer.id, productRetailerId, { isActive });
  };

  if (!currentRetailer) {
    return (
      <div className="container mx-auto py-8">
        <Card>
          <CardContent className="pt-6">
            <p>You are not registered as a retailer yet.</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Product Management</h1>
        <Dialog open={isAddProductOpen} onOpenChange={setIsAddProductOpen}>
          <DialogTrigger asChild>
            <Button>Add Product</Button>
          </DialogTrigger>
          <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Add Product to Store</DialogTitle>
              <DialogDescription>
                Select a product and set your pricing and stock information
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="space-y-2">
                <Label>Select Product</Label>
                <div className="max-h-60 overflow-y-auto border rounded p-2">
                  {availableProducts?.content.map((product) => (
                    <div
                      key={product.id}
                      className={`p-2 border rounded mb-2 cursor-pointer hover:bg-muted ${
                        selectedProduct?.id === product.id ? 'bg-muted' : ''
                      }`}
                      onClick={() => {
                        setSelectedProduct(product);
                        setProductFormData({
                          price: product.price || 0,
                          compareAtPrice: product.salePrice,
                        });
                      }}
                    >
                      <p className="font-medium">{product.name}</p>
                      <p className="text-sm text-muted-foreground">SKU: {product.sku}</p>
                    </div>
                  ))}
                </div>
              </div>

              {selectedProduct && (
                <div className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="price">Price *</Label>
                    <Input
                      id="price"
                      type="number"
                      step="0.01"
                      min="0"
                      value={productFormData.price || ''}
                      onChange={(e) =>
                        setProductFormData({
                          ...productFormData,
                          price: parseFloat(e.target.value) || 0,
                        })
                      }
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="compareAtPrice">Compare At Price</Label>
                    <Input
                      id="compareAtPrice"
                      type="number"
                      step="0.01"
                      min="0"
                      value={productFormData.compareAtPrice || ''}
                      onChange={(e) =>
                        setProductFormData({
                          ...productFormData,
                          compareAtPrice: parseFloat(e.target.value) || undefined,
                        })
                      }
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="costPrice">Cost Price</Label>
                    <Input
                      id="costPrice"
                      type="number"
                      step="0.01"
                      min="0"
                      value={productFormData.costPrice || ''}
                      onChange={(e) =>
                        setProductFormData({
                          ...productFormData,
                          costPrice: parseFloat(e.target.value) || undefined,
                        })
                      }
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="stockQuantity">Stock Quantity</Label>
                    <Input
                      id="stockQuantity"
                      type="number"
                      min="0"
                      value={productFormData.stockQuantity || 0}
                      onChange={(e) =>
                        setProductFormData({
                          ...productFormData,
                          stockQuantity: parseInt(e.target.value) || 0,
                        })
                      }
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="lowStockThreshold">Low Stock Threshold</Label>
                    <Input
                      id="lowStockThreshold"
                      type="number"
                      min="0"
                      value={productFormData.lowStockThreshold || 10}
                      onChange={(e) =>
                        setProductFormData({
                          ...productFormData,
                          lowStockThreshold: parseInt(e.target.value) || 10,
                        })
                      }
                    />
                  </div>
                </div>
              )}
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setIsAddProductOpen(false)}>
                Cancel
              </Button>
              <Button
                onClick={handleAddProduct}
                disabled={!selectedProduct || !productFormData.price}
              >
                Add Product
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {/* Filters */}
      <div className="flex gap-4 mb-6">
        <Input
          placeholder="Search products..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="max-w-sm"
        />
        <Button
          variant={isActiveFilter === undefined ? 'default' : 'outline'}
          onClick={() => setIsActiveFilter(undefined)}
        >
          All
        </Button>
        <Button
          variant={isActiveFilter === true ? 'default' : 'outline'}
          onClick={() => setIsActiveFilter(true)}
        >
          Active
        </Button>
        <Button
          variant={isActiveFilter === false ? 'default' : 'outline'}
          onClick={() => setIsActiveFilter(false)}
        >
          Inactive
        </Button>
      </div>

      {/* Products List */}
      {isLoadingProducts ? (
        <p>Loading products...</p>
      ) : products.length === 0 ? (
        <Card>
          <CardContent className="pt-6">
            <p className="text-center text-muted-foreground">No products found</p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {products.map((product) => (
            <Card key={product.id}>
              <CardHeader>
                <div className="flex justify-between items-start">
                  <div>
                    <CardTitle className="text-lg">{product.product?.name || 'Unknown Product'}</CardTitle>
                    <p className="text-sm text-muted-foreground">SKU: {product.product?.sku}</p>
                  </div>
                  <div className="flex gap-2">
                    <Badge variant={product.isActive ? 'default' : 'secondary'}>
                      {product.isActive ? 'Active' : 'Inactive'}
                    </Badge>
                    {product.isLowStock && (
                      <Badge variant="destructive">Low Stock</Badge>
                    )}
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div>
                    <p className="text-sm text-muted-foreground">Price</p>
                    <p className="text-xl font-bold">${product.price.toFixed(2)}</p>
                    {product.compareAtPrice && (
                      <p className="text-sm text-muted-foreground line-through">
                        ${product.compareAtPrice.toFixed(2)}
                      </p>
                    )}
                  </div>

                  <div>
                    <p className="text-sm text-muted-foreground">Stock</p>
                    <p className={`text-lg font-semibold ${
                      product.isLowStock ? 'text-red-500' : ''
                    }`}>
                      {product.stockQuantity} units
                    </p>
                    <p className="text-xs text-muted-foreground">
                      Low stock threshold: {product.lowStockThreshold}
                    </p>
                  </div>

                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => {
                        const newPrice = prompt('Enter new price:', product.price.toString());
                        if (newPrice) {
                          handleUpdatePrice(product.id, parseFloat(newPrice));
                        }
                      }}
                    >
                      Edit Price
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => {
                        const newStock = prompt('Enter new stock quantity:', product.stockQuantity.toString());
                        if (newStock) {
                          handleUpdateStock(product.id, parseInt(newStock));
                        }
                      }}
                    >
                      Update Stock
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleToggleActive(product.id, !product.isActive)}
                    >
                      {product.isActive ? 'Deactivate' : 'Activate'}
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

