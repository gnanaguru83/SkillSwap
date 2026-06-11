import { useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { pageContent } from '../../api/axios';
import { getConversations, getMessages } from '../../api/chat.api';
import { getUser, searchUsers } from '../../api/user.api';
import { useAuth } from '../../hooks/useAuth';
import { useChat } from '../../hooks/useChat';
import { useMatches } from '../../hooks/useMatches';
import ConversationList from '../../components/chat/ConversationList';
import ChatWindow from '../../components/chat/ChatWindow';

export default function ChatPage() {
  const { userId } = useParams();
  const nav = useNavigate();
  const { currentUser } = useAuth();
  const { sendMessage, isConnected, connectionError } = useChat();
  const { received, sent } = useMatches();
  const [search, setSearch] = useState('');
  const conversations = useQuery({ queryKey: ['chat', 'conversations'], queryFn: getConversations, refetchInterval: 15000 });
  const partner = useQuery({ queryKey: ['user', userId], queryFn: () => getUser(userId), enabled: Boolean(userId) });
  const messages = useQuery({ queryKey: ['chat', userId], queryFn: () => getMessages(userId), enabled: Boolean(userId), refetchInterval: userId ? 4000 : false });
  const list = pageContent(conversations.data);
  const activeConversation = list.find((conversation) => conversation.partnerId === userId);

  // Connected friends = accepted matches. We show them as contacts even before
  // any message has been exchanged (WhatsApp-style contact list).
  const accepted = useMemo(
    () => [...pageContent(received.data), ...pageContent(sent.data)].filter((m) => m.status === 'ACCEPTED'),
    [received.data, sent.data]
  );
  const friends = useMemo(() => accepted.map((m) => {
    const mine = m.requesterId === currentUser?.id;
    return { partnerId: mine ? m.targetId : m.requesterId, partnerName: mine ? m.targetName : m.requesterName };
  }), [accepted, currentUser]);

  // Merge conversations (with messages) and friends (contacts), dedup by partner.
  const merged = useMemo(() => {
    const byId = new Map();
    list.forEach((c) => byId.set(c.partnerId, c));
    friends.forEach((f) => { if (f.partnerId && !byId.has(f.partnerId)) byId.set(f.partnerId, { partnerId: f.partnerId, partnerName: f.partnerName, isFriend: true }); });
    return Array.from(byId.values()).sort((a, b) => {
      if (a.lastMessageAt && b.lastMessageAt) return new Date(b.lastMessageAt) - new Date(a.lastMessageAt);
      if (a.lastMessageAt) return -1;
      if (b.lastMessageAt) return 1;
      return (a.partnerName || '').localeCompare(b.partnerName || '');
    });
  }, [list, friends]);

  const q = search.trim().toLowerCase();
  const convoList = useMemo(
    () => merged.filter((c) => (c.partnerName || c.partnerEmail || '').toLowerCase().includes(q)),
    [merged, q]
  );

  // Search any user (case-insensitive on the backend) to start a brand-new chat.
  const userResults = useQuery({ queryKey: ['chatUserSearch', q], queryFn: () => searchUsers({ skill: q, size: 10 }), enabled: q.length > 1 });
  const people = useMemo(() => {
    const existing = new Set(merged.map((c) => c.partnerId));
    return pageContent(userResults.data).filter((u) => u.id !== currentUser?.id && !existing.has(u.id));
  }, [userResults.data, merged, currentUser]);

  const activePartner = partner.data || (activeConversation ? { id: activeConversation.partnerId, fullName: activeConversation.partnerName, profilePictureUrl: activeConversation.partnerAvatarUrl } : (userId ? { id: userId, fullName: userId } : null));

  return <div className="space-y-5"><div><h1 className="font-display text-4xl font-extrabold">Chat</h1><p className="text-gray-500">Real-time messaging with your learning partners.</p></div>{connectionError&&<div className="rounded-xl bg-yellow-50 p-3 text-sm font-semibold text-yellow-700">Reconnecting... {connectionError}</div>}<div className="grid gap-5 lg:grid-cols-[360px_1fr]"><ConversationList conversations={convoList} people={people} activeId={userId} onOpen={(id)=>nav(`/chat/${id}`)} search={search} onSearch={setSearch}/><ChatWindow partner={activePartner} messages={[...pageContent(messages.data)].sort((a,b)=>new Date(a.sentAt||a.createdAt||0)-new Date(b.sentAt||b.createdAt||0))} currentUserId={currentUser?.id} connected={isConnected} onSend={(content)=>sendMessage(userId,content)}/></div></div>;
}