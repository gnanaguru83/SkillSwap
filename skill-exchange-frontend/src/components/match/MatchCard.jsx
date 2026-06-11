import { Calendar, MapPin, Send } from 'lucide-react';
import { Link } from 'react-router-dom';
import UserAvatar from '../common/UserAvatar';
import SkillBadge from '../common/SkillBadge';
import CompatibilityScore from './CompatibilityScore';

export default function MatchCard({ match, onRequest }) {
  const teach = match.teachSkills || [];
  const learn = match.learnSkills || [];
  return <article className="card flex h-full flex-col gap-4"><div className="flex items-start gap-3"><Link to={`/profile/${match.userId}`}><UserAvatar user={{ fullName: match.fullName, profilePictureUrl: match.profilePicture }} isOnline={match.online} /></Link><div className="min-w-0 flex-1"><Link to={`/profile/${match.userId}`} className="truncate text-lg font-bold text-gray-900 hover:text-purple-700 hover:underline">{match.fullName}</Link><p className="flex items-center gap-1 text-sm text-gray-500"><MapPin size={14} />{match.location || 'Remote friendly'}</p></div></div><CompatibilityScore score={match.compatibilityScore || 0} /><div className="space-y-2"><div className="flex flex-wrap gap-2">{teach.slice(0,3).map((s) => <SkillBadge key={`t-${s.id || s.name}`} name={`Teaches ${s.name}`} type="TEACH" size="sm" />)}</div><div className="flex flex-wrap gap-2">{learn.slice(0,3).map((s) => <SkillBadge key={`l-${s.id || s.name}`} name={`Learns ${s.name}`} type="LEARN" size="sm" />)}</div></div>{match.aiMatchReason && <p className="rounded-lg bg-purple-50 p-3 text-sm italic text-purple-700">{match.aiMatchReason}</p>}<div className="flex flex-wrap gap-2 text-xs text-gray-500">{(match.commonAvailability || []).slice(0,2).map((slot) => <span key={slot} className="inline-flex items-center gap-1 rounded-full bg-gray-50 px-2 py-1"><Calendar size={12}/>{slot}</span>)}</div><button onClick={() => onRequest?.(match)} className="btn-primary mt-auto inline-flex items-center justify-center gap-2"><Send size={16}/>Send Match Request</button></article>;
}