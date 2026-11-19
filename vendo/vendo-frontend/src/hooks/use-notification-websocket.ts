import { useEffect, useRef, useState, useCallback } from 'react';
import { NotificationResponse } from '@/types';
import { useAuthStore } from '@/stores/auth-store';
import { useToast } from '@/hooks/use-toast';

/**
 * WebSocket hook for real-time notifications
 * Connects to backend WebSocket endpoint and listens for notifications
 * Uses native WebSocket API with STOMP protocol support
 */
export function useNotificationWebSocket(
  onNotificationReceived?: (notification: NotificationResponse) => void,
  enabled: boolean = true
) {
  const { isAuthenticated, user } = useAuthStore();
  const { toast } = useToast();
  const [isConnected, setIsConnected] = useState(false);
  const [connectionError, setConnectionError] = useState<string | null>(null);
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const reconnectAttemptsRef = useRef(0);
  const maxReconnectAttempts = 5;
  const reconnectDelay = 5000;

  const connect = useCallback(() => {
    if (!isAuthenticated || !user?.id || !enabled) {
      return;
    }

    // Close existing connection if any
    if (wsRef.current) {
      wsRef.current.close();
    }

    try {
      // Convert HTTP URL to WebSocket URL
      // Spring Boot WebSocket typically uses /ws endpoint with SockJS
      const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';
      // Use SockJS endpoint format: /ws or /api/ws depending on configuration
      const wsUrl = apiUrl.replace(/^http/, 'ws').replace(/\/api$/, '') + '/ws';
      
      const ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        console.log('WebSocket connected');
        setIsConnected(true);
        setConnectionError(null);
        reconnectAttemptsRef.current = 0;

        // Subscribe to user-specific notifications using STOMP protocol
        // STOMP CONNECT frame - send after WebSocket connection is established
        // Include authorization token if available
        const token = localStorage.getItem('accessToken');
        let connectFrame = `CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n`;
        if (token) {
          connectFrame += `Authorization:Bearer ${token}\n`;
        }
        connectFrame += `\n\x00`;
        ws.send(connectFrame);
      };

      ws.onmessage = (event) => {
        try {
          // Parse STOMP message frame
          const message = event.data;
          if (message.startsWith('MESSAGE')) {
            const bodyIndex = message.indexOf('\n\n');
            if (bodyIndex !== -1) {
              const body = message.substring(bodyIndex + 2).replace(/\x00/g, '');
              const notification: NotificationResponse = JSON.parse(body);
              console.log('Notification received:', notification);

              // Call custom handler if provided
              if (onNotificationReceived) {
                onNotificationReceived(notification);
              }

              // Show toast notification for in-app notifications
              if (notification.type === 'in_app') {
                toast({
                  title: notification.title,
                  description: notification.message,
                  variant: 'default',
                });
              }
            }
          }
        } catch (error) {
          console.error('Failed to parse notification:', error);
        }
      };

      ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        setConnectionError('WebSocket connection error');
        setIsConnected(false);
      };

      ws.onclose = () => {
        console.log('WebSocket disconnected');
        setIsConnected(false);

        // Attempt to reconnect
        if (reconnectAttemptsRef.current < maxReconnectAttempts && enabled) {
          reconnectAttemptsRef.current++;
          reconnectTimeoutRef.current = setTimeout(() => {
            connect();
          }, reconnectDelay);
        }
      };

      wsRef.current = ws;
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
      setConnectionError('Failed to connect');
      setIsConnected(false);
    }
  }, [isAuthenticated, user?.id, enabled, onNotificationReceived, toast]);

  const disconnect = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }
    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
      setIsConnected(false);
    }
  }, []);

  useEffect(() => {
    if (enabled && isAuthenticated && user?.id) {
      connect();
    } else {
      disconnect();
    }

    return () => {
      disconnect();
    };
  }, [enabled, isAuthenticated, user?.id, connect, disconnect]);

  return {
    isConnected,
    connectionError,
    connect,
    disconnect,
  };
}
