import { CheckCircle2, Circle, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { NotificationResponse } from '@/types';

interface NotificationItemProps {
  notification: NotificationResponse;
  onMarkAsRead?: (id: number) => void;
  onDelete?: (id: number) => void;
  onClick?: (notification: NotificationResponse) => void;
}

/**
 * Notification Item Component
 * Displays a single notification with read/unread state
 */
export function NotificationItem({
  notification,
  onMarkAsRead,
  onDelete,
  onClick,
}: NotificationItemProps) {
  const handleClick = () => {
    if (onClick) {
      onClick(notification);
    }
    if (!notification.isRead && onMarkAsRead) {
      onMarkAsRead(notification.id);
    }
  };

  const handleMarkAsRead = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onMarkAsRead) {
      onMarkAsRead(notification.id);
    }
  };

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onDelete) {
      onDelete(notification.id);
    }
  };

  // Helper function to format time ago
  const formatTimeAgo = (dateString: string): string => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (diffInSeconds < 60) {
      return 'just now';
    } else if (diffInSeconds < 3600) {
      const minutes = Math.floor(diffInSeconds / 60);
      return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
    } else if (diffInSeconds < 86400) {
      const hours = Math.floor(diffInSeconds / 3600);
      return `${hours} hour${hours > 1 ? 's' : ''} ago`;
    } else if (diffInSeconds < 604800) {
      const days = Math.floor(diffInSeconds / 86400);
      return `${days} day${days > 1 ? 's' : ''} ago`;
    } else {
      return date.toLocaleDateString();
    }
  };

  const timeAgo = formatTimeAgo(notification.createdAt);

  return (
    <div
      className={cn(
        'group relative flex items-start gap-3 p-4 rounded-lg transition-colors cursor-pointer',
        'hover:bg-muted/50',
        !notification.isRead && 'bg-primary/5 border-l-4 border-l-primary'
      )}
      onClick={handleClick}
    >
      <div className="flex-shrink-0 mt-0.5">
        {notification.isRead ? (
          <CheckCircle2 className="h-5 w-5 text-muted-foreground" />
        ) : (
          <Circle className="h-5 w-5 text-primary fill-primary" />
        )}
      </div>

      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between gap-2">
          <div className="flex-1">
            <h4
              className={cn(
                'text-sm font-semibold mb-1',
                !notification.isRead && 'font-bold'
              )}
            >
              {notification.title}
            </h4>
            <p className="text-sm text-muted-foreground line-clamp-2">
              {notification.message}
            </p>
            <p className="text-xs text-muted-foreground mt-1">{timeAgo}</p>
          </div>

          <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
            {!notification.isRead && (
              <Button
                variant="ghost"
                size="icon"
                className="h-7 w-7"
                onClick={handleMarkAsRead}
                title="Mark as read"
              >
                <CheckCircle2 className="h-4 w-4" />
              </Button>
            )}
            <Button
              variant="ghost"
              size="icon"
              className="h-7 w-7 text-destructive hover:text-destructive"
              onClick={handleDelete}
              title="Delete"
            >
              <X className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}

