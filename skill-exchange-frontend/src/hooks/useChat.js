import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { useAuthStore } from '../store/authStore';

export function useChat() {
  const token = useAuthStore((s) => s.token);
  const user = useAuthStore((s) => s.user);
  const qc = useQueryClient();
  const clientRef = useRef(null);
  const [isConnected, setConnected] = useState(false);
  const [connectionError, setConnectionError] = useState(null);

  useEffect(() => {
    if (!token) return undefined;
    const client = new Client({
      webSocketFactory: () => new SockJS(`${import.meta.env.VITE_API_URL || window.location.origin}/ws`),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 4000,
      onConnect: () => {
        setConnected(true); setConnectionError(null);
        client.subscribe('/user/queue/messages', (message) => {
          const body = JSON.parse(message.body);
          const otherId = body.senderId;
          qc.setQueryData(['chat', otherId], (old) => {
            const page = old?.content ? old : { content: old || [] };
            return { ...page, content: [...(page.content || []), body] };
          });
          qc.invalidateQueries({ queryKey: ['chat', 'conversations'] });
        });
      },
      onStompError: (frame) => { setConnectionError(frame.headers?.message || 'Chat connection failed'); toast.error('Chat connection failed'); },
      onWebSocketClose: () => setConnected(false)
    });
    client.activate(); clientRef.current = client;
    return () => client.deactivate();
  }, [token, qc]);

  const sendMessage = (receiverId, content, sessionId) => {
    if (!receiverId || !content?.trim()) return;
    if (!clientRef.current?.connected) { toast.error('Chat is reconnecting. Try again in a moment.'); return; }
    clientRef.current.publish({ destination: '/app/chat.send', headers: { Authorization: `Bearer ${token}` }, body: JSON.stringify({ receiverId, sessionId, content }) });
    // The server only pushes to the receiver, so optimistically add our own
    // message to the open thread immediately.
    const optimistic = { id: `temp-${Date.now()}`, senderId: user?.id, receiverId, content, sentAt: new Date().toISOString(), read: false };
    qc.setQueryData(['chat', receiverId], (old) => {
      const page = old?.content ? old : { content: old || [] };
      return { ...page, content: [...(page.content || []), optimistic] };
    });
    qc.invalidateQueries({ queryKey: ['chat', 'conversations'] });
    qc.invalidateQueries({ queryKey: ['chat', receiverId] });
  };
  const sendTyping = (receiverId) => clientRef.current?.publish({ destination: '/app/chat.typing', body: JSON.stringify({ receiverId }) });
  return { sendMessage, sendTyping, isConnected, connectionError };
}