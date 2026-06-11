import api, { unwrap } from './axios';
export const login = async (email, password) => unwrap(await api.post('/api/v1/auth/login', { email, password }));
export const register = async (data) => unwrap(await api.post('/api/v1/auth/register', data));
export const refresh = async (refreshToken) => unwrap(await api.post('/api/v1/auth/refresh', { refreshToken }));