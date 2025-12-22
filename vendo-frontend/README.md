# Vendo Frontend

Modern React frontend for the Vendo E-commerce Platform.

## Tech Stack

- **React 18** - UI library
- **Vite** - Build tool and dev server
- **React Router** - Routing
- **TanStack Query** - Data fetching and caching
- **Axios** - HTTP client

## Getting Started

### Prerequisites

- Node.js 18+ and npm

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

The application will start on `http://localhost:3000`

### Build

```bash
npm run build
```

### Preview Production Build

```bash
npm run preview
```

## Backend API

The frontend connects to the Spring Boot backend at `http://localhost:8080`

API endpoints:
- `/api/products` - Product listing
- `/api/products/:id` - Product details
- `/api/auth/login` - User login
- `/api/auth/register` - User registration
- `/api/cart` - Shopping cart operations

## Project Structure

```
src/
├── components/     # Reusable components
├── pages/         # Page components
├── services/       # API services
├── App.jsx         # Main app component
└── main.jsx        # Entry point
```

## Features

- ✅ Product listing and search
- ✅ Product detail pages
- ✅ Shopping cart
- ✅ User authentication
- ✅ Responsive design
- ✅ Modern UI/UX

---

**Vendo E-commerce Platform** - Built with React and Spring Boot
