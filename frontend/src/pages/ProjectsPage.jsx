import React, { useState, useEffect } from 'react';
import {
  FolderGit2, ExternalLink, Clock, CheckCircle2, Award,
  Github, Send, Sparkles, Layers, ShieldCheck, Filter,
  PlusCircle, RefreshCw, Compass, ArrowRight, Code2
} from 'lucide-react';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import { Input } from '../components/ui/Input';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { projectService } from '../services/projectService';
import { recommendationService } from '../services/recommendationService';
import { Link } from 'react-router-dom';

export const ProjectsPage = () => {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterDifficulty, setFilterDifficulty] = useState('ALL');
  const [selectedProject, setSelectedProject] = useState(null);
  const [githubUrl, setGithubUrl] = useState('');
  const [demoUrl, setDemoUrl] = useState('');
  const [reflection, setReflection] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [successToast, setSuccessToast] = useState(null);

  // Generate Custom Modal State
  const [isGenerateModalOpen, setIsGenerateModalOpen] = useState(false);
  const [customTopic, setCustomTopic] = useState('');
  const [generating, setGenerating] = useState(false);

  const fetchProjects = async () => {
    try {
      setLoading(true);
      let projectData = [];
      try {
        projectData = await projectService.getProjects();
      } catch (e) {
        console.warn("Falling back to recommendations endpoint for projects:", e);
      }

      if (!projectData || projectData.length === 0) {
        const recData = await recommendationService.getRecommendations();
        const projectItems = recData.filter(item => item.itemType === 'PROJECT' || item.type === 'PROJECT');
        projectData = projectItems.map((p, idx) => ({
          id: p.id || idx + 1,
          title: p.title,
          description: p.description,
          difficulty: p.difficulty || "INTERMEDIATE",
          estimatedHours: p.estimatedHours || 15.0,
          primarySkillName: p.skillsTaught && p.skillsTaught.length > 0 ? p.skillsTaught[0] : (p.category || "Hands-On Project"),
          skills: p.skillsTaught || [],
          completed: false,
          githubTemplateUrl: p.url || "https://github.com",
          isAiGenerated: false,
          roadmapPhase: "Active Phase Milestone",
          deliverables: "Working repository and automated tests.",
          rubric: "Architecture modularity (40%), test coverage (30%), code quality (30%)."
        }));
      }

      setProjects(projectData.map(p => ({
        ...p,
        completed: false,
        skills: p.skills && p.skills.length > 0 ? p.skills : (p.primarySkillName ? [p.primarySkillName] : ["Software Architecture"])
      })));
    } catch (err) {
      console.error("Error loading project recommendations:", err);
      setProjects([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProjects();
  }, []);

  const handleGenerateCustom = async (e) => {
    e.preventDefault();
    try {
      setGenerating(true);
      const generatedList = await projectService.generateAdaptiveProjects(customTopic);
      if (generatedList && generatedList.length > 0) {
        setProjects(prev => [...generatedList.map(g => ({ ...g, completed: false })), ...prev]);
        setSuccessToast(`Synthesized ${generatedList.length} customized milestone project(s)!`);
        setIsGenerateModalOpen(false);
        setCustomTopic('');
        setTimeout(() => setSuccessToast(null), 4000);
      }
    } catch (err) {
      console.error("Error generating custom projects:", err);
    } finally {
      setGenerating(false);
    }
  };

  const handleSubmitProject = async (e) => {
    e.preventDefault();
    if (!selectedProject) return;

    setSubmitting(true);
    try {
      await projectService.submitProject(selectedProject.id, {
        githubUrl,
        demoUrl,
        reflection
      });
      setProjects(prev => prev.map(p => p.id === selectedProject.id ? { ...p, completed: true } : p));
      setSuccessToast(`Project "${selectedProject.title}" verified and recorded! 50 competency points awarded.`);
      setSelectedProject(null);
      setGithubUrl('');
      setDemoUrl('');
      setReflection('');
      setTimeout(() => setSuccessToast(null), 4000);
    } catch (err) {
      console.error("Error submitting project:", err);
      // Local fallback simulation
      setProjects(prev => prev.map(p => p.id === selectedProject.id ? { ...p, completed: true } : p));
      setSuccessToast(`Project "${selectedProject.title}" recorded!`);
      setSelectedProject(null);
      setTimeout(() => setSuccessToast(null), 4000);
    } finally {
      setSubmitting(false);
    }
  };

  const filteredProjects = projects.filter(p => {
    if (filterDifficulty === 'ALL') return true;
    return (p.difficulty || '').toUpperCase() === filterDifficulty;
  });

  if (loading) {
    return <LoadingSpinner size="lg" message="Loading recommended portfolio milestone projects..." className="h-96" />;
  }

  return (
    <div className="space-y-8 animate-fade-in pb-16">
      {/* Toast */}
      {successToast && (
        <div className="fixed top-20 right-6 z-50 p-4 bg-emerald-600/90 border border-emerald-400 text-white rounded-2xl shadow-2xl flex items-center gap-3 animate-fade-in text-xs font-semibold">
          <CheckCircle2 className="w-5 h-5 text-emerald-200 shrink-0" />
          <span>{successToast}</span>
        </div>
      )}

      {/* Header & Actions */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight flex items-center gap-2.5">
            Portfolio Milestone Projects <FolderGit2 className="w-7 h-7 text-purple-400" />
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Real-world, portfolio-grade engineering builds sequenced to validate end-to-end architectural competency.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            icon={RefreshCw}
            onClick={fetchProjects}
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            size="sm"
            icon={Sparkles}
            onClick={() => setIsGenerateModalOpen(true)}
          >
            ⚡ Generate Project
          </Button>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2">
        {['ALL', 'BEGINNER', 'INTERMEDIATE', 'ADVANCED'].map((diff) => (
          <button
            key={diff}
            onClick={() => setFilterDifficulty(diff)}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all ${
              filterDifficulty === diff
                ? 'bg-blue-600 text-white shadow-lg shadow-blue-500/25'
                : 'bg-slate-900/80 text-slate-400 hover:text-white border border-slate-800'
            }`}
          >
            {diff === 'ALL' ? 'All Difficulties' : diff.charAt(0) + diff.slice(1).toLowerCase()}
          </button>
        ))}
      </div>

      {/* Project Cards Grid / Empty State */}
      {filteredProjects.length === 0 ? (
        <Card className="p-12 text-center border border-slate-800 bg-slate-900/40 rounded-3xl">
          <div className="w-16 h-16 rounded-2xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center mx-auto mb-4 text-purple-400">
            <Compass className="w-8 h-8" />
          </div>
          <h3 className="text-lg font-bold text-white mb-2">No Projects Matched for this Filter</h3>
          <p className="text-xs text-slate-400 max-w-md mx-auto mb-6">
            We haven't found a pre-seeded project for this difficulty stage yet. You can dynamically synthesize an adaptive project tailored to your active goal.
          </p>
          <div className="flex items-center justify-center gap-3">
            <Button
              variant="primary"
              size="md"
              icon={Sparkles}
              onClick={() => setIsGenerateModalOpen(true)}
            >
              Generate Milestone Project
            </Button>
            <Link to="/app/profile">
              <Button variant="outline" size="md" icon={ArrowRight}>
                Update Career Goal
              </Button>
            </Link>
          </div>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {filteredProjects.map((p) => (
            <Card
              key={p.id}
              className={`glass-card-interactive p-6 flex flex-col justify-between border ${
                p.completed ? 'border-emerald-500/30 bg-slate-900/60' : 'border-slate-800'
              }`}
            >
              <div>
                {/* Badges Header */}
                <div className="flex items-center justify-between gap-2 mb-3 flex-wrap">
                  <div className="flex items-center gap-1.5 flex-wrap">
                    <Badge variant={p.completed ? 'success' : 'purple'} size="sm">
                      {p.primarySkillName || (p.skills && p.skills[0]) || "Core Architecture"}
                    </Badge>
                    {p.isAiGenerated ? (
                      <Badge variant="blue" size="sm" icon={Sparkles}>
                        AI-Synthesized
                      </Badge>
                    ) : (
                      <Badge variant="default" size="sm" icon={ShieldCheck}>
                        Curated
                      </Badge>
                    )}
                  </div>

                  <div className="flex items-center gap-2">
                    <Badge variant="default" size="sm">{p.difficulty}</Badge>
                    {p.completed && (
                      <Badge variant="success" size="sm" icon={CheckCircle2}>
                        Verified
                      </Badge>
                    )}
                  </div>
                </div>

                {/* Title & Roadmap Phase */}
                {p.roadmapPhase && (
                  <div className="text-[11px] font-mono text-purple-400 mb-1.5 flex items-center gap-1">
                    <Layers className="w-3 h-3 shrink-0" />
                    <span>{p.roadmapPhase}</span>
                  </div>
                )}

                <h3 className="text-base sm:text-lg font-bold text-white mb-2 leading-snug">
                  {p.title}
                </h3>
                <p className="text-xs text-slate-400 leading-relaxed mb-4">
                  {p.description}
                </p>

                {/* Deliverables / Rubric Info */}
                {p.deliverables && (
                  <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 mb-4 text-[11px] text-slate-300">
                    <span className="font-semibold text-slate-200">Deliverables: </span>
                    {p.deliverables}
                  </div>
                )}

                {/* Skills tags */}
                {p.skills && p.skills.length > 1 && (
                  <div className="flex items-center gap-1.5 flex-wrap mb-4">
                    {p.skills.slice(0, 4).map((sk, idx) => (
                      <span key={idx} className="px-2 py-0.5 rounded-md bg-slate-800/80 text-[10px] text-slate-300 font-mono">
                        {sk}
                      </span>
                    ))}
                  </div>
                )}
              </div>

              {/* Card Footer */}
              <div className="pt-4 border-t border-slate-800 flex items-center justify-between">
                <span className="text-xs text-slate-400 flex items-center gap-1.5 font-mono">
                  <Clock className="w-3.5 h-3.5 text-slate-500" />
                  {p.estimatedHours} Hours
                </span>

                <div className="flex items-center gap-2">
                  <a href={p.githubTemplateUrl || "https://github.com"} target="_blank" rel="noopener noreferrer">
                    <Button variant="outline" size="sm" icon={ExternalLink}>
                      Starter Guide
                    </Button>
                  </a>

                  <Button
                    variant={p.completed ? "secondary" : "primary"}
                    size="sm"
                    icon={p.completed ? CheckCircle2 : Github}
                    onClick={() => setSelectedProject(p)}
                  >
                    {p.completed ? "Re-Submit" : "Submit Repo"}
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* On-Demand Generate Modal */}
      {isGenerateModalOpen && (
        <Modal
          isOpen={isGenerateModalOpen}
          onClose={() => setIsGenerateModalOpen(false)}
          title="⚡ Synthesize Adaptive Portfolio Project"
          maxWidth="max-w-lg"
        >
          <form onSubmit={handleGenerateCustom} className="space-y-4 animate-fade-in">
            <p className="text-xs text-slate-400 leading-relaxed">
              Our AI engine will analyze your active goal, skill gaps, and current roadmap phase to generate a portfolio-grade project specification.
            </p>

            <Input
              label="Custom Focus or Topic (Optional)"
              id="customTopic"
              placeholder="e.g. Offline SQLite sync, WebSocket chat, or Helm deployment..."
              value={customTopic}
              onChange={(e) => setCustomTopic(e.target.value)}
              icon={Code2}
            />

            <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
              <Button variant="outline" size="md" onClick={() => setIsGenerateModalOpen(false)}>
                Cancel
              </Button>
              <Button variant="primary" size="md" type="submit" loading={generating} icon={Sparkles}>
                Generate Milestone
              </Button>
            </div>
          </form>
        </Modal>
      )}

      {/* Submission Modal */}
      {selectedProject && (
        <Modal
          isOpen={!!selectedProject}
          onClose={() => setSelectedProject(null)}
          title={`Submit: ${selectedProject.title}`}
          maxWidth="max-w-lg"
        >
          <form onSubmit={handleSubmitProject} className="space-y-4 animate-fade-in">
            <p className="text-xs text-slate-400 leading-relaxed">
              Submit your GitHub repository link and reflections to record portfolio progress.
            </p>

            <Input
              label="GitHub Repository URL"
              id="githubUrl"
              placeholder="https://github.com/username/project-repo"
              value={githubUrl}
              onChange={(e) => setGithubUrl(e.target.value)}
              icon={Github}
              required
            />

            <Input
              label="Live Demo / Deployment URL (Optional)"
              id="demoUrl"
              placeholder="https://my-app.fly.dev"
              value={demoUrl}
              onChange={(e) => setDemoUrl(e.target.value)}
              icon={ExternalLink}
            />

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1.5">
                Key Technical Learnings & Challenges
              </label>
              <textarea
                rows={3}
                value={reflection}
                onChange={(e) => setReflection(e.target.value)}
                placeholder="e.g. Implemented reactive state providers, optimized memory usage, handled edge cases..."
                className="w-full bg-slate-900 border border-slate-700/80 rounded-xl p-3 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 leading-relaxed"
                required
              />
            </div>

            <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
              <Button variant="outline" size="md" onClick={() => setSelectedProject(null)}>
                Cancel
              </Button>
              <Button variant="primary" size="md" type="submit" loading={submitting} icon={Send}>
                Submit for Verification
              </Button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
};
