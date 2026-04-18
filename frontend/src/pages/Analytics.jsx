import { useState, useEffect } from 'react';
import MetricCard from '../components/MetricCard';
import { IndianRupee, TrendingUp, Users, Target } from 'lucide-react';

export default function Analytics() {
  const [mounted, setMounted] = useState(false);
  useEffect(() => { setMounted(true); }, []);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Analytics & Insights</h1>
        <p className="page-subtitle">Track your AI agent's performance and value generated.</p>
      </div>

      <div className="grid-4" style={{ marginBottom: '32px' }}>
        <MetricCard title="Total DSC Collected" value={4520} icon={<Target />} delay={0} />
        <MetricCard title="Total Calls Made" value={5100} icon={<Users />} accent="blue" delay={100} />
        <MetricCard title="Avg. Success Rate" value={88} suffix="%" icon={<TrendingUp />} accent="purple" delay={200} />
        <MetricCard title="Money Saved" value="48,000" prefix="₹" icon={<IndianRupee />} accent="amber" delay={300} />
      </div>

      <div className="grid-2">
        <div className="glass-card animate-fade-up" style={{ padding: '24px', animationDelay: '200ms' }}>
          <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '24px' }}>Cost Savings Calculator</h3>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
             <div style={{ padding: '16px', borderRadius: 'var(--radius-md)', background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.2)' }}>
               <div style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>Human Callers (Estimates)</div>
               <div style={{ color: 'var(--accent-red)', fontSize: '1.5rem', fontWeight: 700 }}>₹ 75,000 / mo</div>
             </div>
             
             <div style={{ padding: '16px', borderRadius: 'var(--radius-md)', background: 'rgba(56, 189, 248, 0.1)', border: '1px solid rgba(56, 189, 248, 0.2)' }}>
               <div style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>ElevenLabs AI Output</div>
               <div style={{ color: 'var(--accent-blue)', fontSize: '1.5rem', fontWeight: 700 }}>₹ 22,000 / mo</div>
             </div>

             <div style={{ padding: '20px', borderRadius: 'var(--radius-md)', background: 'var(--gradient-primary)', marginTop: '8px' }}>
               <div style={{ color: 'rgba(255,255,255,0.8)', fontSize: '0.875rem', fontWeight: 600 }}>Total Savings</div>
               <div style={{ color: 'white', fontSize: '2rem', fontWeight: 800 }}>₹ 53,000</div>
             </div>
          </div>
        </div>

        <div className="glass-card animate-fade-up" style={{ padding: '24px', animationDelay: '300ms' }}>
          <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '24px' }}>Top Campaigns</h3>
          <table className="data-table">
            <thead>
              <tr><th>Campaign Name</th><th>Success Rate</th><th>Volume</th></tr>
            </thead>
            <tbody>
               <tr>
                 <td style={{ fontWeight: 600 }}>March Delivery Area 1</td>
                 <td style={{ color: 'var(--accent-primary)', fontWeight: 700 }}>92%</td>
                 <td>1,200</td>
               </tr>
               <tr>
                 <td style={{ fontWeight: 600 }}>March Delivery Area 2</td>
                 <td style={{ color: 'var(--accent-primary)', fontWeight: 700 }}>89%</td>
                 <td>850</td>
               </tr>
               <tr>
                 <td style={{ fontWeight: 600 }}>Feb End Clearing</td>
                 <td style={{ color: 'var(--accent-amber)', fontWeight: 700 }}>74%</td>
                 <td>2,100</td>
               </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
