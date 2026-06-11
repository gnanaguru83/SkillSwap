import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuthStore } from './store/authStore';
import ProtectedRoute from './components/common/ProtectedRoute';
import AppLayout from './components/layout/AppLayout';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import DiscoverPage from './pages/discover/DiscoverPage';
import MatchesPage from './pages/matches/MatchesPage';
import SessionsPage from './pages/sessions/SessionsPage';
import ChatPage from './pages/chat/ChatPage';
import ProfilePage from './pages/profile/ProfilePage';
import EditProfilePage from './pages/profile/EditProfilePage';
import RatePage from './pages/ratings/RatePage';

export default function App() {
  const authed = useAuthStore((s) => s.isAuthenticated);
  return <Routes><Route path="/" element={<Navigate to={authed ? '/dashboard' : '/login'} replace />} /><Route path="/login" element={<LoginPage />} /><Route path="/register" element={<RegisterPage />} /><Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}><Route path="/dashboard" element={<DashboardPage />} /><Route path="/discover" element={<DiscoverPage />} /><Route path="/matches" element={<MatchesPage />} /><Route path="/sessions" element={<SessionsPage />} /><Route path="/chat" element={<ChatPage />} /><Route path="/chat/:userId" element={<ChatPage />} /><Route path="/profile" element={<ProfilePage />} /><Route path="/profile/edit" element={<EditProfilePage />} /><Route path="/profile/:userId" element={<ProfilePage />} /><Route path="/sessions/:sessionId/rate" element={<RatePage />} /></Route></Routes>;
}