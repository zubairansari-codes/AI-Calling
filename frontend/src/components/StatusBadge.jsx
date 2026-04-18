import { statusColors } from '../utils/format';

export default function StatusBadge({ status }) {
  const config = statusColors[status] || { bg: 'rgba(148,163,184,0.15)', color: 'var(--text-secondary)', label: status };
  const isLive = status === 'IN_PROGRESS' || status === 'RUNNING';

  return (
    <span className="badge" style={{ background: config.bg, color: config.color }}>
      {isLive && (
        <span className="animate-pulse-dot" style={{
          width: '6px', height: '6px', borderRadius: '50%',
          background: config.color, display: 'inline-block',
        }} />
      )}
      {config.label}
    </span>
  );
}
