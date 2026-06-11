import { useEffect, useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeftRight, Calendar, CheckCircle2, ChevronDown, Clock, Link2, X } from 'lucide-react';
import { pageContent } from '../../api/axios';
import { getAllSkills } from '../../api/skill.api';

function minimumLocalDateTime() {
  const date = new Date(Date.now() + 5 * 60 * 1000);
  date.setSeconds(0, 0);
  const offset = date.getTimezoneOffset();
  return new Date(date.getTime() - offset * 60000).toISOString().slice(0, 16);
}

export default function BookSessionModal({ open, onClose, matches = [], currentUser, onBook, isLoading }) {
  const accepted = useMemo(() => matches.filter((match) => match.status === 'ACCEPTED'), [matches]);
  const [matchId, setMatchId] = useState('');
  const [skillName, setSkillName] = useState('');
  const [swapped, setSwapped] = useState(false);
  const [scheduledAt, setScheduledAt] = useState('');
  const [durationMinutes, setDurationMinutes] = useState(60);
  const [meetingLink, setMeetingLink] = useState('');
  const [notes, setNotes] = useState('');

  const skillsQuery = useQuery({ queryKey: ['skills'], queryFn: () => getAllSkills(0, 200), enabled: open });
  const allSkills = pageContent(skillsQuery.data);

  const selected = useMemo(() => accepted.find((match) => match.id === matchId), [accepted, matchId]);

  // Resolve the typed/selected skill name to an existing skill id (case-insensitive).
  const matchedSkill = useMemo(() => {
    const q = skillName.trim().toLowerCase();
    if (!q) return null;
    return allSkills.find((s) => s.name.toLowerCase() === q) || null;
  }, [skillName, allSkills]);

  // Roles: by default the partner teaches and you learn; Swap flips it.
  const roles = useMemo(() => {
    if (!selected || !currentUser) return null;
    const meIsRequester = selected.requesterId === currentUser.id;
    const you = { id: currentUser.id, name: 'You' };
    const partner = meIsRequester
      ? { id: selected.targetId, name: selected.targetName || 'Partner' }
      : { id: selected.requesterId, name: selected.requesterName || 'Partner' };
    return swapped ? { teacher: you, learner: partner } : { teacher: partner, learner: you };
  }, [selected, currentUser, swapped]);

  // Pre-fill the skill with the match's learn skill when a match is chosen.
  useEffect(() => {
    if (!selected) { setSkillName(''); setSwapped(false); return; }
    setSkillName(selected.learnSkillName || selected.teachSkillName || '');
    setSwapped(false);
  }, [selected]);

  if (!open) return null;

  const minDateTime = minimumLocalDateTime();
  const invalidPast = scheduledAt && new Date(scheduledAt) <= new Date();
  const canSubmit = selected && matchedSkill && roles && scheduledAt && !invalidPast && !isLoading;

  const submit = (event) => {
    event.preventDefault();
    if (!canSubmit) return;
    onBook({
      matchId: selected.id,
      teacherId: roles.teacher.id,
      learnerId: roles.learner.id,
      skillId: matchedSkill.id,
      scheduledAt: `${scheduledAt}:00`,
      durationMinutes: Number(durationMinutes),
      meetingLink: meetingLink.trim() || null,
      notes: notes.trim() || null
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/40 p-4 backdrop-blur-sm">
      <form onSubmit={submit} className="w-full max-w-2xl overflow-hidden rounded-2xl bg-white shadow-2xl">
        <div className="flex items-center justify-between border-b border-gray-100 bg-gradient-to-r from-purple-50 to-indigo-50 px-6 py-4">
          <div>
            <h2 className="text-xl font-extrabold text-gray-900">Book a learning session</h2>
            <p className="text-sm text-gray-500">Pick a match, choose the skill, and set who teaches.</p>
          </div>
          <button type="button" onClick={onClose} className="rounded-full p-2 text-gray-500 hover:bg-white"><X size={18}/></button>
        </div>

        <div className="space-y-5 p-6">
          <label className="block text-sm font-semibold">Accepted match
            <div className="relative mt-1">
              <select className="input appearance-none pr-9" value={matchId} onChange={(e)=>setMatchId(e.target.value)} required>
                <option value="">Choose partner</option>
                {accepted.map((match) => {
                  const partner = match.requesterId === currentUser?.id ? match.targetName : match.requesterName;
                  return <option key={match.id} value={match.id}>{partner || 'Partner'} - {match.teachSkillName} / {match.learnSkillName}</option>;
                })}
              </select>
              <ChevronDown size={15} className="pointer-events-none absolute right-3 top-3 text-gray-400"/>
            </div>
          </label>

          {accepted.length === 0 && <div className="rounded-xl border border-yellow-200 bg-yellow-50 p-3 text-sm font-semibold text-yellow-700">No accepted matches yet. Accept a match request before booking a session.</div>}

          <label className="block text-sm font-semibold">Session skill
            <input
              className="input mt-1"
              list="booking-skill-options"
              placeholder="Type or select a skill"
              value={skillName}
              onChange={(e)=>setSkillName(e.target.value)}
              disabled={!selected}
            />
            <datalist id="booking-skill-options">
              {allSkills.map((s) => <option key={s.id} value={s.name} />)}
            </datalist>
            {selected && skillName.trim() && !matchedSkill && (
              <p className="mt-1 text-xs text-amber-600">Pick an existing skill from the list to continue.</p>
            )}
          </label>

          {roles && (
            <div className="rounded-2xl border border-purple-100 bg-purple-50 p-4">
              <div className="flex items-center justify-between">
                <p className="text-xs font-bold uppercase tracking-wide text-purple-700">Session roles</p>
                <button type="button" onClick={()=>setSwapped((s)=>!s)} className="inline-flex items-center gap-1 rounded-lg bg-white px-2 py-1 text-xs font-bold text-purple-700 ring-1 ring-purple-100 hover:bg-purple-100">
                  <ArrowLeftRight size={13}/> Swap teacher/learner
                </button>
              </div>
              <div className="mt-3 grid gap-3 md:grid-cols-3">
                <div><p className="text-xs text-gray-500">Skill</p><p className="font-bold text-gray-900">{matchedSkill?.name || skillName || '—'}</p></div>
                <div><p className="text-xs text-gray-500">Teacher</p><p className="font-bold text-green-700">{roles.teacher.name}</p></div>
                <div><p className="text-xs text-gray-500">Learner</p><p className="font-bold text-blue-700">{roles.learner.name}</p></div>
              </div>
            </div>
          )}

          <div className="grid gap-4 md:grid-cols-2">
            <label className="text-sm font-semibold"><span className="mb-1 flex items-center gap-1"><Calendar size={15}/>Date and time</span>
              <input type="datetime-local" min={minDateTime} className="input" value={scheduledAt} onChange={(e)=>setScheduledAt(e.target.value)} required />
              {invalidPast && <p className="mt-1 text-xs text-red-500">Choose a future date/time.</p>}
            </label>
            <label className="text-sm font-semibold"><span className="mb-1 flex items-center gap-1"><Clock size={15}/>Duration</span>
              <select className="input" value={durationMinutes} onChange={(e)=>setDurationMinutes(e.target.value)}>
                <option value="30">30 minutes</option>
                <option value="45">45 minutes</option>
                <option value="60">60 minutes</option>
                <option value="90">90 minutes</option>
                <option value="120">120 minutes</option>
              </select>
            </label>
          </div>

          <label className="block text-sm font-semibold"><span className="mb-1 flex items-center gap-1"><Link2 size={15}/>Meeting link</span>
            <input type="url" className="input" placeholder="https://meet.google.com/abc-defg-hij" value={meetingLink} onChange={(e)=>setMeetingLink(e.target.value)} />
          </label>
          <label className="block text-sm font-semibold">Notes
            <textarea className="input mt-1" rows="3" maxLength="500" value={notes} onChange={(e)=>setNotes(e.target.value)} placeholder="Topics, goals, or prep notes..." />
          </label>

          <div className="flex justify-end gap-2">
            <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
            <button disabled={!canSubmit} className="btn-primary inline-flex items-center gap-2 disabled:opacity-50"><CheckCircle2 size={16}/>{isLoading ? 'Booking...' : 'Book Session'}</button>
          </div>
        </div>
      </form>
    </div>
  );
}
