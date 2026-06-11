import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { MapPin, Search, UserPlus, Users, AlertCircle, Star, CalendarCheck } from 'lucide-react';
import { pageContent } from '../../api/axios';
import { getMySkills, getUserSkills, searchUsers } from '../../api/user.api';
import { sendRequest } from '../../api/matching.api';
import { useAuth } from '../../hooks/useAuth';
import UserAvatar from '../../components/common/UserAvatar';
import EmptyState from '../../components/common/EmptyState';
import LoadingSpinner from '../../components/common/LoadingSpinner';

function UserRow({ user, onRequest }) {
  const hasRating = user.ratingCount > 0;
  return (
    <article className="card flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div className="flex items-start gap-4">
        <Link to={`/profile/${user.id}`}><UserAvatar user={user} isOnline={user.online} /></Link>
        <div className="min-w-0">
          <Link to={`/profile/${user.id}`} className="block truncate text-lg font-bold text-gray-900 hover:text-purple-700 hover:underline">{user.fullName}</Link>
          {user.headline && (
            <p className="truncate text-sm font-semibold text-purple-700">{user.headline}</p>
          )}
          <p className="mt-0.5 flex items-center gap-1 text-sm text-gray-500">
            <MapPin size={14} />
            {user.location || 'Remote friendly'}
          </p>
          {user.bio && (
            <p className="mt-2 max-w-2xl text-sm text-gray-600">{user.bio}</p>
          )}
          <div className="mt-2 flex flex-wrap items-center gap-4 text-sm text-gray-500">
            <span className="flex items-center gap-1">
              <Star size={14} className="fill-yellow-400 text-yellow-400" />
              <span className="font-bold text-gray-700">
                {hasRating ? Number(user.averageRating).toFixed(1) : 'New'}
              </span>
              {hasRating ? <span>({user.ratingCount})</span> : null}
            </span>
            <span className="flex items-center gap-1">
              <CalendarCheck size={14} />
              {user.totalSessions || 0} {user.totalSessions === 1 ? 'session' : 'sessions'}
            </span>
          </div>
        </div>
      </div>

      <div className="flex shrink-0 gap-2">
        <button onClick={onRequest} className="btn-primary">
          Request Match
        </button>
        <Link to={`/profile/${user.id}`} className="btn-secondary text-center">
          View Profile
        </Link>
      </div>
    </article>
  );
}

function MatchRequestModal({ user, mySkills, onClose, onSend, loading }) {
  const myTeach = mySkills.filter((s) => s.type === 'TEACH');
  const myLearn = mySkills.filter((s) => s.type === 'LEARN');

  // Fetch the target user's skills so we can pre-fill from their side too
  const targetSkills = useQuery({
    queryKey: ['user', user.id, 'skills'],
    queryFn: () => getUserSkills(user.id),
    staleTime: 5 * 60 * 1000,
  });
  const targetTeach = targetSkills.data?.filter((s) => s.type === 'TEACH') || [];
  const targetLearn = targetSkills.data?.filter((s) => s.type === 'LEARN') || [];

  const [form, setForm] = useState({
    learnSkillId: myLearn[0]?.id || '',
    teachSkillId: myTeach[0]?.id || '',
    message: '',
  });

  useEffect(() => {
    if (targetTeach.length && !form.learnSkillId) {
      const match = targetTeach.find((ts) =>
        myLearn.some((ml) => ml.id === ts.id || ml.name === ts.name)
      );
      if (match) setForm((f) => ({ ...f, learnSkillId: match.id }));
    }
  }, [targetSkills.data]);

  const missingMyTeach = myTeach.length === 0;
  const missingMyLearn = myLearn.length === 0;
  const canSend = !missingMyTeach && !missingMyLearn && form.learnSkillId && form.teachSkillId;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/40 p-4">
      <div className="w-full max-w-xl rounded-2xl bg-white p-6 shadow-2xl">
        <h2 className="text-xl font-extrabold">
          Request a match with {user.fullName}
        </h2>
        <p className="mt-2 text-sm text-gray-500">
          Choose what you want to learn and what you can teach in return.
        </p>

        {targetSkills.data && (targetTeach.length > 0 || targetLearn.length > 0) && (
          <div className="mt-4 space-y-1 rounded-xl bg-gray-50 p-3 text-xs">
            <p className="font-bold text-gray-600">{user.fullName}'s skills:</p>
            {targetTeach.length > 0 && (
              <p className="text-green-700">
                Teaches: {targetTeach.map((s) => s.name).join(', ')}
              </p>
            )}
            {targetLearn.length > 0 && (
              <p className="text-blue-700">
                Wants to learn: {targetLearn.map((s) => s.name).join(', ')}
              </p>
            )}
          </div>
        )}

        <div className="mt-5 grid gap-4 md:grid-cols-2">
          <label className="text-sm font-bold">
            I want to learn
            <select
              className="input mt-2"
              value={form.learnSkillId}
              onChange={(e) => setForm({ ...form, learnSkillId: e.target.value })}
            >
              <option value="">Choose skill</option>
              {myLearn.map((s) => (
                <option key={s.id} value={s.id}>{s.name}</option>
              ))}
            </select>
          </label>
          <label className="text-sm font-bold">
            I can teach
            <select
              className="input mt-2"
              value={form.teachSkillId}
              onChange={(e) => setForm({ ...form, teachSkillId: e.target.value })}
            >
              <option value="">Choose skill</option>
              {myTeach.map((s) => (
                <option key={s.id} value={s.id}>{s.name}</option>
              ))}
            </select>
          </label>
        </div>

        {(missingMyTeach || missingMyLearn) && (
          <div className="mt-4 flex items-start gap-2 rounded-lg bg-yellow-50 p-3 text-sm font-semibold text-yellow-700">
            <AlertCircle size={16} className="mt-0.5 flex-shrink-0" />
            <span>
              You need at least one{' '}
              {missingMyTeach && missingMyLearn
                ? 'TEACH skill and one LEARN skill'
                : missingMyTeach
                ? 'TEACH skill'
                : 'LEARN skill'}{' '}
              on your profile before sending a request.{' '}
              <Link to="/profile" className="underline" onClick={onClose}>
                Add skills →
              </Link>
            </span>
          </div>
        )}

        <textarea
          className="input mt-4"
          maxLength="300"
          rows="3"
          placeholder="Optional message to introduce yourself..."
          value={form.message}
          onChange={(e) => setForm({ ...form, message: e.target.value })}
        />

        <div className="mt-5 flex justify-end gap-2">
          <button onClick={onClose} className="btn-secondary">Cancel</button>
          <button
            disabled={loading || !canSend}
            onClick={() => onSend({
              targetId: user.id,
              learnSkillId: form.learnSkillId,
              teachSkillId: form.teachSkillId,
              message: form.message,
            })}
            className="btn-primary inline-flex items-center gap-2 disabled:opacity-50"
          >
            <UserPlus size={16} />
            Send Request
          </button>
        </div>
      </div>
    </div>
  );
}

export default function DiscoverPage() {
  const { currentUser } = useAuth();
  const [query, setQuery]       = useState('');
  const [type, setType]         = useState('BOTH');
  const [location, setLocation] = useState('');
  const [debounced, setDebounced] = useState('');
  const [modal, setModal]       = useState(null);
  const qc = useQueryClient();

  const mine = useQuery({
    queryKey: ['user', 'skills'],
    queryFn: getMySkills,
  });

  useEffect(() => {
    const id = setTimeout(() => setDebounced(query), 400);
    return () => clearTimeout(id);
  }, [query]);

  const users = useQuery({
    queryKey: ['discover', debounced, type, location],
    queryFn: () => searchUsers({ skill: debounced, type, location, size: 30 }),
  });

  const req = useMutation({
    mutationFn: sendRequest,
    onSuccess: () => {
      toast.success('Request sent!');
      setModal(null);
      qc.invalidateQueries({ queryKey: ['matches'] });
    },
    onError: (e) => toast.error(e.response?.data?.message || 'Could not send request'),
  });

  const result = pageContent(users.data).filter((u) => u.id !== currentUser?.id);

  return (
    <div className="space-y-6">
      {/* Hero search bar */}
      <div className="rounded-3xl bg-gradient-to-r from-purple-700 to-indigo-600 p-8 text-white shadow-glow">
        <h1 className="font-display text-4xl font-extrabold">
          Discover your next learning partner
        </h1>
        <p className="mt-2 text-purple-100">
          Search by name, email, skill, learning direction, and location.
        </p>
        <div className="mt-6 grid gap-3 rounded-2xl bg-white p-3 text-gray-900 md:grid-cols-[1fr_auto_220px]">
          <div className="relative">
            <Search className="absolute left-3 top-2.5 text-gray-400" size={18} />
            <input
              className="input pl-10"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search people or skills..."
            />
          </div>
          <div className="flex rounded-lg bg-gray-100 p-1">
            {['BOTH', 'TEACH', 'LEARN'].map((next) => (
              <button
                key={next}
                onClick={() => setType(next)}
                className={`rounded-md px-3 py-2 text-sm font-bold
                  ${type === next ? 'bg-white text-purple-700 shadow-sm' : 'text-gray-500'}`}
              >
                {next}
              </button>
            ))}
          </div>
          <input
            className="input"
            placeholder="Location"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
          />
        </div>
      </div>

      {/* Results — simple vertical list */}
      {users.isLoading ? (
        <LoadingSpinner />
      ) : result.length ? (
        <>
          <p className="text-sm font-semibold text-gray-500">
            {result.length} {result.length === 1 ? 'person' : 'people'} found
          </p>
          <div className="space-y-4">
            {result.map((user) => (
              <UserRow key={user.id} user={user} onRequest={() => setModal(user)} />
            ))}
          </div>
        </>
      ) : (
        <EmptyState
          icon={Users}
          title="No users found"
          description="Try a different search, or check back once more people have joined."
        />
      )}

      {modal && (
        <MatchRequestModal
          user={modal}
          mySkills={mine.data || []}
          onClose={() => setModal(null)}
          onSend={req.mutate}
          loading={req.isPending}
        />
      )}
    </div>
  );
}
