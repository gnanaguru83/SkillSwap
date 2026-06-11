import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as api from '../api/notification.api';
export function useNotifications() {
  const qc = useQueryClient();
  const notifications = useQuery({ queryKey: ['notifications'], queryFn: () => api.getNotifications(0, 10) });
  const count = useQuery({ queryKey: ['notifications', 'count'], queryFn: api.getUnreadCount });
  const refresh = () => { qc.invalidateQueries({ queryKey: ['notifications'] }); qc.invalidateQueries({ queryKey: ['notifications', 'count'] }); };
  const read = useMutation({ mutationFn: api.markRead, onSuccess: refresh });
  const readAll = useMutation({ mutationFn: api.markAllRead, onSuccess: refresh });
  return { notifications, count, read, readAll };
}