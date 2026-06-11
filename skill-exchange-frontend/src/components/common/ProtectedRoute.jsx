import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
export default function ProtectedRoute({ children }) { const authed = useAuthStore((s) => s.isAuthenticated); const location = useLocation(); return authed ? children : <Navigate to="/login" replace state={{ from: location }} />; }