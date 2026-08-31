import React, { useState } from 'react';
import {
  Sparkles, CheckCircle2, AlertTriangle, Clock, BookOpen,
  ThumbsUp, ThumbsDown, ExternalLink, X, ShieldCheck, Award
} from 'lucide-react';
import { Modal } from './ui/Modal';
import { Badge } from './ui/Badge';
import { Button } from './ui/Button';
import { ProgressBar } from './ui/ProgressBar';

export const RecommendationDetailModal = ({ isOpen, onClose, recommendation, onFeedback }) => {
  const [feedbackSent, setFeedbackSent] = useState(false);

  if (!recommendation) return null;

  const factors = recommendation.matchFactors || {
    skillGapMatch: 0.85,
    goalMatch: 0.90,
    prerequisiteMatch: recommendation.isPrerequisitesMet ? 1.0 : 0.4,
    difficultyMatch: 0.85,
    styleMatch: 0.90,
    qualityMatch: 0.95
  };

  const factorList = [
    { label: 'Skill Gap Leverage', weight: '30%', val: Math.round((factors.skillGapMatch || 0.85) * 100), desc: 'Directly addresses your primary identified target role deficiencies.' },
    { label: 'Career Goal Alignment', weight: '25%', val: Math.round((factors.goalMatch || 0.90) * 100), desc: 'Semantic relevance to your target engineering specialization.' },
    { label: 'Prerequisite Readiness', weight: '15%', val: Math.round((factors.prerequisiteMatch || (recommendation.isPrerequisitesMet ? 1.0 : 0.4)) * 100), desc: 'Verifies whether you have mastered prior foundational topics.' },
    { label: 'Difficulty Fit', weight: '10%', val: Math.round((factors.difficultyMatch || 0.85) * 100), desc: 'Matches your current beginner / intermediate proficiency curve.' },
    { label: 'Learning Style Match', weight: '10%', val: Math.round((factors.styleMatch || 0.90) * 100), desc: 'Tailored for your practical / visual study modality preference.' },
    { label: 'Quality & Industry Score', weight: '10%', val: Math.round((factors.qualityMatch || 0.95) * 100), desc: 'Vetted curriculum based on community ratings and relevance.' }
  ];

  const handleFeedbackClick = (rating) => {
    if (onFeedback) {
      onFeedback(recommendation.id, rating);
      setFeedbackSent(true);
      setTimeout(() => setFeedbackSent(false), 3000);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Explainable AI Recommendation Breakdown" maxWidth="max-w-2xl">
      <div className="space-y-6 animate-fade-in">
        {/* Item Header */}
        <div className="p-4 bg-slate-950/70 border border-slate-800 rounded-2xl">
          <div className="flex items-center justify-between mb-2">
            <Badge variant={recommendation.type === 'PROJECT' ? 'purple' : 'brand'} size="sm">
              {recommendation.type}
            </Badge>
            <div className="flex items-center gap-1.5 px-3 py-1 bg-blue-500/10 border border-blue-500/30 rounded-full text-xs font-bold text-sky-400 font-mono">
              <Sparkles className="w-3.5 h-3.5" />
              {recommendation.score}% Match Score
            </div>
          </div>

          <h3 className="text-base sm:text-lg font-bold text-white mb-2 leading-snug">
            {recommendation.title}
          </h3>
          <p className="text-xs text-slate-400 leading-relaxed mb-3">
            {recommendation.description}
          </p>

          <div className="flex flex-wrap items-center gap-4 text-xs text-slate-400 pt-2 border-t border-slate-800">
            <span className="flex items-center gap-1.5">
              <Clock className="w-3.5 h-3.5 text-slate-500" />
              {recommendation.estimatedHours} Hours
            </span>
            <span className="flex items-center gap-1.5">
              <Award className="w-3.5 h-3.5 text-slate-500" />
              Difficulty: {recommendation.difficulty || 'Intermediate'}
            </span>
            <span className="flex items-center gap-1.5">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
              Prerequisites: {recommendation.isPrerequisitesMet ? 'Verified Ready' : 'Pending'}
            </span>
          </div>
        </div>

        {/* Explainable AI Reasoning */}
        <div className="p-4 bg-blue-950/20 border border-blue-500/30 rounded-2xl space-y-2">
          <h4 className="text-xs font-bold uppercase tracking-wider text-sky-400 flex items-center gap-1.5">
            <Sparkles className="w-4 h-4" />
            AI Pedagogical Rationale
          </h4>
          <p className="text-xs text-slate-200 leading-relaxed">
            {recommendation.explanation}
          </p>
        </div>

        {/* Multi-Factor Mathematical Breakdown */}
        <div>
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">
            Multi-Factor Mathematical Weights
          </h4>
          <div className="space-y-3">
            {factorList.map((f, idx) => (
              <div key={idx} className="p-3 bg-slate-900/80 border border-slate-800 rounded-xl space-y-1.5">
                <div className="flex items-center justify-between text-xs font-semibold">
                  <span className="text-slate-200">
                    {f.label} <span className="text-[10px] text-slate-500">({f.weight})</span>
                  </span>
                  <span className="text-sky-400 font-mono">{f.val}%</span>
                </div>
                <ProgressBar value={f.val} variant={f.val >= 80 ? 'brand' : 'warning'} size="sm" />
                <p className="text-[10px] text-slate-500">{f.desc}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Skills Taught */}
        {recommendation.skillsTaught?.length > 0 && (
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">
              Competencies Developed
            </h4>
            <div className="flex flex-wrap gap-1.5">
              {recommendation.skillsTaught.map((s, idx) => (
                <Badge key={idx} variant="brand" size="sm">
                  ✓ {s}
                </Badge>
              ))}
            </div>
          </div>
        )}

        {/* Modal Actions */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pt-4 border-t border-slate-800">
          <div className="flex items-center gap-2 text-xs text-slate-400">
            <span>Feedback:</span>
            <button
              onClick={() => handleFeedbackClick(5)}
              className="p-1.5 text-slate-400 hover:text-emerald-400 hover:bg-slate-800 rounded-lg transition"
              title="Helpful recommendation"
            >
              <ThumbsUp className="w-4 h-4" />
            </button>
            <button
              onClick={() => handleFeedbackClick(1)}
              className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-slate-800 rounded-lg transition"
              title="Not relevant"
            >
              <ThumbsDown className="w-4 h-4" />
            </button>
            {feedbackSent && <span className="text-emerald-400 text-[11px]">Saved!</span>}
          </div>

          <div className="flex items-center gap-3">
            <Button variant="outline" size="md" onClick={onClose}>
              Close
            </Button>
            {recommendation.url && (
              <a href={recommendation.url} target="_blank" rel="noopener noreferrer">
                <Button variant="primary" size="md" icon={ExternalLink}>
                  Start Learning Material
                </Button>
              </a>
            )}
          </div>
        </div>
      </div>
    </Modal>
  );
};
