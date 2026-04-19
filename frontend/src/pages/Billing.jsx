import { useState, useEffect } from 'react';
import { CheckCircle } from 'lucide-react';
import ProgressRing from '../components/ProgressRing';
import api from '../utils/api';
import { useToast } from '../components/Toast';

export default function Billing() {
  const [billing, setBilling] = useState(null);
  const [loading, setLoading] = useState(true);
  const [upgrading, setUpgrading] = useState(false);
  const { success, error: showError } = useToast();

  const fetchBilling = async () => {
    try {
      const { data } = await api.get('/api/billing/current');
      setBilling(data);
    } catch (err) {
      console.error('Failed to fetch billing data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBilling();
  }, []);

  const handleUpgrade = async (planType) => {
    if (billing?.currentPlan === planType) return;
    
    setUpgrading(true);
    try {
      const { data } = await api.post('/api/billing/upgrade', { plan: planType });
      setBilling(data);
      success(`Successfully upgraded to ${planType} plan!`);
    } catch (err) {
      showError('Failed to upgrade plan. Please try again.');
    } finally {
      setUpgrading(false);
    }
  };

  if (loading) return <div style={{ padding: '40px' }}>Loading billing info...</div>;

  const getPlanName = (type) => {
    switch (type) {
      case 'FREE': return 'Free Tier';
      case 'STARTER': return 'Starter Pro';
      case 'PROFESSIONAL': return 'Professional';
      case 'ENTERPRISE': return 'Enterprise';
      default: return type;
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Billing & Plans</h1>
        <p className="page-subtitle">Manage your subscription and monthly call limits.</p>
      </div>

      <div className="glass-card animate-fade-up" style={{ padding: '32px', marginBottom: '32px', display: 'flex', alignItems: 'center', gap: '32px', borderLeft: '4px solid var(--accent-purple)' }}>
         <div style={{ flex: 1 }}>
           <div style={{ textTransform: 'uppercase', fontSize: '0.75rem', fontWeight: 700, color: 'var(--accent-purple)', letterSpacing: '0.05em', marginBottom: '8px' }}>Current Plan</div>
           <h2 style={{ fontSize: '2rem', fontWeight: 800, marginBottom: '8px' }}>{getPlanName(billing?.currentPlan)}</h2>
           <p style={{ color: 'var(--text-secondary)' }}>You are currently on the monthly {getPlanName(billing?.currentPlan)} tier. Your quota resets on the 1st of every month.</p>
         </div>
         <div style={{ flexShrink: 0, display: 'flex', alignItems: 'center', gap: '24px' }}>
           <div style={{ textAlign: 'right' }}>
             <div style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Monthly Call Quota</div>
             <div style={{ fontSize: '1.5rem', fontWeight: 700 }}>{billing?.callsUsedThisMonth} <span style={{ color: 'var(--text-secondary)', fontSize: '1rem' }}>/ {billing?.monthlyCallLimit}</span></div>
             <div style={{ color: 'var(--accent-primary)', fontSize: '0.75rem', fontWeight: 600 }}>{billing?.remainingCalls} Calls Remaining</div>
           </div>
           <ProgressRing progress={billing?.usagePercent || 0} size={100} strokeWidth={8} color="var(--accent-purple)" />
         </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '24px' }}>
        <PlanCard 
          title="Free Tier" calls="100" price="₹0" 
          features={['100 AI Calls', 'Community Support', 'Basic Analytics', 'Standard Hindi Voice']} 
          planType="FREE" currentPlan={billing?.currentPlan} onUpgrade={handleUpgrade} disabled={upgrading}
        />
        <PlanCard 
          title="Starter Pro" calls="1,000" price="₹1,899" popular 
          features={['1,000 AI Calls', 'Priority Support', 'Advanced Analytics', 'Premium ElevenLabs Voice', 'Live Call Transfers']} 
          planType="STARTER" currentPlan={billing?.currentPlan} onUpgrade={handleUpgrade} disabled={upgrading}
        />
        <PlanCard 
          title="Enterprise" calls="Unlimited" price="Custom" 
          features={['Unlimited Calls', 'Dedicated Account Manager', 'Custom Voice Training', 'API Access', 'Custom Integrations']} 
          planType="ENTERPRISE" currentPlan={billing?.currentPlan} onUpgrade={handleUpgrade} disabled={upgrading}
        />
      </div>
    </div>
  );
}

function PlanCard({ title, price, calls, features, popular, planType, currentPlan, onUpgrade, disabled }) {
  const isCurrent = currentPlan === planType;

  return (
    <div className="glass-card animate-scale-in" style={{ 
      padding: '32px', display: 'flex', flexDirection: 'column',
      border: popular && !isCurrent ? '1px solid var(--accent-primary)' : isCurrent ? '2px solid var(--accent-purple)' : '1px solid var(--border-subtle)',
      position: 'relative', overflow: 'hidden'
    }}>
      {popular && !isCurrent && (
        <div style={{ position: 'absolute', top: '12px', right: '-30px', background: 'var(--gradient-primary)', color: 'white', padding: '4px 32px', transform: 'rotate(45deg)', fontSize: '0.625rem', fontWeight: 700, letterSpacing: '0.05em' }}>MOST POPULAR</div>
      )}
      {isCurrent && (
        <div style={{ position: 'absolute', top: '12px', right: '-30px', background: 'var(--accent-purple)', color: 'white', padding: '4px 32px', transform: 'rotate(45deg)', fontSize: '0.625rem', fontWeight: 700, letterSpacing: '0.05em' }}>CURRENT PLAN</div>
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
      
      <button 
        className={isCurrent ? 'btn btn-secondary' : popular ? 'btn btn-primary' : 'btn btn-secondary'} 
        style={{ width: '100%', opacity: (isCurrent || disabled) ? 0.7 : 1, cursor: isCurrent ? 'default' : 'pointer' }}
        onClick={() => !isCurrent && !disabled && onUpgrade(planType)}
        disabled={isCurrent || disabled}
      >
        {isCurrent ? 'Active Plan' : disabled ? 'Upgrading...' : 'Upgrade Plan'}
      </button>
    </div>
  );
}
