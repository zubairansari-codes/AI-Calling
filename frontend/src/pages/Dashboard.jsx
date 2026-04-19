import { useState, useEffect } from 'react';
import api from '../utils/api';
import MetricCard from '../components/MetricCard';
import StatusBadge from '../components/StatusBadge';
import ProgressRing from '../components/ProgressRing';
import { SkeletonCard, SkeletonTable } from '../components/Skeleton';
import { Phone, CheckCircle, Clock, AlertCircle } from 'lucide-react';
import { formatTime, formatPercent, formatDuration } from '../utils/format';
import { mockDashboard } from '../utils/mockData';
import { useToast } from '../components/Toast';
import { useAuth } from '../hooks/useAuth';

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const { error } = useToast();
  const { user } = useAuth();

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      const res = await api.get('/api/calls/dashboard');
      setData(res.data);
    } catch (err) {
      console.warn('Dashboard data unavailable:', err);
      // Show real zeros instead of fake mock data
      setData({
        dscCollectedToday: 0, callsMadeToday: 0, transferredToday: 0, successRateToday: 0,
        activeCampaign: null, recentCalls: [],
        totalDscAllTime: 0, totalCallsAllTime: 0, totalCampaigns: 0
      });
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '20px' }}>
          <SkeletonCard /> <SkeletonCard /> <SkeletonCard /> <SkeletonCard />
        </div>
        <div className="glass-card" style={{ padding: '24px' }}>
          <SkeletonTable rows={4} />
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 className="page-title">Dashboard Overview</h1>
          <p className="page-subtitle">Here's what's happening with your AI calls today.</p>
        </div>
        {user?.elevenLabsAgentId && (
          <elevenlabs-convai agent-id={user.elevenLabsAgentId}></elevenlabs-convai>
        )}
      </div>

      <div className="grid-4" style={{ marginBottom: '32px' }}>
        <MetricCard title="DSC Collected Today" value={data.dscCollectedToday} icon={<CheckCircle />} delay={0} />
        <MetricCard title="Calls Made Today" value={data.callsMadeToday} icon={<Phone />} accent="blue" delay={100} />
        <MetricCard title="Transferred" value={data.transferredToday} icon={<AlertCircle />} accent="purple" delay={200} />
        <MetricCard title="Success Rate" value={data.successRateToday} suffix="%" icon={<Clock />} accent="amber" decimals={1} delay={300} />
      </div>

      <div className="grid-2" style={{ marginBottom: '32px' }}>
        {data.activeCampaign ? (
          <div className="glass-card animate-fade-up" style={{ padding: '24px', display: 'flex', alignItems: 'center', gap: '32px' }}>
            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
                <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Active Campaign</h3>
                <StatusBadge status={data.activeCampaign.status} />
              </div>
              <h4 style={{ fontSize: '1.1rem', color: 'var(--text-primary)', marginBottom: '8px' }}>{data.activeCampaign.name}</h4>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '20px' }}>
                Target: {data.activeCampaign.totalCustomers} customers
              </p>
              
              <div style={{ display: 'flex', gap: '24px', fontSize: '0.875rem' }}>
                <div>
                  <div style={{ color: 'var(--text-muted)', marginBottom: '4px' }}>Completed</div>
                  <div style={{ fontWeight: 600, fontSize: '1.125rem' }}>{data.activeCampaign.completedCalls}</div>
                </div>
                <div>
                  <div style={{ color: 'var(--text-muted)', marginBottom: '4px' }}>Successful</div>
                  <div style={{ color: 'var(--accent-primary)', fontWeight: 600, fontSize: '1.125rem' }}>{data.activeCampaign.successfulCalls}</div>
                </div>
              </div>
            </div>
            <div style={{ flexShrink: 0 }}>
              <ProgressRing progress={data.activeCampaign.completionRate} size={140} strokeWidth={10} color="var(--accent-blue)" />
              <div style={{ textAlign: 'center', marginTop: '12px', fontSize: '0.8125rem', color: 'var(--text-secondary)', fontWeight: 600 }}>COMPLETION</div>
            </div>
          </div>
        ) : (
          <div className="glass-card animate-fade-up" style={{ padding: '40px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center' }}>
            <div style={{ width: '48px', height: '48px', borderRadius: '50%', background: 'var(--bg-tertiary)', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '16px', color: 'var(--text-muted)' }}>
              <Phone size={24} />
            </div>
            <h3 style={{ fontSize: '1.1rem', marginBottom: '8px' }}>No Active Campaigns</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '16px' }}>Start a new campaign to begin automated calling.</p>
            <button className="btn btn-primary" onClick={() => window.location.href='/campaigns'}>New Campaign</button>
          </div>
        )}

        {/* Global Summary */}
        <div className="glass-card animate-fade-up" style={{ padding: '24px', animationDelay: '100ms' }}>
          <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '24px' }}>All-Time Performance</h3>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '16px', borderBottom: '1px solid var(--border-subtle)' }}>
              <span style={{ color: 'var(--text-secondary)' }}>Total DSC Collected</span>
              <span style={{ fontWeight: 700, color: 'var(--accent-primary)', fontSize: '1.25rem' }}>{data.totalDscAllTime.toLocaleString()}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '16px', borderBottom: '1px solid var(--border-subtle)' }}>
              <span style={{ color: 'var(--text-secondary)' }}>Total Calls Processed</span>
              <span style={{ fontWeight: 700, fontSize: '1.25rem' }}>{data.totalCallsAllTime.toLocaleString()}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: 'var(--text-secondary)' }}>Campaigns Run</span>
              <span style={{ fontWeight: 700, fontSize: '1.25rem' }}>{data.totalCampaigns.toLocaleString()}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Calls */}
      <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '16px' }} className="animate-fade-up">Live Call Stream</h3>
      <div className="glass-card animate-fade-up" style={{ padding: '0', overflowX: 'auto', animationDelay: '200ms' }}>
        <table className="data-table">
          <thead>
            <tr>
              <th>Time</th>
              <th>Customer</th>
              <th>Status</th>
              <th>DSC Required</th>
              <th>Duration</th>
            </tr>
          </thead>
          <tbody>
            {data.recentCalls?.length > 0 ? (
              data.recentCalls.map((call, i) => (
                <tr key={i} className="animate-fade-in" style={{ animationDelay: `${i * 50}ms` }}>
                  <td style={{ color: 'var(--text-secondary)' }}>{formatTime(call.calledAt || call.createdAt)}</td>
                  <td>
                    <div style={{ fontWeight: 600 }}>{call.customerName}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{call.customerPhone}</div>
                  </td>
                  <td><StatusBadge status={call.status} /></td>
                  <td>
                    {call.dscNumber ? (
                      <span className="dsc-number">{call.dscNumber}</span>
                    ) : (
                      <span style={{ color: 'var(--text-muted)' }}>—</span>
                    )}
                  </td>
                  <td style={{ color: 'var(--text-secondary)' }}>{formatDuration(call.durationSeconds)}</td>
                </tr>
              ))
            ) : (
              <tr><td colSpan="5" style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>No recent calls</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
