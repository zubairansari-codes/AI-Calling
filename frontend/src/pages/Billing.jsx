import { CheckCircle } from 'lucide-react';
import ProgressRing from '../components/ProgressRing';

export default function Billing() {
  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Billing & Plans</h1>
        <p className="page-subtitle">Manage your subscription and monthly call limits.</p>
      </div>

      <div className="glass-card animate-fade-up" style={{ padding: '32px', marginBottom: '32px', display: 'flex', alignItems: 'center', gap: '32px', borderLeft: '4px solid var(--accent-purple)' }}>
         <div style={{ flex: 1 }}>
           <div style={{ textTransform: 'uppercase', fontSize: '0.75rem', fontWeight: 700, color: 'var(--accent-purple)', letterSpacing: '0.05em', marginBottom: '8px' }}>Current Plan</div>
           <h2 style={{ fontSize: '2rem', fontWeight: 800, marginBottom: '8px' }}>Starter Pro</h2>
           <p style={{ color: 'var(--text-secondary)' }}>You are currently on the monthly Starter Pro tier. Your quota resets on the 1st of every month.</p>
         </div>
         <div style={{ flexShrink: 0, display: 'flex', alignItems: 'center', gap: '24px' }}>
           <div style={{ textAlign: 'right' }}>
             <div style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Monthly Call Quota</div>
             <div style={{ fontSize: '1.5rem', fontWeight: 700 }}>450 <span style={{ color: 'var(--text-secondary)', fontSize: '1rem' }}>/ 1,000</span></div>
             <div style={{ color: 'var(--accent-primary)', fontSize: '0.75rem', fontWeight: 600 }}>550 Calls Remaining</div>
           </div>
           <ProgressRing progress={45} size={100} strokeWidth={8} color="var(--accent-purple)" />
         </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '24px' }}>
        <PlanCard title="Basic" calls="500" price="₹999" features={['500 AI Calls', 'Email Support', 'Basic Analytics', 'Standard Hindi Voice']} />
        <PlanCard title="Starter Pro" popular calls="1,000" price="₹1,899" features={['1,000 AI Calls', 'Priority Support', 'Advanced Analytics', 'Premium ElevenLabs Voice', 'Live Call Transfers']} />
        <PlanCard title="Enterprise" calls="Unlimited" price="Custom" features={['Unlimited Calls', 'Dedicated Account Manager', 'Custom Voice Training', 'API Access', 'Custom Integrations']} />
      </div>
    </div>
  );
}

function PlanCard({ title, price, calls, features, popular }) {
  return (
    <div className="glass-card animate-scale-in" style={{ 
      padding: '32px', display: 'flex', flexDirection: 'column',
      border: popular ? '1px solid var(--accent-primary)' : '1px solid var(--border-subtle)',
      position: 'relative', overflow: 'hidden'
    }}>
      {popular && (
        <div style={{ position: 'absolute', top: '12px', right: '-30px', background: 'var(--gradient-primary)', color: 'white', padding: '4px 32px', transform: 'rotate(45deg)', fontSize: '0.625rem', fontWeight: 700, letterSpacing: '0.05em' }}>MOST POPULAR</div>
      )}
      
      <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '8px' }}>{title}</h3>
      <div style={{ fontSize: '2rem', fontWeight: 800, marginBottom: '24px', display: 'flex', alignItems: 'baseline' }}>
         {price} <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', fontWeight: 500, marginLeft: '4px' }}>/mo</span>
      </div>
      
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '16px', marginBottom: '32px' }}>
        {features.map((f, i) => (
          <div key={i} style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <CheckCircle size={16} color="var(--accent-primary)" />
            <span style={{ fontSize: '0.875rem', color: 'var(--text-primary)' }}>{f}</span>
          </div>
        ))}
      </div>
      
      <button className={popular ? 'btn btn-primary' : 'btn btn-secondary'} style={{ width: '100%' }}>
        {popular ? 'Current Plan' : 'Upgrade Plan'}
      </button>
    </div>
  );
}
