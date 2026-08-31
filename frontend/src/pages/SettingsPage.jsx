import React, { useState } from 'react';
import { Settings, CheckCircle2 } from 'lucide-react';
import { Card, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';

export const SettingsPage = () => {
  const [weights, setWeights] = useState({
    skillGap: 30,
    goalRelevance: 25,
    prerequisites: 15,
    difficulty: 10,
    learningStyle: 10,
    quality: 10,
  });
  const [saved, setSaved] = useState(false);

  const handleSave = () => {
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6 sm:space-y-8 animate-fade-in pb-16 min-w-0 max-w-full">
      <div>
        <h1 className="text-xl sm:text-2xl lg:text-3xl font-extrabold text-white tracking-tight flex items-center gap-2.5 flex-wrap">
          <span>Settings & Recommendation Weights</span>
          <Settings className="w-5 h-5 sm:w-6 sm:h-6 text-slate-400 shrink-0" />
        </h1>
        <p className="text-xs sm:text-sm text-slate-400 mt-1 leading-relaxed">
          Customize the mathematical scoring algorithm parameters and platform preferences.
        </p>
      </div>

      {saved && (
        <div className="p-4 bg-emerald-500/10 border border-emerald-500/30 rounded-2xl flex items-center gap-3 text-xs text-emerald-300">
          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          <span>Recommendation scoring algorithm weights updated!</span>
        </div>
      )}

      <Card className="glass-card p-4 sm:p-6 lg:p-8">
        <CardHeader>
          <CardTitle>Recommendation Scoring Weights (%)</CardTitle>
        </CardHeader>

        <div className="space-y-3 sm:space-y-4">
          {Object.entries(weights).map(([key, val]) => (
            <div key={key} className="p-3.5 bg-slate-900/80 border border-slate-800 rounded-xl">
              <div className="flex items-center justify-between text-xs font-semibold mb-2">
                <span className="text-slate-200 capitalize">{key.replace(/([A-Z])/g, ' $1')} Weight</span>
                <span className="text-sky-400 font-mono font-bold">{val}%</span>
              </div>
              <input
                type="range"
                min="0"
                max="50"
                step="5"
                value={val}
                onChange={(e) => setWeights({ ...weights, [key]: parseInt(e.target.value, 10) })}
                className="w-full h-1.5 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-blue-500"
              />
            </div>
          ))}
        </div>

        <div className="pt-4 sm:pt-6 mt-6 border-t border-slate-800 flex justify-end">
          <Button variant="primary" size="md" onClick={handleSave} className="w-full sm:w-auto">
            Save Algorithm Weights
          </Button>
        </div>
      </Card>
    </div>
  );
};

