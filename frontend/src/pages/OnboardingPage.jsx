import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { profileService } from '../services/profileService';
import { roadmapService } from '../services/roadmapService';
import {
  Compass, Sparkles, Brain, Check, ArrowRight, ArrowLeft,
  Clock, BookOpen, Sliders, CheckCircle2, AlertCircle
} from 'lucide-react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { Select } from '../components/ui/Select';
import { Badge } from '../components/ui/Badge';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';

export const OnboardingPage = () => {
  const [step, setStep] = useState(1);
  const [goalPrompt, setGoalPrompt] = useState(
    'I want to learn RAG and generative AI applications with Python. I am starting from the basics.'
  );
  const [targetRole, setTargetRole] = useState('Generative AI & RAG Engineer');
  const [careerGoal, setCareerGoal] = useState('');
  const [experienceLevel, setExperienceLevel] = useState('BEGINNER');
  const [weeklyHours, setWeeklyHours] = useState(10);
  const [preferredStyle, setPreferredStyle] = useState('PRACTICAL');

  const [skills, setSkills] = useState([
    { skillName: 'Python Programming', proficiencyLevel: 30 },
    { skillName: 'Prompt Engineering & LLM APIs', proficiencyLevel: 25 },
    { skillName: 'Vector Databases & Embeddings', proficiencyLevel: 10 },
    { skillName: 'RAG Architecture & LangChain', proficiencyLevel: 0 },
    { skillName: 'NumPy & Pandas', proficiencyLevel: 15 },
    { skillName: 'Deep Learning & PyTorch', proficiencyLevel: 0 },
    { skillName: 'Model Deployment & FastAPI', proficiencyLevel: 0 },
  ]);

  const [extracting, setExtracting] = useState(false);
  const [extractedData, setExtractedData] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const { refreshProfile } = useAuth();
  const navigate = useNavigate();

  const handleExtractGoal = async (overridePrompt) => {
    const promptToUse = overridePrompt || goalPrompt;
    if (!promptToUse.trim()) return;
    setExtracting(true);
    setError('');

    try {
      const result = await profileService.extractGoal(promptToUse);
      setExtractedData(result);
      if (result.targetRole) setTargetRole(result.targetRole);
      if (result.experienceLevel) setExperienceLevel(result.experienceLevel);
      setCareerGoal(promptToUse);

      // Dynamically construct full domain skill matrix for this target role
      const isBeginner = result.experienceLevel === 'BEGINNER';
      const isAdvanced = result.experienceLevel === 'ADVANCED';
      const baseKnown = isAdvanced ? 85 : isBeginner ? 30 : 70;
      const baseMissing = isAdvanced ? 35 : isBeginner ? 10 : 20;

      const dynamicSkills = [];
      const seen = new Set();

      (result.extractedSkills || []).forEach((sName) => {
        if (!seen.has(sName.toLowerCase())) {
          seen.add(sName.toLowerCase());
          dynamicSkills.push({ skillName: sName, proficiencyLevel: baseKnown });
        }
      });

      (result.missingSkills || []).forEach((sName) => {
        if (!seen.has(sName.toLowerCase())) {
          seen.add(sName.toLowerCase());
          dynamicSkills.push({ skillName: sName, proficiencyLevel: baseMissing });
        }
      });

      if (dynamicSkills.length > 0) {
        setSkills(dynamicSkills);
      }
    } catch (err) {
      console.error('Goal extraction error:', err);
    } finally {
      setExtracting(false);
    }
  };

  const handleSkillSliderChange = (index, value) => {
    const updated = [...skills];
    updated[index].proficiencyLevel = parseInt(value, 10);
    setSkills(updated);
  };

  const handleAddSkill = (skillName) => {
    if (skills.some((s) => s.skillName.toLowerCase() === skillName.toLowerCase())) return;
    setSkills([...skills, { skillName, proficiencyLevel: 50 }]);
  };

  const handleFinishOnboarding = async () => {
    setSubmitting(true);
    setError('');

    try {
      await profileService.updateProfile({
        targetRole,
        careerGoal: careerGoal || goalPrompt,
        experienceLevel,
        weeklyHours: parseInt(weeklyHours, 10),
        preferredStyle,
        preferredResourceTypes: 'COURSE,PROJECT,DOCUMENTATION',
        skills: skills.map((s) => ({
          skillName: s.skillName,
          proficiencyLevel: s.proficiencyLevel,
        })),
      });

      // Generate the initial personalized learning path
      await roadmapService.generateRoadmap();
      await refreshProfile();
      navigate('/app/dashboard');
    } catch (err) {
      setError(err.message || 'Failed to complete onboarding setup');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col items-center justify-center p-3.5 sm:p-8 selection:bg-blue-500 selection:text-white min-w-0 max-w-full overflow-hidden">
      {/* Background glow - bounded */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 sm:w-[500px] h-64 sm:h-[400px] bg-blue-500/10 blur-[130px] rounded-full pointer-events-none" />

      <div className="w-full max-w-3xl relative z-10 animate-fade-in">
        {/* Stepper Indicator */}
        <div className="flex items-center justify-between mb-6 sm:mb-8 px-1 sm:px-4">
          {[
            { num: 1, label: 'Goal' },
            { num: 2, label: 'Skills' },
            { num: 3, label: 'Pace & Style' },
          ].map((s) => (
            <div key={s.num} className="flex items-center gap-2 sm:gap-3">
              <div
                className={`w-8 h-8 sm:w-9 sm:h-9 rounded-xl flex items-center justify-center font-bold text-xs transition-all shrink-0 ${
                  step === s.num
                    ? 'bg-gradient-to-r from-blue-600 to-sky-500 text-white shadow-lg shadow-blue-500/25 ring-2 ring-blue-400/40'
                    : step > s.num
                    ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                    : 'bg-slate-900 text-slate-500 border border-slate-800'
                }`}
              >
                {step > s.num ? <Check className="w-4 h-4" /> : s.num}
              </div>
              <span className="text-xs font-semibold hidden sm:inline text-slate-300">
                {s.label}
              </span>
            </div>
          ))}
        </div>

        {error && (
          <div className="mb-6 p-4 bg-rose-500/10 border border-rose-500/20 rounded-xl flex items-center gap-3 text-xs text-rose-300">
            <AlertCircle className="w-4 h-4 text-rose-400 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Step 1: Natural Language Goal Extraction */}
        {step === 1 && (
          <Card className="glass-panel border-slate-800 p-4 sm:p-8 shadow-2xl">
            <div className="flex items-center gap-2.5 mb-2">
              <div className="w-8 h-8 rounded-lg bg-blue-500/10 text-sky-400 flex items-center justify-center border border-blue-500/20 shrink-0">
                <Brain className="w-4 h-4" />
              </div>
              <h2 className="text-lg sm:text-xl font-bold text-white">What is your learning goal?</h2>
            </div>
            <p className="text-xs text-slate-400 mb-6 leading-relaxed">
              Type your goal in plain English. Our AI will automatically extract your target career, timeline, existing proficiencies, and missing prerequisites.
            </p>

            <div className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-2">
                  Describe what you want to achieve:
                </label>
                <textarea
                  rows={4}
                  value={goalPrompt}
                  onChange={(e) => setGoalPrompt(e.target.value)}
                  placeholder="e.g. I want to become a backend Java developer in 6 months..."
                  className="w-full bg-slate-900/90 border border-slate-800 rounded-xl p-3.5 sm:p-4 text-xs sm:text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 leading-relaxed"
                />
              </div>

              <div className="flex flex-col-reverse sm:flex-row justify-between items-stretch sm:items-center gap-3 pt-2">
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => handleExtractGoal(goalPrompt)}
                  loading={extracting}
                  icon={Sparkles}
                  className="w-full sm:w-auto"
                >
                  Analyze with AI
                </Button>

                <Button
                  variant="primary"
                  size="md"
                  icon={ArrowRight}
                  onClick={async () => {
                    await handleExtractGoal(goalPrompt);
                    setStep(2);
                  }}
                  className="w-full sm:w-auto"
                >
                  Continue
                </Button>
              </div>
            </div>

            {/* Extracted Data Card */}
            {extractedData && (
              <div className="mt-6 p-4 sm:p-5 bg-slate-900/90 border border-blue-500/30 rounded-2xl animate-fade-in">
                <div className="flex items-center gap-2 mb-3">
                  <Sparkles className="w-4 h-4 text-sky-400" />
                  <h4 className="text-xs font-bold uppercase tracking-wider text-sky-300">
                    AI Goal Understanding
                  </h4>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4 text-xs mb-4">
                  <div>
                    <span className="text-slate-400 block mb-0.5">Target Role:</span>
                    <span className="font-semibold text-white break-words">{extractedData.targetRole}</span>
                  </div>
                  <div>
                    <span className="text-slate-400 block mb-0.5">Estimated Timeline:</span>
                    <span className="font-semibold text-white font-mono">{extractedData.estimatedMonths} Months</span>
                  </div>

                </div>

                <div className="space-y-2 text-xs">
                  <div>
                    <span className="text-slate-400 block mb-1.5">Existing Skills Identified:</span>
                    <div className="flex flex-wrap gap-1.5">
                      {extractedData.extractedSkills?.map((s, idx) => (
                        <Badge key={idx} variant="success" size="sm">
                          ✓ {s}
                        </Badge>
                      ))}
                    </div>
                  </div>

                  <div className="pt-2">
                    <span className="text-slate-400 block mb-1.5">Potential Skill Gaps to Fill:</span>
                    <div className="flex flex-wrap gap-1.5">
                      {extractedData.missingSkills?.map((s, idx) => (
                        <Badge key={idx} variant="warning" size="sm">
                          ⚡ {s}
                        </Badge>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            )}
          </Card>
        )}

        {/* Step 2: Skill Proficiencies */}
        {step === 2 && (
          <Card className="glass-panel border-slate-800 p-4 sm:p-8 shadow-2xl">
            <div className="flex items-center gap-2.5 mb-2">
              <div className="w-8 h-8 rounded-lg bg-blue-500/10 text-sky-400 flex items-center justify-center border border-blue-500/20 shrink-0">
                <Sliders className="w-4 h-4" />
              </div>
              <h2 className="text-lg sm:text-xl font-bold text-white">Fine-Tune Your Current Skills</h2>
            </div>
            <p className="text-xs text-slate-400 mb-6 leading-relaxed">
              Adjust your self-assessed proficiency. The recommendation engine will sequence prerequisites and fast-track topics you already know.
            </p>

            <div className="space-y-3 max-h-[380px] overflow-y-auto pr-1 sm:pr-2">
              {skills.map((skill, index) => (
                <div key={index} className="p-3.5 bg-slate-900/80 border border-slate-800 rounded-xl">
                  <div className="flex items-center justify-between text-xs font-semibold mb-2">
                    <span className="text-slate-200 break-words">{skill.skillName}</span>
                    <span className="text-sky-400 font-mono font-bold">{skill.proficiencyLevel}%</span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="100"
                    step="5"
                    value={skill.proficiencyLevel}
                    onChange={(e) => handleSkillSliderChange(index, e.target.value)}
                    className="w-full h-1.5 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-blue-500"
                  />
                </div>
              ))}
            </div>

            {/* Quick add popular skills */}
            <div className="mt-4 pt-4 border-t border-slate-800">
              <span className="text-[11px] text-slate-400 block mb-2">Add other skills:</span>
              <div className="flex flex-wrap gap-1.5">
                {['Docker & Containers', 'System Design & Microservices', 'React.js', 'Python Programming', 'Git & Version Control'].map((s) => (
                  <button
                    key={s}
                    type="button"
                    onClick={() => handleAddSkill(s)}
                    className="text-xs px-2.5 py-1 rounded-lg bg-slate-900 hover:bg-slate-800 border border-slate-800 text-slate-300 transition"
                  >
                    + {s}
                  </button>
                ))}
              </div>
            </div>

            <div className="flex flex-col-reverse sm:flex-row justify-between items-stretch sm:items-center gap-3 mt-6 pt-4 border-t border-slate-800">
              <Button variant="ghost" size="md" icon={ArrowLeft} onClick={() => setStep(1)} className="w-full sm:w-auto">
                Back
              </Button>
              <Button variant="primary" size="md" icon={ArrowRight} onClick={() => setStep(3)} className="w-full sm:w-auto">
                Next: Preferences
              </Button>
            </div>
          </Card>
        )}

        {/* Step 3: Pace & Learning Preferences */}
        {step === 3 && (
          <Card className="glass-panel border-slate-800 p-4 sm:p-8 shadow-2xl">
            <div className="flex items-center gap-2.5 mb-2">
              <div className="w-8 h-8 rounded-lg bg-blue-500/10 text-sky-400 flex items-center justify-center border border-blue-500/20 shrink-0">
                <Clock className="w-4 h-4" />
              </div>
              <h2 className="text-lg sm:text-xl font-bold text-white">Learning Availability & Style</h2>
            </div>
            <p className="text-xs text-slate-400 mb-6 leading-relaxed">
              Configure your study schedule and resource format preferences to personalize recommendation weights.
            </p>

            <div className="space-y-6">
              {/* Target Role & Experience */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input
                  label="Target Career Role"
                  id="targetRole"
                  value={targetRole}
                  onChange={(e) => setTargetRole(e.target.value)}
                  required
                />

                <Select
                  label="Current Experience Level"
                  id="experienceLevel"
                  value={experienceLevel}
                  onChange={(e) => setExperienceLevel(e.target.value)}
                  options={[
                    { value: 'BEGINNER', label: 'Beginner (0 - 1 years)' },
                    { value: 'INTERMEDIATE', label: 'Intermediate (1 - 3 years)' },
                    { value: 'ADVANCED', label: 'Advanced (3+ years)' },
                  ]}
                />
              </div>

              {/* Weekly Availability */}
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-2">
                  Weekly Available Study Time: <span className="text-sky-400 font-bold font-mono">{weeklyHours} Hours / Week</span>
                </label>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 sm:gap-3">
                  {[5, 10, 15, 20].map((h) => (
                    <button
                      key={h}
                      type="button"
                      onClick={() => setWeeklyHours(h)}
                      className={`p-3 rounded-xl border text-center transition ${
                        weeklyHours === h
                          ? 'bg-blue-600/20 border-blue-500 text-sky-300 font-bold'
                          : 'bg-slate-900 border-slate-800 text-slate-400 hover:border-slate-700'
                      }`}
                    >
                      <div className="text-sm font-semibold">{h} hrs</div>
                      <div className="text-[10px] text-slate-500">
                        {h === 5 ? 'Casual' : h === 10 ? 'Standard' : h === 15 ? 'Intensive' : 'Full-Time'}
                      </div>
                    </button>
                  ))}
                </div>
              </div>

              {/* Preferred Style */}
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-2">
                  Preferred Learning Modality:
                </label>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5 sm:gap-3">
                  {[
                    { id: 'PRACTICAL', label: 'Practical Projects', desc: 'Code-first & hands-on builds' },
                    { id: 'VIDEO', label: 'Video Tutorials', desc: 'Visual walkthroughs & screencasts' },
                    { id: 'READING', label: 'Documentation & Books', desc: 'In-depth architecture references' },
                  ].map((style) => (
                    <button
                      key={style.id}
                      type="button"
                      onClick={() => setPreferredStyle(style.id)}
                      className={`p-3.5 rounded-xl border text-left transition ${
                        preferredStyle === style.id
                          ? 'bg-blue-600/20 border-blue-500 text-sky-300'
                          : 'bg-slate-900 border-slate-800 text-slate-400 hover:border-slate-700'
                      }`}
                    >
                      <div className="text-xs font-bold text-white mb-1">{style.label}</div>
                      <div className="text-[10px] text-slate-500 leading-snug">{style.desc}</div>
                    </button>
                  ))}
                </div>
              </div>
            </div>

            <div className="flex flex-col-reverse sm:flex-row justify-between items-stretch sm:items-center gap-3 mt-8 pt-4 border-t border-slate-800">
              <Button variant="ghost" size="md" icon={ArrowLeft} onClick={() => setStep(2)} className="w-full sm:w-auto">
                Back
              </Button>
              <Button
                variant="primary"
                size="lg"
                loading={submitting}
                icon={Sparkles}
                onClick={handleFinishOnboarding}
                className="w-full sm:w-auto"
              >
                Generate My Roadmap
              </Button>
            </div>
          </Card>
        )}
      </div>
    </div>
  );
};

