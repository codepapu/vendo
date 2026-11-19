# Vendo E-commerce Platform

A modern, full-stack e-commerce platform built with Spring Boot, React 18, and React Native.

## 🏗️ Architecture

This is a **monorepo** containing:

- **Backend**: Monolithic Spring Boot 3.4.0 application
- **Frontend**: React 18 + Vite + TypeScript + TailwindCSS + shadcn/ui
- **Mobile**: React Native + Expo (Android/iOS)
- **Shared**: Common TypeScript types and API clients

## 📁 Project Structure

```
vendo/
├── vendo-backend/          # Spring Boot backend
│   ├── src/
│   └── build.gradle.kts
│
├── vendo/                  # Frontend monorepo
│   ├── packages/
│   │   └── shared/        # Shared TypeScript package
│   │       └── src/
│   │           ├── types/     # TypeScript types
│   │           └── api/       # API clients
│   │
│   ├── vendo-frontend/     # React web frontend
│   │   ├── src/
│   │   │   ├── components/    # Reusable UI components
│   │   │   ├── pages/         # Page components
│   │   │   ├── stores/        # Zustand state management
│   │   │   ├── hooks/         # Custom React hooks
│   │   │   └── lib/           # Utilities and API clients
│   │   └── package.json
│   │
│   ├── vendo-mobile/      # React Native mobile app
│   │   ├── app/               # Expo Router pages
│   │   ├── src/               # Shared mobile code
│   │   └── package.json
│   │
│   └── package.json       # Root workspace config
│
└── README.md
```

## 📚 Documentation

All project documentation lives in the [`docs/`](docs/) folder:

- [Setup](docs/setup/SETUP.md)
- [Architecture](docs/architecture/ARCHITECTURE.md) and [database schema](docs/architecture/DATABASE_SCHEMA.md)
- [Backend](docs/backend/README.md) (configuration, deployment, devtools, Maven migration)
- [Deployment](docs/deployment/DEPLOYMENT.md)
- [Frontend](docs/frontend/README.md) and [Mobile](docs/mobile/README.md)
- [Implementation progress](docs/progress/IMPLEMENTATION_PROGRESS.md) and [status](docs/progress/IMPLEMENTATION_STATUS.md)

## 🚀 Quick Start

### Prerequisites

- **Node.js** 18+ and npm 9+
- **Java** 21+
- **PostgreSQL** 16+
- **Redis** (optional but recommended)
- **Expo CLI** (for mobile development)

### 1. Install Dependencies

From the root directory:

```bash
cd vendo
npm install
```

This will install dependencies for all workspaces (frontend, mobile, and shared).

### 2. Backend Setup

```bash
cd vendo-backend

# Initialize database
./scripts/init-databases.sh

# Build and run
./gradlew bootRun
```

Backend runs on `http://localhost:8080`

### 3. Frontend Setup

```bash
cd vendo/vendo-frontend

# Install dependencies (if not done from root)
npm install

# Start development server
npm run dev
```

Frontend runs on `http://localhost:3000`

### 4. Mobile App Setup

```bash
cd vendo/vendo-mobile

# Install dependencies (if not done from root)
npm install

# Start Expo development server
npm start

# Or run on specific platform
npm run ios      # iOS simulator
npm run android  # Android emulator
```

## 🛠️ Development

### Frontend Development

Navigate to the frontend workspace:

```bash
cd vendo/vendo-frontend
```

The frontend uses:

- **React 18** with TypeScript
- **Vite** for fast development and building
- **TailwindCSS** for styling
- **shadcn/ui** for UI components
- **Framer Motion** for animations
- **Zustand** for state management
- **TanStack Query** for data fetching
- **React Router** for routing

#### Available Scripts

```bash
npm run dev          # Start development server
npm run build        # Build for production
npm run preview      # Preview production build
npm run lint         # Run ESLint
npm run lint:fix     # Fix ESLint errors
npm run format       # Format code with Prettier
npm run type-check   # Type check with TypeScript
```

#### Code Quality

- **ESLint** + **Prettier** configured
- **Husky** pre-commit hooks with **lint-staged**
- Automatic code formatting on commit

#### Features

- ✅ Fully responsive design
- ✅ Dark/Light mode with system preference detection
- ✅ Framer Motion animations
- ✅ Lazy loading and code splitting
- ✅ Absolute imports (`@/components`, `@/pages`, etc.)
- ✅ Protected routes
- ✅ JWT authentication with refresh tokens

### Mobile Development

Navigate to the mobile workspace:

```bash
cd vendo/vendo-mobile
```

The mobile app uses:

- **React Native** with Expo
- **Expo Router** for file-based routing
- **TypeScript** for type safety
- **TanStack Query** for data fetching
- **Zustand** for state management
- Shared business logic from `@vendo/shared`

#### Available Scripts

```bash
npm start           # Start Expo dev server
npm run ios         # Run on iOS simulator
npm run android     # Run on Android emulator
npm run web         # Run in web browser
npm run lint        # Run ESLint
npm run type-check  # Type check with TypeScript
```

### Shared Package

The `packages/shared` package contains:

- TypeScript types and interfaces
- API client classes
- Common utilities

Both frontend and mobile apps import from `@vendo/shared` to share business logic.

## 📦 Building for Production

### Frontend

```bash
cd vendo/vendo-frontend
npm run build
```

Output: `vendo/vendo-frontend/dist/`

### Mobile

```bash
cd vendo-mobile

# Build for iOS
eas build --platform ios

# Build for Android
eas build --platform android
```

## 🔧 Configuration

### Environment Variables

#### Frontend

Create `vendo/vendo-frontend/.env`:

```env
VITE_API_URL=http://localhost:8080/api
```

#### Mobile

Update `vendo/vendo-mobile/app.json`:

```json
{
  "expo": {
    "extra": {
      "apiUrl": "http://localhost:8080/api"
    }
  }
}
```

For production, use your backend URL.

### API Base URL

The default API base URL is `http://localhost:8080/api`. Update this in:

- Frontend: `vendo/vendo-frontend/src/lib/api.ts`
- Mobile: `vendo/vendo-mobile/src/lib/api.ts`

## 🎨 UI/UX Features

### Web Frontend

- Modern, clean design with shadcn/ui components
- Fully responsive (mobile, tablet, desktop)
- Dark/light mode toggle
- Smooth animations with Framer Motion
- Lazy loading for better performance
- Accessible components (ARIA compliant)

### Mobile App

- Native iOS and Android support
- Tab-based navigation
- Optimized for touch interactions
- Platform-specific design patterns

## 🔐 Authentication

Both web and mobile apps use JWT authentication:

1. User logs in with email/username and password
2. Backend returns `accessToken` and `refreshToken`
3. Tokens stored securely (localStorage for web, AsyncStorage for mobile)
4. Access token included in API requests
5. Automatic token refresh on 401 errors

## 📱 Pages & Routes

### Web Frontend

- `/` - Home page with featured products
- `/products` - Product listing with search
- `/products/:id` - Product detail page
- `/cart` - Shopping cart (protected)
- `/checkout` - Checkout flow (protected)
- `/login` - Login page
- `/register` - Registration page

### Mobile App

- `/(tabs)` - Tab navigation
  - `index` - Home screen
  - `products` - Products list
  - `cart` - Shopping cart
  - `profile` - User profile
- `/login` - Login screen
- `/register` - Registration screen
- `/product/[id]` - Product detail

## 🧪 Testing

### Frontend

```bash
cd vendo-frontend
npm run test  # When tests are added
```

### Mobile

```bash
cd vendo-mobile
npm run test  # When tests are added
```

## 📚 API Documentation

Once the backend is running, access Swagger UI at:
- http://localhost:8080/swagger-ui.html

## 🐛 Troubleshooting

### Frontend Issues

1. **Port already in use**: Change port in `vendo/vendo-frontend/vite.config.ts`
2. **Type errors**: Run `npm run type-check`
3. **Lint errors**: Run `npm run lint:fix`

### Mobile Issues

1. **Metro bundler issues**: Clear cache with `npx expo start -c`
2. **iOS build fails**: Ensure Xcode and CocoaPods are installed
3. **Android build fails**: Ensure Android Studio and SDK are set up

### Shared Package Issues

If `@vendo/shared` imports fail:

1. Ensure you've run `npm install` from `vendo/` directory
2. Rebuild the shared package: `cd vendo/packages/shared && npm run build`

## 📄 License

Proprietary - All rights reserved

## 👤 Author

code.papu@gmail.com

---

**Vendo E-commerce Platform** - Modern, scalable, full-stack e-commerce solution with web and mobile support.
