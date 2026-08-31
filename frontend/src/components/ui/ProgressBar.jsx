import React from 'react';

export const ProgressBar = ({
  value = 0,
  max = 100,
  variant = 'brand',
  size = 'md',
  showLabel = false,
  label = '',
  className = '',
}) => {
  const percentage = Math.min(100, Math.max(0, Math.round((value / max) * 100)));

  const variants = {
    brand: "from-blue-600 via-sky-500 to-indigo-500",
    success: "from-emerald-600 to-teal-500",
    warning: "from-amber-600 to-yellow-500",
    danger: "from-rose-600 to-red-500",
    purple: "from-purple-600 to-pink-500",
  };

  const sizes = {
    sm: "h-1.5",
    md: "h-2.5",
    lg: "h-4",
  };

  return (
    <div className={`w-full ${className}`}>
      {showLabel && (
        <div className="flex justify-between items-center text-xs text-slate-400 mb-1.5 font-medium">
          <span>{label}</span>
          <span className="text-slate-200">{percentage}%</span>
        </div>
      )}
      <div className={`w-full bg-slate-800/80 rounded-full overflow-hidden border border-slate-700/40 p-0.5 ${sizes[size]}`}>
        <div
          className={`h-full rounded-full bg-gradient-to-r ${variants[variant]} transition-all duration-500 ease-out`}
          style={{ width: `${percentage}%` }}
        />
      </div>
    </div>
  );
};
