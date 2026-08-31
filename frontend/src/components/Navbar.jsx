import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Flame, Bell, Sparkles, User as UserIcon, LogOut, ChevronRight
} from 'lucide-react';
import { Badge } from './ui/Badge';

export const Navbar = ({ onOpenAssistant }) => {
  const { user, profile, logout } = useAuth();
  const location = useLocation();

  const getBreadcrumb = () => {
    const path = location.pathname.replace('/app/', '');
    if (!path || path === '/app' || path === 'dashboard') return 'Dashboard';
    return path.charAt(0).toUpperCase() + path.slice(1);
  };

  return (
    <header className="h-16 border-b border-slate-800/80 bg-slate-950/80 backdrop-blur-md sticky top-0 z-30 px-6 flex items-center justify-between">
      {/* Left: Breadcrumbs / Role */}
      <div className="flex items-center gap-3">
        <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">PathFinder</span>
        <ChevronRight className="w-3.5 h-3.5 text-slate-600" />
        <span className="text-sm font-semibold text-slate-100">{getBreadcrumb()}</span>
        {profile?.targetRole && (
          <Badge variant="brand" size="sm" className="hidden sm:inline-flex ml-2">
            {profile.targetRole}
          </Badge>
        )}
      </div>

      {/* Right: Actions & User */}
      <div className="flex items-center gap-3">
        {/* Streak Badge */}
        <div className="flex items-center gap-1.5 px-3 py-1 bg-amber-500/10 border border-amber-500/20 rounded-full text-xs font-semibold text-amber-400">
          <Flame className="w-3.5 h-3.5 fill-amber-400" />
          <span>{profile?.streakDays || 1} Day Streak</span>
        </div>

        {/* AI Copilot Quick Launcher */}
        <button
          onClick={onOpenAssistant}
          className="flex items-center gap-1.5 px-3 py-1.5 bg-gradient-to-r from-blue-600/20 to-sky-600/20 border border-blue-500/30 hover:border-blue-500/60 rounded-xl text-xs font-medium text-sky-300 transition hover:bg-blue-600/30"
        >
          <Sparkles className="w-3.5 h-3.5 text-sky-400" />
          <span className="hidden md:inline">AI Copilot</span>
        </button>

        {/* User profile dropdown info */}
        <div className="flex items-center gap-2 pl-2 border-l border-slate-800">
          <Link
            to="/app/profile"
            className="w-8 h-8 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center text-white text-xs font-bold shadow-md hover:ring-2 hover:ring-blue-500 transition"
          >
            {user?.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
          </Link>
          <div className="hidden lg:block text-left">
            <p className="text-xs font-medium text-slate-200 leading-none">{user?.fullName || user?.username}</p>
            <p className="text-[10px] text-slate-500 mt-0.5">{user?.email}</p>
          </div>
          <button
            onClick={logout}
            title="Log out"
            className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-slate-800 rounded-lg transition ml-1"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </header>
  );
};
