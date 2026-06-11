import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { formatDistanceToNow } from 'date-fns';
import { Inbox, Send, Sparkles } from 'lucide-react';
import { pageContent } from '../../api/axios';
import { useMatches } from '../../hooks/useMatches';
import EmptyState from '../../components/common/EmptyState';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import MatchCard from '../../components/match/MatchCard';

const validTabs = ['suggestions', 'received', 'sent'];

export default function MatchesPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialTab = validTabs.includes(searchParams.get('tab')) ? searchParams.get('tab') : 'suggestions';
  const [tab, setTab] = useState(initialTab);
  const { suggestions, received, sent, send, accept, reject } = useMatches();
  const tabs = [['suggestions','Suggestions'],['received','Received'],['sent','Sent']];

  useEffect(() => {
    const next = searchParams.get('tab');
    if (validTabs.includes(next)) setTab(next);
  }, [searchParams]);

  const chooseTab = (next) => {
    setTab(next);
    setSearchParams({ tab: next });
  };

  return <div className="space-y-6"><div><h1 className="font-display text-4xl font-extrabold">Matches</h1><p className="text-gray-500">AI and algorithmic partner matching, incoming requests, and sent requests.</p></div><div className="card flex gap-2 p-2">{tabs.map(([id,label])=><button key={id} onClick={()=>chooseTab(id)} className={`flex-1 rounded-lg px-4 py-3 font-bold ${tab===id?'bg-purple-600 text-white':'text-gray-600 hover:bg-gray-50'}`}>{label}</button>)}</div>{tab==='suggestions' && (suggestions.isLoading?<LoadingSpinner/>:(suggestions.data||[]).length?<div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">{suggestions.data.map(m=><MatchCard key={m.userId} match={m} onRequest={(match)=>send.mutate({targetId:match.userId,teachSkillId:match.learnSkills?.[0]?.id,learnSkillId:match.teachSkills?.[0]?.id,message:'I think we would be a strong skill-swap match.'})}/>)}</div>:<EmptyState icon={Sparkles} title="No match suggestions yet" description="Add skills to your profile to get matched!"/>)}{tab==='received' && <RequestList items={pageContent(received.data).filter((r)=>r.status==='PENDING')} received onAccept={accept.mutate} onReject={reject.mutate}/>} {tab==='sent' && <RequestList items={pageContent(sent.data)} />}</div>;
}

function RequestList({items=[],received,onAccept,onReject}){ if(!items.length) return <EmptyState icon={received?Inbox:Send} title={received?'No incoming requests':'No sent requests'} description={received?'When someone wants to learn with you, requests appear here.':'Send a request from suggestions or discovery.'}/>; return <div className="space-y-4">{items.map(r=><article key={r.id} className="card"><div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between"><div><Link to={`/profile/${received?r.requesterId:r.targetId}`} className="font-bold text-gray-900 hover:text-purple-700 hover:underline">{received?r.requesterName:r.targetName}</Link><p className="text-sm text-gray-500">Wants to learn {r.learnSkillName} - Can teach {r.teachSkillName}</p>{r.message&&<p className="mt-2 rounded-lg bg-gray-50 p-3 text-sm text-gray-600">{r.message}</p>}<p className="mt-2 text-xs text-gray-400">{r.createdAt?formatDistanceToNow(new Date(r.createdAt),{addSuffix:true}):'recently'}</p></div><div className="flex gap-2">{received?<><button onClick={()=>onAccept(r.id)} className="rounded-lg bg-green-500 px-4 py-2 font-bold text-white">Accept</button><button onClick={()=>onReject(r.id)} className="rounded-lg bg-red-500 px-4 py-2 font-bold text-white">Decline</button></>:<span className={`rounded-full px-3 py-1 text-sm font-bold ${r.status==='ACCEPTED'?'bg-green-50 text-green-700':r.status==='REJECTED'?'bg-red-50 text-red-700':'bg-yellow-50 text-yellow-700'}`}>{r.status}</span>}</div></div></article>)}</div> }