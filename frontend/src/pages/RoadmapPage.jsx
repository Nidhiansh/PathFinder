import React, { useState, useEffect } from 'react';
import { roadmapService } from '../services/roadmapService';
import {
  Map, Lock, CheckCircle2, Play, BookOpen, Clock,
  Sparkles, Layers, Sliders, ExternalLink, RefreshCw, Filter, Search, ChevronRight
} from 'lucide-react';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Input } from '../components/ui/Input';
import { ProgressBar } from '../components/ui/ProgressBar';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { ErrorMessage } from '../components/ui/ErrorMessage';

export const RoadmapPage = () => {
  const [roadmap, setRoadmap] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [completingId, setCompletingId] = useState(null);
  const [generating, setGenerating] = useState(false);
  const [weeklyHours, setWeeklyHours] = useState(10);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [adaptiveToast, setAdaptiveToast] = useState(null);

  const fetchRoadmap = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await roadmapService.getRoadmap();
      setRoadmap(data);
    } catch (err) {
      setError(err.message || 'Failed to load learning roadmap');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRoadmap();
  }, []);

  const handleRegenerate = async () => {
    setGenerating(true);
    try {
      const data = await roadmapService.generateRoadmap();
      setRoadmap(data);
      setAdaptiveToast('Roadmap dynamically re-synthesized based on updated skill matrix!');
      setTimeout(() => setAdaptiveToast(null), 4000);
    } catch (err) {
      console.error(err);
    } finally {
      setGenerating(false);
    }
  };

  const handleStatusChange = async (itemId, currentStatus, itemTitle) => {
    setCompletingId(itemId);
    const newStatus = currentStatus === 'COMPLETED' ? 'AVAILABLE' : 'COMPLETED';
    try {
      const updated = await roadmapService.updateItemStatus(itemId, newStatus);
      setRoadmap(updated);
      if (newStatus === 'COMPLETED') {
        setAdaptiveToast(`Completed "${itemTitle}"! Progress updated to ${updated.overallProgressPercentage}%.`);
        setTimeout(() => setAdaptiveToast(null), 4000);
      }
    } catch (err) {
      console.error("Failed to update item", err);
    } finally {
      setCompletingId(null);
    }
  };

  const handleRecalculateHours = async (newHours) => {
    setWeeklyHours(newHours);
    try {
      const updated = await roadmapService.recalculateTimeline(newHours);
      setRoadmap(updated);
      setAdaptiveToast(`Timeline adapted for ${newHours} hrs/week study pace!`);
      setTimeout(() => setAdaptiveToast(null), 3000);
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) {
    return <LoadingSpinner size="lg" message="Synthesizing prerequisite DAG roadmap..." className="h-96" />;
  }

  if (error) {
    return <ErrorMessage message={error} onRetry={fetchRoadmap} />;
  }

  const filteredPhases = roadmap?.phases?.filter(phase => {
    if (statusFilter === 'AVAILABLE' && phase.status !== 'AVAILABLE') return false;
    if (statusFilter === 'LOCKED' && phase.status !== 'LOCKED') return false;
    if (statusFilter === 'COMPLETED' && phase.status !== 'COMPLETED') return false;
    return true;
  }) || [];

  return (
    <div className="space-y-8 animate-fade-in pb-16">
      {/* Toast Alert for Adaptive Updates */}
      {adaptiveToast && (
        <div className="fixed top-20 right-6 z-50 p-4 bg-blue-600/90 border border-blue-400 text-white rounded-2xl shadow-2xl flex items-center gap-3 animate-fade-in text-xs font-semibold">
          <Sparkles className="w-4 h-4 text-sky-200" />
          <span>{adaptiveToast}</span>
        </div>
      )}

      {/* Header & Dynamic Pace Controller */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6 glass-panel p-6 sm:p-8 rounded-3xl border-slate-800 shadow-2xl">
        <div>
          <div className="flex items-center gap-2 mb-2">
            <Badge variant="brand" size="sm" icon={Map}>
              Topological Prerequisite DAG
            </Badge>
            <span className="text-xs text-slate-400 font-mono">
              Total Curriculum: {roadmap?.totalEstimatedHours || 0} Hours
            </span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
            {roadmap?.title || 'Learning Roadmap'}
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1 max-w-2xl leading-relaxed">
            Prerequisite-locked phased curriculum. Completing checkpoint assessments automatically unlocks downstream architectural modules.
          </p>
        </div>

        {/* Dynamic Study Pace Controller Slider */}
        <div className="p-4 bg-slate-900/90 border border-slate-800 rounded-2xl flex flex-col gap-2.5 w-full lg:w-80">
          <div className="flex items-center justify-between text-xs font-semibold">
            <span className="text-slate-300 flex items-center gap-1.5">
              <Clock className="w-3.5 h-3.5 text-sky-400" />
              Study Pace Commitment
            </span>
            <span className="text-sky-400 font-bold font-mono">{weeklyHours} hrs / wk</span>
          </div>

          <input
            type="range"
            min="5"
            max="30"
            step="5"
            value={weeklyHours}
            onChange={(e) => handleRecalculateHours(parseInt(e.target.value, 10))}
            className="w-full h-1.5 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-blue-500"
          />

          <div className="flex items-center justify-between text-[10px] text-slate-500 font-mono">
            <span>5h (Casual)</span>
            <span>15h (Standard)</span>
            <span>30h (Intensive)</span>
          </div>
        </div>
      </div>

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card className="p-4 border-slate-800">
          <span className="text-xs text-slate-400">Path Completion</span>
          <div className="text-2xl font-bold text-white my-1">{roadmap?.overallProgressPercentage || 0}%</div>
          <ProgressBar value={roadmap?.overallProgressPercentage || 0} variant="brand" size="sm" />
        </Card>

        <Card className="p-4 border-slate-800">
          <span className="text-xs text-slate-400">Estimated Duration at {weeklyHours}h/wk</span>
          <div className="text-2xl font-bold text-sky-400 my-1 font-mono">
            {roadmap?.estimatedWeeks || 12} <span className="text-xs text-slate-400 font-normal">Weeks (~{Math.ceil((roadmap?.estimatedWeeks || 12) / 4.3)} Mos)</span>
          </div>
          <span className="text-[10px] text-slate-500">Recalculates dynamically with hours/week</span>
        </Card>

        <Card className="p-4 border-slate-800">
          <span className="text-xs text-slate-400">Completed Curriculum Units</span>
          <div className="text-2xl font-bold text-emerald-400 my-1 font-mono">
            {roadmap?.completedItems || 0} / {roadmap?.totalItems || 0}
          </div>
          <span className="text-[10px] text-slate-500">Courses, projects & diagnostic quizzes</span>
        </Card>
      </div>

      {/* Phase Status Filters */}
      <div className="flex items-center justify-between gap-4">
        <div className="flex space-x-1 p-1 bg-slate-900/80 border border-slate-800 rounded-xl">
          {[
            { id: 'ALL', label: 'All Phases' },
            { id: 'AVAILABLE', label: 'In Progress & Ready' },
            { id: 'LOCKED', label: 'Prerequisite Locked' },
            { id: 'COMPLETED', label: 'Completed' },
          ].map(f => (
            <button
              key={f.id}
              onClick={() => setStatusFilter(f.id)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                statusFilter === f.id ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'
              }`}
            >
              {f.label}
            </button>
          ))}
        </div>

        <Button variant="secondary" size="sm" icon={RefreshCw} loading={generating} onClick={handleRegenerate}>
          Regenerate Roadmap
        </Button>
      </div>

      {/* Phases Visual Timeline */}
      <div className="space-y-6">
        {filteredPhases.map((phase, pIdx) => {
          const isLocked = phase.status === 'LOCKED';
          const isCompleted = phase.status === 'COMPLETED';

          return (
            <Card
              key={phase.id || pIdx}
              className={`p-6 sm:p-8 border transition-all ${
                isLocked
                  ? 'border-slate-800/60 bg-slate-950/40 opacity-70'
                  : isCompleted
                  ? 'border-emerald-500/30 bg-slate-900/50'
                  : 'border-blue-500/40 bg-slate-900/90 shadow-xl'
              }`}
            >
              {/* Phase Header */}
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800/80 mb-6">
                <div className="flex items-center gap-3.5">
                  <div
                    className={`w-10 h-10 rounded-2xl flex items-center justify-center font-bold text-sm ${
                      isCompleted
                        ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                        : isLocked
                        ? 'bg-slate-800 text-slate-500 border border-slate-700'
                        : 'bg-blue-600 text-white shadow-lg shadow-blue-500/30 ring-2 ring-blue-400/30'
                    }`}
                  >
                    {isCompleted ? <CheckCircle2 className="w-6 h-6" /> : isLocked ? <Lock className="w-5 h-5" /> : phase.phaseNumber}
                  </div>
                  <div>
                    <h3 className="text-base sm:text-lg font-bold text-white flex items-center gap-2">
                      {phase.title}
                      <Badge
                        variant={isCompleted ? 'success' : isLocked ? 'default' : 'brand'}
                        size="sm"
                      >
                        {phase.status}
                      </Badge>
                    </h3>
                    <p className="text-xs text-slate-400 mt-0.5 max-w-xl leading-relaxed">
                      {phase.description}
                    </p>
                  </div>
                </div>

                <div className="text-right sm:shrink-0 text-xs text-slate-400">
                  <span className="font-semibold text-slate-200 font-mono">{phase.estimatedHours} hrs</span>
                  <div className="w-32 mt-1.5">
                    <ProgressBar value={phase.progressPercentage || 0} variant={isCompleted ? 'success' : 'brand'} size="sm" />
                  </div>
                </div>
              </div>

              {/* Phase Items */}
              <div className="space-y-3.5">
                {phase.items?.map((item) => {
                  const itemLocked = item.status === 'LOCKED';
                  const itemDone = item.status === 'COMPLETED';

                  return (
                    <div
                      key={item.id}
                      className={`p-4 sm:p-5 rounded-2xl border flex flex-col sm:flex-row sm:items-center justify-between gap-4 transition ${
                        itemDone
                          ? 'bg-slate-900/40 border-slate-800/80 text-slate-400'
                          : itemLocked
                          ? 'bg-slate-950/60 border-slate-800/60 text-slate-500'
                          : 'bg-slate-900/90 border-slate-700/90 hover:border-blue-500/40 text-slate-200 shadow-md'
                      }`}
                    >
                      <div className="flex items-start gap-4">
                        <button
                          disabled={itemLocked || completingId === item.id}
                          onClick={() => handleStatusChange(item.id, item.status, item.title)}
                          className={`mt-0.5 w-6 h-6 rounded-xl flex items-center justify-center border transition ${
                            itemDone
                              ? 'bg-emerald-500 border-emerald-500 text-white'
                              : itemLocked
                              ? 'border-slate-800 text-transparent cursor-not-allowed'
                              : 'border-slate-600 hover:border-sky-400 text-transparent hover:bg-sky-500/10'
                          }`}
                          title={itemDone ? "Mark as in progress" : "Mark as completed"}
                        >
                          <CheckCircle2 className="w-4 h-4" />
                        </button>

                        <div>
                          <div className="flex items-center gap-2 mb-1.5">
                            <Badge
                              variant={
                                item.itemType === 'PROJECT'
                                  ? 'purple'
                                  : item.itemType === 'ASSESSMENT'
                                  ? 'warning'
                                  : 'brand'
                              }
                              size="sm"
                            >
                              {item.itemType}
                            </Badge>
                            {item.recommendationScore && (
                              <span className="text-[11px] text-sky-400 font-mono font-bold">
                                {item.recommendationScore}% Match
                              </span>
                            )}
                          </div>

                          <h4 className={`text-sm font-semibold leading-snug ${itemDone ? 'line-through text-slate-500' : 'text-white'}`}>
                            {item.title}
                          </h4>

                          <p className="text-xs text-slate-400 mt-1 leading-relaxed max-w-2xl">
                            {item.recommendationReason || item.description}
                          </p>

                          {itemLocked && item.requiredPrerequisites?.length > 0 && (
                            <div className="flex items-center gap-1.5 text-[11px] text-amber-400 mt-2 font-medium">
                              <Lock className="w-3.5 h-3.5" />
                              <span>Required Prerequisites: {item.requiredPrerequisites.join(', ')}</span>
                            </div>
                          )}
                        </div>
                      </div>

                      <div className="flex items-center justify-between sm:justify-end gap-3 shrink-0 pt-3 sm:pt-0 border-t sm:border-0 border-slate-800">
                        <span className="text-xs text-slate-400 flex items-center gap-1.5 font-mono">
                          <Clock className="w-3.5 h-3.5 text-slate-500" />
                          {item.estimatedHours} hrs
                        </span>

                        {item.url && !itemLocked && (
                          <a href={item.url} target="_blank" rel="noopener noreferrer">
                            <Button variant="outline" size="sm" icon={ExternalLink}>
                              Open Material
                            </Button>
                          </a>
                        )}

                        {item.itemType === 'ASSESSMENT' && !itemLocked && (
                          <a href="/app/progress">
                            <Button variant="primary" size="sm" icon={ChevronRight}>
                              Take Quiz
                            </Button>
                          </a>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </Card>
          );
        })}
      </div>
    </div>
  );
};
