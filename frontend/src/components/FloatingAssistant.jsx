import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { chatService } from '../services/chatService';
import {
  Bot, Send, X, Sparkles, RefreshCw, ArrowRight, CheckCircle2
} from 'lucide-react';
import { Button } from './ui/Button';

export const FloatingAssistant = ({ isOpen, onClose }) => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [quickReplies, setQuickReplies] = useState([
    "What should I learn next?",
    "Why did you recommend Spring Boot?",
    "I only have 5 hours this week"
  ]);
  const messagesEndRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (isOpen) {
      loadHistory();
    }
  }, [isOpen]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  const loadHistory = async () => {
    try {
      const history = await chatService.getChatHistory();
      if (history && history.length > 0) {
        setMessages(history);
      } else {
        setMessages([
          {
            id: 0,
            sender: 'ASSISTANT',
            message: "Hello! I'm your AI Learning Copilot. I can explain recommendations, adapt your roadmap duration, or answer questions about your skill gaps.",
          }
        ]);
      }
    } catch {
      setMessages([
        {
          id: 0,
          sender: 'ASSISTANT',
          message: "Hello! I'm your AI Learning Copilot. How can I help optimize your roadmap today?",
        }
      ]);
    }
  };

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
      if (response.quickReplies && response.quickReplies.length > 0) {
        setQuickReplies(response.quickReplies);
      }
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now() + 1,
          sender: 'ASSISTANT',
          message: "I'm analyzing your roadmap. (Network error communicating with AI engine).",
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed bottom-6 right-6 w-96 max-w-[calc(100vw-3rem)] h-[560px] bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl z-50 flex flex-col overflow-hidden animate-fade-in">
      {/* Header */}
      <div className="px-4 py-3 bg-slate-950 border-b border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-blue-600 to-sky-500 flex items-center justify-center text-white shadow-md">
            <Bot className="w-4 h-4" />
          </div>
          <div>
            <h4 className="text-xs font-semibold text-white flex items-center gap-1.5">
              AI Learning Copilot
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
            </h4>
            <p className="text-[10px] text-slate-400">Context-Aware Roadmap Assistant</p>
          </div>
        </div>
        <button
          onClick={onClose}
          className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      {/* Messages */}
      <div className="flex-1 p-4 overflow-y-auto space-y-3 text-xs">
        {messages.map((m) => (
          <div
            key={m.id}
            className={`flex ${m.sender === 'USER' ? 'justify-end' : 'justify-start'}`}
          >
            <div
              className={`max-w-[85%] rounded-2xl px-3.5 py-2.5 leading-relaxed space-y-2 ${
                m.sender === 'USER'
                  ? 'bg-blue-600 text-white rounded-br-none'
                  : 'bg-slate-800/90 text-slate-200 border border-slate-700/60 rounded-bl-none'
              }`}
            >
              <div>{m.message}</div>

              {m.actionPayload && m.actionPayload.url && (
                <div className="pt-1.5 border-t border-slate-700/60">
                  <button
                    type="button"
                    onClick={() => {
                      navigate(m.actionPayload.url);
                      onClose();
                    }}
                    className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-blue-600/30 hover:bg-blue-600/40 text-sky-300 border border-blue-500/30 text-[11px] font-semibold transition"
                  >
                    <Sparkles className="w-3 h-3" />
                    <span>{m.actionPayload.label || 'Open View'}</span>
                    <ArrowRight className="w-2.5 h-2.5" />
                  </button>
                </div>
              )}
            </div>
          </div>
        ))}
        {loading && (
          <div className="flex justify-start">
            <div className="bg-slate-800/90 border border-slate-700/60 rounded-2xl rounded-bl-none px-4 py-2.5 text-xs text-slate-400 flex items-center gap-2">
              <RefreshCw className="w-3.5 h-3.5 animate-spin text-sky-400" />
              <span>AI Copilot is reasoning...</span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Quick Replies */}
      <div className="px-3 py-2 bg-slate-950/60 border-t border-slate-800/60 flex items-center gap-1.5 overflow-x-auto no-scrollbar">
        {quickReplies.map((q, idx) => (
          <button
            key={idx}
            onClick={() => handleSend(q)}
            className="text-[11px] px-2.5 py-1 rounded-full bg-slate-800/80 hover:bg-slate-700 text-sky-300 border border-slate-700 transition whitespace-nowrap"
          >
            {q}
          </button>
        ))}
      </div>

      {/* Input */}
      <form
        onSubmit={(e) => {
          e.preventDefault();
          handleSend();
        }}
        className="p-3 bg-slate-950 border-t border-slate-800 flex items-center gap-2"
      >
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask anything about your roadmap..."
          className="flex-1 bg-slate-900 border border-slate-800 rounded-xl px-3.5 py-2 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        />
        <button
          type="submit"
          disabled={!input.trim() || loading}
          className="p-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white disabled:opacity-40 transition"
        >
          <Send className="w-3.5 h-3.5" />
        </button>
      </form>
    </div>
  );
};
