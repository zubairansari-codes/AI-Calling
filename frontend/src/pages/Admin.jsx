import { useState } from 'react';
import MetricCard from '../components/MetricCard';
import { SkeletonTable } from '../components/Skeleton';
import StatusBadge from '../components/StatusBadge';
import { Users, Activity, Banknote, ShieldAlert } from 'lucide-react';
import { mockAgencies } from '../utils/mockData';

export default function Admin() {
  const [agencies] = useState(mockAgencies || []);

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
      <div className="page-header" style={{ borderBottom: '1px solid var(--border-subtle)', paddingBottom: '24px', marginBottom: '32px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <ShieldAlert size={28} color="var(--accent-primary)" />
          <div>
            <h1 className="page-title" style={{ margin: 0 }}>Super Admin Console</h1>
            <p className="page-subtitle" style={{ margin: 0 }}>System overview and agency tenant management.</p>
          </div>
        </div>
      </div>

      <div className="grid-4" style={{ marginBottom: '32px' }}>
        <MetricCard title="Total Agencies" value={14} icon={<Users />} delay={0} />
        <MetricCard title="Total Calls Processed" value={125430} icon={<Activity />} accent="blue" delay={100} />
        <MetricCard title="MRR (Monthly)" value="45,000" prefix="₹" icon={<Banknote />} accent="green" delay={200} />
        <MetricCard title="System Uptime" value={99.9} suffix="%" icon={<ShieldAlert />} accent="purple" delay={300} decimals={1} />
      </div>

      <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '24px' }} className="animate-fade-up">Registered Agencies</h3>
      
      <div className="glass-card animate-fade-up" style={{ padding: 0, overflow: 'hidden', animationDelay: '200ms' }}>
        <table className="data-table">
          <thead>
            <tr>
              <th>Agency Name</th>
              <th>Owner & Contact</th>
              <th>Plan / Usage</th>
              <th>Status</th>
              <th>Joined Date</th>
            </tr>
          </thead>
          <tbody>
            {agencies.map((agency, i) => (
              <tr key={agency.id} className="animate-fade-in clickable" style={{ animationDelay: `${i * 50}ms` }}>
                <td>
                  <div style={{ fontWeight: 600 }}>{agency.name}</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{agency.city}</div>
                </td>
                <td>
                  <div style={{ fontWeight: 500 }}>{agency.ownerName}</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{agency.email} • {agency.phone}</div>
                </td>
                <td>
                  <div style={{ display: 'inline-block', padding: '2px 8px', borderRadius: '4px', background: 'var(--bg-tertiary)', fontSize: '0.75rem', fontWeight: 600, color: 'var(--accent-purple)', marginBottom: '4px' }}>
                    {agency.plan}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Used: {agency.usageThisMonth} calls</div>
                </td>
                <td>
                  <StatusBadge status={agency.isActive ? 'RUNNING' : 'PAUSED'} />
                </td>
                <td style={{ color: 'var(--text-secondary)' }}>{agency.createdAt}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
