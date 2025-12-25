import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Loader2, CheckCheck, BellOff } from 'lucide-react';
import { NotificationItem } from './notification-item';
import { notificationApi } from '@/services/notification-api';
import { NotificationResponse } from '@/types';
import { useToast } from '@/hooks/use-toast';

interface NotificationCenterProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onNotificationClick?: (notification: NotificationResponse) => void;
}

/**
 * Notification Center Component
 * Displays all user notifications in a dialog
 */
export function NotificationCenter({
  open,
  onOpenChange,
  onNotificationClick,
}: NotificationCenterProps) {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const pageSize = 20;

  // Fetch notifications
  const {
    data: notificationsData,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ['notifications', page],
    queryFn: () => notificationApi.getNotifications(page, pageSize),
    enabled: open,
    refetchInterval: 30000, // Refetch every 30 seconds
  });

  // Mark as read mutation
  const markAsReadMutation = useMutation({
    mutationFn: (id: number) => notificationApi.markAsRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadCount'] });
    },
  });

  // Mark all as read mutation
  const markAllAsReadMutation = useMutation({
    mutationFn: () => notificationApi.markAllAsRead(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadCount'] });
      toast({
        title: 'All notifications marked as read',
        variant: 'default',
      });
    },
  });

  const handleMarkAsRead = (id: number) => {
    markAsReadMutation.mutate(id);
  };

  const handleMarkAllAsRead = () => {
    markAllAsReadMutation.mutate();
  };

  const handleNotificationClick = (notification: NotificationResponse) => {
    if (onNotificationClick) {
      onNotificationClick(notification);
    }
  };

  const notifications = notificationsData?.content || [];
  const unreadCount = notifications.filter((n) => !n.isRead).length;
  const hasMore = notificationsData && !notificationsData.last;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px] max-h-[80vh] flex flex-col p-0">
        <DialogHeader className="px-6 pt-6 pb-4 border-b">
          <div className="flex items-center justify-between">
            <div>
              <DialogTitle>Notifications</DialogTitle>
              <DialogDescription>
                {notificationsData?.totalElements
                  ? `${notificationsData.totalElements} total notification${notificationsData.totalElements > 1 ? 's' : ''}`
                  : 'No notifications'}
              </DialogDescription>
            </div>
            {unreadCount > 0 && (
              <Button
                variant="outline"
                size="sm"
                onClick={handleMarkAllAsRead}
                disabled={markAllAsReadMutation.isPending}
              >
                <CheckCheck className="h-4 w-4 mr-2" />
                Mark all read
              </Button>
            )}
          </div>
        </DialogHeader>

        <div className="flex-1 px-6 overflow-y-auto max-h-[60vh]">
          {isLoading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
          ) : isError ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <p className="text-sm text-muted-foreground mb-2">
                Failed to load notifications
              </p>
              <Button
                variant="outline"
                size="sm"
                onClick={() => queryClient.invalidateQueries({ queryKey: ['notifications'] })}
              >
                Retry
              </Button>
            </div>
          ) : notifications.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <BellOff className="h-12 w-12 text-muted-foreground mb-4" />
              <p className="text-sm font-medium mb-1">No notifications</p>
              <p className="text-xs text-muted-foreground">
                You're all caught up! New notifications will appear here.
              </p>
            </div>
          ) : (
            <div className="py-4 space-y-1">
              {notifications.map((notification) => (
                <NotificationItem
                  key={notification.id}
                  notification={notification}
                  onMarkAsRead={handleMarkAsRead}
                  onClick={handleNotificationClick}
                  // Note: Delete functionality not implemented in backend yet
                  // onDelete can be added when backend supports it
                />
              ))}
            </div>
          )}

          {hasMore && (
            <div className="py-4 text-center">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => p + 1)}
                disabled={isLoading}
              >
                Load more
              </Button>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}

