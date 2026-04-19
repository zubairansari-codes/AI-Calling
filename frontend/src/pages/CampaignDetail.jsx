import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Play, Pause, Download, Trash2, Phone, CheckCircle, XCircle } from 'lucide-react';
import api from '../utils/api';
import StatusBadge from '../components/StatusBadge';
import ProgressRing from '../components/ProgressRing';
import { SkeletonTable } from '../components/Skeleton';
import { useToast } from '../components/Toast';
import { formatDateTime, formatDate, formatPercent, formatDuration } from '../utils/format';

export default function CampaignDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [campaign, setCampaign] = useState(null);
  const [calls, setCalls] = useState([]);
  const [loading, setLoading] = useState(true);
  const { success, error: showError } = useToast();
  
  const intervalRef = useRef(null);

  const fetchDetails = async () => {
    try {
      const [campRes, callsRes] = await Promise.all([
        api.get(`/api/campaigns/${id}`),
        api.get(`/api/campaigns/${id}/calls?size=1000`)
      ]);
      
      setCampaign(campRes.data);
      setCalls(callsRes.data.content || callsRes.data);

      // Auto-refresh if running
      if (['IN_PROGRESS', 'QUEUED'].includes(campRes.data.status)) {
        if (!intervalRef.current) intervalRef.current = setInterval(fetchDetails, 5000);
      } else {
        if (intervalRef.current) { clearInterval(intervalRef.current); intervalRef.current = null; }
      }
    } catch (err) {
      showError('Failed to load campaign details');
      navigate('/campaigns');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDetails();
    return () => { if (intervalRef.current) clearInterval(intervalRef.current); };
  }, [id]);

  const handleAction = async (action) => {
    try {
      if (action === 'delete') {
        if (!confirm('Are you sure you want to delete this draft campaign?')) return;
        await api.delete(`/api/campaigns/${id}`);
        success('Campaign deleted');
        navigate('/campaigns');
        return;
      }
      
      const { data } = await api.post(`/api/campaigns/${id}/${action}`);
      setCampaign(data);
      success(`Campaign ${action}ed successfully`);
      fetchDetails(); // fetch fresh calls
    } catch (err) {
      showError(`Failed to ${action} campaign: ${err.response?.data?.message || err.message}`);
    }
  };

  const downloadDsc = async () => {
    try {
      const response = await api.get(`/api/campaigns/${id}/export`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `${campaign.name.replace(/\s+/g, '_')}_dsc.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      showError('Failed to download DSC report');
    }
  };

  if (loading) return <div style={{ padding: '40px' }}><SkeletonTable rows={10} /></div>;
  if (!campaign) return null;

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <button onClick={() => navigate('/campaigns')} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', padding: 0, marginBottom: '16px' }}>
            <ArrowLeft size={16} /> Back to Campaigns
          </button>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '8px' }}>
            <h1 className="page-title" style={{ margin: 0 }}>{campaign.name}</h1>
            <StatusBadge status={campaign.status} />
          </div>
          <p className="page-subtitle">Created on {formatDateTime(campaign.createdAt)} • Target: {formatDate(campaign.deliveryDate)}</p>
        </div>

        <div style={{ display: 'flex', gap: '12px' }}>
          {campaign.status === 'DRAFT' && (
            <>
              <button className="btn btn-secondary" onClick={() => handleAction('delete')} style={{ color: 'var(--accent-red)', borderColor: 'rgba(239, 68, 68, 0.3)' }}><Trash2 size={16} /> Delete</button>
              <button className="btn btn-primary" onClick={() => handleAction('start')}><Play size={16} fill="currentColor" /> Start Campaign</button>
            </>
          )}
          {campaign.status === 'IN_PROGRESS' && (
            <button className="btn btn-secondary" onClick={() => handleAction('pause')} style={{ color: 'var(--accent-amber)', borderColor: 'var(--accent-amber)' }}><Pause size={16} fill="currentColor" /> Pause Campaign</button>
          )}
          {campaign.status === 'PAUSED' && (
            <button className="btn btn-primary" onClick={() => handleAction('resume')}><Play size={16} fill="currentColor" /> Resume</button>
          )}
          {campaign.successfulCalls > 0 && (
            <button className="btn btn-secondary" onClick={downloadDsc}><Download size={16} /> Export DSC</button>
          )}
        </div>
      </div>

      <div className="grid-4" style={{ marginBottom: '32px' }}>
        <div className="glass-card animate-scale-in" style={{ padding: '24px', display: 'flex', alignItems: 'center', gap: '24px' }}>
           <ProgressRing progress={campaign.completionRate} size={80} strokeWidth={6} color="var(--accent-blue)" />
           <div>
             <div style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '4px' }}>Completion</div>
             <div style={{ fontSize: '1.5rem', fontWeight: 700 }}>{formatPercent(campaign.completionRate)}</div>
             <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>{campaign.completedCalls} / {campaign.totalCustomers} Calls</div>
           </div>
        </div>
        <div className="glass-card animate-scale-in" style={{ padding: '24px', animationDelay: '50ms' }}>
           <div style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '8px' }}>
             <CheckCircle size={16} color="var(--accent-primary)" /> Success Rate
           </div>
           <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--accent-primary)' }}>{formatPercent(campaign.successRate)}</div>
           <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>{campaign.successfulCalls} DSCs Collected</div>
        </div>
        <div className="glass-card animate-scale-in" style={{ padding: '24px', animationDelay: '100ms' }}>
           <div style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '8px' }}>
             <Phone size={16} color="var(--accent-purple)" /> Transferred
           </div>
           <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--accent-purple)' }}>{campaign.transferredCalls}</div>
           <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>Needed human assistance</div>
        </div>
        <div className="glass-card animate-scale-in" style={{ padding: '24px', animationDelay: '150ms' }}>
           <div style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '8px' }}>
             <XCircle size={16} color="var(--accent-red)" /> Failed
           </div>
           <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--accent-red)' }}>{campaign.failedCalls}</div>
           <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>Voicemail or no answer</div>
        </div>
      </div>

      <div className="glass-card animate-fade-up" style={{ padding: 0, overflow: 'hidden', animationDelay: '200ms' }}>
        <table className="data-table">
          <thead>
            <tr>
              <th>Customer</th>
              <th>Status</th>
              <th>DSC Number</th>
              <th>Duration</th>
              <th>Notes</th>
            </tr>
          </thead>
          <tbody>
            {calls.length > 0 ? calls.map((call, i) => (
              <tr key={call.id} className="animate-fade-in" style={{ animationDelay: `${i * 20}ms` }}>
                <td>
                  <div style={{ fontWeight: 600 }}>{call.customerName}</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{call.customerPhone}</div>
                </td>
                <td><StatusBadge status={call.status} /></td>
                <td>
                  {call.dscNumber ? (
                    <div className="dsc-number">{call.dscNumber}</div>
                  ) : (
                    <span style={{ color: 'var(--text-muted)' }}>—</span>
                  )}
                </td>
                <td style={{ color: 'var(--text-secondary)' }}>{formatDuration(call.durationSeconds)}</td>
                <td>
                  <div style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', maxWidth: '250px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {call.transferReason || call.notes || '—'}
                  </div>
                </td>
              </tr>
            )) : (
              <tr><td colSpan="5" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>No calls found for this campaign.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
