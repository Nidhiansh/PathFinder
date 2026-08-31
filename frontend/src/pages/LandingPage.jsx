import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Sparkles, Compass, Route, CheckCircle2, ArrowRight, Brain, Zap,
  Layers, Lock, ShieldCheck, Flame, BookOpen, Terminal, ChevronRight
} from 'lucide-react';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Card } from '../components/ui/Card';

export const LandingPage = () => {
  const { isAuthenticated } = useAuth();

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col selection:bg-blue-500 selection:text-white">
      {/* Top Header */}
      <header className="h-20 border-b border-slate-800/80 bg-slate-950/60 backdrop-blur-lg sticky top-0 z-40 px-6 sm:px-12 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-blue-600 via-sky-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-blue-500/25">
            <Compass className="w-6 h-6 text-white" />
          </div>
          <div>
            <span className="font-extrabold text-lg tracking-tight text-white flex items-center gap-2">
              PathFinder <span className="text-xs uppercase font-mono px-2 py-0.5 rounded-full bg-blue-500/10 text-sky-400 border border-blue-500/30">AI</span>
            </span>
            <span className="text-[11px] text-slate-400 hidden sm:block">Intelligent Learning Path Engine</span>
          </div>
        </div>

        <div className="flex items-center gap-3">
          {isAuthenticated ? (
            <Link to="/app/dashboard">
              <Button variant="primary" size="md" icon={ArrowRight}>
                Go to Dashboard
              </Button>
            </Link>
          ) : (
            <>
              <Link to="/login">
                <Button variant="ghost" size="md">
                  Log in
                </Button>
              </Link>
              <Link to="/register">
                <Button variant="primary" size="md">
                  Get Started
                </Button>
              </Link>
            </>
          )}
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative pt-16 pb-20 px-6 sm:px-12 max-w-7xl mx-auto text-center flex-1 flex flex-col items-center justify-center">
        {/* Glow backdrop */}
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[350px] bg-blue-500/10 blur-[120px] rounded-full pointer-events-none" />

        <Badge variant="brand" size="lg" icon={Sparkles} className="mb-6 animate-pulse-subtle">
          Autonomous Skill-Gap & Prerequisite Reasoning Engine
        </Badge>

        <h1 className="text-4xl sm:text-6xl lg:text-7xl font-extrabold tracking-tight max-w-4xl mx-auto leading-[1.1] mb-6">
          Your Personalized Path to Your <span className="gradient-text">Dream Engineering Career</span>
        </h1>

        <p className="text-base sm:text-lg text-slate-400 max-w-2xl mx-auto leading-relaxed mb-8">
          Stop wondering what to learn next. PathFinder AI analyzes your current skills, target role, and available hours to generate, explain, and dynamically adapt a prerequisite-aware learning roadmap.
        </p>

        {/* CTA Buttons */}
        <div className="flex flex-col sm:flex-row items-center gap-4 mb-16 w-full justify-center">
          <Link to="/register" className="w-full sm:w-auto">
            <Button variant="primary" size="lg" icon={ArrowRight} className="w-full sm:w-auto shadow-xl shadow-blue-500/25">
              Start Free Assessment
            </Button>
          </Link>
          <Link to="/login" className="w-full sm:w-auto">
            <Button variant="outline" size="lg" className="w-full sm:w-auto">
              Sign In to Account
            </Button>
          </Link>
        </div>

        {/* Core Architecture Features Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-5xl w-full text-left">
          <Card className="border-slate-800">
            <div className="w-10 h-10 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center mb-4 border border-blue-500/20">
              <Route className="w-5 h-5" />
            </div>
            <h4 className="text-base font-semibold text-white mb-2">Prerequisite DAG Graph</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Topological dependency resolution guarantees that you never encounter locked advanced modules before foundational skills are verified.
            </p>
          </Card>

          <Card className="border-slate-800">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center mb-4 border border-emerald-500/20">
              <Brain className="w-5 h-5" />
            </div>
            <h4 className="text-base font-semibold text-white mb-2">Multi-Factor Scoring</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Every course and project recommendation receives a calculated match score factoring skill gaps, goals, style, and verified difficulty.
            </p>
          </Card>

          <Card className="border-slate-800">
            <div className="w-10 h-10 rounded-xl bg-purple-500/10 text-purple-400 flex items-center justify-center mb-4 border border-purple-500/20">
              <Zap className="w-5 h-5" />
            </div>
            <h4 className="text-base font-semibold text-white mb-2">Adaptive Feedback Loop</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Scoring &ge;90% on checkpoint assessments fast-tracks downstream modules, while weekly hour adjustments automatically scale target dates.
            </p>
          </Card>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-slate-800/80 py-8 px-6 sm:px-12 text-center text-xs text-slate-500">
        <p>© 2026 PathFinder AI. Production-grade AI-Powered Learning Path Recommender.</p>
      </footer>
    </div>
  );
};
