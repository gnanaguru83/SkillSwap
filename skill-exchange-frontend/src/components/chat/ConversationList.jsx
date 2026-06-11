import { formatDistanceToNow } from 'date-fns';
import UserAvatar from '../common/UserAvatar';

function conversationId(conversation) {
  return conversation.partnerId || conversation.userId || conversation.id || conversation;
}

function conversationUser(conversation) {
  return {
    id: conversationId(conversation),
    fullName: conversation.partnerName || conversation.fullName || conversation.name || String(conversationId(conversation)),
    email: conversation.partnerEmail,
    profilePictureUrl: conversation.partnerAvatarUrl || conversation.profilePictureUrl
  };
}

export default function ConversationList({ conversations = [], people = [], activeId, onOpen, search, onSearch }) {
  const preview = (c) => {
    if (!c.lastMessage) return 'Tap to start chatting';
    const fromMe = c.lastMessageIsFromMe ?? c.lastMessageIsMe;
    return `${fromMe ? 'You: ' : ''}${c.lastMessage}`;
  };
  return <aside className="card flex h-full flex-col overflow-hidden p-0">
    <div className="border-b border-gray-100 p-3">
      <input value={search} onChange={(e)=>onSearch(e.target.value)} placeholder="Search friends or people..." className="input" />
    </div>
    <div className="flex-1 overflow-y-auto">
      {conversations.map((conversation) => { const id = conversationId(conversation); const user = conversationUser(conversation); return <button key={id} onClick={() => onOpen(id)} className={`flex w-full items-center gap-3 border-b border-gray-50 p-3 text-left hover:bg-purple-50 ${activeId === id ? 'bg-purple-50' : ''}`}><UserAvatar user={user} isOnline={conversation.partnerOnline || conversation.online} /><div className="min-w-0 flex-1"><div className="flex items-center justify-between gap-2"><p className={`truncate text-gray-900 ${conversation.unreadCount > 0 ? 'font-extrabold' : 'font-bold'}`}>{user.fullName}</p>{conversation.lastMessageAt && <span className="shrink-0 text-[11px] text-gray-400">{formatDistanceToNow(new Date(conversation.lastMessageAt), { addSuffix: true })}</span>}</div><p className={`truncate text-xs ${conversation.unreadCount > 0 ? 'font-semibold text-gray-700' : 'text-gray-500'}`}>{preview(conversation)}</p></div>{conversation.unreadCount > 0 && <span className="rounded-full bg-purple-600 px-2 py-0.5 text-xs font-bold text-white">{conversation.unreadCount}</span>}</button>; })}
      {people.length > 0 && <><p className="border-b border-gray-100 bg-gray-50 px-4 py-2 text-xs font-bold uppercase tracking-wide text-gray-500">Start a new chat</p>{people.map((u) => <button key={u.id} onClick={() => onOpen(u.id)} className="flex w-full items-center gap-3 border-b border-gray-50 p-3 text-left hover:bg-purple-50"><UserAvatar user={u} isOnline={u.online} /><div className="min-w-0 flex-1"><p className="truncate font-bold text-gray-900">{u.fullName}</p><p className="truncate text-xs text-gray-500">{u.headline || u.location || 'Tap to start chatting'}</p></div></button>)}</>}
      {conversations.length === 0 && people.length === 0 && <p className="p-6 text-center text-sm text-gray-500">{search.trim() ? 'No people found. Try another name.' : 'No connections yet. Accept a match or search to start chatting.'}</p>}
    </div>
  </aside>;
}