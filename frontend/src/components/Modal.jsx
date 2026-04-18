import { X } from 'lucide-react';

export default function Modal({ isOpen, onClose, title, children, width = '480px' }) {
  if (!isOpen) return null;

  return (
    <div style={{
      position: 'fixed', inset: 0, zIndex: 1000,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      padding: '20px', paddingLeft: 'calc(var(--sidebar-width) + 20px)',
    }} onClick={onClose}>
      {/* Backdrop */}
      <div style={{
        position: 'absolute', inset: 0,
        background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(8px)',
      }} />

      {/* Content */}
      <div className="animate-scale-in" onClick={e => e.stopPropagation()} style={{
        position: 'relative', width: '100%', maxWidth: width,
        background: 'var(--bg-secondary)', border: '1px solid var(--border-medium)',
        borderRadius: 'var(--radius-xl)', padding: '28px',
        boxShadow: 'var(--shadow-lg)', maxHeight: '90vh', overflow: 'auto',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
          <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>{title}</h3>
          <button onClick={onClose} className="btn-ghost" style={{ padding: '6px', borderRadius: 'var(--radius-sm)' }}>
            <X size={18} />
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}
