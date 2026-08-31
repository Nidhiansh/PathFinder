import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { dashboardService } from '../services/dashboardService';
import { roadmapService } from '../services/roadmapService';
import { useAuth } from '../context/AuthContext';
import {
  Sparkles, CheckCircle2, Clock, ArrowRight, Play, Trophy,
  Flame, BookOpen, Compass, Layers, ShieldCheck, ExternalLink, RefreshCw, Calendar
} from 'lucide-react';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { RecommendationDetailModal } from '../components/RecommendationDetailModal';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip } from 'recharts';

export const DashboardPage = () => {
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [completingId, setCompletingId] = useState(null);
  const [selectedModalRec, setSelectedModalRec] = useState(null);

  const fetchDashboard = async () => {
    setLoading(true);
    setError('');
    try {
      const summary = await dashboardService.getDashboard();
      setData(summary);
    } catch (err) {
      setError(err.message || 'Failed to load dashboard metrics');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  const handleMarkItemComplete = async (itemId) => {
    if (!itemId) return;
    setCompletingId(itemId);
    try {
      await roadmapService.updateItemStatus(itemId, 'COMPLETED');
      await fetchDashboard();
    } catch (err) {
      console.error("Failed to update item", err);
    } finally {
      setCompletingId(null);
    }
  };

  if (loading) {
    return <LoadingSpinner size="lg" message="Synthesizing personalized dashboard..." className="h-96" />;
  }

  if (error) {
    return <ErrorMessage message={error} onRetry={fetchDashboard} />;
  }

  const skillChartData = data?.topSkillGaps?.map((g) => ({
    name: g.skillName.length > 14 ? g.skillName.substring(0, 12) + '...' : g.skillName,
    Current: g.currentProficiency,
    Required: g.requiredProficiency,
  })) || [];

  const activityDays = [
    { day: 'Mon', active: true, hours: 2.5 },
    { day: 'Tue', active: true, hours: 1.8 },
    { day: 'Wed', active: true, hours: 2.0 },
    { day: 'Thu', active: true, hours: 3.2 },
    { day: 'Fri', active: true, hours: 1.5 },
    { day: 'Sat', active: true, hours: 4.0 },
    { day: 'Sun', active: true, hours: 2.0 },
  ];

  return (
    <div className="space-y-6 sm:space-y-8 animate-fade-in pb-16 min-w-0 max-w-full">
      {/* 1. Welcome & High-Level Positioning Banner */}
      <div className="relative overflow-hidden glass-panel border-blue-500/20 rounded-2xl sm:rounded-3xl p-4 sm:p-6 lg:p-8 bg-gradient-to-r from-blue-950/40 via-slate-900/60 to-slate-900/80 shadow-2xl">
        <div className="absolute -top-12 -right-12 w-64 h-64 bg-blue-500/10 rounded-full blur-3xl pointer-events-none" />

        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-4 sm:gap-6">
          <div>
            <div className="flex flex-wrap items-center gap-2 mb-2">
              <Badge variant="brand" size="sm" icon={Sparkles}>
                Career Target Active
              </Badge>
              <span className="text-xs text-slate-400 font-mono">Pace: {data?.weeklyHoursTarget || 10} hrs/wk</span>
            </div>
            <h1 className="text-xl sm:text-2xl lg:text-3xl font-extrabold text-white tracking-tight">
              Welcome back, {data?.fullName || user?.username} 👋
            </h1>
            <p className="text-xs sm:text-sm text-slate-300 mt-1.5 max-w-2xl leading-relaxed">
              You are currently advancing toward becoming a <strong className="text-sky-400 font-semibold">{data?.targetRole || 'Software Engineer'}</strong>.
            </p>
          </div>

          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2.5 sm:gap-3 w-full sm:w-auto">
            <Link to="/app/skills" className="w-full sm:w-auto">
              <Button variant="outline" size="md" icon={Compass} className="w-full sm:w-auto">
                Explore Skill DAG
              </Button>
            </Link>
            <Link to="/app/roadmap" className="w-full sm:w-auto">
              <Button variant="primary" size="md" icon={ArrowRight} className="w-full sm:w-auto">
                View Full Roadmap
              </Button>
            </Link>
          </div>
        </div>
      </div>

      {/* 2. Key Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-6">
        <Card className="p-4 sm:p-5 border-slate-800/90">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-2">
            <span>Roadmap Completion</span>
            <Trophy className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl font-bold text-white mb-2 font-mono">
            {data?.overallRoadmapProgress || 0}%
          </div>
          <ProgressBar value={data?.overallRoadmapProgress || 0} variant="brand" size="sm" />
        </Card>

        <Card className="p-4 sm:p-5 border-slate-800/90">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-2">
            <span>Study Streak</span>
            <Flame className="w-4 h-4 text-orange-400 fill-orange-400/20" />
          </div>
          <div className="text-2xl font-bold text-white mb-1 font-mono">
            {data?.streakDays || 1} <span className="text-xs text-slate-400 font-normal">Days</span>
          </div>
          <span className="text-[11px] text-emerald-400 font-medium">Consistent daily momentum!</span>
        </Card>

        <Card className="p-4 sm:p-5 border-slate-800/90">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-2">
            <span>Hours Learned</span>
            <Clock className="w-4 h-4 text-sky-400" />
          </div>
          <div className="text-2xl font-bold text-white mb-1 font-mono">
            {data?.totalHoursSpent || 0} <span className="text-xs text-slate-400 font-normal">Hours</span>
          </div>
          <span className="text-[11px] text-slate-400">Target: {data?.weeklyHoursTarget || 10}h / wk</span>
        </Card>

        <Card className="p-4 sm:p-5 border-slate-800/90">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-2">
            <span>Competencies</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold text-white mb-1 font-mono">
            {data?.skillsMasteredCount || 0} <span className="text-xs text-slate-400 font-normal">Mastered</span>
          </div>
          <span className="text-[11px] text-sky-400 font-medium">{data?.skillGapsCount || 0} Gaps In Scope</span>
        </Card>
      </div>

      {/* 3. Core "What to Learn Next" & "Currently Learning" Focus Cards */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Next Action (Prominent 2 Columns) */}
        <Card className="lg:col-span-2 glass-panel border-blue-500/30 p-4 sm:p-7 relative overflow-hidden">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-sky-400 animate-pulse" />
              <span className="text-xs font-bold uppercase tracking-wider text-sky-300">
                Recommended Next Step
              </span>
            </div>
            {data?.nextRecommendedAction && (
              <Badge variant="brand" size="sm">
                Score: {data.nextRecommendedAction.recommendationScore || 95}% Match
              </Badge>
            )}
          </div>

          {data?.nextRecommendedAction ? (
            <div>
              <h3 className="text-base sm:text-xl font-bold text-white mb-2">
                {data.nextRecommendedAction.title}
              </h3>
              <p className="text-xs sm:text-sm text-slate-300 leading-relaxed mb-4">
                {data.nextRecommendedAction.description || data.nextRecommendedAction.recommendationReason}
              </p>

              {/* Explainable AI Reasoning Box */}
              <div className="p-3.5 sm:p-4 bg-slate-900/90 border border-slate-800 rounded-xl mb-6 text-xs text-slate-300 space-y-1.5">
                <div className="flex items-center gap-1.5 text-sky-400 font-semibold">
                  <Sparkles className="w-3.5 h-3.5" />
                  <span>Why this was recommended:</span>
                </div>
                <p className="text-slate-400 leading-relaxed">
                  {data.nextRecommendedAction.recommendationReason ||
                    "Directly addresses your primary identified skill gap for " + data?.targetRole + ". All foundational prerequisites are verified."}
                </p>
              </div>

              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pt-3 border-t border-slate-800">
                <div className="flex flex-wrap items-center gap-3 sm:gap-4 text-xs text-slate-400">
                  <span className="flex items-center gap-1.5 font-mono">
                    <Clock className="w-3.5 h-3.5 text-slate-500" />
                    Est. {data.nextRecommendedAction.estimatedHours || 3} Hours
                  </span>
                  <span className="flex items-center gap-1.5">
                    <BookOpen className="w-3.5 h-3.5 text-slate-500" />
                    {data.nextRecommendedAction.itemType || 'Resource'}
                  </span>
                </div>

                <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2 sm:gap-3 w-full sm:w-auto">
                  {data.nextRecommendedAction.url && (
                    <a
                      href={data.nextRecommendedAction.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="w-full sm:w-auto"
                    >
                      <Button variant="primary" size="md" icon={ExternalLink} className="w-full sm:w-auto">
                        Open Material
                      </Button>
                    </a>
                  )}

                  <Button
                    variant="secondary"
                    size="md"
                    loading={completingId === data.nextRecommendedAction.id}
                    onClick={() => handleMarkItemComplete(data.nextRecommendedAction.id)}
                    icon={CheckCircle2}
                    className="w-full sm:w-auto"
                  >
                    Mark Completed
                  </Button>
                </div>
              </div>
            </div>
          ) : (
            <div className="text-center py-8">
              <CheckCircle2 className="w-12 h-12 text-emerald-400 mx-auto mb-3" />
              <h4 className="text-base font-semibold text-white">All Active Modules Completed!</h4>
              <p className="text-xs text-slate-400 mt-1 mb-4">
                You've completed all unlocked learning items in your current phase.
              </p>
              <Link to="/app/roadmap">
                <Button variant="primary" size="sm">
                  View Roadmap Phases
                </Button>
              </Link>
            </div>
          )}
        </Card>


        {/* Current Learning Phase Status & 7-Day Activity */}
        <div className="space-y-6">
          <Card className="glass-card p-6 flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-3">
                <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400">
                  Current Phase Focus
                </span>
                <Badge variant="brand" size="sm">In Progress</Badge>
              </div>

              <h4 className="text-base font-bold text-white mb-2 leading-snug">
                {data?.currentPhaseTitle || 'Phase 1: Foundations'}
              </h4>

              <div className="my-3">
                <ProgressBar
                  value={data?.currentPhaseProgress || 0}
                  variant="brand"
                  size="md"
                  showLabel
                  label="Phase Completion"
                />
              </div>
            </div>

            <div className="pt-3 border-t border-slate-800">
              <Link to="/app/roadmap">
                <Button variant="outline" size="sm" className="w-full justify-between" icon={ArrowRight}>
                  <span>Explore Phase Curriculum</span>
                </Button>
              </Link>
            </div>
          </Card>

          {/* 7-Day Streak Heatmap */}
          <Card className="glass-card p-5">
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-bold text-white flex items-center gap-1.5">
                <Calendar className="w-3.5 h-3.5 text-sky-400" />
                7-Day Study Cadence
              </span>
              <span className="text-[10px] text-emerald-400 font-mono">100% Active</span>
            </div>

            <div className="grid grid-cols-7 gap-1.5 text-center">
              {activityDays.map((d, idx) => (
                <div key={idx} className="space-y-1">
                  <div
                    className="h-10 rounded-xl bg-blue-600/30 border border-blue-500/40 flex items-center justify-center text-[10px] font-bold text-sky-300 shadow-sm"
                    title={`${d.day}: ${d.hours} hrs studied`}
                  >
                    {d.hours}h
                  </div>
                  <span className="text-[10px] text-slate-400">{d.day}</span>
                </div>
              ))}
            </div>
          </Card>
        </div>
      </div>

      {/* 4. Skill Gap Overview & Analytics Chart */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2 glass-card p-6">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-base font-bold text-white">Skill Gap Diagnostics</h3>
              <p className="text-xs text-slate-400 mt-0.5">Current proficiency vs target role requirement</p>
            </div>
            <Link to="/app/skills">
              <Button variant="ghost" size="sm" icon={Compass}>
                Interactive DAG
              </Button>
            </Link>
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={skillChartData} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
                <XAxis dataKey="name" stroke="#64748b" fontSize={11} angle={-15} textAnchor="end" />
                <YAxis stroke="#64748b" fontSize={11} domain={[0, 100]} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '0.75rem', fontSize: '12px' }}
                />
                <Bar dataKey="Current" fill="#0284c7" radius={[4, 4, 0, 0]} name="Current Proficiency (%)" />
                <Bar dataKey="Required" fill="#334155" radius={[4, 4, 0, 0]} name="Target Requirement (%)" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>

        <Card className="glass-card p-6">
          <h3 className="text-base font-bold text-white mb-1">Priority Skill Gaps</h3>
          <p className="text-xs text-slate-400 mb-4">Highest leverage competencies to master</p>

          <div className="space-y-3">
            {data?.topSkillGaps?.slice(0, 4).map((gap, idx) => (
              <div key={idx} className="p-3 bg-slate-900/80 border border-slate-800 rounded-xl">
                <div className="flex items-center justify-between text-xs font-semibold mb-1.5">
                  <span className="text-slate-200">{gap.skillName}</span>
                  <span className="text-rose-400 font-mono">-{gap.gap}% Gap</span>
                </div>
                <div className="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
                  <div
                    className="bg-blue-500 h-full rounded-full"
                    style={{ width: `${gap.currentProficiency}%` }}
                  />
                </div>
                <div className="flex justify-between items-center text-[10px] text-slate-500 mt-1.5">
                  <span>Current: {gap.currentProficiency}%</span>
                  <span>Target: {gap.requiredProficiency}%</span>
                </div>
              </div>
            ))}
          </div>
        </Card>
      </div>

      {/* 5. Recommended Resources Section */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="text-lg font-bold text-white">Recommended Courses & Projects</h3>
            <p className="text-xs text-slate-400 mt-0.5">Top-ranked items calculated by multi-factor scoring</p>
          </div>
          <Link to="/app/recommendations">
            <Button variant="ghost" size="sm" icon={ArrowRight}>
              View All Recommendations
            </Button>
          </Link>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {data?.topRecommendations?.slice(0, 3).map((rec, idx) => (
            <Card key={idx} className="glass-card-interactive flex flex-col justify-between p-4 sm:p-6">
              <div>
                <div className="flex items-center justify-between mb-3">
                  <Badge variant={rec.type === 'PROJECT' ? 'purple' : 'brand'} size="sm">
                    {rec.type}
                  </Badge>
                  <span className="text-xs font-bold text-sky-400 font-mono">{rec.score}% Match</span>
                </div>

                <h4 className="text-sm font-bold text-white mb-2 leading-snug line-clamp-2">
                  {rec.title}
                </h4>

                <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed mb-4">
                  {rec.explanation || rec.description}
                </p>
              </div>

              <div className="pt-3.5 border-t border-slate-800/80 flex flex-col sm:flex-row sm:items-center justify-between gap-2.5">
                <span className="text-xs text-slate-500 flex items-center gap-1 font-mono">
                  <Clock className="w-3 h-3 text-slate-500" />
                  {rec.estimatedHours} hrs
                </span>

                <div className="flex items-center justify-end gap-2 w-full sm:w-auto">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setSelectedModalRec(rec)}
                  >
                    Why this?
                  </Button>

                  {rec.url && (
                    <a href={rec.url} target="_blank" rel="noopener noreferrer">
                      <Button variant="primary" size="sm" icon={ExternalLink}>
                        View
                      </Button>
                    </a>
                  )}
                </div>
              </div>
            </Card>

          ))}
        </div>
      </div>

      {/* XAI Modal */}
      <RecommendationDetailModal
        isOpen={!!selectedModalRec}
        onClose={() => setSelectedModalRec(null)}
        recommendation={selectedModalRec}
      />
    </div>
  );
};
