import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import * as matchingApi from '../api/matching.api';
const err = (e) => e.response?.data?.message || 'Request failed';
export function useMatches() {
  const qc = useQueryClient();
  const suggestions = useQuery({ queryKey: ['matches', 'suggestions'], queryFn: matchingApi.getSuggestions });
  const received = useQuery({ queryKey: ['matches', 'received'], queryFn: () => matchingApi.getReceived() });
  const sent = useQuery({ queryKey: ['matches', 'sent'], queryFn: () => matchingApi.getSent() });
  const invalidate = () => { qc.invalidateQueries({ queryKey: ['matches'] }); qc.invalidateQueries({ queryKey: ['sessions'] }); };
  const send = useMutation({ mutationFn: matchingApi.sendRequest, onSuccess: () => { toast.success('Request sent!'); invalidate(); }, onError: (e) => toast.error(err(e)) });
  const accept = useMutation({ mutationFn: matchingApi.acceptMatch, onSuccess: () => { toast.success('Match accepted! You can now book a session'); invalidate(); }, onError: (e) => toast.error(err(e)) });
  const reject = useMutation({ mutationFn: matchingApi.rejectMatch, onSuccess: () => { toast.success('Match declined'); invalidate(); }, onError: (e) => toast.error(err(e)) });
  return { suggestions, received, sent, send, accept, reject };
}