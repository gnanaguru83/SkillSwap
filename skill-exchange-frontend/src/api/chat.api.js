import api, { unwrap } from './axios';
export const getConversations = async () => unwrap(await api.get('/api/v1/chat/conversations'));
export const getMessages = async (userId, page = 0, size = 100) => unwrap(await api.get(`/api/v1/chat/messages/${userId}`, { params: { page, size } }));