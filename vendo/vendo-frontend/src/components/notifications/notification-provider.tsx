import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { NotificationBadge } from './notification-badge';
import { NotificationCenter } from './notification-center';
import { useNotificationWebSocket } from '@/hooks/use-notification-websocket';
import { notificationApi } from '@/services/notification-api';
import { NotificationResponse } from '@/types';
import { useAuthStore } from '@/stores/auth-store';

interface NotificationProviderProps {
  onNotificationClick?: (_notification: NotificationResponse) => void;
}

/**
 * Notification Provider Component
 * Manages notification state, WebSocket connection, and badge display
 */
export function NotificationProvider({ onNotificationClick }: NotificationProviderProps) {
  const { isAuthenticated } = useAuthStore();
  const [notificationCenterOpen, setNotificationCenterOpen] = useState(false);
  const queryClient = useQueryClient();

  // Fetch unread count
  const { data: unreadCount = 0 } = useQuery({
    queryKey: ['unreadCount'],
    queryFn: () => notificationApi.getUnreadCount(),
    enabled: isAuthenticated,
    refetchInterval: 30000, // Refetch every 30 seconds
  });

  // Handle incoming WebSocket notifications
  const handleNotificationReceived = (_notification: NotificationResponse) => {
    // Invalidate queries to refresh notification list and count
    queryClient.invalidateQueries({ queryKey: ['notifications'] });
    queryClient.invalidateQueries({ queryKey: ['unreadCount'] });
  };

  // WebSocket connection for real-time notifications
  useNotificationWebSocket(
    handleNotificationReceived,
    isAuthenticated
  );

  if (!isAuthenticated) {
    return null;
  }

  return (
    <>
      <NotificationBadge
        count={unreadCount}
        onClick={() => setNotificationCenterOpen(true)}
      />
      <NotificationCenter
        open={notificationCenterOpen}
        onOpenChange={setNotificationCenterOpen}
        onNotificationClick={onNotificationClick}
      />
    </>
  );
}

