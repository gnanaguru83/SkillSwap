import api, { pageContent, unwrap } from './axios';

export const getSuggestions = async () => unwrap(await api.get('/api/v1/matches/suggestions'));
export const sendRequest = async (data) => unwrap(await api.post('/api/v1/matches/request', data));
export const getReceived = async (page = 0, size = 20) => unwrap(await api.get('/api/v1/matches/received', { params: { page, size } }));
export const getSent = async (page = 0, size = 20) => unwrap(await api.get('/api/v1/matches/sent', { params: { page, size } }));
export const acceptMatch = async (id) => unwrap(await api.put(`/api/v1/matches/${id}/accept`));
export const rejectMatch = async (id) => unwrap(await api.put(`/api/v1/matches/${id}/reject`));

export const getAcceptedMatches = async () => {
  const [sent, received] = await Promise.all([getSent(0, 100), getReceived(0, 100)]);
  return [...pageContent(sent), ...pageContent(received)].filter((match) => match.status === 'ACCEPTED');
};

export const getMatchSuggestions = getSuggestions;
export const sendMatchRequest = sendRequest;
export const getReceivedRequests = getReceived;
export const getSentRequests = getSent;