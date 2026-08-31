import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Flame, Sparkles, User as UserIcon, LogOut, ChevronRight, Menu
} from 'lucide-react';
import { Badge } from './ui/Badge';

export const Navbar = ({ onOpenAssistant, onToggleSidebar }) => {
  const { user, profile, logout } = useAuth();
  const location = useLocation();

  const getBreadcrumb = () => {
    const path = location.pathname.replace('/app/', '');
    if (!path || path === '/app' || path === 'dashboard') return 'Dashboard';
    return path.charAt(0).toUpperCase() + path.slice(1);
  };

  return (
    <header className="h-16 border-b border-slate-800/80 bg-slate-950/80 backdrop-blur-md sticky top-0 z-30 px-3 sm:px-6 flex items-center justify-between gap-2 max-w-full">
      {/* Left: Hamburger & Breadcrumbs */}
      <div className="flex items-center gap-2 sm:gap-3 min-w-0">
        {/* Mobile Hamburger Drawer Trigger */}
        <button
          onClick={onToggleSidebar}
          className="p-1.5 text-slate-400 hover:text-white rounded-xl hover:bg-slate-900 border border-slate-800 lg:hidden shrink-0 transition"
          aria-label="Toggle navigation menu"
        >
          <Menu className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-1.5 sm:gap-2 min-w-0">
          <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-500 hidden sm:inline shrink-0">PathFinder</span>
          <ChevronRight className="w-3.5 h-3.5 text-slate-600 hidden sm:inline shrink-0" />
          <span className="text-xs sm:text-sm font-semibold text-slate-100 truncate">{getBreadcrumb()}</span>
          {profile?.targetRole && (
            <Badge variant="brand" size="sm" className="hidden md:inline-flex ml-1.5 truncate max-w-[140px]">
              {profile.targetRole}
            </Badge>
          )}
        </div>
      </div>

      {/* Right: Actions & User */}
      <div className="flex items-center gap-1.5 sm:gap-3 shrink-0">
        {/* Streak Badge */}
        <div className="flex items-center gap-1 px-2 sm:px-3 py-1 bg-amber-500/10 border border-amber-500/20 rounded-full text-xs font-semibold text-amber-400">
          <Flame className="w-3.5 h-3.5 fill-amber-400 shrink-0" />
          <span className="hidden xs:inline">{profile?.streakDays || 1}d Streak</span>
          <span className="xs:hidden font-mono">{profile?.streakDays || 1}d</span>
        </div>

        {/* AI Copilot Quick Launcher */}
        <button
          onClick={onOpenAssistant}
          className="flex items-center gap-1 px-2 sm:px-3 py-1.5 bg-gradient-to-r from-blue-600/20 to-sky-600/20 border border-blue-500/30 hover:border-blue-500/60 rounded-xl text-xs font-medium text-sky-300 transition hover:bg-blue-600/30 shrink-0"
          title="Open AI Learning Copilot"
        >
          <Sparkles className="w-3.5 h-3.5 text-sky-400" />
          <span className="hidden md:inline">AI Copilot</span>
        </button>

        {/* User Profile & Logout */}
        <div className="flex items-center gap-1.5 sm:gap-2 pl-1.5 sm:pl-2 border-l border-slate-800 shrink-0">
          <Link
            to="/app/profile"
            className="w-7 h-7 sm:w-8 sm:h-8 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center text-white text-xs font-bold shadow-md hover:ring-2 hover:ring-blue-500 transition shrink-0"
            title="View profile"
          >
            {user?.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
          </Link>
          <div className="hidden lg:block text-left max-w-[120px]">
            <p className="text-xs font-medium text-slate-200 leading-none truncate">{user?.fullName || user?.username}</p>
            <p className="text-[10px] text-slate-500 mt-0.5 truncate">{user?.email}</p>
          </div>
          <button
            onClick={logout}
            title="Log out"
            className="p-1 sm:p-1.5 text-slate-400 hover:text-rose-400 hover:bg-slate-800 rounded-lg transition"
          >
            <LogOut className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
          </button>
        </div>
      </div>
    </header>
  );
};

