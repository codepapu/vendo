# Vendo E-commerce Platform

A full-stack e-commerce platform: a Spring Boot REST API, a React web storefront, and a React Native (Expo) mobile app.

## 🏗️ Architecture

| Part | Location | Stack |
| --- | --- | --- |
| Backend | `vendo-backend/` | Spring Boot 3.4.0, Java 21, Maven, PostgreSQL, Flyway |
| Web frontend | `vendo/vendo-frontend/` | React 18, Vite, TypeScript, Tailwind CSS, shadcn/ui, TanStack Query, Zustand |
| Mobile app | `vendo/vendo-mobile/` | React Native, Expo ~50, Expo Router, Zustand |
| Shared package | `vendo/packages/shared/` | `@vendo/shared`: TypeScript types and API clients used by web and mobile |

The web frontend and mobile app are npm workspaces inside `vendo/`. The backend is a separate Maven project.

## 📁 Project Structure

```
vendo/                          # Git repository root
├── README.md
├── docs/                       # All project documentation (Markdown)
│   ├── architecture/           # Architecture and database schema
│   ├── backend/                # Backend configuration, deployment, devtools
│   ├── deployment/
│   ├── frontend/
│   ├── mobile/
│   ├── progress/               # Implementation progress and status
│   └── setup/                  # Setup guide
│
├── vendo-backend/              # Spring Boot backend (Maven)
│   ├── pom.xml
│   ├── scripts/                # Database setup, build and start scripts
│   └── src/
│       ├── main/java/com/vendo/
│       ├── main/resources/     # application.properties, Flyway migrations
│       └── test/java/
│
└── vendo/                      # Frontend monorepo (npm workspaces)
    ├── package.json
    ├── packages/
    │   └── shared/             # @vendo/shared: types and API clients
    ├── vendo-frontend/         # React web app (Vite)
    └── vendo-mobile/           # React Native / Expo app
```

## 📚 Documentation

All project documentation lives in the [`docs/`](docs/) folder:

- [Setup guide](docs/setup/SETUP.md)
- [Architecture](docs/architecture/ARCHITECTURE.md) and [database schema](docs/architecture/DATABASE_SCHEMA.md)
- [Backend](docs/backend/README.md), including [configuration](docs/backend/CONFIGURATION.md) and [deployment](docs/backend/DEPLOYMENT.md)
- [Deployment](docs/deployment/DEPLOYMENT.md)
- [Frontend](docs/frontend/README.md) and [mobile](docs/mobile/README.md)
- [Implementation progress](docs/progress/IMPLEMENTATION_PROGRESS.md) and [status](docs/progress/IMPLEMENTATION_STATUS.md)

## 🚀 Quick Start

### Prerequisites

- **Java** 21+
- **Maven** 3.6.3+
- **Node.js** 18+ and **npm** 9+
- **PostgreSQL** 16+ running on `localhost:5432`
- **Redis** (optional; the default cache is in-memory)
- **Expo Go** or an iOS/Android emulator (for the mobile app)

### 1. Set Up the Database

The backend needs two database passwords, supplied as environment variables. There are no defaults, so the scripts stop if they are not set.

```bash
cd vendo-backend

export DB_PASSWORD='<password for the vendo_app user>'
export DB_READONLY_PASSWORD='<password for the vendo_readonly user>'

# Creates the vendo_db database and the vendo_app / vendo_readonly roles
./scripts/init-databases.sh
```

Flyway runs the schema migrations automatically when the backend starts.

### 2. Run the Backend

```bash
cd vendo-backend
export DB_PASSWORD='<same password as above>'

./scripts/start.sh        # checks PostgreSQL, then runs: mvn spring-boot:run
```

The API runs on `http://localhost:8080`, with endpoints under `/api/v1/...`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

### 3. Install JavaScript Dependencies

From the frontend monorepo root (installs all workspaces: web, mobile and shared):

```bash
cd vendo
npm install
```

### 4. Run the Web Frontend

```bash
cd vendo/vendo-frontend
npm run dev
```

The app runs on `http://localhost:3000`. Vite proxies `/api` requests to `http://localhost:8080`.

### 5. Run the Mobile App

```bash
cd vendo/vendo-mobile

npm start           # Expo development server
npm run android     # Android emulator
npm run ios         # iOS simulator
npm run web         # Web browser
```

## 🛠️ Development

### Backend

```bash
cd vendo-backend
mvn clean package     # Build (or ./scripts/build.sh)
mvn test              # Run backend tests
mvn spring-boot:run   # Run the application
```

Backend tests are in `vendo-backend/src/test/java`.

### Web Frontend

Run these from `vendo/vendo-frontend`:

```bash
npm run dev           # Start the Vite development server
npm run build         # Type-check (tsc) and build for production
npm run preview       # Preview the production build
npm run test          # Run unit tests with Vitest
npm run type-check    # Type-check only
npm run lint          # Run ESLint
npm run lint:fix      # Fix ESLint issues
npm run format        # Format source files with Prettier
```

The frontend uses:

- **React 18** with TypeScript (strict mode)
- **Vite** for development and builds
- **Tailwind CSS** and **shadcn/ui** components
- **Framer Motion** for animations
- **Zustand** for client state
- **TanStack Query** for server data
- **React Router** for routing
- **React Hook Form** and **Zod** for forms and validation

Other conventions:

- Absolute imports via `@/` (for example `@/components`, `@/pages`)
- Protected routes for account, cart, checkout and admin pages
- Lazy-loaded page components
- Code quality: ESLint, Prettier, Husky, and lint-staged (pre-commit)

### Mobile App

Run these from `vendo/vendo-mobile`:

```bash
npm start           # Start the Expo dev server
npm run android     # Run on Android
npm run ios         # Run on iOS
npm run web         # Run in a web browser
npm run lint        # Run ESLint
npm run type-check  # Type-check with TypeScript
```

The mobile app uses:

- **React Native** with **Expo** and **Expo Router** (file-based routing in `app/`)
- **Zustand** for state, with auth persisted through AsyncStorage
- Shared types and API clients from `@vendo/shared`

### Shared Package

`packages/shared` (`@vendo/shared`) contains:

- TypeScript types (`src/types`)
- API clients (`src/api`: auth, products, and the base client)

Both the web frontend and the mobile app depend on it. Run its checks with `npm run type-check` inside `vendo/packages/shared`.

## 📦 Building for Production

### Backend

```bash
cd vendo-backend
mvn clean package     # Produces a runnable JAR in target/
```

### Web Frontend

```bash
cd vendo/vendo-frontend
npm run build         # Output: vendo/vendo-frontend/dist/
```

### Mobile App

Builds use [EAS Build](https://docs.expo.dev/build/introduction/):

```bash
cd vendo/vendo-mobile

eas build --platform ios
eas build --platform android
```

## 🔧 Configuration

### Backend Environment Variables

Settings are read from `vendo-backend/src/main/resources/application.properties`. Full details are in [`docs/backend/CONFIGURATION.md`](docs/backend/CONFIGURATION.md).

| Variable | Required | Purpose |
| --- | --- | --- |
| `DB_PASSWORD` | Yes | Password for the `vendo_app` database user |
| `DB_READONLY_PASSWORD` | For DB setup | Password for the `vendo_readonly` user (used by `init-databases.sh`) |
| `DB_USERNAME` | No | Application database user (default `vendo_app`) |
| `JWT_SECRET` | **Yes in production** | Signing key for JWTs. The default is a placeholder and must be replaced. |
| `FRONTEND_URL` | No | Web app origin (default `http://localhost:3000`) |
| `CACHE_TYPE` | No | Cache implementation (default `simple`) |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | No | SMTP settings for transactional email |

Payment, marketing, storage, and other integrations are also configured through environment variables. Each one defaults to empty and is disabled unless set.

Never commit secrets (database passwords, JWT secrets, or payment keys) to the repository. Provide them through environment variables or a secrets manager.

### Web Frontend Environment Variables

Create `vendo/vendo-frontend/.env` (this file is git-ignored):

```env
VITE_API_URL=http://localhost:8080/api
VITE_STRIPE_PUBLISHABLE_KEY=
VITE_PAYPAL_CLIENT_ID=
VITE_GOOGLE_PAY_MERCHANT_ID=
VITE_GOOGLE_PAY_ENVIRONMENT=
```

`VITE_*` values are bundled into the browser build and are visible to anyone. Only put publishable or public client IDs here, never secret keys.

### Mobile App Configuration

The API URL is read from `extra.apiUrl` in `vendo/vendo-mobile/app.json`:

```json
{
  "expo": {
    "extra": {
      "apiUrl": "http://localhost:8080/api"
    }
  }
}
```

For production, point it at your deployed backend.

### API Base URL

The default API base URL is `http://localhost:8080/api`. It is set in:

- Web frontend: `vendo/vendo-frontend/src/lib/api.ts` (overridden by `VITE_API_URL`)
- Mobile app: `vendo/vendo-mobile/src/lib/api.ts` (overridden by `extra.apiUrl`)

## 🔐 Authentication

The web and mobile apps use JWT authentication:

1. The user logs in with email and password.
2. The backend returns an access token and a refresh token.
3. Tokens are stored on the device: `localStorage` in the web app, AsyncStorage in the mobile app.
4. The access token is sent as a `Bearer` header on API requests.
5. On a `401` response, the client calls `/v1/auth/refresh` and retries the request once. If the refresh fails, the user is logged out.

## 📱 Pages & Routes

### Web Frontend

Public pages:

- `/`: home
- `/products`, `/products/:id`: catalog and product details
- `/categories`, `/stores`, `/stores/:slug`, `/deals`, `/search`
- `/login`, `/register`, `/forgot-password`
- `/blog`, `/blog/:slug`, `/pages/:slug`
- Help and information: `/about`, `/contact`, `/faq`, `/help`, `/shipping`, `/returns`, `/privacy`, `/terms`, `/cookie-policy`, `/careers`, `/press`, `/sitemap`

Checkout and order pages:

- `/checkout/guest` and `/checkout/confirmation` are available without login
- `/payment/success` and `/payment/cancel` are the payment return pages

Protected pages (login required):

- `/cart`, `/checkout`, `/payment`
- `/orders/:id`
- `/profile`, `/profile/addresses`, `/profile/orders`, `/profile/settings`
- `/admin/...` (admin area, including content management)

### Mobile App

Screens in `vendo/vendo-mobile/app/`:

- `(tabs)`: home, products, cart, profile
- `product/[id]`: product detail
- `stores`, `stores/[slug]`
- `checkout`, `login`, `register`, `settings`, `contact`, `terms`
- `b2b/`: products, orders, invoices (business accounts)

## 🐛 Troubleshooting

### Backend

1. **`DB_PASSWORD: Set DB_PASSWORD ...`**: export `DB_PASSWORD` in the same shell before running the scripts.
2. **PostgreSQL is not running**: start it (for example `brew services start postgresql@16`), then retry.
3. **Port 8080 already in use**: stop the other process, or change `server.port` in `application.properties` (and the proxy target in `vite.config.ts`).
4. **`mvn: command not found`**: install Maven 3.6.3+ and Java 21.

### Web Frontend

1. **Port 3000 already in use**: change `server.port` in `vendo/vendo-frontend/vite.config.ts`.
2. **API requests fail**: confirm the backend is running on port 8080 and that `VITE_API_URL` points to `http://localhost:8080/api`.
3. **Type errors**: run `npm run type-check`.
4. **Lint errors**: run `npm run lint:fix`.

### Mobile App

1. **Metro bundler issues**: clear the cache with `npx expo start -c`.
2. **iOS build fails**: make sure Xcode and CocoaPods are installed.
3. **Android build fails**: make sure Android Studio and the Android SDK are installed.

### Shared Package

If `@vendo/shared` imports fail:

1. Run `npm install` from the `vendo/` directory.
2. Run `npm run type-check` inside `vendo/packages/shared` to find type errors.

## 📄 License

Proprietary - All rights reserved

## 👤 Author

**Papu Sahu** — code.papu@gmail.com

---

**Vendo E-commerce Platform**: a full-stack e-commerce solution with web and mobile clients.
