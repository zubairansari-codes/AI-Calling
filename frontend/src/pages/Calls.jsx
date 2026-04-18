import { useState, useEffect, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import api from '../utils/api';
import StatusBadge from '../components/StatusBadge';
import { SkeletonTable } from '../components/Skeleton';
import { formatDateTime, formatDuration } from '../utils/format';
import { mockDashboard } from '../utils/mockData';
import { Phone, Search, Filter } from 'lucide-react';
import { useToast } from '../components/Toast';

export default function Calls() {
  const [calls, setCalls] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchParams, setSearchParams] = useSearchParams();
  const { error } = useToast();

  const searchQuery = searchParams.get('q') || '';
  const statusFilter = searchParams.get('status') || '';

  const intervalRef = useRef(null);

  useEffect(() => { fetchCalls(); return () => { if (intervalRef.current) clearInterval(intervalRef.current); }; }, [searchQuery, statusFilter]);

  const fetchCalls = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (searchQuery) params.append('search', searchQuery);
      if (statusFilter) params.append('status', statusFilter);
      
      const { data } = await api.get(`/api/calls?${params.toString()}`);
      const callList = data.content || data;
      setCalls(callList);

      // Auto-refresh if there are active calls
      const hasActive = callList.some(c => ['PENDING', 'QUEUED', 'IN_PROGRESS'].includes(c.status));
      if (hasActive && !intervalRef.current) {
        intervalRef.current = setInterval(fetchCalls, 10000);
      } else if (!hasActive && intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    } catch (err) {
      setTimeout(() => setCalls(mockDashboard.recentCalls), 600);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const q = fd.get('q');
    if (q) searchParams.set('q', q); else searchParams.delete('q');
    setSearchParams(searchParams);
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Call Logs</h1>
        <p className="page-subtitle">Detailed history and transcripts of all AI interactions.</p>
      </div>

      {/* Filters */}
      <div className="glass-card animate-fade-up" style={{ padding: '16px 24px', marginBottom: '24px', display: 'flex', gap: '20px', alignItems: 'center' }}>
        <form onSubmit={handleSearch} style={{ flex: 1, display: 'flex', alignItems: 'center', background: 'var(--bg-tertiary)', border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-md)', padding: '0 12px' }}>
          <Search size={16} color="var(--text-muted)" />
          <input name="q" defaultValue={searchQuery} placeholder="Search by name or phone..." className="input-field" style={{ border: 'none', background: 'transparent', boxShadow: 'none' }} />
        </form>

        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Filter size={16} color="var(--text-muted)" />
          <select 
            className="input-field" 
            style={{ width: '180px', padding: '8px 12px' }}
            value={statusFilter}
            onChange={e => {
              if (e.target.value) searchParams.set('status', e.target.value);
              else searchParams.delete('status');
              setSearchParams(searchParams);
            }}
          >
            <option value="">All Statuses</option>
            <option value="PENDING">Pending</option>
            <option value="QUEUED">Queued</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="DSC_COLLECTED">DSC Collected</option>
            <option value="TRANSFERRED">Transferred</option>
            <option value="NO_ANSWER">No Answer / Voicemail</option>
            <option value="FAILED">Failed</option>
          </select>
        </div>
      </div>

      <div className="glass-card animate-fade-up" style={{ padding: 0, overflow: 'hidden', animationDelay: '100ms' }}>
        {loading ? (
          <div style={{ padding: '24px' }}><SkeletonTable rows={10} /></div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Time</th>
                <th>Customer</th>
                <th>Status / DSC</th>
                <th>Duration</th>
                <th>Notes / Reason</th>
              </tr>
            </thead>
            <tbody>
              {calls.length > 0 ? calls.map((call, i) => (
                <tr key={call.id} className="animate-fade-in" style={{ animationDelay: `${i * 30}ms` }}>
                  <td>
                    <div style={{ fontWeight: 500 }}>{formatDateTime(call.calledAt || call.createdAt).split(' ')[0]}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{formatDateTime(call.calledAt || call.createdAt).split(' ').slice(1).join(' ')}</div>
                  </td>
                  <td>
                    <div style={{ fontWeight: 600 }}>{call.customerName}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{call.customerPhone}</div>
                  </td>
                  <td>
                    <StatusBadge status={call.status} />
                    {call.dscNumber && <div className="dsc-number" style={{ marginTop: '4px', display: 'inline-block' }}>{call.dscNumber}</div>}
                  </td>
                  <td style={{ color: 'var(--text-secondary)' }}>{formatDuration(call.durationSeconds)}</td>
                  <td>
                    <div style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', maxWidth: '250px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {call.transferReason || call.notes || '—'}
                    </div>
                  </td>
                </tr>
              )) : (
                <tr><td colSpan="5" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>No calls found matching your criteria.</td></tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
