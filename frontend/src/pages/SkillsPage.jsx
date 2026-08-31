import React, { useState, useEffect } from 'react';
import { skillService } from '../services/skillService';
import {
  Compass, Award, Sliders, CheckCircle2, AlertTriangle, RefreshCw, Plus, Search, Info, ShieldCheck, Sparkles
} from 'lucide-react';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Input } from '../components/ui/Input';
import { Tabs } from '../components/ui/Tabs';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { SkillGraphVisualizer } from '../components/SkillGraphVisualizer';

export const SkillsPage = () => {
  const [gaps, setGaps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [updatingSkill, setUpdatingSkill] = useState(null);

  const fetchGaps = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await skillService.getSkillGaps();
      setGaps(data || []);
    } catch (err) {
      setError(err.message || 'Failed to load skill gaps');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchGaps();
  }, []);

  const handleSliderChange = async (skillName, newProficiency) => {
    const val = parseInt(newProficiency, 10);
    // Optimistic UI update
    setGaps(prev => prev.map(g => g.skillName === skillName ? {
      ...g,
      currentProficiency: val,
      gap: Math.max(0, g.requiredProficiency - val),
      status: val >= g.requiredProficiency ? 'MASTERED' : val > 0 ? 'IN_PROGRESS' : 'MISSING'
    } : g));

    setUpdatingSkill(skillName);
    try {
      await skillService.updateProficiency(skillName, val);
    } catch (err) {
      console.error("Failed to update proficiency", err);
    } finally {
      setUpdatingSkill(null);
    }
  };

  const filtered = gaps.filter(gap => {
    const role = gap.skillRole || (gap.unsatisfiedPrerequisites?.length > 0 ? 'REQUIRED_PREREQUISITE' : 'DIRECT_CORE');
    const matchesRole = roleFilter === 'ALL' || role === roleFilter;
    const matchesSearch = !searchQuery || gap.skillName.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesRole && matchesSearch;
  });

  const coreCount = gaps.filter(g => (g.skillRole === 'DIRECT_CORE' || !g.skillRole)).length;
  const prereqCount = gaps.filter(g => g.skillRole === 'REQUIRED_PREREQUISITE').length;

  if (loading) {
    return <LoadingSpinner size="lg" message="Synthesizing knowledge ontology & prerequisite graph..." className="h-96" />;
  }

  if (error) {
    return <ErrorMessage message={error} onRetry={fetchGaps} />;
  }

  return (
    <div className="space-y-6 sm:space-y-8 animate-fade-in pb-16 min-w-0 max-w-full">
      {/* Header */}
      <div>
        <h1 className="text-xl sm:text-2xl lg:text-3xl font-extrabold text-white tracking-tight flex items-center gap-2 flex-wrap">
          <span>Knowledge Graph & Competency Matrix</span>
          <Compass className="w-6 h-6 sm:w-7 sm:h-7 text-sky-400 shrink-0" />
        </h1>
        <p className="text-xs sm:text-sm text-slate-400 mt-1 leading-relaxed">
          Knowledge-grounded prerequisite DAG. Skills are categorized into Direct Core objectives and verified Foundational Prerequisites.
        </p>
      </div>

      {/* 1. Interactive Topological DAG Graph */}
      <SkillGraphVisualizer skills={gaps} />

      {/* 2. Diagnostic Skill Gap Analysis & Controls */}
      <div className="space-y-4 sm:space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4">
          <Tabs
            tabs={[
              { id: 'ALL', label: 'All Required Skills', count: gaps.length },
              { id: 'DIRECT_CORE', label: 'Core Competencies', count: coreCount },
              { id: 'REQUIRED_PREREQUISITE', label: 'Foundational Prerequisites', count: prereqCount },
            ]}
            activeTab={roleFilter}
            onChange={setRoleFilter}
          />

          <div className="w-full sm:w-64">
            <Input
              placeholder="Search competencies..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              icon={Search}
            />
          </div>
        </div>

        {/* Skill Gap Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
          {filtered.map((gap, idx) => {
            const isPrereq = gap.skillRole === 'REQUIRED_PREREQUISITE';
            return (
              <Card key={idx} className="glass-card-interactive p-4 sm:p-6 flex flex-col justify-between">
                <div>
                  <div className="flex items-center justify-between mb-2 gap-1.5 flex-wrap">
                    <div className="flex items-center gap-1.5 flex-wrap">
                      <Badge
                        variant={
                          gap.status === 'MASTERED'
                            ? 'success'
                            : gap.status === 'IN_PROGRESS'
                            ? 'brand'
                            : 'warning'
                        }
                        size="sm"
                      >
                        {gap.status}
                      </Badge>
                      <Badge
                        variant={isPrereq ? 'warning' : 'outline'}
                        size="sm"
                        className="text-[10px]"
                      >
                        {isPrereq ? 'Prerequisite' : 'Core Objective'}
                      </Badge>
                    </div>
                    <span className="text-[10px] font-mono text-slate-400">{gap.source || 'ESCO'}</span>
                  </div>

                  <h3 className="text-sm sm:text-base font-bold text-white mb-1.5 break-words">{gap.skillName}</h3>

                  {gap.reason && (
                    <div className="flex items-start gap-1.5 mb-3 text-[11px] text-slate-400 bg-slate-800/40 p-2.5 rounded-lg border border-slate-700/50">
                      <Info className="w-3.5 h-3.5 text-sky-400 flex-shrink-0 mt-0.5" />
                      <span className="leading-relaxed break-words">{gap.reason}</span>
                    </div>
                  )}

                  <div className="space-y-2 mb-4">
                    <div className="flex justify-between text-xs font-semibold">
                      <span className="text-slate-300">Proficiency</span>
                      <span className="text-sky-400 font-mono">
                        {gap.currentProficiency}% / {gap.requiredProficiency}% Target
                      </span>
                    </div>

                    {/* Interactive Slider */}
                    <input
                      type="range"
                      min="0"
                      max="100"
                      step="5"
                      value={gap.currentProficiency}
                      onChange={(e) => handleSliderChange(gap.skillName, e.target.value)}
                      className="w-full h-1.5 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-blue-500"
                    />
                    <div className="flex justify-between text-[10px] text-slate-500 font-mono">
                      <span>0% (Beginner)</span>
                      <span>100% (Master)</span>
                    </div>
                  </div>

                  {gap.unsatisfiedPrerequisites?.length > 0 && (
                    <div className="p-3 bg-amber-500/10 border border-amber-500/20 rounded-xl text-xs text-amber-300">
                      <div className="font-semibold flex items-center gap-1 mb-1">
                        <AlertTriangle className="w-3.5 h-3.5 shrink-0" />
                        <span>Unsatisfied Prerequisites:</span>
                      </div>
                      <p className="text-[11px] text-amber-300/80 break-words">
                        {gap.unsatisfiedPrerequisites.join(', ')}
                      </p>
                    </div>
                  )}
                </div>

                <div className="pt-3.5 border-t border-slate-800/80 flex items-center justify-between text-[11px] text-slate-400">
                  <span>Gap: <strong className="text-rose-400 font-mono">-{gap.gap}%</strong></span>
                  {updatingSkill === gap.skillName ? (
                    <span className="text-sky-400 animate-pulse">Syncing...</span>
                  ) : (
                    <span className="text-slate-500 text-[10px]">Confidence: {Math.round((gap.confidence || 0.94) * 100)}%</span>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      </div>
    </div>

  );
};
