import { Bell, CheckCheck } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';
import { pageContent } from '../../api/axios';

export default function NotificationDropdown({ open, notifications, onItemClick, onReadAll }) {
  if (!open) return null;
  const items = pageContent(notifications?.data);
  return <div className="absolute right-0 top-12 z-40 w-96 overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-xl"><div className="flex items-center justify-between border-b border-gray-100 p-4"><h3 className="font-bold">Notifications</h3><button onClick={onReadAll} className="text-xs font-bold text-purple-600">Mark all as read</button></div><div className="max-h-96 overflow-y-auto">{items.length ? items.map((n) => <button key={n.id} onClick={() => onItemClick(n)} className={`flex w-full gap-3 border-b border-gray-50 p-4 text-left hover:bg-gray-50 ${!n.read ? 'border-l-4 border-l-purple-600' : ''}`}><Bell className="mt-1 text-purple-600" size={18}/><span><p className="font-bold text-gray-900">{n.title}</p><p className="text-sm text-gray-500">{n.message}</p><p className="mt-1 text-xs text-gray-400">{n.createdAt ? formatDistanceToNow(new Date(n.createdAt), { addSuffix: true }) : 'just now'}</p></span></button>) : <div className="p-8 text-center text-sm text-gray-500">No notifications yet.</div>}</div><div className="flex items-center justify-center gap-2 p-3 text-xs font-bold text-gray-500"><CheckCheck size={14}/>You are all caught up</div></div>;
}