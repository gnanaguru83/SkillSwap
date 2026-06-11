import { Link, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { format, subDays } from 'date-fns';
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import {
  Activity,
  ArrowRight,
  Award,
  Bell,
  BookOpen,
  Calendar,
  Clock,
  Star,
  TrendingUp,
  Users,
} from 'lucide-react';
import { pageContent } from '../../api/axios';
import { getMyBadges, getMySkills } from '../../api/user.api';
import { useAuth } from '../../hooks/useAuth';
import { useMatches } from '../../hooks/useMatches';
import { useSessions } from '../../hooks/useSessions';
import { useNotifications } from '../../hooks/useNotifications';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import SkillBadge from '../../components/common/SkillBadge';
import MatchCard from '../../components/match/MatchCard';
import SessionCard from '../../components/session/SessionCard';
import RatingStars from '../../components/common/RatingStars';

const asArray = (value) => (Array.isArray(value) ? value : pageContent(value));

export default function DashboardPage() {
  const navigate = useNavigate();
  const { currentUser } = useAuth();
  const skillsQuery = useQuery({ queryKey: ['user', 'skills'], queryFn: getMySkills });
  const badgesQuery = useQuery({ queryKey: ['user', 'badges'], queryFn: getMyBadges });
  const { suggestions, received, sent } = useMatches();
  const { upcoming, history } = useSessions();
  const { notifications, count } = useNotifications();

  const isLoading = skillsQuery.isLoading || suggestions.isLoading || upcoming.isLoading || history.isLoading;
  if (isLoading) return <LoadingSpinner />;

  const skillList = skillsQuery.data || [];
  const teachSkills = skillList.filter((skill) => skill.type === 'TEACH');
  const learnSkills = skillList.filter((skill) => skill.type === 'LEARN');
  const suggestionList = asArray(suggestions.data);
  const upcomingSessions = asArray(upcoming.data);
  const sessionHistory = asArray(history.data);
  const recentNotifications = asArray(notifications.data);
  const receivedRequests = asArray(received.data);
  const sentRequests = asArray(sent.data);
  const badges = badgesQuery.data || [];

  const completedSessions = sessionHistory.filter((session) => session.status === 'COMPLETED');
  const acceptedMatches = [...receivedRequests, ...sentRequests].filter((match) => match.status === 'ACCEPTED');
  const pendingRequests = receivedRequests.filter((match) => match.status === 'PENDING');
  const firstName = (currentUser?.fullName || 'there').split(' ')[0];
  const averageRating = Number(currentUser?.averageRating || currentUser?.rating || 0);
  const unreadNotifications = typeof count.data === 'number' ? count.data : recentNotifications.filter((item) => !item.read).length;

  const activityData = buildActivityData(sessionHistory);
  const skillData = [
    { name: 'Teach', value: teachSkills.length, color: '#22c55e' },
    { name: 'Learn', value: learnSkills.length, color: '#3b82f6' },
    { name: 'Matches', value: acceptedMatches.length, color: '#8b5cf6' },
  ];

  return (
    <div className="space-y-8">
      <section className="overflow-hidden rounded-[2rem] border border-purple-100 bg-gradient-to-br from-white via-purple-50 to-blue-50 p-6 shadow-sm md:p-8">
        <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="mb-3 inline-flex items-center gap-2 rounded-full bg-white px-3 py-1 text-sm font-bold text-purple-700 shadow-sm">
              <Activity size={15} /> Learning command center
            </p>
            <h1 className="font-display text-4xl font-extrabold text-gray-950 md:text-5xl">Welcome back, {firstName}!</h1>
            <p className="mt-3 max-w-2xl text-gray-600">
              Track your skills, match requests, sessions, and community momentum from one place.
            </p>
          </div>
          <div className="flex flex-wrap gap-3">
            <Link to="/discover" className="btn-primary inline-flex items-center gap-2">
              Find a partner <ArrowRight size={16} />
            </Link>
            <Link to="/profile" className="btn-secondary inline-flex items-center gap-2">
              Edit skills
            </Link>
          </div>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard icon={BookOpen} label="Total Skills Listed" value={skillList.length} hint={`${teachSkills.length} teach, ${learnSkills.length} learn`} to="/profile#skills-on-swap" />
        <StatCard icon={Calendar} label="Sessions Completed" value={completedSessions.length} hint={`${upcomingSessions.length} upcoming`} to="/sessions?tab=history" />
        <StatCard icon={Star} label="Average Rating" value={averageRating ? averageRating.toFixed(1) : 'New'} hint={<RatingStars rating={averageRating || 0} size={15} />} to="/profile" />
        <StatCard icon={Users} label="Pending Requests" value={pendingRequests.length} hint={`${acceptedMatches.length} accepted matches`} badge={pendingRequests.length > 0} to="/matches?tab=received" />
      </section>

      <section className="grid gap-6 xl:grid-cols-[1.45fr_.85fr]">
        <div className="space-y-6">
          <Panel title="Suggested Matches" link="/matches?tab=suggestions">
            {suggestionList.slice(0, 3).length ? (
              <div className="grid gap-4 lg:grid-cols-3">
                {suggestionList.slice(0, 3).map((match) => (
                  <MatchCard key={match.userId || match.id} match={match} onRequest={() => navigate('/matches?tab=suggestions')} />
                ))}
              </div>
            ) : (
              <EmptyState
                icon={Users}
                title="No match suggestions yet"
                description="Add teach and learn skills to your profile so the platform can find strong matches."
                actionLabel="Add skills"
                onAction={() => navigate('/profile')}
              />
            )}
          </Panel>

          <Panel title="Upcoming Sessions" link="/sessions?tab=upcoming">
            {upcomingSessions.slice(0, 2).length ? (
              <div className="space-y-4">
                {upcomingSessions.slice(0, 2).map((session) => (
                  <SessionCard key={session.id} session={session} currentUserId={currentUser?.id} />
                ))}
              </div>
            ) : (
              <EmptyState
                icon={Calendar}
                title="No upcoming sessions"
                description="Accept a match request, then book your first peer learning session."
                actionLabel="View matches"
                onAction={() => navigate('/matches?tab=received')}
              />
            )}
          </Panel>

          <Panel title="Learning Activity">
            <div className="card h-80">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={activityData} margin={{ top: 12, right: 12, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="activityFill" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#7c3aed" stopOpacity={0.35} />
                      <stop offset="95%" stopColor="#7c3aed" stopOpacity={0.02} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                  <XAxis dataKey="day" tickLine={false} axisLine={false} />
                  <YAxis allowDecimals={false} tickLine={false} axisLine={false} />
                  <Tooltip />
                  <Area type="monotone" dataKey="sessions" stroke="#7c3aed" strokeWidth={3} fill="url(#activityFill)" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </Panel>
        </div>

        <aside className="space-y-6">
          <Panel title="Recent Notifications" link="/dashboard">
            <div className="card space-y-3">
              {recentNotifications.slice(0, 5).length ? recentNotifications.slice(0, 5).map((notification) => (
                <button
                  key={notification.id}
                  onClick={() => navigate(notification.link || '/dashboard')}
                  className={`w-full rounded-xl border p-3 text-left transition hover:border-purple-200 hover:bg-purple-50 ${notification.read ? 'border-gray-100 bg-gray-50' : 'border-purple-100 bg-white shadow-sm'}`}
                >
                  <div className="flex gap-3">
                    <span className="mt-1 rounded-full bg-purple-100 p-2 text-purple-700"><Bell size={15} /></span>
                    <span className="min-w-0 flex-1">
                      <span className="block truncate text-sm font-bold text-gray-900">{notification.title || 'Notification'}</span>
                      <span className="mt-1 block text-sm text-gray-500">{notification.message}</span>
                    </span>
                  </div>
                </button>
              )) : (
                <p className="py-8 text-center text-sm text-gray-500">No notifications yet.</p>
              )}
            </div>
          </Panel>

          <Panel title="My Skills">
            <div className="card space-y-5">
              <SkillGroup title="Skills I Teach" skills={teachSkills} type="TEACH" />
              <SkillGroup title="Skills I Want to Learn" skills={learnSkills} type="LEARN" />
              <Link to="/profile" className="btn-secondary inline-flex w-full items-center justify-center gap-2">
                Manage skills <ArrowRight size={15} />
              </Link>
            </div>
          </Panel>

          <Panel title="Portfolio Snapshot">
            <div className="card space-y-5">
              <div className="h-44">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={skillData} margin={{ top: 10, right: 4, left: -20, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                    <XAxis dataKey="name" tickLine={false} axisLine={false} />
                    <YAxis allowDecimals={false} tickLine={false} axisLine={false} />
                    <Tooltip />
                    <Bar dataKey="value" radius={[10, 10, 0, 0]}>
                      {skillData.map((entry) => <Cell key={entry.name} fill={entry.color} />)}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
              <div className="grid grid-cols-2 gap-3 text-sm">
                <MiniMetric icon={Award} label="Badges" value={badges.length} />
                <MiniMetric icon={Clock} label="Unread" value={unreadNotifications} />
              </div>
              {badges.slice(0, 3).length ? (
                <div className="flex flex-wrap gap-2">
                  {badges.slice(0, 3).map((badge) => (
                    <span key={badge.id || badge.name} className="inline-flex items-center gap-1 rounded-full bg-amber-50 px-3 py-1.5 text-xs font-bold text-amber-700 ring-1 ring-amber-100">
                      <Award size={13} /> {badge.name}
                    </span>
                  ))}
                </div>
              ) : (
                <p className="rounded-xl bg-gray-50 p-3 text-sm text-gray-500">Complete sessions to start earning badges.</p>
              )}
            </div>
          </Panel>
        </aside>
      </section>
    </div>
  );
}

function StatCard({ icon: Icon, label, value, hint, badge, to }) {
  const content = (
    <>
      {badge && <span className="absolute right-4 top-4 h-3 w-3 rounded-full bg-red-500 ring-4 ring-red-100" />}
      <div className="mb-4 inline-flex rounded-2xl bg-purple-50 p-3 text-purple-700">
        <Icon size={24} />
      </div>
      <p className="text-sm font-semibold text-gray-500">{label}</p>
      <div className="mt-2 text-3xl font-extrabold text-gray-950">{value}</div>
      <div className="mt-2 text-sm text-gray-500">{hint}</div>
    </>
  );
  if (to) {
    return (
      <Link to={to} className="card relative block overflow-hidden transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lg">
        {content}
      </Link>
    );
  }
  return <div className="card relative overflow-hidden">{content}</div>;
}

function Panel({ title, link, children }) {
  return (
    <section>
      <div className="mb-3 flex items-center justify-between">
        <h2 className="flex items-center gap-2 text-xl font-extrabold text-gray-950">
          <TrendingUp size={20} className="text-purple-600" /> {title}
        </h2>
        {link && <Link className="text-sm font-bold text-purple-600 hover:text-purple-700" to={link}>View All</Link>}
      </div>
      {children}
    </section>
  );
}

function SkillGroup({ title, skills, type }) {
  return (
    <div>
      <div className="mb-2 flex items-center justify-between">
        <h3 className="text-sm font-extrabold text-gray-700">{title}</h3>
        <span className="text-xs font-bold text-gray-400">{skills.length}</span>
      </div>
      {skills.length ? (
        <div className="flex flex-wrap gap-2">
          {skills.slice(0, 6).map((skill) => (
            <SkillBadge key={`${type}-${skill.id}`} name={skill.name} type={type} size="sm" />
          ))}
        </div>
      ) : (
        <p className="rounded-xl bg-gray-50 p-3 text-sm text-gray-500">No skills added yet.</p>
      )}
    </div>
  );
}

function MiniMetric({ icon: Icon, label, value }) {
  return (
    <div className="rounded-xl bg-gray-50 p-3">
      <Icon size={16} className="mb-2 text-purple-600" />
      <p className="text-xs font-semibold text-gray-500">{label}</p>
      <p className="text-lg font-extrabold text-gray-900">{value}</p>
    </div>
  );
}

function buildActivityData(sessions) {
  const lastSevenDays = Array.from({ length: 7 }, (_, index) => subDays(new Date(), 6 - index));
  return lastSevenDays.map((day) => {
    const key = format(day, 'yyyy-MM-dd');
    const sessionsOnDay = sessions.filter((session) => {
      if (!session.scheduledAt) return false;
      return format(new Date(session.scheduledAt), 'yyyy-MM-dd') === key;
    }).length;
    return { day: format(day, 'EEE'), sessions: sessionsOnDay };
  });
}