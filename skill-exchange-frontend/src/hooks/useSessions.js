import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import * as sessionApi from '../api/session.api';

const err = (e) => e.response?.data?.message || 'Session action failed';

export function useSessions() {
  const qc = useQueryClient();
  const upcoming = useQuery({ queryKey: ['sessions', 'upcoming'], queryFn: () => sessionApi.getUpcoming() });
  const history = useQuery({ queryKey: ['sessions', 'history'], queryFn: () => sessionApi.getHistory() });
  const refresh = () => qc.refetchQueries({ queryKey: ['sessions'] });
  const book = useMutation({ mutationFn: sessionApi.bookSession, onSuccess: async () => { toast.success('Session booked! Check your upcoming sessions'); await refresh(); }, onError: (e) => toast.error(err(e)) });
  const cancel = useMutation({ mutationFn: sessionApi.cancelSession, onSuccess: async () => { toast.success('Session cancelled'); await refresh(); }, onError: (e) => toast.error(err(e)) });
  const complete = useMutation({ mutationFn: sessionApi.completeSession, onSuccess: async () => { toast.success('Session completed'); await refresh(); }, onError: (e) => toast.error(err(e)) });
  return { upcoming, history, book, cancel, complete };
}