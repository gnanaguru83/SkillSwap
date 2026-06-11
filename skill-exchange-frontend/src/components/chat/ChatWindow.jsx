import { useEffect, useRef, useState } from 'react';
import { Send, SmilePlus, User } from 'lucide-react';
import { Link } from 'react-router-dom';
import UserAvatar from '../common/UserAvatar';
import MessageBubble from './MessageBubble';

export default function ChatWindow({ partner, messages = [], currentUserId, onSend, connected }) {
  const [text, setText] = useState(''); const bottomRef = useRef(null);
  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages.length]);
  const submit = () => { if (!text.trim()) return; onSend(text.trim()); setText(''); };
  if (!partner) return <div className="card flex h-full items-center justify-center text-gray-500">Choose a conversation to start chatting.</div>;
  return <section className="card flex h-[760px] flex-col p-0"><header className="flex items-center justify-between border-b border-gray-100 p-4"><Link to={`/profile/${partner.id}`} className="flex items-center gap-3 rounded-lg p-1 hover:bg-gray-50"><UserAvatar user={partner} isOnline={partner.online} /><div><h2 className="font-bold text-gray-900 hover:text-purple-700 hover:underline">{partner.fullName || partner.name || partner.id}</h2><p className="text-xs text-gray-500">{connected ? 'Online connection active' : 'Reconnecting...'}</p></div></Link><Link to={`/profile/${partner.id}`} className="btn-secondary inline-flex items-center gap-1"><User size={15}/>View Profile</Link></header><div className="soft-grid flex-1 space-y-3 overflow-y-auto bg-gray-50 p-4">{messages.map((m) => <MessageBubble key={m.id || `${m.sentAt}-${m.content}`} message={m} mine={m.senderId === currentUserId} />)}<div ref={bottomRef}/></div><footer className="flex items-center gap-2 border-t border-gray-100 p-4"><button className="rounded-lg p-2 text-gray-500 hover:bg-gray-100"><SmilePlus size={20}/></button><input value={text} onChange={(e)=>setText(e.target.value)} onKeyDown={(e)=>{ if(e.key==='Enter') submit(); }} placeholder="Type a message..." className="input"/><button onClick={submit} className="btn-primary inline-flex items-center gap-2"><Send size={16}/>Send</button></footer></section>;
}