import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { CalendarPlus } from 'lucide-react';
import { pageContent } from '../../api/axios';
import { useAuth } from '../../hooks/useAuth';
import { useMatches } from '../../hooks/useMatches';
import { useSessions } from '../../hooks/useSessions';
import EmptyState from '../../components/common/EmptyState';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import SessionCard from '../../components/session/SessionCard';
import BookSessionModal from '../../components/session/BookSessionModal';

const validTabs = ['upcoming', 'history'];

export default function SessionsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialTab = validTabs.includes(searchParams.get('tab')) ? searchParams.get('tab') : 'upcoming';
  const [tab, setTab] = useState(initialTab);
  const [booking, setBooking] = useState(false);
  const nav = useNavigate();
  const { currentUser } = useAuth();
  const { upcoming, history, book, cancel, complete } = useSessions();
  const { received, sent } = useMatches();
  const accepted = [...pageContent(received.data), ...pageContent(sent.data)].filter((m) => m.status === 'ACCEPTED');
  const data = tab === 'upcoming' ? pageContent(upcoming.data) : pageContent(history.data);

  useEffect(() => {
    const next = searchParams.get('tab');
    if (validTabs.includes(next)) setTab(next);
  }, [searchParams]);

  const chooseTab = (next) => {
    setTab(next);
    setSearchParams({ tab: next });
  };

  return <div className="space-y-6"><div className="flex flex-col justify-between gap-4 md:flex-row md:items-center"><div><h1 className="font-display text-4xl font-extrabold">Sessions</h1><p className="text-gray-500">Book, join, complete, cancel, and rate peer learning sessions.</p></div><button onClick={()=>setBooking(true)} className="btn-primary inline-flex items-center gap-2"><CalendarPlus size={18}/>Book Session</button></div><div className="card flex gap-2 p-2"><button onClick={()=>chooseTab('upcoming')} className={`flex-1 rounded-lg py-3 font-bold ${tab==='upcoming'?'bg-purple-600 text-white':'text-gray-600'}`}>Upcoming</button><button onClick={()=>chooseTab('history')} className={`flex-1 rounded-lg py-3 font-bold ${tab==='history'?'bg-purple-600 text-white':'text-gray-600'}`}>History</button></div>{(upcoming.isLoading||history.isLoading)?<LoadingSpinner/>:data.length?<div className="space-y-4">{data.map(s=><SessionCard key={s.id} session={s} currentUserId={currentUser?.id} onCancel={(x)=>confirm('Cancel this session?')&&cancel.mutate(x.id)} onComplete={(x)=>complete.mutate(x.id)} onRate={(x)=>nav(`/sessions/${x.id}/rate`)}/>)}</div>:<EmptyState icon={CalendarPlus} title={tab==='upcoming'?'No upcoming sessions':'No past sessions'} description={tab==='upcoming'?'No upcoming sessions. Accept a match request to book your first session!':'Completed and cancelled sessions will appear here.'}/>}<BookSessionModal open={booking} onClose={()=>setBooking(false)} matches={accepted} currentUser={currentUser} onBook={(v)=>book.mutate(v,{onSuccess:()=>{setBooking(false);chooseTab('upcoming');}})} isLoading={book.isPending}/></div>;
}