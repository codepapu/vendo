# API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <access_token>
```

## API Endpoints

### Authentication

#### Register User
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "username": "username",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "userType": "CUSTOMER" // CUSTOMER, BUSINESS, RETAILER
}
```

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "emailOrUsername": "user@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here",
    "tokenType": "Bearer",
    "expiresAt": "2024-12-23T00:00:00",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "username": "username",
      "firstName": "John",
      "lastName": "Doe",
      "userType": "CUSTOMER"
    }
  }
}
```

### B2B Endpoints

#### Create Business Account
```http
POST /api/v1/b2b/accounts
Authorization: Bearer <token>
Content-Type: application/json

{
  "companyName": "Acme Corp",
  "businessRegistrationNumber": "REG123456",
  "taxId": "TAX789",
  "businessType": "CORPORATE",
  "creditLimit": 100000.00,
  "creditTermsDays": 30,
  "billingAddress": {
    "addressLine1": "123 Business St",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA"
  }
}
```

#### Get B2B Pricing
```http
GET /api/v1/b2b/pricing/{productId}?quantity=100
Authorization: Bearer <token>
```

Response:
```json
{
  "success": true,
  "data": {
    "productId": 1,
    "quantity": 100,
    "unitPrice": 45.00,
    "discount": 10.00,
    "discountPercentage": 10.0,
    "totalPrice": 4050.00,
    "pricingTier": "VOLUME_100"
  }
}
```

#### Create Purchase Order
```http
POST /api/v1/b2b/purchase-orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderId": 123,
  "notes": "Urgent order for Q1 inventory"
}
```

#### Get Invoices
```http
GET /api/v1/b2b/invoices?status=PENDING&page=0&size=20
Authorization: Bearer <token>
```

### Retailer Endpoints

#### Register Retailer
```http
POST /api/v1/retailers/register
Authorization: Bearer <token>
Content-Type: application/json

{
  "storeName": "My Store",
  "storeSlug": "my-store",
  "description": "Best products in town",
  "commissionRate": 15.00,
  "bankAccount": {
    "accountHolderName": "My Store",
    "accountNumber": "123456789",
    "bankName": "Bank Name",
    "ifscCode": "IFSC123"
  }
}
```

#### List Retailer Products
```http
GET /api/v1/retailers/{retailerId}/products?page=0&size=20
```

#### Add Product to Retailer
```http
POST /api/v1/retailers/{retailerId}/products
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": 1,
  "price": 99.99,
  "compareAtPrice": 129.99,
  "costPrice": 50.00,
  "stockQuantity": 100,
  "lowStockThreshold": 10
}
```

#### Get Retailer Dashboard
```http
GET /api/v1/retailers/dashboard
Authorization: Bearer <token>
```

Response:
```json
{
  "success": true,
  "data": {
    "totalSales": 50000.00,
    "totalOrders": 150,
    "pendingOrders": 5,
    "totalProducts": 45,
    "lowStockProducts": 3,
    "recentOrders": [...],
    "salesChart": {
      "labels": ["Jan", "Feb", "Mar"],
      "data": [10000, 15000, 25000]
    }
  }
}
```

### Product Endpoints

#### List Products
```http
GET /api/v1/products?page=0&size=20&categoryId=1&search=laptop
```

#### Get Product Details
```http
GET /api/v1/products/{id}
```

#### Get B2B Pricing for Product
```http
GET /api/v1/products/{id}/b2b-pricing
Authorization: Bearer <token>
```

### Order Endpoints

#### Create Order
```http
POST /api/v1/orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderType": "B2C", // B2C, B2B, RETAILER
  "items": [
    {
      "productId": 1,
      "retailerId": 1, // For marketplace orders
      "quantity": 2
    }
  ],
  "shippingAddressId": 1,
  "billingAddressId": 1,
  "shippingMethodId": 1,
  "paymentMethod": "CREDIT_CARD"
}
```

#### Get Orders
```http
GET /api/v1/orders?status=CONFIRMED&page=0&size=20
Authorization: Bearer <token>
```

#### Get Order Details
```http
GET /api/v1/orders/{id}
Authorization: Bearer <token>
```

### Cart Endpoints

#### Get Cart
```http
GET /api/v1/cart
Authorization: Bearer <token>
```

#### Add to Cart
```http
POST /api/v1/cart/items
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": 1,
  "retailerId": 1, // Optional, for marketplace
  "quantity": 1
}
```

#### Update Cart Item
```http
PUT /api/v1/cart/items/{itemId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "quantity": 3
}
```

#### Remove from Cart
```http
DELETE /api/v1/cart/items/{itemId}
Authorization: Bearer <token>
```

### Payment Endpoints

#### Process Payment
```http
POST /api/v1/payments
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderId": 123,
  "paymentMethod": "CREDIT_CARD",
  "paymentGateway": "STRIPE",
  "amount": 199.99,
  "cardToken": "tok_visa" // For Stripe
}
```

## Error Responses

All errors follow this format:

```json
{
  "success": false,
  "message": "Error message",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    }
  ],
  "timestamp": "2024-12-22T12:00:00"
}
```

## Status Codes

- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Invalid request
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Pagination

List endpoints support pagination:

```
GET /api/v1/products?page=0&size=20&sort=name,asc
```

Response includes pagination metadata:
```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

## Filtering & Sorting

Most list endpoints support:
- `search` - Text search
- `sort` - Sort field and direction (e.g., `sort=price,desc`)
- `filter` - Additional filters (product-specific)

## Rate Limiting

API requests are rate-limited:
- Authenticated: 1000 requests/hour
- Unauthenticated: 100 requests/hour

Rate limit headers:
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1640000000
```
