import React from 'react';

export const LoadingSpinner = ({ size = 'md', message = 'Loading...', className = '' }) => {
  const sizeClass = size === 'sm' ? 'w-5 h-5' : size === 'lg' ? 'w-10 h-10' : 'w-7 h-7';
  return (
    <div className={`flex flex-col items-center justify-center p-8 text-center ${className}`}>
      <div className={`animate-spin rounded-full border-2 border-slate-700 border-t-blue-500 ${sizeClass} mb-3`} />
      {message && <p className="text-xs text-slate-400 font-medium animate-pulse">{message}</p>}
    </div>
  );
};

export const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse bg-slate-800/60 rounded-xl ${className}`} />
);
