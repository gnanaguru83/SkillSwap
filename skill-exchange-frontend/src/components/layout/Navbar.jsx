import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { Bell, LogOut, Menu, Sparkles, UserCog } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useNotifications } from '../../hooks/useNotifications';
import UserAvatar from '../common/UserAvatar';
import NotificationDropdown from '../notification/NotificationDropdown';

const links = [['/dashboard','Dashboard'],['/discover','Discover'],['/matches','Matches'],['/sessions','Sessions'],['/chat','Chat']];

function notificationTarget(notification) {
  switch (notification?.type) {
    case 'MATCH_REQUEST_RECEIVED':
      return '/matches?tab=received';
    case 'MATCH_ACCEPTED':
    case 'MATCH_REJECTED':
      return '/matches?tab=sent';
    case 'SESSION_BOOKED':
    case 'SESSION_REMINDER':
      return '/sessions?tab=upcoming';
    case 'SESSION_CANCELLED':
    case 'RATE_SESSION':
      return '/sessions?tab=history';
    case 'NEW_MESSAGE_RECEIVED':
      return '/chat';
    case 'NEW_RATING_RECEIVED':
    case 'BADGE_EARNED':
      return '/profile';
    default:
      return '/notifications';
  }
}

export default function Navbar({ onMenu }) {
  const { currentUser, logout } = useAuth();
  const { notifications, count, read, readAll } = useNotifications();
  const [open, setOpen] = useState(false);
  const [userOpen, setUserOpen] = useState(false);
  const menuRef = useRef(null);
  const navigate = useNavigate();
  const unread = count.data?.count || 0;

  useEffect(() => {
    function closeOnOutsideClick(event) {
      if (!menuRef.current?.contains(event.target)) {
        setOpen(false);
        setUserOpen(false);
      }
    }
    document.addEventListener('mousedown', closeOnOutsideClick);
    return () => document.removeEventListener('mousedown', closeOnOutsideClick);
  }, []);

  const closeMenus = () => {
    setOpen(false);
    setUserOpen(false);
  };

  const toggleNotifications = () => {
    setOpen((value) => !value);
    setUserOpen(false);
  };

  const toggleProfile = () => {
    setUserOpen((value) => !value);
    setOpen(false);
  };

  const handleNotificationClick = (notification) => {
    if (!notification?.read) read.mutate(notification.id);
    closeMenus();
    navigate(notificationTarget(notification));
  };

  const handleReadAll = () => {
    readAll.mutate();
    closeMenus();
  };

  return <header className="sticky top-0 z-30 border-b border-gray-100 bg-white/90 backdrop-blur"><div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4"><div className="flex items-center gap-3"><button onClick={onMenu} className="rounded-lg p-2 md:hidden"><Menu/></button><Link onClick={closeMenus} to="/dashboard" className="flex items-center gap-2"><span className="rounded-xl bg-purple-600 p-2 text-white"><Sparkles size={20}/></span><span className="font-display text-2xl font-extrabold text-gray-900">SkillSwap</span></Link></div><nav className="hidden items-center gap-1 md:flex">{links.map(([to,label]) => <NavLink key={to} onClick={closeMenus} to={to} className={({isActive}) => `rounded-lg px-3 py-2 text-sm font-bold ${isActive ? 'bg-purple-50 text-purple-700' : 'text-gray-600 hover:bg-gray-50'}`}>{label}{label==='Chat' && unread>0 && <span className="ml-1 rounded-full bg-purple-600 px-1.5 text-[10px] text-white">{unread}</span>}</NavLink>)}</nav><div ref={menuRef} className="relative flex items-center gap-3"><button onClick={toggleNotifications} className="relative rounded-xl border border-gray-100 p-2 text-gray-600 hover:bg-gray-50"><Bell size={20}/>{unread>0 && <span className="absolute -right-1 -top-1 rounded-full bg-red-500 px-1.5 text-[10px] font-bold text-white">{unread}</span>}</button><NotificationDropdown open={open} notifications={notifications} onItemClick={handleNotificationClick} onReadAll={handleReadAll}/><button onClick={toggleProfile}><UserAvatar user={currentUser} /></button>{userOpen && <div className="absolute right-0 top-12 z-40 w-52 rounded-2xl border border-gray-100 bg-white p-2 shadow-xl"><Link onClick={closeMenus} className="block rounded-lg px-3 py-2 text-sm font-semibold hover:bg-gray-50" to="/profile">My Profile</Link><Link onClick={closeMenus} className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-semibold hover:bg-gray-50" to="/profile/edit"><UserCog size={16}/>Edit Profile</Link><button onClick={()=>{closeMenus();logout();}} className="mt-2 flex w-full items-center gap-2 border-t border-gray-100 px-3 py-2 text-left text-sm font-semibold text-red-600"><LogOut size={16}/>Sign Out</button></div>}</div></div></header>;
}