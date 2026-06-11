import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import * as authApi from '../api/auth.api';
import { getMe } from '../api/user.api';
import { useAuthStore } from '../store/authStore';

const messageOf = (error) => error.response?.data?.message || 'Something went wrong';

export function useAuth() {
  const store = useAuthStore();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const meQuery = useQuery({ queryKey: ['user', 'me'], queryFn: getMe, enabled: store.isAuthenticated });

  const loginMutation = useMutation({
    mutationFn: ({ email, password }) => authApi.login(email, password),
    onSuccess: async (tokens) => {
      localStorage.setItem('skillSwapToken', tokens.accessToken);
      const user = await queryClient.fetchQuery({ queryKey: ['user', 'me'], queryFn: getMe });
      store.login(tokens.accessToken, user);
      toast.success('Welcome back!');
      navigate('/dashboard');
    },
    onError: (error) => toast.error(messageOf(error))
  });

  const registerMutation = useMutation({
    mutationFn: authApi.register,
    onSuccess: async (tokens) => {
      localStorage.setItem('skillSwapToken', tokens.accessToken);
      const user = await queryClient.fetchQuery({ queryKey: ['user', 'me'], queryFn: getMe });
      store.login(tokens.accessToken, user);
      toast.success('Account created. Welcome to SkillSwap!');
      navigate('/discover');
    },
    onError: (error) => toast.error(messageOf(error))
  });

  const logout = () => { store.logout(); queryClient.clear(); navigate('/login'); };
  return { ...store, currentUser: meQuery.data || store.user, loginMutation, registerMutation, logout };
}