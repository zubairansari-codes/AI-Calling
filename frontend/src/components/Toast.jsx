import { useState, useCallback, useMemo, createContext, useContext } from 'react';
import { X, CheckCircle, AlertCircle, Info } from 'lucide-react';

const ToastContext = createContext(null);

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const addToast = useCallback((message, type = 'info', duration = 4000) => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, message, type }]);
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), duration);
  }, []);

  const success = useCallback((msg) => addToast(msg, 'success'), [addToast]);
  const error = useCallback((msg) => addToast(msg, 'error'), [addToast]);
  const info = useCallback((msg) => addToast(msg, 'info'), [addToast]);

  // Memoize the context value so consumers don't re-render when toasts change.
  // Without this, every toast appearance/dismissal re-renders ALL useToast() consumers,
  // causing input fields across the app to lose focus after typing one character.
  const contextValue = useMemo(() => ({ success, error, info }), [success, error, info]);

  return (
    <ToastContext.Provider value={contextValue}>
      {children}
      <div style={{ position: 'fixed', top: '20px', right: '20px', zIndex: 9999, display: 'flex', flexDirection: 'column', gap: '8px' }}>
        {toasts.map((toast) => (
          <ToastItem key={toast.id} toast={toast} onClose={() => setToasts(prev => prev.filter(t => t.id !== toast.id))} />
        ))}
      </div>
    </ToastContext.Provider>
  );
}

function ToastItem({ toast, onClose }) {
  const icons = { success: <CheckCircle size={18} />, error: <AlertCircle size={18} />, info: <Info size={18} /> };
  const colors = {
    success: { bg: 'var(--accent-primary-dim)', border: 'var(--accent-primary)', color: 'var(--accent-primary)' },
    error: { bg: 'var(--accent-red-dim)', border: 'var(--accent-red)', color: 'var(--accent-red)' },
    info: { bg: 'var(--accent-blue-dim)', border: 'var(--accent-blue)', color: 'var(--accent-blue)' },
  };
  const c = colors[toast.type] || colors.info;

  return (
    <div className="animate-slide-in-right" style={{
      background: 'var(--bg-secondary)', border: `1px solid ${c.border}`,
      borderRadius: 'var(--radius-md)', padding: '12px 16px', minWidth: '300px', maxWidth: '420px',
      display: 'flex', alignItems: 'center', gap: '10px', boxShadow: 'var(--shadow-lg)',
      backdropFilter: 'blur(12px)',
    }}>
      <span style={{ color: c.color, flexShrink: 0 }}>{icons[toast.type]}</span>
      <span style={{ flex: 1, fontSize: '0.875rem', color: 'var(--text-primary)' }}>{toast.message}</span>
      <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', padding: '2px', flexShrink: 0 }}>
        <X size={14} />
      </button>
    </div>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
}
