import React, { useState, useEffect } from 'react';
import { assessmentService } from '../services/assessmentService';
import {
  TrendingUp, Award, CheckCircle2, AlertCircle, Clock,
  Sparkles, ArrowRight, RefreshCw, Layers, ShieldCheck, ChevronRight
} from 'lucide-react';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';

export const AssessmentsPage = () => {
  const [availableQuizzes, setAvailableQuizzes] = useState([]);
  const [assessmentId, setAssessmentId] = useState(null);
  const [assessment, setAssessment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedAnswers, setSelectedAnswers] = useState({});
  const [result, setResult] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [timeLeft, setTimeLeft] = useState(300); // 5 min countdown

  useEffect(() => {
    const loadQuizList = async () => {
      try {
        const quizzes = await assessmentService.getAllAssessments();
        if (quizzes && quizzes.length > 0) {
          setAvailableQuizzes(quizzes);
          setAssessmentId(quizzes[0].id);
        } else {
          setAvailableQuizzes([]);
          setLoading(false);
        }
      } catch (err) {
        console.error("Failed to load assessments:", err);
        setLoading(false);
      }
    };
    loadQuizList();
  }, []);

  const fetchAssessment = async (id) => {
    if (!id) return;
    setLoading(true);
    setResult(null);
    setSelectedAnswers({});
    setTimeLeft(300);
    try {
      const data = await assessmentService.getAssessment(id);
      setAssessment(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (assessmentId) {
      fetchAssessment(assessmentId);
    }
  }, [assessmentId]);

  useEffect(() => {
    if (result || loading || !assessment) return;
    const timer = setInterval(() => {
      setTimeLeft((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => clearInterval(timer);
  }, [result, loading, assessment]);

  const handleSelectOption = (questionId, optionIndex) => {
    if (result) return;
    setSelectedAnswers((prev) => ({ ...prev, [questionId]: optionIndex }));
  };

  const handleSubmit = async () => {
    if (!assessment) return;
    setSubmitting(true);
    try {
      const res = await assessmentService.submitAssessment(assessment.id, selectedAnswers);
      setResult(res);
    } catch (err) {
      console.error(err);
    } finally {
      setSubmitting(false);
    }
  };

  const formatTime = (secs) => {
    const mins = Math.floor(secs / 60);
    const s = secs % 60;
    return `${mins}:${s < 10 ? '0' : ''}${s}`;
  };

  if (loading) {
    return <LoadingSpinner size="lg" message="Loading adaptive competency quiz..." className="h-96" />;
  }

  const answeredCount = Object.keys(selectedAnswers).length;
  const totalQuestions = assessment?.questions?.length || 0;
  const progressPct = totalQuestions > 0 ? Math.round((answeredCount / totalQuestions) * 100) : 0;

  return (
    <div className="max-w-4xl mx-auto space-y-6 sm:space-y-8 animate-fade-in pb-16 min-w-0 max-w-full">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 sm:gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl lg:text-3xl font-extrabold text-white tracking-tight flex items-center gap-2.5 flex-wrap">
            <span>Adaptive Skill Checkpoints</span>
            <TrendingUp className="w-6 h-6 sm:w-7 sm:h-7 text-emerald-400 shrink-0" />
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1 leading-relaxed">
            Demonstrate subject mastery. Scoring &ge;90% triggers automatic fast-tracking of downstream prerequisite roadmap phases.
          </p>
        </div>

        {/* Quiz Selector Pills */}
        <div className="flex space-x-1.5 p-1 bg-slate-900 border border-slate-800 rounded-xl overflow-x-auto no-scrollbar max-w-full">
          {availableQuizzes.map((q) => (
            <button
              key={q.id}
              onClick={() => setAssessmentId(q.id)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition shrink-0 ${
                assessmentId === q.id
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              {q.skill}
            </button>
          ))}
        </div>
      </div>

      {assessment && (
        <Card className="glass-panel border-slate-800 p-4 sm:p-6 lg:p-8 shadow-2xl">
          {/* Quiz Header with Timer & Progress */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4 pb-4 border-b border-slate-800 mb-4 sm:mb-6">
            <div>
              <div className="flex items-center gap-2 mb-1 flex-wrap">
                <Badge variant="brand" size="sm">
                  {assessment.skillName || 'Skill Evaluation'}
                </Badge>
                <span className="text-xs text-slate-400">Pass Threshold: {assessment.passingScore}%</span>
              </div>
              <h2 className="text-base sm:text-lg font-bold text-white break-words">{assessment.title}</h2>
              <p className="text-xs text-slate-400 mt-0.5">{assessment.description}</p>
            </div>

            {!result && (
              <div className="flex items-center justify-between sm:justify-end gap-3 sm:gap-4 shrink-0 pt-2 sm:pt-0 border-t sm:border-0 border-slate-800">
                <div className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-900 border border-slate-800 rounded-xl font-mono text-xs font-bold text-amber-400">
                  <Clock className="w-3.5 h-3.5 text-amber-400" />
                  <span>{formatTime(timeLeft)}</span>
                </div>
                <div className="w-28 text-right text-[11px] text-slate-400">
                  <span>{answeredCount}/{totalQuestions} Answered</span>
                  <div className="w-full mt-1">
                    <ProgressBar value={progressPct} variant="brand" size="sm" />
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Results View */}
          {result ? (
            <div className="space-y-4 sm:space-y-6 animate-fade-in">
              <div
                className={`p-4 sm:p-6 rounded-2xl border ${
                  result.passed
                    ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
                    : 'bg-rose-500/10 border-rose-500/30 text-rose-300'
                }`}
              >
                <div className="flex items-center justify-between mb-2 gap-2 flex-wrap">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-5 h-5 sm:w-6 sm:h-6 text-emerald-400 shrink-0" />
                    <h3 className="text-base sm:text-lg font-bold text-white">
                      {result.passed ? '🎉 Checkpoint Assessment Passed!' : 'Remediation Review Recommended'}
                    </h3>
                  </div>
                  <span className="text-2xl sm:text-3xl font-black font-mono">
                    {result.scorePercentage}%
                  </span>
                </div>
                <p className="text-xs leading-relaxed mb-3">{result.feedbackSummary}</p>
                <div className="p-3 bg-slate-950/80 rounded-xl text-xs font-mono text-sky-300 flex items-center gap-2 border border-slate-800">
                  <Sparkles className="w-4 h-4 text-sky-400 shrink-0" />
                  <span className="break-words">Adaptive Action: {result.adaptiveActionTaken}</span>
                </div>
              </div>

              {/* Question By Question Explanations */}
              <div className="space-y-3 sm:space-y-4">
                <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                  Comprehensive Diagnostic Breakdown
                </h4>
                {result.reviewedQuestions?.map((q, idx) => (
                  <div key={idx} className="p-3.5 sm:p-4 bg-slate-900/90 border border-slate-800 rounded-xl space-y-2">
                    <p className="text-xs font-semibold text-white break-words">{idx + 1}. {q.questionText}</p>
                    <p className="text-xs text-slate-400">
                      Correct Answer: <strong className="text-emerald-400">{q.options[q.correctAnswerIndex]}</strong>
                    </p>
                    <div className="p-3 bg-slate-950/60 rounded-lg text-[11px] text-slate-300 border border-slate-800/80 leading-relaxed break-words">
                      💡 <strong>Explanation:</strong> {q.explanation}
                    </div>
                  </div>
                ))}
              </div>

              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pt-4 border-t border-slate-800">
                <Button variant="secondary" size="md" icon={RefreshCw} onClick={() => fetchAssessment(assessmentId)} className="w-full sm:w-auto">
                  Retake Checkpoint
                </Button>
                <a href="/app/roadmap" className="w-full sm:w-auto">
                  <Button variant="primary" size="md" icon={ChevronRight} className="w-full sm:w-auto">
                    Return to Roadmap
                  </Button>
                </a>
              </div>
            </div>
          ) : (
            /* Active Question Stream */
            <div className="space-y-4 sm:space-y-6">
              {assessment.questions?.map((q, qIdx) => (
                <div key={q.id} className="p-4 sm:p-5 bg-slate-900/70 border border-slate-800/90 rounded-2xl space-y-3">
                  <div className="flex items-center justify-between text-xs text-slate-400">
                    <span className="font-bold text-sky-400">Question {qIdx + 1} of {totalQuestions}</span>
                    <Badge variant="default" size="sm">Single Choice</Badge>
                  </div>

                  <h4 className="text-xs sm:text-sm font-semibold text-white leading-relaxed break-words">
                    {q.questionText}
                  </h4>

                  <div className="space-y-2 pt-1">
                    {q.options?.map((opt, oIdx) => {
                      const isSelected = selectedAnswers[q.id] === oIdx;
                      return (
                        <button
                          key={oIdx}
                          type="button"
                          onClick={() => handleSelectOption(q.id, oIdx)}
                          className={`w-full p-3 sm:p-3.5 rounded-xl border text-left text-xs transition flex items-start sm:items-center gap-2.5 sm:gap-3.5 ${
                            isSelected
                              ? 'bg-blue-600/20 border-blue-500 text-white font-semibold shadow-sm'
                              : 'bg-slate-950/60 border-slate-800 text-slate-300 hover:border-slate-700'
                          }`}
                        >
                          <span
                            className={`w-5 h-5 rounded-full border flex items-center justify-center text-[10px] shrink-0 font-bold mt-0.5 sm:mt-0 ${
                              isSelected ? 'border-blue-400 bg-blue-500 text-white' : 'border-slate-700 text-slate-500'
                            }`}
                          >
                            {String.fromCharCode(65 + oIdx)}
                          </span>
                          <span className="break-words leading-relaxed">{opt}</span>
                        </button>
                      );
                    })}
                  </div>
                </div>
              ))}

              <div className="pt-4 border-t border-slate-800 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <span className="text-xs text-slate-400">
                  {answeredCount === totalQuestions ? '✅ All questions answered' : `⚠️ ${totalQuestions - answeredCount} questions remaining`}
                </span>
                <Button
                  variant="primary"
                  size="lg"
                  loading={submitting}
                  icon={CheckCircle2}
                  onClick={handleSubmit}
                  disabled={answeredCount === 0}
                  className="w-full sm:w-auto"
                >
                  Submit for Evaluation
                </Button>
              </div>
            </div>
          )}
        </Card>
      )}
    </div>
  );
};

