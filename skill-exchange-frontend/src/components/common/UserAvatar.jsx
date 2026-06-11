import OnlineDot from './OnlineDot';
const sizes = { sm: 'h-9 w-9 text-xs', md: 'h-11 w-11 text-sm', lg: 'h-24 w-24 text-2xl' };
export default function UserAvatar({ user = {}, size = 'md', isOnline = false }) {
  const initials = (user.fullName || user.name || user.email || 'U').split(' ').map((p) => p[0]).slice(0, 2).join('').toUpperCase();
  return <div className="relative inline-flex"><div className={`${sizes[size]} flex items-center justify-center overflow-hidden rounded-full bg-gradient-to-br from-purple-600 to-indigo-500 font-bold text-white shadow-sm`}>{user.profilePictureUrl || user.profilePicture ? <img src={user.profilePictureUrl || user.profilePicture} alt={user.fullName} className="h-full w-full object-cover" /> : initials}</div><span className="absolute bottom-0 right-0"><OnlineDot online={isOnline} /></span></div>;
}