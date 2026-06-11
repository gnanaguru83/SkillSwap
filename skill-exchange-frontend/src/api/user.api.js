import api, { unwrap } from './axios';

export const getMe = async () => unwrap(await api.get('/api/v1/users/me'));
export const updateMe = async (data) => unwrap(await api.put('/api/v1/users/me', data));
export const getUser = async (id) => unwrap(await api.get(`/api/v1/users/${id}`));

export const searchUsers = async (filters = {}) => {
  const params = typeof filters === 'string'
    ? { skill: filters || undefined, page: 0, size: 30 }
    : {
        skill: filters.skill || undefined,
        type: filters.type === 'BOTH' ? undefined : filters.type,
        location: filters.location || undefined,
        page: filters.page ?? 0,
        size: filters.size ?? 30
      };
  return unwrap(await api.get('/api/v1/users/search', { params }));
};

export const getMySkills = async () => unwrap(await api.get('/api/v1/users/me/skills'));
export const getUserSkills = async (userId) => unwrap(await api.get(`/api/v1/users/${userId}/skills`));
export const addSkill = async (data) => unwrap(await api.post('/api/v1/users/me/skills', data));
export const removeSkill = async (skillId) => unwrap(await api.delete(`/api/v1/users/me/skills/${skillId}`));
export const deleteSkill = removeSkill;
export const getMyBadges = async () => unwrap(await api.get('/api/v1/users/me/badges'));

export const getMyCertifications = async () => unwrap(await api.get('/api/v1/users/me/certifications'));
export const getUserCertifications = async (id) => unwrap(await api.get(`/api/v1/users/${id}/certifications`));
export const addCertification = async (data) => unwrap(await api.post('/api/v1/users/me/certifications', data));
export const removeCertification = async (id) => unwrap(await api.delete(`/api/v1/users/me/certifications/${id}`));

export const getMyEducation = async () => unwrap(await api.get('/api/v1/users/me/education'));
export const getUserEducation = async (id) => unwrap(await api.get(`/api/v1/users/${id}/education`));
export const addEducation = async (data) => unwrap(await api.post('/api/v1/users/me/education', data));
export const removeEducation = async (id) => unwrap(await api.delete(`/api/v1/users/me/education/${id}`));

export const getMyExperience = async () => unwrap(await api.get('/api/v1/users/me/experience'));
export const getUserExperience = async (id) => unwrap(await api.get(`/api/v1/users/${id}/experience`));
export const addExperience = async (data) => unwrap(await api.post('/api/v1/users/me/experience', data));
export const removeExperience = async (id) => unwrap(await api.delete(`/api/v1/users/me/experience/${id}`));

export const getUserPublicProfile = async (id) => {
  const [user, skills] = await Promise.all([
    getUser(id),
    getUserSkills(id).catch(() => [])
  ]);
  return { ...user, skills };
};