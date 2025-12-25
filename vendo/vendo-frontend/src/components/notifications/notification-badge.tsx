import { Bell } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';

interface NotificationBadgeProps {
  count: number;
  onClick?: () => void;
  className?: string;
  variant?: 'default' | 'outline' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
}

/**
 * Notification Badge Component
 * Displays a bell icon with unread notification count
 */
export function NotificationBadge({
  count,
  onClick,
  className,
  variant = 'ghost',
  size = 'md',
}: NotificationBadgeProps) {
  const sizeClasses = {
    sm: 'h-8 w-8',
    md: 'h-10 w-10',
    lg: 'h-12 w-12',
  };

  const iconSizes = {
    sm: 'h-4 w-4',
    md: 'h-5 w-5',
    lg: 'h-6 w-6',
  };

  return (
    <Button
      variant={variant}
      size="icon"
      className={cn('relative', sizeClasses[size], className)}
      onClick={onClick}
      aria-label={`Notifications${count > 0 ? ` (${count} unread)` : ''}`}
    >
      <Bell className={iconSizes[size]} />
      {count > 0 && (
        <Badge
          variant="destructive"
          className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs font-bold rounded-full min-w-[20px]"
        >
          {count > 99 ? '99+' : count}
        </Badge>
      )}
    </Button>
  );
}

