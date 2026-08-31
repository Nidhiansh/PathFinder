import React, { useState, useEffect } from 'react';
import { skillService } from '../services/skillService';
import {
  Compass, Award, Sliders, CheckCircle2, AlertTriangle, RefreshCw, Plus, Search
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
  const [categoryFilter, setCategoryFilter] = useState('ALL');
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
    const matchesCategory = categoryFilter === 'ALL' || gap.category === categoryFilter;
    const matchesSearch = !searchQuery || gap.skillName.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  if (loading) {
    return <LoadingSpinner size="lg" message="Synthesizing skill matrix & dependency graph..." className="h-96" />;
  }

  if (error) {
    return <ErrorMessage message={error} onRetry={fetchGaps} />;
  }

  return (
    <div className="space-y-8 animate-fade-in pb-16">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight flex items-center gap-2.5">
          Skill Matrix & Prerequisite DAG <Compass className="w-7 h-7 text-sky-400" />
        </h1>
        <p className="text-xs sm:text-sm text-slate-400 mt-1">
          Interactive competency dependency visualizer. Adjust proficiency sliders to recalculate prerequisite locks and roadmap sequencing.
        </p>
      </div>

      {/* 1. Interactive Topological DAG Graph */}
      <SkillGraphVisualizer skills={gaps} />

      {/* 2. Diagnostic Skill Gap Analysis & Controls */}
      <div className="space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <Tabs
            tabs={[
              { id: 'ALL', label: 'All Skills', count: gaps.length },
              { id: 'LANGUAGE', label: 'Languages' },
              { id: 'FRAMEWORK', label: 'Frameworks' },
              { id: 'DATABASE', label: 'Databases' },
              { id: 'SYSTEM_DESIGN', label: 'Systems' },
            ]}
            activeTab={categoryFilter}
            onChange={setCategoryFilter}
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
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filtered.map((gap, idx) => (
            <Card key={idx} className="glass-card-interactive p-6 flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between mb-2">
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
                  <span className="text-[11px] font-mono text-slate-400">{gap.category}</span>
                </div>

                <h3 className="text-base font-bold text-white mb-3">{gap.skillName}</h3>

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
                      <AlertTriangle className="w-3.5 h-3.5" />
                      <span>Missing Prerequisites:</span>
                    </div>
                    <p className="text-[11px] text-amber-300/80">
                      {gap.unsatisfiedPrerequisites.join(', ')}
                    </p>
                  </div>
                )}
              </div>

              <div className="pt-4 border-t border-slate-800/80 flex items-center justify-between text-[11px] text-slate-400">
                <span>Gap: <strong className="text-rose-400 font-mono">-{gap.gap}%</strong></span>
                {updatingSkill === gap.skillName && (
                  <span className="text-sky-400 animate-pulse">Syncing...</span>
                )}
              </div>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
};
