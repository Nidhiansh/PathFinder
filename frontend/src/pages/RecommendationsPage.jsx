import React, { useState, useEffect } from 'react';
import { recommendationService } from '../services/recommendationService';
import {
  Sparkles, ThumbsUp, ThumbsDown, ExternalLink, Clock,
  Search, Filter, BookOpen, Layers, ShieldCheck, HelpCircle
} from 'lucide-react';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Input } from '../components/ui/Input';
import { Select } from '../components/ui/Select';
import { Tabs } from '../components/ui/Tabs';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { RecommendationDetailModal } from '../components/RecommendationDetailModal';

export const RecommendationsPage = () => {
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filterType, setFilterType] = useState('ALL');
  const [difficultyFilter, setDifficultyFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState('SCORE_DESC');
  const [selectedModalRec, setSelectedModalRec] = useState(null);
  const [feedbackSuccess, setFeedbackSuccess] = useState({});

  const fetchRecs = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await recommendationService.getRecommendations();
      setRecommendations(data || []);
    } catch (err) {
      setError(err.message || 'Failed to load recommendations');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRecs();
  }, []);

  const handleFeedback = async (id, rating) => {
    try {
      await recommendationService.submitFeedback(id, rating, rating === 5 ? 'Helpful' : 'Not relevant');
      setFeedbackSuccess((prev) => ({ ...prev, [id]: true }));
      setTimeout(() => {
        setFeedbackSuccess((prev) => ({ ...prev, [id]: false }));
      }, 3000);
    } catch (err) {
      console.error(err);
    }
  };

  const filtered = recommendations
    .filter((r) => {
      if (filterType === 'PROJECTS' && r.type !== 'PROJECT') return false;
      if (filterType === 'COURSES' && r.type !== 'COURSE') return false;
      if (filterType === 'DOCUMENTATION' && r.type !== 'DOCUMENTATION' && r.type !== 'BOOK') return false;
      if (difficultyFilter !== 'ALL' && r.difficulty !== difficultyFilter) return false;
      if (searchQuery && !r.title.toLowerCase().includes(searchQuery.toLowerCase()) && !r.description?.toLowerCase().includes(searchQuery.toLowerCase())) {
        return false;
      }
      return true;
    })
    .sort((a, b) => {
      if (sortBy === 'SCORE_DESC') return b.score - a.score;
      if (sortBy === 'HOURS_ASC') return a.estimatedHours - b.estimatedHours;
      if (sortBy === 'HOURS_DESC') return b.estimatedHours - a.estimatedHours;
      return 0;
    });

  if (loading) {
    return <LoadingSpinner size="lg" message="Computing multi-factor recommendation rankings..." className="h-96" />;
  }

  if (error) {
    return <ErrorMessage message={error} onRetry={fetchRecs} />;
  }

  return (
    <div className="space-y-8 animate-fade-in pb-16">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight flex items-center gap-2.5">
            AI Recommendation Hub <Sparkles className="w-7 h-7 text-sky-400" />
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Ranked educational resources and hands-on milestones mathematically scored by skill gaps, career goals, and prerequisites.
          </p>
        </div>

        <Tabs
          tabs={[
            { id: 'ALL', label: 'All Items', count: recommendations.length },
            { id: 'COURSES', label: 'Courses' },
            { id: 'PROJECTS', label: 'Projects' },
            { id: 'DOCUMENTATION', label: 'Docs & Books' },
          ]}
          activeTab={filterType}
          onChange={setFilterType}
        />
      </div>

      {/* Search & Filter Bar */}
      <div className="p-4 glass-panel rounded-2xl border-slate-800 flex flex-col sm:flex-row items-center gap-4">
        <div className="flex-1 w-full">
          <Input
            placeholder="Search by topic, keyword, framework (e.g. Spring, Docker)..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            icon={Search}
          />
        </div>

        <div className="flex items-center gap-3 w-full sm:w-auto">
          <div className="w-40">
            <Select
              value={difficultyFilter}
              onChange={(e) => setDifficultyFilter(e.target.value)}
              options={[
                { value: 'ALL', label: 'All Difficulties' },
                { value: 'BEGINNER', label: 'Beginner' },
                { value: 'INTERMEDIATE', label: 'Intermediate' },
                { value: 'ADVANCED', label: 'Advanced' },
              ]}
            />
          </div>

          <div className="w-44">
            <Select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              options={[
                { value: 'SCORE_DESC', label: 'Highest Match Score' },
                { value: 'HOURS_ASC', label: 'Shortest Duration' },
                { value: 'HOURS_DESC', label: 'Deepest Study' },
              ]}
            />
          </div>
        </div>
      </div>

      {/* Grid of Ranked Items */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {filtered.map((rec) => (
          <Card key={rec.id} className="glass-card-interactive p-6 flex flex-col justify-between">
            <div>
              {/* Header Badge & Match Score */}
              <div className="flex items-center justify-between mb-3">
                <Badge variant={rec.type === 'PROJECT' ? 'purple' : 'brand'} size="sm">
                  {rec.type}
                </Badge>

                <div className="flex items-center gap-2">
                  <div className="text-xs font-bold px-3 py-1 rounded-full bg-blue-500/10 border border-blue-500/30 text-sky-300 font-mono flex items-center gap-1.5">
                    <Sparkles className="w-3.5 h-3.5 text-sky-400" />
                    {rec.score}% Match
                  </div>
                </div>
              </div>

              <h3 className="text-base sm:text-lg font-bold text-white mb-2 leading-snug">
                {rec.title}
              </h3>

              <p className="text-xs text-slate-400 leading-relaxed mb-4 line-clamp-3">
                {rec.description}
              </p>

              {/* Explainable AI Reasoning Box */}
              <div className="p-3.5 bg-slate-900/90 border border-slate-800 rounded-xl mb-4 text-xs space-y-1.5">
                <div className="flex items-center gap-1.5 text-sky-400 font-semibold">
                  <Sparkles className="w-3.5 h-3.5" />
                  <span>Why Recommended:</span>
                </div>
                <p className="text-slate-300 leading-relaxed">
                  {rec.explanation}
                </p>
              </div>
            </div>

            {/* Card Footer: Metadata, Detailed Modal trigger, Feedback */}
            <div>
              <div className="pt-4 border-t border-slate-800/80 flex items-center justify-between">
                <div className="flex items-center gap-3 text-xs text-slate-400">
                  <span className="flex items-center gap-1">
                    <Clock className="w-3.5 h-3.5 text-slate-500" />
                    {rec.estimatedHours} hrs
                  </span>
                  <span className="text-slate-500">•</span>
                  <span>{rec.difficulty || 'Intermediate'}</span>
                </div>

                <div className="flex items-center gap-2">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setSelectedModalRec(rec)}
                    icon={HelpCircle}
                  >
                    Why this?
                  </Button>

                  <button
                    onClick={() => handleFeedback(rec.id, 5)}
                    className="p-1.5 text-slate-500 hover:text-emerald-400 rounded-lg hover:bg-slate-800 transition"
                    title="Helpful"
                  >
                    <ThumbsUp className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => handleFeedback(rec.id, 1)}
                    className="p-1.5 text-slate-500 hover:text-rose-400 rounded-lg hover:bg-slate-800 transition"
                    title="Not relevant"
                  >
                    <ThumbsDown className="w-3.5 h-3.5" />
                  </button>

                  {rec.url && (
                    <a href={rec.url} target="_blank" rel="noopener noreferrer">
                      <Button variant="primary" size="sm" icon={ExternalLink}>
                        Start
                      </Button>
                    </a>
                  )}
                </div>
              </div>

              {feedbackSuccess[rec.id] && (
                <p className="text-[10px] text-emerald-400 mt-2 text-center">
                  Feedback recorded! Model weights updated.
                </p>
              )}
            </div>
          </Card>
        ))}
      </div>

      {/* Explainable AI Detail Modal */}
      <RecommendationDetailModal
        isOpen={!!selectedModalRec}
        onClose={() => setSelectedModalRec(null)}
        recommendation={selectedModalRec}
        onFeedback={handleFeedback}
      />
    </div>
  );
};
