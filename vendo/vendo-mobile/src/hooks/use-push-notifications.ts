import { useEffect, useRef, useState } from 'react';
import * as Notifications from 'expo-notifications';
import { Platform } from 'react-native';
import { useRouter } from 'expo-router';
import { useAuthStore } from '@/stores/auth-store';
import { notificationApi } from '@/services/notification-api';
import Constants from 'expo-constants';

// Configure notification handler
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
  }),
});

export function usePushNotifications() {
  const router = useRouter();
  const [expoPushToken, setExpoPushToken] = useState<string | null>(null);
  const [notification, setNotification] = useState<Notifications.Notification | null>(null);
  const notificationListener = useRef<Notifications.Subscription>();
  const responseListener = useRef<Notifications.Subscription>();
  const { isAuthenticated, user } = useAuthStore();

  useEffect(() => {
    registerForPushNotificationsAsync().then((token) => {
      if (token) {
        setExpoPushToken(token);
        // Register token with backend if user is authenticated
        if (isAuthenticated && user) {
          registerDeviceToken(token);
        }
      }
    });

    // Listen for notifications received while app is foregrounded
    notificationListener.current = Notifications.addNotificationReceivedListener((notification) => {
      setNotification(notification);
    });

    // Listen for user tapping on notifications
    responseListener.current = Notifications.addNotificationResponseReceivedListener((response) => {
      console.log('Notification tapped:', response);
      // Handle navigation based on notification data
      handleNotificationNavigation(response.notification);
    });

    return () => {
      if (notificationListener.current) {
        Notifications.removeNotificationSubscription(notificationListener.current);
      }
      if (responseListener.current) {
        Notifications.removeNotificationSubscription(responseListener.current);
      }
    };
  }, [isAuthenticated, user]);

  const registerDeviceToken = async (token: string) => {
    try {
      const platform = Platform.OS === 'ios' ? 'ios' : 'android';
      await notificationApi.registerDeviceToken({
        deviceToken: token,
        platform,
      });
    } catch (error) {
      console.error('Failed to register device token:', error);
    }
  };

  const handleNotificationNavigation = (notification: Notifications.Notification) => {
    const data = notification.request.content.data as any;
    
    // Handle navigation based on notification type
    if (data?.type) {
      switch (data.type) {
        case 'ORDER_UPDATE':
        case 'ORDER_CONFIRMED':
        case 'ORDER_SHIPPED':
        case 'ORDER_DELIVERED':
          if (data.orderId) {
            router.push(`/orders/${data.orderId}`);
          } else {
            router.push('/orders');
          }
          break;
        case 'PROMOTION':
        case 'NEW_PRODUCT':
          if (data.productId) {
            router.push(`/product/${data.productId}`);
          } else {
            router.push('/(tabs)/products');
          }
          break;
        case 'LOW_STOCK':
          if (data.productId) {
            router.push(`/product/${data.productId}`);
          } else {
            router.push('/(tabs)/products');
          }
          break;
        default:
          // Default to home if no specific route
          router.push('/(tabs)');
      }
    }
  };

  return {
    expoPushToken,
    notification,
  };
}

async function registerForPushNotificationsAsync(): Promise<string | null> {
  let token: string | null = null;

  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('default', {
      name: 'default',
      importance: Notifications.AndroidImportance.MAX,
      vibrationPattern: [0, 250, 250, 250],
      lightColor: '#3b82f6',
    });
  }

  if (Constants.isDevice) {
    const { status: existingStatus } = await Notifications.getPermissionsAsync();
    let finalStatus = existingStatus;
    
    if (existingStatus !== 'granted') {
      const { status } = await Notifications.requestPermissionsAsync();
      finalStatus = status;
    }
    
    if (finalStatus !== 'granted') {
      console.warn('Failed to get push token for push notification!');
      return null;
    }
    
    try {
      const projectId = Constants.expoConfig?.extra?.eas?.projectId || Constants.easConfig?.projectId;
      if (!projectId) {
        throw new Error('Project ID not found');
      }
      
      token = (await Notifications.getExpoPushTokenAsync({ projectId })).data;
      console.log('Expo push token:', token);
    } catch (e) {
      console.error('Error getting push token:', e);
      token = null;
    }
  } else {
    console.warn('Must use physical device for Push Notifications');
  }

  return token;
}

