import api, { unwrap } from './axios';
export const getAllSkills = async (page = 0, size = 100) => unwrap(await api.get('/api/v1/skills', { params: { page, size } }));
export const searchSkills = async (q = '') => unwrap(await api.get('/api/v1/skills/search', { params: { q, page: 0, size: 50 } }));
export const getCategories = async () => unwrap(await api.get('/api/v1/skills/categories'));