import { useState, useEffect } from 'react';
import { useToast } from '../components/Toast';
import { Save, Phone, Loader2 } from 'lucide-react';
import api from '../utils/api';

export default function Settings() {
  const { success, error: showError } = useToast();
  const [saving, setSaving] = useState(false);
  const [testing, setTesting] = useState(false);
  const [loading, setLoading] = useState(true);

  const [form, setForm] = useState({
    name: '',
    city: '',
    email: '',
    agentName: '',
    transferNumber: '',
  });

  useEffect(() => {
    api.get('/api/agency/profile')
      .then(res => {
        const p = res.data;
        setForm({
          name: p.name || '',
          city: p.city || '',
          email: p.email || '',
          agentName: p.agentName || '',
          transferNumber: p.transferNumber || '',
        });
      })
      .catch(() => showError('Failed to load profile'))
      .finally(() => setLoading(false));
  }, []);

  const handleChange = (field) => (e) => {
    setForm(prev => ({ ...prev, [field]: e.target.value }));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await api.put('/api/agency/profile', {
        name: form.name,
        city: form.city || null,
        agentName: form.agentName || null,
        transferNumber: form.transferNumber || null,
      });
      success('Settings updated successfully!');
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to save settings';
      showError(msg);
    } finally {
      setSaving(false);
    }
  };

  const handleTestCall = async () => {
    setTesting(true);
    try {
      const res = await api.post('/api/agency/test-call');
      success(res.data.message || 'Test call initiated!');
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to trigger test call';
      showError(msg);
    } finally {
      setTesting(false);
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
        <Loader2 size={32} className="spin" style={{ color: 'var(--accent)' }} />
      </div>
    );
  }

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Settings</h1>
        <p className="page-subtitle">Configure your agency profile and AI agent behavior.</p>
      </div>

      <form onSubmit={handleSave} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '32px' }}>
        
        {/* Agency Info */}
        <div className="glass-card animate-fade-up" style={{ padding: '32px' }}>
          <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '24px', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '12px' }}>Agency Information</h3>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <div>
              <label className="input-label">Agency Name</label>
              <input className="input-field" value={form.name} onChange={handleChange('name')} />
            </div>
            <div>
              <label className="input-label">City / Region</label>
              <input className="input-field" value={form.city} onChange={handleChange('city')} />
            </div>
            <div>
              <label className="input-label">Owner Email</label>
              <input type="email" className="input-field" value={form.email} disabled style={{ opacity: 0.6, cursor: 'not-allowed' }} />
            </div>
          </div>
        </div>

        {/* AI Config */}
        <div className="glass-card animate-fade-up" style={{ padding: '32px', animationDelay: '100ms' }}>
          <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '24px', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '12px' }}>AI Agent Routing</h3>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <div>
              <label className="input-label">AI Agent Caller Name</label>
              <input className="input-field" value={form.agentName} onChange={handleChange('agentName')} />
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '4px' }}>The name your AI agent uses when introducing itself on calls.</div>
            </div>
            <div>
              <label className="input-label">Transfer Phone Number</label>
              <input className="input-field" value={form.transferNumber} onChange={handleChange('transferNumber')} />
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '4px' }}>If customer asks for a human, calls transfer here.</div>
            </div>
            <div style={{ marginTop: '16px' }}>
              <button 
                type="button" 
                className="btn btn-secondary" 
                style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
                onClick={handleTestCall}
                disabled={testing}
              >
                {testing ? <Loader2 size={18} className="spin" /> : <Phone size={18} />}
                {testing ? 'Calling...' : 'Send Test Call to Transfer Number'}
              </button>
            </div>
          </div>
        </div>

        <div style={{ gridColumn: '1 / -1', display: 'flex', justifyContent: 'flex-end', marginTop: '16px' }}>
           <button type="submit" className="btn btn-primary" disabled={saving}>
             <Save size={18} /> {saving ? 'Saving...' : 'Save All Changes'}
           </button>
        </div>
      </form>
    </div>
  );
}
