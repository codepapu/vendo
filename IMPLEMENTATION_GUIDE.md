# Complete Implementation Guide

## Getting Started - End to End

This guide will walk you through building the complete Vendo platform from scratch.

## Prerequisites

### Required Software
- Java 21+
- Node.js 18+
- PostgreSQL 16+
- Redis (optional but recommended)
- Docker (optional, for containerization)
- Git

### Development Tools
- IDE: IntelliJ IDEA / VS Code
- Postman / Insomnia (API testing)
- DBeaver / pgAdmin (Database management)

## Step 1: Project Setup

### 1.1 Clone and Initialize

```bash
# Clone the repository
git clone https://github.com/codepapu/vendo.git
cd vendo

# Initialize git submodules if any
git submodule update --init --recursive
```

### 1.2 Backend Setup

```bash
cd vendo-backend

# Initialize database
./scripts/init-databases.sh

# Build the project
./gradlew clean build

# Run database migrations
./gradlew flywayMigrate

# Start the backend
./gradlew bootRun
```

Backend will run on: `http://localhost:8080`

### 1.3 Frontend Setup

```bash
cd vendo/vendo-frontend

# Install dependencies
npm install

# Create environment file
cat > .env << EOF
VITE_API_URL=http://localhost:8080/api
EOF

# Start development server
npm run dev
```

Frontend will run on: `http://localhost:3000`

### 1.4 Mobile App Setup

```bash
cd vendo/vendo-mobile

# Install dependencies
npm install

# Start Expo
npm start
```

## Step 2: Database Setup

### 2.1 Create Database Schema

Run the migrations from `DATABASE_SCHEMA.md`:

```bash
cd vendo-backend

# Create migration file
touch src/main/resources/db/migration/V2__create_b2b_tables.sql
touch src/main/resources/db/migration/V3__create_retailer_tables.sql
touch src/main/resources/db/migration/V4__create_enhanced_orders.sql
```

### 2.2 Seed Initial Data

```bash
# Create seed data script
touch src/main/resources/db/migration/V5__seed_initial_data.sql
```

Seed data should include:
- Admin user
- Sample categories
- Sample products
- Sample shipping methods
- Sample payment methods

## Step 3: Backend Implementation

### 3.1 Create Entity Classes

Based on the database schema, create JPA entities:

```
vendo-backend/src/main/java/com/vendo/
├── user/
│   ├── entity/
│   │   ├── User.java
│   │   ├── BusinessAccount.java
│   │   ├── BusinessContact.java
│   │   └── Retailer.java
├── product/
│   ├── entity/
│   │   ├── Product.java
│   │   ├── ProductRetailer.java
│   │   ├── B2BPricingTier.java
│   │   └── Category.java
├── order/
│   ├── entity/
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── PurchaseOrder.java
│   │   └── Invoice.java
└── ...
```

### 3.2 Create Repository Interfaces

```java
// Example: BusinessAccountRepository.java
@Repository
public interface BusinessAccountRepository extends JpaRepository<BusinessAccount, Long> {
    Optional<BusinessAccount> findByUserId(Long userId);
    List<BusinessAccount> findByStatus(String status);
    Page<BusinessAccount> findByStatus(String status, Pageable pageable);
}
```

### 3.3 Create Service Classes

```java
// Example: B2BService.java
@Service
@RequiredArgsConstructor
public class B2BService {
    private final BusinessAccountRepository businessAccountRepository;
    private final B2BPricingService pricingService;
    
    public BusinessAccount createBusinessAccount(BusinessAccountRequest request) {
        // Implementation
    }
    
    public PricingResponse getB2BPricing(Long productId, Long businessAccountId, Integer quantity) {
        // Implementation
    }
}
```

### 3.4 Create REST Controllers

```java
// Example: B2BController.java
@RestController
@RequestMapping("/api/v1/b2b")
@RequiredArgsConstructor
public class B2BController {
    private final B2BService b2BService;
    
    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<BusinessAccountResponse>> createAccount(
            @Valid @RequestBody BusinessAccountRequest request) {
        // Implementation
    }
    
    @GetMapping("/pricing/{productId}")
    public ResponseEntity<ApiResponse<PricingResponse>> getPricing(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        // Implementation
    }
}
```

### 3.5 Implement Business Logic

Key services to implement:

1. **B2B Service**
   - Business account management
   - B2B pricing calculation
   - Purchase order workflow
   - Invoice generation

2. **Retailer Service**
   - Retailer onboarding
   - Product listing management
   - Commission calculation
   - Payout management

3. **Order Service** (Enhanced)
   - Multi-type order processing
   - B2B order workflow
   - Marketplace order splitting
   - Order status management

4. **Pricing Service**
   - Dynamic pricing calculation
   - Volume discounts
   - B2B pricing tiers
   - Promotional pricing

5. **Inventory Service**
   - Multi-warehouse management
   - Stock level tracking
   - Low stock alerts
   - Stock movements

## Step 4: Frontend Implementation

### 4.1 Create Type Definitions

```typescript
// src/types/b2b.ts
export interface BusinessAccount {
  id: number;
  companyName: string;
  businessType: string;
  creditLimit: number;
  creditTermsDays: number;
  status: string;
}

export interface B2BPricing {
  productId: number;
  quantity: number;
  unitPrice: number;
  discount: number;
  totalPrice: number;
}
```

### 4.2 Create API Services

```typescript
// src/services/b2b-api.ts
import { apiClient } from '@/lib/api';
import { BusinessAccount, B2BPricing } from '@/types/b2b';

export const b2bApi = {
  async createAccount(data: BusinessAccountRequest): Promise<BusinessAccount> {
    const response = await apiClient.instance.post('/v1/b2b/accounts', data);
    return response.data.data;
  },
  
  async getPricing(productId: number, quantity: number): Promise<B2BPricing> {
    const response = await apiClient.instance.get(
      `/v1/b2b/pricing/${productId}`,
      { params: { quantity } }
    );
    return response.data.data;
  }
};
```

### 4.3 Create Pages

**B2B Pages:**
- `/b2b/register` - Business account registration
- `/b2b/dashboard` - B2B dashboard
- `/b2b/products` - B2B product catalog with volume pricing
- `/b2b/orders` - Purchase orders
- `/b2b/invoices` - Invoice management

**Retailer Pages:**
- `/retailer/register` - Retailer registration
- `/retailer/dashboard` - Retailer dashboard
- `/retailer/products` - Product management
- `/retailer/orders` - Order management
- `/retailer/analytics` - Sales analytics

### 4.4 Create Components

**B2B Components:**
- `BusinessAccountForm` - Account registration form
- `B2BProductCard` - Product card with volume pricing
- `PurchaseOrderForm` - PO creation form
- `InvoiceList` - Invoice listing

**Retailer Components:**
- `RetailerDashboard` - Dashboard overview
- `ProductManagement` - Product CRUD
- `OrderManagement` - Order processing
- `AnalyticsChart` - Sales charts

## Step 5: Testing

### 5.1 Unit Tests

```java
@SpringBootTest
class B2BServiceTest {
    @Autowired
    private B2BService b2BService;
    
    @Test
    void testCreateBusinessAccount() {
        // Test implementation
    }
}
```

### 5.2 Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class B2BControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCreateAccountEndpoint() throws Exception {
        // Test implementation
    }
}
```

### 5.3 Frontend Tests

```typescript
// Example: B2BService.test.ts
describe('B2BService', () => {
  it('should create business account', async () => {
    // Test implementation
  });
});
```

## Step 6: Deployment

### 6.1 Build for Production

**Backend:**
```bash
cd vendo-backend
./gradlew clean build
./gradlew bootJar
```

**Frontend:**
```bash
cd vendo/vendo-frontend
npm run build
```

### 6.2 Docker Setup

Create `Dockerfile` for each service:

```dockerfile
# Backend Dockerfile
FROM openjdk:21-jdk-slim
COPY build/libs/vendo-backend.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 6.3 Environment Configuration

Create production configuration files:
- `application-prod.yml`
- `.env.production`

## Step 7: Monitoring & Maintenance

### 7.1 Logging
- Configure log levels
- Set up log aggregation
- Implement structured logging

### 7.2 Monitoring
- Set up health checks
- Configure metrics collection
- Set up alerting

### 7.3 Backup Strategy
- Database backups
- File storage backups
- Disaster recovery plan

## Development Workflow

### Daily Development

1. **Start Backend:**
   ```bash
   cd vendo-backend
   ./gradlew bootRun
   ```

2. **Start Frontend:**
   ```bash
   cd vendo/vendo-frontend
   npm run dev
   ```

3. **Run Tests:**
   ```bash
   # Backend
   ./gradlew test
   
   # Frontend
   npm run test
   ```

4. **Code Quality:**
   ```bash
   # Lint
   npm run lint
   
   # Format
   npm run format
   ```

### Git Workflow

1. Create feature branch
2. Make changes
3. Write tests
4. Commit changes
5. Push to remote
6. Create pull request

## Next Steps

1. Review `FEATURE_ROADMAP.md` for prioritized features
2. Start with Phase 1 (B2C Core Features)
3. Implement database migrations
4. Build backend services
5. Create frontend pages
6. Test thoroughly
7. Deploy incrementally
