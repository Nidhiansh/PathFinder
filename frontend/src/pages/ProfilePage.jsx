import React, { useState, useEffect } from 'react';
import { profileService } from '../services/profileService';
import { skillService } from '../services/skillService';
import { useAuth } from '../context/AuthContext';
import {
  User, Mail, Target, Sliders, Clock, BookOpen, Save,
  CheckCircle2, AlertCircle, Plus, Trash2, Award
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Select } from '../components/ui/Select';
import { Badge } from '../components/ui/Badge';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { ErrorMessage } from '../components/ui/ErrorMessage';

export const ProfilePage = () => {
  const { user, refreshProfile } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const [error, setError] = useState('');

  // Editable Form Fields
  const [fullName, setFullName] = useState('');
  const [targetRole, setTargetRole] = useState('');
  const [careerGoal, setCareerGoal] = useState('');
  const [experienceLevel, setExperienceLevel] = useState('INTERMEDIATE');
  const [weeklyHours, setWeeklyHours] = useState(10);
  const [preferredStyle, setPreferredStyle] = useState('PRACTICAL');
  const [skills, setSkills] = useState([]);
  const [newSkillName, setNewSkillName] = useState('');

  const fetchProfileData = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await profileService.getProfile();
      setProfile(data);
      setFullName(data.fullName || '');
      setTargetRole(data.targetRole || '');
      setCareerGoal(data.careerGoal || '');
      setExperienceLevel(data.experienceLevel || 'INTERMEDIATE');
      setWeeklyHours(data.weeklyHours || 10);
      setPreferredStyle(data.preferredStyle || 'PRACTICAL');
      setSkills(data.skills || []);
    } catch (err) {
      setError(err.message || 'Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfileData();
  }, []);

  const handleSkillSlider = (index, value) => {
    const updated = [...skills];
    updated[index].proficiencyLevel = parseInt(value, 10);
    setSkills(updated);
  };

  const handleAddSkill = () => {
    if (!newSkillName.trim()) return;
    if (skills.some((s) => s.skillName.toLowerCase() === newSkillName.toLowerCase())) return;

    setSkills([
      ...skills,
      { skillId: Date.now(), skillName: newSkillName.trim(), category: 'CUSTOM', proficiencyLevel: 50, isVerified: false }
    ]);
    setNewSkillName('');
  };

  const handleRemoveSkill = (index) => {
    const updated = [...skills];
    updated.splice(index, 1);
    setSkills(updated);
  };

  const [extracting, setExtracting] = useState(false);

  const handleAiExtract = async () => {
    if (!careerGoal.trim()) return;
    setExtracting(true);
    setError('');
    try {
      const data = await profileService.extractGoal(careerGoal);
      if (data.targetRole) setTargetRole(data.targetRole);
      if (data.experienceLevel) setExperienceLevel(data.experienceLevel);
      
      const newSkills = [];
      if (data.extractedSkills) {
        data.extractedSkills.forEach(name => {
          newSkills.push({
            skillId: Date.now() + Math.random(),
            skillName: name,
            category: 'EXTRACTED',
            proficiencyLevel: 60,
            isVerified: false
          });
        });
      }
      if (data.missingSkills) {
        data.missingSkills.forEach(name => {
          if (!newSkills.some(s => s.skillName.toLowerCase() === name.toLowerCase())) {
            newSkills.push({
              skillId: Date.now() + Math.random(),
              skillName: name,
              category: 'ROADMAP_TARGET',
              proficiencyLevel: 20,
              isVerified: false
            });
          }
        });
      }
      if (newSkills.length > 0) {
        setSkills(newSkills);
      }
      setSuccessMsg(`AI analyzed goal: Target role set to "${data.targetRole || targetRole}". Review your skill proficiencies below and click Save Changes.`);
    } catch (err) {
      setError("AI analysis failed: " + (err.message || "Unknown error"));
    } finally {
      setExtracting(false);
    }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccessMsg('');

    try {
      await profileService.updateProfile({
        fullName,
        targetRole,
        careerGoal,
        experienceLevel,
        weeklyHours: parseInt(weeklyHours, 10),
        preferredStyle,
        skills: skills.map((s) => ({
          skillName: s.skillName,
          proficiencyLevel: s.proficiencyLevel,
        })),
      });
      await refreshProfile();
      setSuccessMsg('Profile and skill proficiencies updated successfully!');
      setTimeout(() => setSuccessMsg(''), 4000);
    } catch (err) {
      setError(err.message || 'Failed to save profile changes');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <LoadingSpinner size="lg" message="Loading learner profile..." className="h-96" />;
  }

  if (error && !profile) {
    return <ErrorMessage message={error} onRetry={fetchProfileData} />;
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6 sm:space-y-8 animate-fade-in pb-16 min-w-0 max-w-full">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl lg:text-3xl font-extrabold text-white tracking-tight">
            Learner Profile & Competencies
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1 leading-relaxed">
            Manage your career aspirations, verified skills, and recommendation parameters.
          </p>
        </div>

        <Button variant="primary" size="md" icon={Save} loading={saving} onClick={handleSave} className="w-full sm:w-auto">
          Save Changes
        </Button>
      </div>

      {successMsg && (
        <div className="p-4 bg-emerald-500/10 border border-emerald-500/30 rounded-2xl flex items-center gap-3 text-xs text-emerald-300 animate-fade-in">
          <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />
          <span>{successMsg}</span>
        </div>
      )}

      {error && (
        <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-2xl flex items-center gap-3 text-xs text-rose-300 animate-fade-in">
          <AlertCircle className="w-5 h-5 text-rose-400 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Main Profile Form */}
      <form onSubmit={handleSave} className="space-y-6">
        {/* Personal & Career Goal Card */}
        <Card className="glass-card p-4 sm:p-6 lg:p-8">
          <CardHeader>
            <CardTitle>Core Career Target</CardTitle>
          </CardHeader>

          <div className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input
                label="Full Name"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                icon={User}
                required
              />

              <Input
                label="Target Engineering Role"
                value={targetRole}
                onChange={(e) => setTargetRole(e.target.value)}
                icon={Target}
                required
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                Natural-Language Career Goal & Intent
              </label>
              <textarea
                value={careerGoal}
                onChange={(e) => setCareerGoal(e.target.value)}
                rows={3}
                placeholder="e.g. I want to build production full-stack apps with Spring Boot and React, master distributed caching and Docker..."
                className="w-full bg-slate-900 border border-slate-700/80 rounded-xl p-3.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              />
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2.5 mt-2">
                <span className="text-[11px] text-slate-500">
                  AI extracts target roles and skill competencies directly from your goal statement.
                </span>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  icon={Sparkles}
                  loading={extracting}
                  onClick={handleAiExtract}
                  className="w-full sm:w-auto"
                >
                  Analyze Goal with AI
                </Button>

              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-2 border-t border-slate-800/80">
              <Select
                label="Current Experience"
                value={experienceLevel}
                onChange={(e) => setExperienceLevel(e.target.value)}
                options={[
                  { value: 'BEGINNER', label: 'Beginner (0-1 yrs)' },
                  { value: 'INTERMEDIATE', label: 'Intermediate (1-3 yrs)' },
                  { value: 'ADVANCED', label: 'Advanced (3+ yrs)' },
                ]}
              />

              <Select
                label="Weekly Commitment"
                value={weeklyHours.toString()}
                onChange={(e) => setWeeklyHours(e.target.value)}
                options={[
                  { value: '5', label: '5 Hours / Week (Casual)' },
                  { value: '10', label: '10 Hours / Week (Standard)' },
                  { value: '15', label: '15 Hours / Week (Accelerated)' },
                  { value: '20', label: '20 Hours / Week (Immersive)' },
                ]}
              />

              <Select
                label="Learning Style"
                value={preferredStyle}
                onChange={(e) => setPreferredStyle(e.target.value)}
                options={[
                  { value: 'PRACTICAL', label: 'Practical (Projects)' },
                  { value: 'VIDEO', label: 'Visual (Videos)' },
                  { value: 'READING', label: 'Reading (Docs/Books)' },
                ]}
              />
            </div>
          </div>
        </Card>

        {/* Skills Proficiency Sliders Card */}
        <Card className="glass-card p-4 sm:p-6 lg:p-8">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
            <div>
              <CardTitle>Skill Matrix & Proficiencies</CardTitle>
              <p className="text-xs text-slate-400 mt-1 leading-relaxed">
                Verified proficiencies dynamically calculate missing prerequisite locks on your roadmap.
              </p>
            </div>

            <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2 w-full sm:w-auto">
              <input
                type="text"
                placeholder="Add new skill..."
                value={newSkillName}
                onChange={(e) => setNewSkillName(e.target.value)}
                className="bg-slate-900 border border-slate-700/80 rounded-xl px-3 py-1.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-blue-500 w-full sm:w-44"
              />
              <Button variant="secondary" size="sm" icon={Plus} onClick={handleAddSkill} className="w-full sm:w-auto">
                Add
              </Button>
            </div>
          </div>

          <div className="space-y-3 sm:space-y-4">
            {skills.map((skill, index) => (
              <div key={index} className="p-3.5 sm:p-4 bg-slate-900/80 border border-slate-800 rounded-xl">
                <div className="flex flex-wrap items-center justify-between text-xs font-semibold mb-2 gap-1.5">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="text-slate-200 break-words">{skill.skillName}</span>
                    {skill.isVerified && (
                      <Badge variant="success" size="sm" icon={Award}>
                        Verified
                      </Badge>
                    )}
                  </div>
                  <div className="flex items-center gap-2.5">
                    <span className="text-sky-400 font-mono font-bold">
                      {skill.proficiencyLevel}%
                    </span>
                    <button
                      type="button"
                      onClick={() => handleRemoveSkill(index)}
                      className="text-slate-500 hover:text-rose-400 transition p-1"
                      title="Remove skill"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>

                <input
                  type="range"
                  min="0"
                  max="100"
                  step="5"
                  value={skill.proficiencyLevel}
                  onChange={(e) => handleSkillSlider(index, e.target.value)}
                  className="w-full h-1.5 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-blue-500"
                />
              </div>
            ))}
          </div>
        </Card>
      </form>
    </div>
  );
};
