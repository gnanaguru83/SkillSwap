import { create } from 'zustand';

const storedToken = localStorage.getItem('skillSwapToken');
const storedUser = localStorage.getItem('skillSwapUser');

function parseStoredUser(value) {
  if (!value) return null;
  try {
    return JSON.parse(value);
  } catch {
    localStorage.removeItem('skillSwapUser');
    localStorage.removeItem('skillSwapToken');
    return null;
  }
}

const initialUser = parseStoredUser(storedUser);
const initialToken = initialUser ? storedToken : null;

export const useAuthStore = create((set) => ({
  token: initialToken,
  user: initialUser,
  isAuthenticated: Boolean(initialToken),
  login: (token, user) => {
    localStorage.setItem('skillSwapToken', token);
    localStorage.setItem('skillSwapUser', JSON.stringify(user ?? {}));
    set({ token, user, isAuthenticated: true });
  },
  logout: () => {
    localStorage.removeItem('skillSwapToken');
    localStorage.removeItem('skillSwapUser');
    set({ token: null, user: null, isAuthenticated: false });
  },
  updateUser: (user) => {
    localStorage.setItem('skillSwapUser', JSON.stringify(user ?? {}));
    set({ user });
  }
}));
