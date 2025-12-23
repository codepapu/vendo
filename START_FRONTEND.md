# Starting the Frontend

## Quick Start

1. **Navigate to the frontend directory:**
   ```bash
   cd vendo/vendo-frontend
   ```

2. **Install dependencies (if not already installed):**
   ```bash
   npm install
   ```

3. **Start the development server:**
   ```bash
   npm run dev
   ```

## Expected Output

After running `npm run dev`, you should see:

```
  VITE v5.x.x  ready in xxx ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: use --host to expose
  ➜  press h + enter to show help
```

## Access the Frontend

Open your browser and navigate to:
- **Local**: http://localhost:3000

## Troubleshooting

### If port 3000 is already in use:

The server will automatically try the next available port, or you can change it in `vite.config.ts`:

```typescript
server: {
  port: 3001, // Change this
}
```

### If dependencies are missing:

```bash
# Remove node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### If you see TypeScript errors:

```bash
# Run type check
npm run type-check
```

### If you see build errors:

```bash
# Clear Vite cache
rm -rf node_modules/.vite
npm run dev
```

## Environment Variables

Make sure you have a `.env` file in `vendo/vendo-frontend/`:

```env
VITE_API_URL=http://localhost:8080/api
```

**Note**: Make sure your backend is running on `http://localhost:8080` for the frontend to work properly.
