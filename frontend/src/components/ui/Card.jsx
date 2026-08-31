import React from 'react';

export const Card = ({
  children,
  className = '',
  interactive = false,
  onClick,
  ...props
}) => {
  const cardStyle = interactive ? 'glass-card-interactive cursor-pointer' : 'glass-card';
  return (
    <div
      className={`rounded-2xl p-6 ${cardStyle} ${className}`}
      onClick={onClick}
      {...props}
    >
      {children}
    </div>
  );
};

export const CardHeader = ({ children, className = '' }) => (
  <div className={`flex items-center justify-between mb-4 ${className}`}>{children}</div>
);

export const CardTitle = ({ children, className = '' }) => (
  <h3 className={`text-lg font-semibold text-white tracking-tight ${className}`}>{children}</h3>
);

export const CardDescription = ({ children, className = '' }) => (
  <p className={`text-sm text-slate-400 mt-1 ${className}`}>{children}</p>
);

export const CardContent = ({ children, className = '' }) => (
  <div className={className}>{children}</div>
);

export const CardFooter = ({ children, className = '' }) => (
  <div className={`mt-5 pt-4 border-t border-slate-800/80 flex items-center justify-between ${className}`}>{children}</div>
);
