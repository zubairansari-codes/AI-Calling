import { useState, useEffect } from 'react';
import MetricCard from '../components/MetricCard';
import { SkeletonTable, SkeletonMetric } from '../components/Skeleton';
import StatusBadge from '../components/StatusBadge';
import { Users, Activity, Banknote, ShieldAlert, Check, X } from 'lucide-react';
import { useToast } from '../components/Toast';
import api from '../utils/api';
import { formatDateTime } from '../utils/format';

export default function Admin() {
  const [stats, setStats] = useState(null);
  const [agencies, setAgencies] = useState([]);
  const [loading, setLoading] = useState(true);
  const { success, error: showError } = useToast();

  const fetchAdminData = async () => {
    try {
      const [statsRes, agenciesRes] = await Promise.all([
        api.get('/admin/stats'),
        api.get('/admin/agencies?size=100')
      ]);
      setStats(statsRes.data);
      setAgencies(agenciesRes.data.content || agenciesRes.data);
    } catch (err) {
      if (err.response?.status === 403) {
        showError('You do not have permission to view the admin console.');
      } else {
        showError('Failed to load admin data');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAdminData();
  }, []);

  const handleStatusChange = async (id, currentStatus) => {
    try {
      await api.put(`/admin/agencies/${id}/status?active=${!currentStatus}`);
      success(`Agency ${!currentStatus ? 'activated' : 'deactivated'}`);
      fetchAdminData();
    } catch (err) {
      showError('Failed to update agency status');
    }
  };

  const handlePlanChange = async (id, currentPlan) => {
    const plans = ['FREE', 'STARTER', 'PROFESSIONAL', 'ENTERPRISE'];
    const nextPlan = plans[(plans.indexOf(currentPlan) + 1) % plans.length];
    
    try {
      await api.put(`/admin/agencies/${id}/plan?plan=${nextPlan}`);
      success(`Plan upgraded to ${nextPlan}`);
      fetchAdminData();
    } catch (err) {
      showError('Failed to change plan');
    }
  };

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
        {loading ? (
          <><SkeletonMetric /><SkeletonMetric /><SkeletonMetric /><SkeletonMetric /></>
        ) : (
          <>
            <MetricCard title="Total Agencies" value={stats?.totalAgencies || 0} icon={<Users />} delay={0} />
            <MetricCard title="Active Campaigns" value={stats?.activeCampaigns || 0} icon={<Activity />} accent="blue" delay={100} />
            <MetricCard title="Total Calls Processed" value={stats?.totalCalls || 0} icon={<Banknote />} accent="green" delay={200} />
            <MetricCard title="Total DSC Collected" value={stats?.totalDsc || 0} icon={<ShieldAlert />} accent="purple" delay={300} />
          </>
        )}
      </div>

      <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '24px' }} className="animate-fade-up">Registered Agencies</h3>
      
      <div className="glass-card animate-fade-up" style={{ padding: 0, overflow: 'hidden', animationDelay: '200ms' }}>
        {loading ? (
          <div style={{ padding: '24px' }}><SkeletonTable rows={5} /></div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Agency Name</th>
                <th>Owner & Contact</th>
                <th>Plan / Usage</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {agencies.map((agency, i) => (
                <tr key={agency.id} className="animate-fade-in" style={{ animationDelay: `${i * 50}ms` }}>
                  <td>
                    <div style={{ fontWeight: 600 }}>{agency.name}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{formatDateTime(agency.createdAt)}</div>
                  </td>
                  <td>
                    <div style={{ fontWeight: 500 }}>{agency.ownerName}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{agency.email} • {agency.phone}</div>
                  </td>
                  <td>
                    <div 
                      onClick={() => handlePlanChange(agency.id, agency.planType)}
                      style={{ display: 'inline-block', padding: '2px 8px', borderRadius: '4px', background: 'var(--bg-tertiary)', fontSize: '0.75rem', fontWeight: 600, color: 'var(--accent-purple)', marginBottom: '4px', cursor: 'pointer' }}
                      title="Click to cycle plans"
                    >
                      {agency.planType}
                    </div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Used: {agency.callsUsedThisMonth} / {agency.monthlyCallLimit}</div>
                  </td>
                  <td>
                    <StatusBadge status={agency.setupCompleted ? 'COMPLETED' : 'PENDING'} />
                    <div style={{ marginTop: '4px' }}>
                      <span style={{ fontSize: '0.75rem', color: agency.active ? 'var(--accent-green)' : 'var(--accent-red)' }}>
                        {agency.active ? '● Active' : '● Disabled'}
                      </span>
                    </div>
                  </td>
                  <td>
                    <button 
                      onClick={() => handleStatusChange(agency.id, agency.active)}
                      className="btn btn-secondary" 
                      style={{ padding: '6px 12px', fontSize: '0.75rem' }}
                    >
                      {agency.active ? 'Disable' : 'Enable'}
                    </button>
                  </td>
                </tr>
              ))}
              {agencies.length === 0 && (
                <tr><td colSpan="5" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>No agencies registered.</td></tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
