import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../utils/api';
import Modal from '../components/Modal';
import CSVUploader from '../components/CSVUploader';
import StatusBadge from '../components/StatusBadge';
import { SkeletonTable } from '../components/Skeleton';
import { mockCampaigns } from '../utils/mockData';
import { Plus, Play, Pause, Download, ChevronRight } from 'lucide-react';
import { formatDate, formatPercent } from '../utils/format';
import { useToast } from '../components/Toast';

export default function Campaigns() {
  const [campaigns, setCampaigns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setModalOpen] = useState(false);
  const { error, success } = useToast();
  const navigate = useNavigate();

  // Create form
  const [name, setName] = useState('');
  const [deliveryDate, setDeliveryDate] = useState('');
  const [csvFile, setCsvFile] = useState(null);
  const [creating, setCreating] = useState(false);

  useEffect(() => { fetchCampaigns(); }, []);

  const fetchCampaigns = async () => {
    try {
      const { data } = await api.get('/api/campaigns');
      setCampaigns(data.content || data); 
    } catch (err) {
      console.warn('Fallback to mock data', err);
      setTimeout(() => setCampaigns(mockCampaigns), 600);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!csvFile) return error('Please upload a CSV file');

    setCreating(true);
    const formData = new FormData();
    formData.append('campaign', new Blob([JSON.stringify({ name, deliveryDate })], { type: 'application/json' }));
    formData.append('file', csvFile);

    try {
      await api.post('/api/campaigns', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
      success('Campaign created successfully');
      setModalOpen(false);
      resetForm();
      fetchCampaigns();
    } catch (err) {
      error(err.response?.data?.message || 'Failed to create campaign');
    } finally {
      setCreating(false);
    }
  };

  const startCampaign = async (e, id) => {
    e.stopPropagation();
    try {
      await api.post(`/api/campaigns/${id}/start`);
      success('Campaign started');
      fetchCampaigns();
    } catch (err) {
      error(err.response?.data?.message || 'Failed to start');
    }
  };

  const pauseCampaign = async (e, id) => {
    e.stopPropagation();
    try {
      await api.post(`/api/campaigns/${id}/pause`);
      success('Campaign paused');
      fetchCampaigns();
    } catch (err) {
      error(err.response?.data?.message || 'Failed to pause');
    }
  };

  const downloadDsc = async (e, id) => {
    e.stopPropagation();
    try {
      const res = await api.get(`/api/campaigns/${id}/export`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `dsc_numbers_${id}.csv`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (err) {
      error('Export failed');
    }
  };

  const resetForm = () => { setName(''); setDeliveryDate(''); setCsvFile(null); };

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 className="page-title">Campaigns</h1>
          <p className="page-subtitle">Manage calling batches and target lists.</p>
        </div>
        <button className="btn btn-primary animate-scale-in" onClick={() => setModalOpen(true)}>
          <Plus size={18} /> New Campaign
        </button>
      </div>

      <div className="glass-card animate-fade-up" style={{ padding: 0, overflow: 'hidden' }}>
        {loading ? (
          <div style={{ padding: '24px' }}><SkeletonTable rows={5} /></div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Name & Target Date</th>
                <th>Status</th>
                <th>Progress</th>
                <th>Success Rate</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {campaigns.length > 0 ? campaigns.map((campaign, i) => (
                <tr key={campaign.id} className="clickable animate-fade-in" style={{ animationDelay: `${i * 50}ms` }}
                    onClick={() => navigate(`/campaigns/${campaign.id}`)}>
                  <td>
                    <div style={{ fontWeight: 600 }}>{campaign.name}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{formatDate(campaign.deliveryDate)}</div>
                  </td>
                  <td><StatusBadge status={campaign.status} /></td>
                  <td style={{ width: '200px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', marginBottom: '4px' }}>
                      <span>{campaign.completedCalls} / {campaign.totalCustomers}</span>
                      <span style={{ fontWeight: 600 }}>{formatPercent(campaign.completionRate)}</span>
                    </div>
                    <div style={{ width: '100%', height: '6px', background: 'var(--bg-tertiary)', borderRadius: 'var(--radius-full)', overflow: 'hidden' }}>
                      <div style={{ width: `${campaign.completionRate}%`, height: '100%', background: 'var(--accent-blue)', transition: 'width 1s ease' }} />
                    </div>
                  </td>
                  <td>
                    <span style={{ color: campaign.successRate > 40 ? 'var(--accent-primary)' : 'var(--text-secondary)', fontWeight: 600 }}>
                      {formatPercent(campaign.successRate)}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '8px' }}>
                      {['DRAFT', 'PAUSED'].includes(campaign.status) && (
                        <button className="btn btn-ghost" onClick={(e) => startCampaign(e, campaign.id)} title="Start" style={{ padding: '6px', color: 'var(--accent-primary)' }}><Play size={16} /></button>
                      )}
                      {['IN_PROGRESS', 'RUNNING'].includes(campaign.status) && (
                        <button className="btn btn-ghost" onClick={(e) => pauseCampaign(e, campaign.id)} title="Pause" style={{ padding: '6px', color: 'var(--accent-amber)' }}><Pause size={16} /></button>
                      )}
                      {campaign.status === 'COMPLETED' && (
                        <button className="btn btn-ghost" onClick={(e) => downloadDsc(e, campaign.id)} title="Download DSC" style={{ padding: '6px', color: 'var(--accent-secondary)' }}><Download size={16} /></button>
                      )}
                      <button className="btn btn-ghost" style={{ padding: '6px' }}><ChevronRight size={16} /></button>
                    </div>
                  </td>
                </tr>
              )) : (
                <tr><td colSpan="5" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>No campaigns created yet. Click "New Campaign" to start.</td></tr>
              )}
            </tbody>
          </table>
        )}
      </div>

      <Modal isOpen={isModalOpen} onClose={() => { setModalOpen(false); resetForm(); }} title="Create New Campaign" width="560px">
        <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <label className="input-label">Campaign Name</label>
            <input required className="input-field" value={name} onChange={e => setName(e.target.value)} placeholder="e.g. Area 5 Deliveries - 15 March" />
          </div>
          <div>
            <label className="input-label">Delivery Date target</label>
            <input type="date" required className="input-field" value={deliveryDate} onChange={e => setDeliveryDate(e.target.value)} />
          </div>
          <div>
            <label className="input-label">Customer List (CSV)</label>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '8px' }}>Format: Name, Phone, Address (optional), Consumer No (optional)</div>
            <CSVUploader onUpload={setCsvFile} />
          </div>
          
          <div style={{ display: 'flex', gap: '12px', marginTop: '16px' }}>
            <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" disabled={creating} className="btn btn-primary" style={{ flex: 2 }}>{creating ? 'Processing CSV & Creating...' : 'Create Campaign'}</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
