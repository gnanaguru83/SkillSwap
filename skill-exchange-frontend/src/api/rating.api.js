import api, { unwrap } from './axios';
export const submitRating = async (data) => unwrap(await api.post('/api/v1/ratings', data));
export const getUserRatings = async (userId, page = 0, size = 20) => unwrap(await api.get(`/api/v1/ratings/user/${userId}`, { params: { page, size } }));