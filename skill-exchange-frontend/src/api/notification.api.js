import api, { unwrap } from './axios';
export const getNotifications = async (page = 0, size = 10) => unwrap(await api.get('/api/v1/notifications', { params: { page, size } }));
export const markRead = async (id) => unwrap(await api.put(`/api/v1/notifications/${id}/read`));
export const markAllRead = async () => unwrap(await api.put('/api/v1/notifications/read-all'));
export const getUnreadCount = async () => unwrap(await api.get('/api/v1/notifications/unread-count'));