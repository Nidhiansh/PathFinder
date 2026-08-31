import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { chatService } from '../services/chatService';
import {
  Bot, Send, Sparkles, RefreshCw, User, ArrowRight, CheckCircle2,
  Clock, Compass, BookOpen, TrendingUp, Layers
} from 'lucide-react';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';

export const AssistantPage = () => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [quickReplies, setQuickReplies] = useState([
    "What should I learn next?",
    "Why did you recommend Spring Boot?",
    "Can I skip SQL?",
    "I only have 5 hours this week"
  ]);
  const messagesEndRef = useRef(null);
  const navigate = useNavigate();

  const fetchHistory = async () => {
    try {
      const data = await chatService.getChatHistory();
      if (data && data.length > 0) {
        setMessages(data);
      } else {
        setMessages([
          {
            id: 0,
            sender: 'ASSISTANT',
            message: "Hello! I am your AI Learning Copilot. I analyze your profile, active roadmap phases, and checkpoint scores to answer queries, explain recommendation reasoning, or adjust your study schedule.",
          }
        ]);
      }
    } catch {
      setMessages([
        {
          id: 0,
          sender: 'ASSISTANT',
          message: "Hello! I'm your AI Learning Copilot. How can I optimize your learning journey today?",
        }
      ]);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  const handleSend = async (textToSend) => {
    const text = textToSend || input;
    if (!text || !text.trim()) return;

    const userMsg = { id: Date.now(), sender: 'USER', message: text };
    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setLoading(true);

    try {
      const response = await chatService.sendMessage(text);
      const botMsg = {
        id: Date.now() + 1,
        sender: 'ASSISTANT',
        message: response.reply,
        actionType: response.actionType,
        actionPayload: response.actionPayload,
      };
      setMessages((prev) => [...prev, botMsg]);
      if (response.quickReplies?.length > 0) {
        setQuickReplies(response.quickReplies);
      }
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        { id: Date.now() + 1, sender: 'ASSISTANT', message: "I'm analyzing your roadmap. Please check AI service connectivity." }
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6 animate-fade-in pb-16">
      <div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight flex items-center gap-2.5">
          Conversational AI Copilot <Bot className="w-7 h-7 text-sky-400" />
        </h1>
        <p className="text-xs sm:text-sm text-slate-400 mt-1">
          Chat directly with your learning assistant to inquire about recommendations, prerequisite rationale, or roadmap adaptation.
        </p>
      </div>

      <Card className="glass-panel border-slate-800 h-[640px] flex flex-col p-0 overflow-hidden shadow-2xl">
        {/* Chat History */}
        <div className="flex-1 p-6 overflow-y-auto space-y-4 text-xs sm:text-sm">
          {messages.map((m) => (
            <div
              key={m.id}
              className={`flex gap-3 ${m.sender === 'USER' ? 'justify-end' : 'justify-start'}`}
            >
              {m.sender === 'ASSISTANT' && (
                <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-blue-600 to-sky-500 flex items-center justify-center text-white shrink-0 shadow-md">
                  <Bot className="w-4 h-4" />
                </div>
              )}
              <div
                className={`max-w-[85%] rounded-2xl px-4 py-3.5 leading-relaxed space-y-2.5 ${
                  m.sender === 'USER'
                    ? 'bg-blue-600 text-white rounded-br-none shadow-md'
                    : 'bg-slate-900 border border-slate-800 text-slate-200 rounded-bl-none shadow-sm'
                }`}
              >
                <div className="whitespace-pre-wrap">{m.message}</div>

                {/* Structured Action Card in Chat Bubble */}
                {m.actionPayload && m.actionPayload.url && (
                  <div className="pt-2 border-t border-slate-800/80">
                    <button
                      type="button"
                      onClick={() => navigate(m.actionPayload.url)}
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-blue-600/20 hover:bg-blue-600/30 text-sky-300 border border-blue-500/30 text-xs font-semibold transition"
                    >
                      <Sparkles className="w-3.5 h-3.5" />
                      <span>{m.actionPayload.label || 'Open Action'}</span>
                      <ArrowRight className="w-3 h-3" />
                    </button>
                  </div>
                )}

                {m.actionType === 'PACE_ADAPTED' && (
                  <div className="pt-2 border-t border-slate-800/80 flex items-center gap-2 text-emerald-400 text-xs font-semibold">
                    <CheckCircle2 className="w-4 h-4" />
                    <span>Roadmap pacing updated automatically in backend!</span>
                  </div>
                )}
              </div>
            </div>
          ))}

          {loading && (
            <div className="flex gap-3 justify-start">
              <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-blue-600 to-sky-500 flex items-center justify-center text-white shrink-0">
                <Bot className="w-4 h-4" />
              </div>
              <div className="bg-slate-900 border border-slate-800 rounded-2xl rounded-bl-none px-4 py-3 text-xs text-slate-400 flex items-center gap-2">
                <RefreshCw className="w-4 h-4 animate-spin text-sky-400" />
                <span>Analyzing your profile & DAG prerequisites...</span>
              </div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* Quick Suggestions Chips */}
        <div className="px-6 py-2.5 bg-slate-950/80 border-t border-slate-800/80 flex items-center gap-2 overflow-x-auto no-scrollbar">
          {quickReplies.map((q, idx) => (
            <button
              key={idx}
              onClick={() => handleSend(q)}
              className="text-xs px-3 py-1.5 rounded-full bg-slate-900 hover:bg-slate-800 text-sky-300 border border-slate-800 transition whitespace-nowrap"
            >
              {q}
            </button>
          ))}
        </div>

        {/* Input Bar */}
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSend();
          }}
          className="p-4 bg-slate-950 border-t border-slate-800 flex items-center gap-3"
        >
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask about your learning path, why a course was recommended, or adjust your pace..."
            className="flex-1 bg-slate-900 border border-slate-800 rounded-xl px-4 py-3 text-xs sm:text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <Button
            type="submit"
            variant="primary"
            size="md"
            icon={Send}
            disabled={!input.trim() || loading}
          >
            Send
          </Button>
        </form>
      </Card>
    </div>
  );
};
