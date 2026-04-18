import AnimatedCounter from './AnimatedCounter';

export default function MetricCard({ title, value, prefix, suffix, decimals, accent = 'primary', icon, delay = 0 }) {
  const accentVar = `var(--accent-${accent})`;
  const accentDim = `var(--accent-${accent}-dim)`;

  return (
    <div
      className="glass-card animate-fade-up"
      style={{
        padding: '24px',
        position: 'relative',
        overflow: 'hidden',
        animationDelay: `${delay}ms`,
      }}
    >
      {/* Background gradient orb */}
      <div style={{
        position: 'absolute', top: '-30px', right: '-30px',
        width: '100px', height: '100px', borderRadius: '50%',
        background: accentDim, filter: 'blur(40px)', opacity: 0.5,
      }} />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px', position: 'relative' }}>
        <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          {title}
        </span>
        {icon && (
          <div style={{
            width: '36px', height: '36px', borderRadius: 'var(--radius-md)',
            background: accentDim, display: 'flex', alignItems: 'center', justifyContent: 'center',
            color: accentVar,
          }}>
            {icon}
          </div>
        )}
      </div>

      <div style={{ fontSize: '2rem', fontWeight: 800, fontFamily: "'Plus Jakarta Sans', sans-serif", color: accentVar, position: 'relative' }}>
        <AnimatedCounter value={value} prefix={prefix} suffix={suffix} decimals={decimals} />
      </div>
    </div>
  );
}
