import { useState, useEffect } from 'react';
import api from '../utils/api';
import MetricCard from '../components/MetricCard';
import { IndianRupee, TrendingUp, Users, Target } from 'lucide-react';
import { SkeletonMetric, SkeletonTable } from '../components/Skeleton';

export default function Analytics() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        const response = await api.get('/api/analytics/summary');
        setData(response.data);
      } catch (err) {
        console.error('Failed to load analytics', err);
      } finally {
        setLoading(false);
      }
    };
    fetchAnalytics();
  }, []);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Analytics & Insights</h1>
        <p className="page-subtitle">Track your AI agent's performance and value generated.</p>
      </div>

      <div className="grid-4" style={{ marginBottom: '32px' }}>
        {loading ? (
          <>
            <SkeletonMetric />
            <SkeletonMetric />
            <SkeletonMetric />
            <SkeletonMetric />
          </>
        ) : (
          <>
            <MetricCard title="Total DSC Collected" value={data?.totalDscCollected || 0} icon={<Target />} delay={0} />
            <MetricCard title="Total Calls Made" value={data?.totalCalls || 0} icon={<Users />} accent="blue" delay={100} />
            <MetricCard title="Avg. Success Rate" value={data?.avgSuccessRate || 0} suffix="%" icon={<TrendingUp />} accent="purple" delay={200} />
            <MetricCard title="Money Saved" value={(data?.estimatedMonthlySavings || 0).toLocaleString()} prefix="₹" icon={<IndianRupee />} accent="amber" delay={300} />
          </>
        )}
      </div>

      <div className="grid-2">
        <div className="glass-card animate-fade-up" style={{ padding: '24px', animationDelay: '200ms' }}>
          <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '24px' }}>Cost Savings Calculator</h3>
          
          {loading ? (
            <div style={{ height: '300px', background: 'var(--bg-tertiary)', borderRadius: 'var(--radius-md)', animation: 'pulse 1.5s infinite' }} />
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
               <div style={{ padding: '16px', borderRadius: 'var(--radius-md)', background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.2)' }}>
                 <div style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>Human Callers (Estimates)</div>
                 <div style={{ color: 'var(--accent-red)', fontSize: '1.5rem', fontWeight: 700 }}>₹ {(data?.estimatedHumanCostMonthly || 0).toLocaleString()} / mo</div>
               </div>
               
               <div style={{ padding: '16px', borderRadius: 'var(--radius-md)', background: 'rgba(56, 189, 248, 0.1)', border: '1px solid rgba(56, 189, 248, 0.2)' }}>
                 <div style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>ElevenLabs AI Output</div>
                 <div style={{ color: 'var(--accent-blue)', fontSize: '1.5rem', fontWeight: 700 }}>₹ {(data?.estimatedAiCostMonthly || 0).toLocaleString()} / mo</div>
               </div>

               <div style={{ padding: '20px', borderRadius: 'var(--radius-md)', background: 'var(--gradient-primary)', marginTop: '8px' }}>
                 <div style={{ color: 'rgba(255,255,255,0.8)', fontSize: '0.875rem', fontWeight: 600 }}>Total Savings</div>
                 <div style={{ color: 'white', fontSize: '2rem', fontWeight: 800 }}>₹ {(data?.estimatedMonthlySavings || 0).toLocaleString()}</div>
               </div>
            </div>
          )}
        </div>

        <div className="glass-card animate-fade-up" style={{ padding: '24px', animationDelay: '300ms' }}>
          <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '24px' }}>Top Campaigns</h3>
          {loading ? (
            <SkeletonTable rows={4} />
          ) : (
            <table className="data-table">
              <thead>
                <tr><th>Campaign Name</th><th>Success Rate</th><th>Volume</th></tr>
              </thead>
              <tbody>
                {data?.topCampaigns?.length > 0 ? (
                  data.topCampaigns.map(camp => (
                    <tr key={camp.id}>
                      <td style={{ fontWeight: 600 }}>{camp.name}</td>
                      <td style={{ color: 'var(--accent-primary)', fontWeight: 700 }}>{camp.successRate}%</td>
                      <td>{camp.totalCustomers}</td>
                    </tr>
                  ))
                ) : (
                  <tr><td colSpan="3" style={{ textAlign: 'center', padding: '20px', color: 'var(--text-muted)' }}>No campaigns found.</td></tr>
                )}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
