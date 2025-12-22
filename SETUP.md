# Complete Setup Guide

This guide will walk you through setting up the entire Vendo platform from scratch.

## Prerequisites

### Required Software

1. **Node.js** 18+ and npm 9+
   ```bash
   node --version  # Should be 18.0.0 or higher
   npm --version   # Should be 9.0.0 or higher
   ```

2. **Java** 21+
   ```bash
   java -version  # Should be 21 or higher
   ```

3. **PostgreSQL** 16+
   ```bash
   psql --version  # Should be 16 or higher
   ```

4. **Git**

### Optional but Recommended

- **Redis** (for caching)
- **Docker** (for easier database setup)
- **Expo CLI** (for mobile development)
  ```bash
   npm install -g expo-cli
   ```

## Step-by-Step Setup

### 1. Clone and Navigate

```bash
cd /Users/dox/Downloads/vendo
```

### 2. Install Root Dependencies

```bash
cd vendo
npm install
```

This installs dependencies for all workspaces (frontend, mobile, shared).

### 3. Backend Setup

#### 3.1 Database Setup

```bash
cd vendo-backend

# Initialize PostgreSQL database
./scripts/init-databases.sh
```

If the script doesn't exist, create the database manually:

```bash
createdb vendo
createdb vendo_test
```

#### 3.2 Configure Backend

Edit `vendo-backend/src/main/resources/application.yml` if needed (defaults should work).

#### 3.3 Start Backend

```bash
./gradlew bootRun
```

Backend will start on `http://localhost:8080`

Verify it's running:
- API: http://localhost:8080/api/v1/products
- Swagger: http://localhost:8080/swagger-ui.html

### 4. Frontend Setup

#### 4.1 Install Dependencies

```bash
cd vendo/vendo-frontend
npm install
```

#### 4.2 Configure Environment

Create `.env` file:

```bash
cp .env.example .env
```

Edit `.env` if your backend runs on a different port:

```env
VITE_API_URL=http://localhost:8080/api
```

#### 4.3 Start Frontend

```bash
npm run dev
```

Frontend will start on `http://localhost:3000`

### 5. Mobile App Setup

#### 5.1 Install Dependencies

```bash
cd vendo/vendo-mobile
npm install
```

#### 5.2 Configure API URL

Edit `vendo/vendo-mobile/app.json`:

```json
{
  "expo": {
    "extra": {
      "apiUrl": "http://YOUR_IP:8080/api"
    }
  }
}
```

**Important**: For physical devices, replace `localhost` with your computer's IP address:
- macOS/Linux: `ifconfig | grep "inet "`
- Windows: `ipconfig`

#### 5.3 Start Mobile App

```bash
npm start
```

Then:
- Press `i` for iOS simulator
- Press `a` for Android emulator
- Scan QR code with Expo Go app on your phone

## Development Workflow

### Running Everything

1. **Terminal 1 - Backend**:
   ```bash
   cd vendo-backend
   ./gradlew bootRun
   ```

2. **Terminal 2 - Frontend**:
   ```bash
   cd vendo-frontend
   npm run dev
   ```

3. **Terminal 3 - Mobile** (optional):
   ```bash
   cd vendo-mobile
   npm start
   ```

### Code Quality

Before committing, ensure code quality:

```bash
# From root
npm run lint
npm run format

# Or from individual workspaces
cd vendo-frontend
npm run lint:fix
npm run format
```

### Git Hooks

Husky is configured to run lint-staged on commit. This will:
- Run ESLint
- Format code with Prettier
- Type check TypeScript

## Troubleshooting

### Backend Issues

**Port 8080 already in use:**
```bash
# Find and kill process
lsof -ti:8080 | xargs kill -9
```

**Database connection failed:**
- Ensure PostgreSQL is running: `brew services start postgresql@16`
- Check credentials in `application.yml`

### Frontend Issues

**Port 3000 already in use:**
- Change port in `vendo/vendo-frontend/vite.config.ts`:
  ```typescript
  server: {
    port: 3001, // Change this
  }
  ```

**Module not found errors:**
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

**Type errors:**
```bash
npm run type-check
```

### Mobile Issues

**Metro bundler cache issues:**
```bash
npx expo start -c
```

**Cannot connect to API:**
- Use your computer's IP address, not `localhost`
- Ensure backend is running
- Check firewall settings

**iOS build fails:**
- Install Xcode from App Store
- Run: `cd ios && pod install`

**Android build fails:**
- Install Android Studio
- Set up Android SDK
- Create an emulator

### Shared Package Issues

**`@vendo/shared` not found:**
```bash
# From vendo directory
cd vendo
npm install

# Rebuild shared package
cd packages/shared
npm run build
```

## Next Steps

1. **Create a user account** via `/register`
2. **Browse products** at `/products`
3. **Add items to cart** and checkout
4. **Explore the mobile app** on your device

## Production Deployment

### Frontend

```bash
cd vendo/vendo-frontend
npm run build
```

Deploy the `dist/` folder to your hosting service (Vercel, Netlify, etc.).

### Mobile

```bash
cd vendo/vendo-mobile

# Install EAS CLI
npm install -g eas-cli

# Login to Expo
eas login

# Configure build
eas build:configure

# Build for production
eas build --platform ios
eas build --platform android
```

## Support

For issues or questions:
- Check the main README.md
- Review individual README files in each workspace
- Check backend logs for API errors
- Check browser console for frontend errors

---

Happy coding! 🚀
