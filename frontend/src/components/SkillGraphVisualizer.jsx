import React, { useState, useMemo } from 'react';
import { Compass, CheckCircle2, Lock, ArrowRight, BookOpen, Sparkles, X, Award, ExternalLink } from 'lucide-react';
import { Badge } from './ui/Badge';
import { Button } from './ui/Button';

export const SkillGraphVisualizer = ({ skills = [], onSelectSkill }) => {
  const [selectedSkill, setSelectedSkill] = useState(null);

  // Pre-configured DAG structural layout for curriculum skills
  const graphNodes = useMemo(() => {
    // Defined topological tiers (columns)
    const baseTiers = {
      0: ['Object-Oriented Programming (OOP)', 'Git & Version Control', 'Prompt Engineering & LLM APIs'],
      1: ['Java', 'Python Programming', 'JavaScript (ES6+)', 'Foundations of AI & ML'],
      2: ['Data Structures & Algorithms', 'SQL & Relational Databases', 'React.js', 'Vector Databases & Embeddings'],
      3: ['Spring Boot', 'NumPy & Pandas', 'Node.js & Express', 'RAG Architecture & LangChain'],
      4: ['RESTful APIs', 'Spring Data JPA & Hibernate', 'Scikit-Learn', 'Chunking, Reranking & Retrieval Optimization'],
      5: ['Spring Security & JWT', 'Docker & Containers', 'Deep Learning & PyTorch', 'LLM Evaluation & Guardrails'],
      6: ['System Design & Microservices', 'Cloud Infrastructure & Kubernetes', 'Model Deployment & FastAPI']
    };

    // Calculate node coordinates
    const nodes = [];
    const skillMap = new Map();
    skills.forEach(s => skillMap.set(s.skillName.toLowerCase(), s));

    // Also include any user skills that might be outside default tiers into Tier 3
    const knownSkills = new Set();
    Object.values(baseTiers).forEach(list => list.forEach(name => knownSkills.add(name.toLowerCase())));

    const customSkills = [];
    skills.forEach(s => {
      if (!knownSkills.has(s.skillName.toLowerCase())) {
        customSkills.push(s.skillName);
      }
    });

    const activeTiers = { ...baseTiers };
    if (customSkills.length > 0) {
      activeTiers[3] = [...activeTiers[3], ...customSkills];
    }

    Object.entries(activeTiers).forEach(([tierStr, skillNames]) => {
      const tier = parseInt(tierStr, 10);
      const x = 90 + tier * 190;
      const count = skillNames.length;
      
      skillNames.forEach((name, idx) => {
        const y = 70 + idx * 95 + (count === 1 ? 60 : count === 2 ? 30 : 0);
        const userSkill = skillMap.get(name.toLowerCase()) || {
          skillName: name,
          currentProficiency: 0,
          requiredProficiency: 75,
          status: 'MISSING',
          category: 'GENERAL'
        };

        nodes.push({
          id: name,
          name,
          x,
          y,
          tier,
          ...userSkill
        });
      });
    });

    return nodes;
  }, [skills]);

  // Edges representing directed prerequisite relationships
  const edges = useMemo(() => {
    const deps = [
      { from: 'Object-Oriented Programming (OOP)', to: 'Java' },
      { from: 'Java', to: 'Data Structures & Algorithms' },
      { from: 'Java', to: 'Spring Boot' },
      { from: 'SQL & Relational Databases', to: 'Spring Data JPA & Hibernate' },
      { from: 'Spring Boot', to: 'RESTful APIs' },
      { from: 'Spring Boot', to: 'Spring Data JPA & Hibernate' },
      { from: 'RESTful APIs', to: 'Spring Security & JWT' },
      { from: 'Spring Security & JWT', to: 'Docker & Containers' },
      { from: 'Docker & Containers', to: 'System Design & Microservices' },
      { from: 'RESTful APIs', to: 'System Design & Microservices' },
      { from: 'JavaScript (ES6+)', to: 'React.js' },
      { from: 'Python Programming', to: 'NumPy & Pandas' },
      { from: 'Python Programming', to: 'Prompt Engineering & LLM APIs' },
      { from: 'Python Programming', to: 'Foundations of AI & ML' },
      { from: 'Foundations of AI & ML', to: 'Vector Databases & Embeddings' },
      { from: 'Vector Databases & Embeddings', to: 'RAG Architecture & LangChain' },
      { from: 'Prompt Engineering & LLM APIs', to: 'RAG Architecture & LangChain' },
      { from: 'RAG Architecture & LangChain', to: 'Chunking, Reranking & Retrieval Optimization' },
      { from: 'RAG Architecture & LangChain', to: 'LLM Evaluation & Guardrails' },
      { from: 'NumPy & Pandas', to: 'Scikit-Learn' },
      { from: 'Scikit-Learn', to: 'Deep Learning & PyTorch' },
      { from: 'Deep Learning & PyTorch', to: 'Model Deployment & FastAPI' },
      { from: 'Docker & Containers', to: 'Cloud Infrastructure & Kubernetes' }
    ];

    const nodeMap = new Map(graphNodes.map(n => [n.name, n]));
    return deps
      .filter(d => nodeMap.has(d.from) && nodeMap.has(d.to))
      .map(d => ({
        fromNode: nodeMap.get(d.from),
        toNode: nodeMap.get(d.to)
      }));
  }, [graphNodes]);

  const getNodeColor = (status, proficiency = 0) => {
    if (status === 'MASTERED' || proficiency >= 70) {
      return {
        bg: '#064e3b',
        border: '#10b981',
        text: '#a7f3d0',
        glow: 'rgba(16, 185, 129, 0.4)'
      };
    }
    if (status === 'IN_PROGRESS' || proficiency > 20) {
      return {
        bg: '#0c4a6e',
        border: '#0284c7',
        text: '#bae6fd',
        glow: 'rgba(2, 132, 199, 0.4)'
      };
    }
    return {
      bg: '#1e293b',
      border: '#475569',
      text: '#94a3b8',
      glow: 'none'
    };
  };

  return (
    <div className="relative glass-panel rounded-3xl border border-slate-800 p-6 overflow-hidden">
      {/* Visualizer Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6 pb-4 border-b border-slate-800">
        <div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Compass className="w-5 h-5 text-sky-400" />
            Topological Skill Competency DAG
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            Directed Acyclic Graph with prerequisite dependency paths and proficiency states. Click any skill node to inspect.
          </p>
        </div>

        {/* Legend */}
        <div className="flex items-center gap-3 text-[11px]">
          <span className="flex items-center gap-1.5 text-emerald-400 font-medium">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 shadow-sm" /> Mastered (&ge;70%)
          </span>
          <span className="flex items-center gap-1.5 text-sky-400 font-medium">
            <span className="w-2.5 h-2.5 rounded-full bg-sky-500 shadow-sm" /> In Progress (20-69%)
          </span>
          <span className="flex items-center gap-1.5 text-slate-400 font-medium">
            <span className="w-2.5 h-2.5 rounded-full bg-slate-600" /> Missing / Locked
          </span>
        </div>
      </div>

      {/* SVG Canvas with Horizontal Scroll */}
      <div className="overflow-x-auto pb-4 no-scrollbar">
        <div className="min-w-[1350px] h-[480px] relative bg-slate-950/70 rounded-2xl border border-slate-900 overflow-hidden select-none">
          {/* Grid Background Lines */}
          <svg className="absolute inset-0 w-full h-full pointer-events-none opacity-20">
            <defs>
              <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
                <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#334155" strokeWidth="0.5" />
              </pattern>
            </defs>
            <rect width="100%" height="100%" fill="url(#grid)" />
          </svg>

          {/* Directed Edges */}
          <svg className="absolute inset-0 w-full h-full pointer-events-none">
            <defs>
              <marker
                id="arrowhead"
                markerWidth="8"
                markerHeight="6"
                refX="8"
                refY="3"
                orient="auto"
              >
                <polygon points="0 0, 8 3, 0 6" fill="#475569" />
              </marker>
              <marker
                id="arrowhead-active"
                markerWidth="8"
                markerHeight="6"
                refX="8"
                refY="3"
                orient="auto"
              >
                <polygon points="0 0, 8 3, 0 6" fill="#0284c7" />
              </marker>
            </defs>

            {edges.map((edge, idx) => {
              const active = (edge.fromNode.currentProficiency || 0) >= 50;
              return (
                <g key={idx}>
                  <path
                    d={`M ${edge.fromNode.x + 60} ${edge.fromNode.y} C ${edge.fromNode.x + 110} ${edge.fromNode.y}, ${edge.toNode.x - 110} ${edge.toNode.y}, ${edge.toNode.x - 60} ${edge.toNode.y}`}
                    fill="none"
                    stroke={active ? '#0284c7' : '#334155'}
                    strokeWidth={active ? 2 : 1.5}
                    strokeDasharray={active ? 'none' : '4 4'}
                    markerEnd={active ? 'url(#arrowhead-active)' : 'url(#arrowhead)'}
                  />
                </g>
              );
            })}
          </svg>

          {/* Interactive Nodes */}
          {graphNodes.map((node) => {
            const colors = getNodeColor(node.status, node.currentProficiency);
            const isSelected = selectedSkill?.name === node.name;

            return (
              <div
                key={node.id}
                onClick={() => {
                  setSelectedSkill(node);
                  if (onSelectSkill) onSelectSkill(node);
                }}
                style={{
                  left: `${node.x}px`,
                  top: `${node.y}px`,
                  transform: 'translate(-50%, -50%)',
                  backgroundColor: colors.bg,
                  borderColor: isSelected ? '#38bdf8' : colors.border,
                  boxShadow: isSelected ? '0 0 20px rgba(56, 189, 248, 0.6)' : colors.glow,
                }}
                className={`absolute w-36 py-2.5 px-3 rounded-2xl border-2 cursor-pointer transition-all duration-200 hover:scale-105 z-10 flex flex-col justify-between`}
              >
                <div className="flex items-center justify-between text-[10px] mb-1">
                  <span className="font-mono text-slate-400">T-{node.tier + 1}</span>
                  <span className="font-bold text-white font-mono">
                    {node.currentProficiency || 0}%
                  </span>
                </div>
                <div className="text-xs font-bold text-slate-100 line-clamp-2 leading-tight">
                  {node.name}
                </div>
                <div className="w-full bg-slate-900/80 h-1 rounded-full overflow-hidden mt-1.5">
                  <div
                    className="h-full bg-sky-400 rounded-full"
                    style={{ width: `${node.currentProficiency || 0}%` }}
                  />
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Selected Skill Drawer Modal */}
      {selectedSkill && (
        <div className="mt-4 p-5 bg-slate-900 border border-slate-800 rounded-2xl flex flex-col md:flex-row md:items-center justify-between gap-4 animate-fade-in">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <h4 className="text-sm font-bold text-white">{selectedSkill.name}</h4>
              <Badge
                variant={
                  selectedSkill.status === 'MASTERED' || selectedSkill.currentProficiency >= 70
                    ? 'success'
                    : selectedSkill.currentProficiency > 20
                    ? 'brand'
                    : 'default'
                }
                size="sm"
              >
                {selectedSkill.status || (selectedSkill.currentProficiency >= 70 ? 'MASTERED' : 'IN_PROGRESS')}
              </Badge>
            </div>
            <p className="text-xs text-slate-400">
              Proficiency: <strong className="text-sky-400">{selectedSkill.currentProficiency || 0}%</strong> / Required: {selectedSkill.requiredProficiency || 75}%
            </p>
          </div>

          <div className="flex items-center gap-3">
            <Button
              variant="outline"
              size="sm"
              icon={X}
              onClick={() => setSelectedSkill(null)}
            >
              Close
            </Button>
            <Button
              variant="primary"
              size="sm"
              icon={BookOpen}
              onClick={() => {
                window.location.href = `/app/recommendations`;
              }}
            >
              Find Practice Modules
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};
