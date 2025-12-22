# Vendo Frontend

Modern React 18 frontend built with Vite, TypeScript, TailwindCSS, and shadcn/ui.

## Features

- ⚡ **Vite** - Lightning fast development
- ⚛️ **React 18** - Latest React features
- 📘 **TypeScript** - Type safety
- 🎨 **TailwindCSS** - Utility-first CSS
- 🧩 **shadcn/ui** - Beautiful, accessible components
- 🌙 **Dark Mode** - Automatic theme switching
- ✨ **Framer Motion** - Smooth animations
- 🔄 **Lazy Loading** - Code splitting for performance
- 📦 **Absolute Imports** - Clean import paths
- 🔐 **JWT Auth** - Secure authentication
- 📱 **Responsive** - Mobile-first design

## Getting Started

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## Code Quality

```bash
# Lint code
npm run lint

# Fix linting issues
npm run lint:fix

# Format code
npm run format

# Type check
npm run type-check
```

## Project Structure

```
src/
├── components/      # Reusable components
│   ├── ui/         # shadcn/ui components
│   └── layout/     # Layout components
├── pages/          # Page components
├── stores/         # Zustand stores
├── hooks/          # Custom hooks
└── lib/            # Utilities and API clients
```

## Environment Variables

Create `.env` file:

```env
VITE_API_URL=http://localhost:8080/api
```

## Absolute Imports

Use `@/` prefix for clean imports:

```typescript
import { Button } from '@/components/ui/button';
import { useAuthStore } from '@/stores/auth-store';
import { formatPrice } from '@/lib/utils';
```
