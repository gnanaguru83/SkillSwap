import api, { unwrap } from './axios';
export const bookSession = async (data) => unwrap(await api.post('/api/v1/sessions', data));
export const getUpcoming = async (page = 0, size = 20) => unwrap(await api.get('/api/v1/sessions/upcoming', { params: { page, size } }));
export const getHistory = async (page = 0, size = 20) => unwrap(await api.get('/api/v1/sessions/history', { params: { page, size } }));
export const getSession = async (id) => unwrap(await api.get(`/api/v1/sessions/${id}`));
export const cancelSession = async (id) => unwrap(await api.put(`/api/v1/sessions/${id}/cancel`));
export const completeSession = async (id) => unwrap(await api.put(`/api/v1/sessions/${id}/complete`));

export const getUpcomingSessions = getUpcoming;
export const getSessionHistory = getHistory;