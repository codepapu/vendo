# Vendo Mobile App

React Native mobile app built with Expo, TypeScript, and shared business logic.

## Features

- 📱 **React Native** - Cross-platform mobile development
- 🚀 **Expo** - Easy development and deployment
- 📘 **TypeScript** - Type safety
- 🧭 **Expo Router** - File-based routing
- 🔄 **Shared Logic** - Uses `@vendo/shared` package
- 🔐 **JWT Auth** - Secure authentication
- 📦 **TanStack Query** - Data fetching

## Getting Started

### Prerequisites

- Node.js 18+
- Expo CLI: `npm install -g expo-cli`
- iOS: Xcode (for iOS simulator)
- Android: Android Studio (for Android emulator)

### Installation

```bash
# Install dependencies
npm install

# Start Expo development server
npm start

# Run on iOS
npm run ios

# Run on Android
npm run android

# Run on web
npm run web
```

## Project Structure

```
app/
├── (tabs)/         # Tab navigation screens
│   ├── index.tsx   # Home
│   ├── products.tsx
│   ├── cart.tsx
│   └── profile.tsx
├── login.tsx       # Login screen
├── register.tsx    # Registration screen
└── product/[id].tsx  # Product detail

src/
├── components/     # Reusable components
├── stores/         # Zustand stores
└── lib/            # Utilities and API clients
```

## Configuration

Update API URL in `app.json`:

```json
{
  "expo": {
    "extra": {
      "apiUrl": "http://localhost:8080/api"
    }
  }
}
```

For physical devices, use your computer's IP address instead of `localhost`.

## Building

### Development Build

```bash
# iOS
eas build --profile development --platform ios

# Android
eas build --profile development --platform android
```

### Production Build

```bash
# iOS
eas build --platform ios

# Android
eas build --platform android
```

## Troubleshooting

- **Metro bundler cache**: `npx expo start -c`
- **iOS build issues**: Ensure Xcode and CocoaPods are installed
- **Android build issues**: Ensure Android Studio and SDK are configured
