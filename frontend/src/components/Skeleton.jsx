export default function Skeleton({ width = '100%', height = '20px', radius = 'var(--radius-md)', style = {} }) {
  return (
    <div className="animate-shimmer" style={{
      width, height, borderRadius: radius,
      background: 'var(--bg-tertiary)',
      ...style,
    }} />
  );
}

export function SkeletonCard() {
  return (
    <div className="glass-card" style={{ padding: '24px' }}>
      <Skeleton width="40%" height="14px" style={{ marginBottom: '16px' }} />
      <Skeleton width="60%" height="32px" style={{ marginBottom: '12px' }} />
      <Skeleton width="80%" height="14px" />
    </div>
  );
}

export function SkeletonTable({ rows = 5, cols = 4 }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} style={{ display: 'flex', gap: '16px', padding: '12px 0' }}>
          {Array.from({ length: cols }).map((_, j) => (
            <Skeleton key={j} width={`${100 / cols}%`} height="16px" />
          ))}
        </div>
      ))}
    </div>
  );
}

export function SkeletonMetric() {
  return (
    <div className="glass-card" style={{ padding: '24px', position: 'relative', overflow: 'hidden' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
        <Skeleton width="40%" height="16px" />
        <Skeleton width="32px" height="32px" radius="8px" />
      </div>
      <Skeleton width="60%" height="32px" />
    </div>
  );
}
