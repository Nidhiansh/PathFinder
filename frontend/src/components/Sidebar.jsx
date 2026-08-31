import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, Map, Sparkles, Compass, FolderGit2,
  TrendingUp, Bot, User, Settings, Compass as LogoIcon
} from 'lucide-react';

export const Sidebar = ({ isOpen, onClose }) => {
  const navItems = [
    { to: '/app/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/app/roadmap', label: 'Learning Roadmap', icon: Map },
    { to: '/app/recommendations', label: 'Recommendations', icon: Sparkles },
    { to: '/app/skills', label: 'Skill Gap Analysis', icon: Compass },
    { to: '/app/projects', label: 'Projects Hub', icon: FolderGit2 },
    { to: '/app/progress', label: 'Assessments & Quizzes', icon: TrendingUp },
    { to: '/app/assistant', label: 'AI Learning Copilot', icon: Bot },
  ];

  const bottomNavItems = [
    { to: '/app/profile', label: 'Learner Profile', icon: User },
    { to: '/app/settings', label: 'Preferences & Pace', icon: Settings },
  ];

  return (
    <>
      {/* Mobile Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-40 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar Aside */}
      <aside
        className={`fixed top-0 bottom-0 left-0 w-64 bg-slate-950 border-r border-slate-800/80 flex flex-col z-50 transition-transform duration-300 ease-in-out lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Brand Logo & Mobile Dismiss */}
        <div className="h-16 px-5 sm:px-6 flex items-center justify-between border-b border-slate-800/80">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-blue-600 via-sky-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-blue-500/20 shrink-0">
              <LogoIcon className="w-5 h-5 text-white" />
            </div>
            <div>
              <h1 className="font-bold text-base tracking-tight text-white flex items-center gap-1.5">
                PathFinder <span className="text-[10px] uppercase font-mono px-1.5 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20">AI</span>
              </h1>
              <p className="text-[10px] text-slate-500">Personalized Learning</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-white rounded-xl hover:bg-slate-900 border border-slate-800 lg:hidden"
            aria-label="Close navigation"
          >
            <span className="text-sm font-bold">✕</span>
          </button>
        </div>


        {/* Main Navigation */}
        <div className="flex-1 py-5 px-3 space-y-1 overflow-y-auto">
          <div className="px-3 pb-2 text-[10px] font-bold uppercase tracking-wider text-slate-500">
            Navigation
          </div>
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={onClose}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-medium transition-all ${
                    isActive
                      ? 'bg-blue-600/15 text-sky-400 border border-blue-500/30 shadow-sm'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900/60'
                  }`
                }
              >
                <Icon className="w-4 h-4" />
                <span>{item.label}</span>
              </NavLink>
            );
          })}

          <div className="pt-6 px-3 pb-2 text-[10px] font-bold uppercase tracking-wider text-slate-500">
            Account & Settings
          </div>
          {bottomNavItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={onClose}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-medium transition-all ${
                    isActive
                      ? 'bg-blue-600/15 text-sky-400 border border-blue-500/30 shadow-sm'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900/60'
                  }`
                }
              >
                <Icon className="w-4 h-4" />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </div>

        {/* Pro / Engine Status Pill */}
        <div className="p-4 border-t border-slate-800/80">
          <div className="glass-card rounded-xl p-3 border-slate-800">
            <div className="flex items-center gap-2 mb-1">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              <span className="text-[11px] font-semibold text-slate-300">AI Engine Active</span>
            </div>
            <p className="text-[10px] text-slate-500">DAG Prerequisite Resolver v1.0</p>
          </div>
        </div>
      </aside>
    </>
  );
};
