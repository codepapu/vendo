# Quick Start - Frontend

## The Issue

When using npm workspaces, dependencies are hoisted to the parent directory. To run vite, use one of these methods:

## Solution 1: Use npx (Recommended)

```bash
cd vendo/vendo-frontend
npx vite
```

## Solution 2: Use the npm script (now updated)

The `package.json` has been updated to use `npx vite`. Now you can run:

```bash
cd vendo/vendo-frontend
npm run dev
```

## Solution 3: Use the full path

```bash
cd vendo/vendo-frontend
../node_modules/.bin/vite
```

## Verify Installation

Check if vite is available:
```bash
cd vendo/vendo-frontend
npx vite --version
```

You should see: `v5.0.8` (or similar version)

## Start the Server

Once dependencies are installed, start the dev server:

```bash
cd vendo/vendo-frontend
npm run dev
```

Or directly:
```bash
npx vite
```

The server will start on **http://localhost:3000**
